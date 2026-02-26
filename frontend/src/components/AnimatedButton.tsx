'use client';
import { Box, Button, ButtonBase, keyframes, styled } from "@mui/material";

const rotateGlow = keyframes`
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
`;

const AnimatedButton = styled(Button)(({ theme }) => ({
  position: 'relative',
  padding: '1px',
  borderRadius: '12px',
  transition: 'all 0.4s ease',
  overflow: 'hidden',
  display: 'flex',
  justifyContent: 'center',
  alignItems: 'center',
  ":hover": {
      transform: 'translateY(-2px)',
      boxShadow: '0 0 30px rgba(59, 130, 246, 0.2)',
      "::before": {
        opacity: 1,
      }
  },
  "> div": {
    zIndex: 100,
    background: 'var(--mui-palette-background-default)',
    borderRadius: '11px',
    width: '100%',
    height: '100%',
    border: '1px solid rgba(59, 130, 246, 0.2)',
  },
  "::before": {
    background: `conic-gradient(
        transparent, 
        #3b82f6, 
        #60a5fa, 
        transparent 40%
      )`,
    content: '""',
    position: 'absolute',
    width: '300%',
    height: '300%',
    animation: `${rotateGlow} 4s linear infinite`,
    opacity: 0,
    transition: 'opacity 0.4s ease',
    borderRadius: 'inherit',
    zIndex: 0,
  }
}));

export default AnimatedButton;