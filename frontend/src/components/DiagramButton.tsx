"use client";
import { Box, Chip, Collapse, Divider, Fab, Stack, Theme, Typography, useMediaQuery } from "@mui/material";
import { Monitor, TableProperties } from "lucide-react";
import React, { useMemo, useRef, useState } from "react";
import { AnimatedBox } from "./AnimatedButton";
import { chunkArray } from "@/lib/utils/Array";
import Mulesoft from "./Mulesoft";
import AddIcon from '@mui/icons-material/Add';
import RemoveIcon from '@mui/icons-material/Remove';
import ResetIcon from '@mui/icons-material/Refresh';
import StorageOutlinedIcon from '@mui/icons-material/StorageOutlined';

type NodeType = "POSTGRES" | "MULESOFT" | "NEXT";

const nodeIcons : Record<NodeType, React.ReactNode> = {
  "POSTGRES": <StorageOutlinedIcon color="secondary" />,
  "MULESOFT": <Mulesoft color="primary" />,
  "NEXT": <Monitor />,
}

type Node = {
  id: number;
  name: string;
  type: NodeType;
  description: string;
  sideNodes?: Node[];
  nodes?: Node[];
}

export type Diagram = {
  id: number;
  name: string;
  icon: React.ReactNode;
  nodes: Node;
}

const NODE_WIDTH = 120;
const NODE_HEIGHT_CLOSED = 90;
const NODE_GAP = 70; 
const SIDE_OFFSET = 220; 

export default function DiagramButton({
  show,
  setShow,
  diagram,
}: {
  show: number;
  setShow: (id: number) => void;
  diagram: Diagram;
}) {
  const id = diagram.id;
  return (
    <AnimatedBox
      sx={{
        transition: "all 0.6s cubic-bezier(0.4, 0, 0.2, 1)",
        opacity: show === 0 ? 1 : show === id ? 1 : 0,
      }}
      width={show === 0 ? "100%" : show === id ? "100%" : "0px"}
      key={id}
      onClick={(e) => {
        if (show === id) {
          setShow(0);
        } else {
          setShow(id);
        }
      }}
    >
      <Stack direction="column" spacing={4} alignItems="center" p={2}>
        {diagram.icon}
        <Typography
          variant="h6"
          sx={{ fontWeight: 800, color: "primary.main" }}
        >
          {diagram.name}
        </Typography>
        <Collapse in={show === id} timeout={1000} unmountOnExit>
          <Box
            sx={{
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
              position: "relative",
              mx: "auto",
            }}
          >
            <AnimatedSvgTree data={diagram.nodes} />
          </Box>
        </Collapse>
      </Stack>
    </AnimatedBox>
  );
}

export function DiagramGroupButtons({diagrams} : {diagrams: Diagram[]}) {
  const [show, setShow] = React.useState<number>(0);
  return (
    <Stack width={'100%'} direction="row" spacing={4}  p={2}>
    {diagrams.map(diagram => (
      <DiagramButton key={diagram.id} diagram={diagram} show={show} setShow={setShow} />
      ))}
    </Stack>
  );
}

export function DiagramGroupButtonsAutosize({diagrams} : {diagrams: Diagram[]}) {
  const isSmallScreen = useMediaQuery((theme: Theme) => theme.breakpoints.down('sm'));
  const isMediumScreen = useMediaQuery((theme: Theme) => theme.breakpoints.between('sm', 'md'));

  const chunkSize = isSmallScreen ? 2 : isMediumScreen ? 3 : 4;
  const chunkedNodes = chunkArray(diagrams, chunkSize);

  return (
      chunkedNodes.map((chunk, index) => (
        <DiagramGroupButtons key={index} diagrams={chunk} />
      ))
  );
}

type InternalNode = Node & {
  x: number;
  y: number;
  expanded: boolean;
  incomingEdgeId: string | null;
  isSide?: boolean;
}

type Edge = {
  id: string;
  d: string;
  parentEdgeId: string | null;
}

const AnimatedSvgTree = ({ data } : { data: Node }) => {
  const isSmallScreen = useMediaQuery((theme: Theme) => theme.breakpoints.down('sm'));
  const [expandedNodes, setExpandedNodes] = useState<Record<number, boolean>>({});
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState(false);
  const [transform, setTransform] = useState({ x: 0, y: 0, scale: 0.5 });
  const zoomIn = (e : React.MouseEvent) => {
    setTransform(prev => ({ ...prev, scale: Math.min(prev.scale + 0.2, 3) }))
    e.stopPropagation();
  };
  const zoomOut = (e : React.MouseEvent) => {
    setTransform(prev => ({ ...prev, scale: Math.max(prev.scale - 0.2, 0.2) }));
    e.stopPropagation();
  }
  const containerRef = useRef(null);
  const svgWidth = 600;

  const toggleNode = (id : number) => {
    setExpandedNodes(prev => ({ ...prev, [id]: !prev[id] }));
  };

  const { nodes, edges, lastEdgeIds, totalHeight, sideEdges } = useMemo(() => {
    const nodes : InternalNode[] = [];
    const edges : Edge[] = [];
    const lastEdgeIds : string[] = [];
    const sideEdges : Edge[] = [];
    let edgeCounter = 0;

    const traverse = (node : Node, x : number, y : number, width : number, parentEdgeId : string | null = null) => {
      const isExpanded = !!expandedNodes[node.id];
      
      const currentHeight = isExpanded ? 200 : NODE_HEIGHT_CLOSED;

      nodes.push({ 
        id: node.id,
        x, y, 
        name: node.name, 
        type: node.type, 
        description: node.description,
        expanded: isExpanded,
        incomingEdgeId: parentEdgeId 
      });

      if (node.sideNodes) {
        node.sideNodes.forEach((side, idx) => {
          const sx = x + SIDE_OFFSET;
          const sy = y + (idx * (NODE_HEIGHT_CLOSED + 20)) - (((node.sideNodes || []).length - 1) * 45);
          const sId = `side-${node.id}-${idx}`;
          
          nodes.push({ ...side, x: sx, y: sy, expanded: false, isSide: true, incomingEdgeId: null });
          
          // INVERTIDO: Origem no Side Node (sx, sy) e Destino no Main Node (x, y)
          const sidePath = `M ${sx - 140/2} ${sy} L ${x + NODE_WIDTH/2} ${y}`;
          sideEdges.push({ id: sId, d: sidePath, parentEdgeId: parentEdgeId });
        });
      }

      let maxYOfChildren = y + currentHeight;

      const sideNodesHeight = (node.sideNodes ? node.sideNodes.length * (NODE_HEIGHT_CLOSED + 20) : 0);

      if (node.nodes && node.nodes.length > 0) {
        const childCount = node.nodes.length;
        const childWidth = width / childCount;

        node.nodes.forEach((branch, index) => {
          const childX = (x - width / 2) + (childWidth * index) + (childWidth / 2);
          
          const childY = (y + currentHeight / 2 + NODE_GAP + NODE_HEIGHT_CLOSED / 2) + (sideNodesHeight / 1.5);
          
          const currentEdgeId = `e${++edgeCounter}`;
          
          const startY = y + currentHeight / 2;
          const endY = childY - NODE_HEIGHT_CLOSED / 2;
          const midY = (startY + endY) / 2;
          const pathD = `M ${x} ${startY} C ${x} ${midY}, ${childX} ${midY}, ${childX} ${endY}`;
          
          edges.push({ id: currentEdgeId, d: pathD, parentEdgeId: parentEdgeId });
          const subtreeMaxY = traverse(branch, childX, childY, childWidth, currentEdgeId);
          if (subtreeMaxY > maxYOfChildren) maxYOfChildren = subtreeMaxY;
        });
      } else if (parentEdgeId) {
        lastEdgeIds.push(parentEdgeId);
      }
      return maxYOfChildren;
    };

    const totalHeight = traverse(data, svgWidth / 2 , 80, svgWidth, null);
    return { nodes, edges, lastEdgeIds, totalHeight: totalHeight + 100, sideEdges };
  }, [data, expandedNodes]);
  const finalEdgeTrigger = lastEdgeIds.length > 0 ? `${lastEdgeIds[lastEdgeIds.length - 1]}.end + 2s` : "0s";
  const startTrigger = `0s; ${finalEdgeTrigger}`;

  const resetTransform = () => setTransform({ x: 0, y: 0, scale: 0.8 });

 const handleTouchStart = (e : React.TouchEvent) => {
    if (e.touches.length !== 1) return;
    e.stopPropagation();
    setIsDragging(true);
    setDragStart({ 
      x: e.touches[0].clientX - transform.x, 
      y: e.touches[0].clientY - transform.y 
    });
  };

  const handleTouchMove = (e : React.TouchEvent) => {
    if (!isDragging || e.touches.length !== 1) return;
    e.stopPropagation();
    if (e.cancelable) e.preventDefault(); 
    setTransform(prev => ({
      ...prev,
      x: e.touches[0].clientX - dragStart.x,
      y: e.touches[0].clientY - dragStart.y
    }));
  };

  const handleTouchEnd = (e : React.TouchEvent) => {
    setIsDragging(false);
    e.stopPropagation();
  };

  return (
      <Box 
        ref={containerRef}
        onTouchStart={handleTouchStart}
        onTouchMove={handleTouchMove}
        onTouchEnd={handleTouchEnd}
        sx={{ 
          width: '100%',
          position: 'relative',
          height: { xs: '40vh', md: totalHeight }, 
          overflow: 'hidden',
          userSelect: 'none',
          backgroundSize: `${24 * transform.scale}px ${24 * transform.scale}px`,
          backgroundPosition: `${transform.x}px ${transform.y}px`
        }}
      >
        {isSmallScreen && <Box sx={{ position: 'absolute', bottom: 20, right: 20, display: 'flex', flexDirection: 'column', gap: 1, zIndex: 10 }}>
          <Fab size="small" color="primary" onClick={zoomIn}><AddIcon /></Fab>
          <Fab size="small" color="primary" onClick={zoomOut}><RemoveIcon /></Fab>
          <Fab size="small" sx={{ bgcolor: 'white' }} onClick={resetTransform}><ResetIcon /></Fab>
        </Box>}
        <svg 
          width="100%" 
          height={isSmallScreen ? "100%" : totalHeight}
          viewBox={isSmallScreen ? "" : `0 0 ${svgWidth} ${totalHeight}`} 
          style={{ overflow: 'visible' }}
        >
          <defs>
            <linearGradient id="rotatingGradient" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#3b82f6" />
              <stop offset="50%" stopColor="#60a5fa" />
              <stop offset="100%" stopColor="#3b82f6" />
              <animateTransform
                attributeName="gradientTransform"
                type="rotate"
                from="0 0.5 0.5"
                to="360 0.5 0.5"
                dur="2s"
                repeatCount="indefinite"
              />
            </linearGradient>
          </defs>
          <g transform={isSmallScreen ? `translate(${transform.x}, ${transform.y}) scale(${transform.scale})` : ""}>
            <g fill="none" stroke="var(--mui-palette-primary-main)" strokeWidth="2">
              {edges.map((edge) => (
                <path 
                  key={`line-${edge.id}`} 
                  d={edge.d} 
                  strokeDasharray="4 4" 
                  style={{ transition: 'd 0.3s ease' }}
                />
              ))}
            </g>

            <g fill="none" stroke="var(--mui-palette-primary-main)" strokeWidth="1.5" strokeDasharray="2 2">
              {sideEdges.map(e => <path key={e.id} d={e.d} />)}
            </g>

            {edges.map((edge) => {
              const beginCondition = edge.parentEdgeId ? `${edge.parentEdgeId}.end` : startTrigger;
              return (
                <circle key={`dot-${edge.id}`} r="3" fill="var(--mui-palette-secondary-main)" opacity="0" style={{ filter: 'drop-shadow(0 0 5px #3b82f6)' }}>
                  <animate attributeName="opacity" from="1" to="1" begin={beginCondition} dur="1.5s" />
                  <animateMotion id={edge.id} dur="1.5s" path={edge.d} begin={beginCondition} fill="remove" />
                </circle>
              );
            })}

            {sideEdges.map(e => (
              <circle key={`side-dot-${e.id}`} r="3" fill="var(--mui-palette-secondary-main)" opacity="0">
                <animate attributeName="opacity" values="0;1;1;0" keyTimes="0;0.1;0.9;1" begin={`${e.parentEdgeId || '0s'}.end`} dur="1s" />
                <animateMotion id={e.id} dur="1s" path={e.d} begin={`${e.parentEdgeId || '0s'}.end`} fill="remove" />
              </circle>
            ))}

            {nodes.map((node) => (
              <ApiNode 
                key={node.id}
                node={node}
                onToggle={() => toggleNode(node.id)}
              />
            ))}
          </g>
        </svg>
      </Box>
  );
};


const ApiNode = ({ node, onToggle } : { node: InternalNode, onToggle: () => void }) => {
  const { x, y, name, type, description, expanded, incomingEdgeId } = node;
  const handleClick = (e : React.MouseEvent) => {
    onToggle();
    e.stopPropagation();
  };
  return (
    <g transform={`translate(${x}, ${y})`} style={{ cursor: 'pointer' }} onClick={handleClick}>
      <rect 
        x={-NODE_WIDTH / 2 - 1} 
        y={-NODE_HEIGHT_CLOSED / 2 - 1} 
        width={NODE_WIDTH + 2} 
        height={NODE_HEIGHT_CLOSED + 2} 
        rx={13} 
        fill="none"
        stroke="url(#rotatingGradient)"
        strokeWidth="2"
        opacity="0"
        style={{ filter: 'url(#glowBlur)' }}
      >
        {incomingEdgeId && (
          <animate 
            attributeName="opacity" 
            values="0; 1; 1; 0" 
            keyTimes="0; 0.1; 0.7; 1"
            dur="1s" 
            begin={`${incomingEdgeId}.end`} 
            fill="remove" 
          />
        )}
      </rect>

      <g>
        {incomingEdgeId && (
          <animateTransform
            attributeName="transform" 
            type="scale" 
            values="1; 1.04; 1"
            keyTimes="0; 0.15; 1"
            dur="0.4s" 
            begin={`${incomingEdgeId}.end`} 
            additive="replace" 
            fill="remove"
          />
        )}
        
        <foreignObject 
          x={-NODE_WIDTH / 2} 
          y={-NODE_HEIGHT_CLOSED / 2} 
          width={NODE_WIDTH} 
          height={400}
          style={{ overflow: 'visible', pointerEvents: 'none' }}
        >
          <AnimatedBox 
            sx={{ 
              width: NODE_WIDTH,
              borderRadius: 3,
              display: 'flex',
              flexDirection: 'column',
              transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
              pointerEvents: 'auto'
            }}
          >
            <Stack direction="column" alignItems="center" p={1}>
              <Box sx={{ display: 'flex', justifyContent: 'center', mb: 0.5, position: 'relative' }}>
                {nodeIcons[type] || <Monitor size={24} color="#64748b" />}
              </Box>
              <Typography variant="caption" sx={{ display: 'block', fontSize: '0.75rem', lineHeight: 1.1 }}>
                {name}
              </Typography>
              <Chip variant="outlined" label={type} color="primary" size="small" sx={{ mt: 1, fontSize: 10 }} />
              <Collapse in={expanded}>
                <Divider sx={{ my: 1 }} />
                <Typography variant="caption" sx={{ fontSize: '0.65rem', color: '#64748b', textAlign: 'left', display: 'block' }}>
                  {description}
                </Typography>
              </Collapse>
            </Stack>
          </AnimatedBox>
        </foreignObject>
      </g>
    </g>
  );
};