"use server";
import {
  Container,
  Divider,
  Fade,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import ArrowForwardOutlinedIcon from "@mui/icons-material/ArrowForwardOutlined";
import Image from "next/image";
import { AnimatedButton } from "@/components/AnimatedButton";
import HubOutlinedIcon from "@mui/icons-material/HubOutlined";
import Kafka from "@/components/Kafka";
import StatusBadge from "@/components/StatusBadge";
import StorageOutlinedIcon from "@mui/icons-material/StorageOutlined";
import React from "react";
import CloudOutlinedIcon from "@mui/icons-material/CloudOutlined";
import Kubernetes from "@/components/Kubernetes";
import {
  Diagram,
  DiagramGroupButtonsAutosize,
} from "@/components/DiagramButton";
import SupervisorAccountOutlinedIcon from "@mui/icons-material/SupervisorAccountOutlined";
import { monitoringApi } from "@/lib/mulesoft-client";
import { node } from "@/lib/utils/Nodes";
import LoginOutlinedIcon from "@mui/icons-material/LoginOutlined";
import MulesoftAppBar from "@/components/MulesoftAppBar";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";
import CategoryIcon from "@mui/icons-material/Category";
import InformationBox from "@/components/mulesoft/InformationBox";

type Infra = {
  status?: "OK" | "WARNING" | "ERROR";
  nodes?: number;
  totalNodes?: number;
  path?: string;
  name: string;
  description: string;
  icon: React.ReactNode;
  error: boolean;
};

export default async function Mulesoft() {
  let infra: Infra[] = [
    {
      path: "kafka",
      name: "Kafka",
      description: "Brokers",
      totalNodes: 3,
      icon: <Kafka sx={{ fontSize: 32, color: "black" }} />,
      error: false,
    },
    {
      path: "postgres",
      name: "PostgreSQL",
      description: "Nodes",
      totalNodes: 3,
      icon: (
        <StorageOutlinedIcon sx={{ fontSize: 32, color: "primary.main" }} />
      ),
      error: false,
    },
    {
      name: "Cloudflare Tunnel",
      description: "Status",
      icon: <CloudOutlinedIcon sx={{ fontSize: 32, color: "warning.main" }} />,
      status: "OK",
      totalNodes: 1,
      nodes: 1,
      error: false,
    },
    {
      name: "Kubernetes Cluster",
      description: "Pods",
      icon: <Kubernetes sx={{ fontSize: 32, color: "primary.main" }} />,
      status: "OK",
      error: false,
      path: "kubernetes",
    },
  ];

  await Promise.all(
    infra.map(async (service) => {
      if (!service.path) return;
      try {
        const data = await monitoringApi.getMonitoring(service.path);
        service.status = data.status;
        service.nodes = data.nodes;
        service.totalNodes = data.totalNodes;
      } catch (error) {
        console.error(error);
        service.status = "ERROR";
        service.nodes = 0;
        service.error = true;
      }
    }),
  );

  infra = infra.map((service) => {
    if (
      service.name === "Cloudflare Tunnel" &&
      infra.filter((s) => s.error).length >= infra.length - 1
    ) {
      return {
        ...service,
        status: "ERROR",
        nodes: 0,
      };
    }
    return service;
  });

  return (
    <>
      <MulesoftAppBar />
      <Fade in timeout={800}>
        <Container fixed sx={{ mt: 4, mb: 4 }}>
          <Stack spacing={4} alignItems={"center"}>
            <Image
              src="https://developer.salesforce.com/resources2/certification-site/images/2024/2024-02_SF-Cert-Badge_MuleSoft-Developer-I.svg"
              alt="Mulesoft Developer I"
              width={100}
              height={100}
            />
            <AnimatedButton color="primary" href="/mulesoft/demo">
              <Stack
                sx={{ padding: 3 }}
                direction={"row"}
                spacing={2}
                alignItems={"center"}
              >
                <Typography variant="h5">Ver sistema</Typography>
                <ArrowForwardOutlinedIcon />
              </Stack>
            </AnimatedButton>
            <InformationBox />
            <AnimatedButton
              sx={{
                width: {
                  xs: "100%",
                  md: "80%",
                  lg: "50%",
                },
              }}
            >
              <Stack
                p={3}
                direction={"column"}
                spacing={2}
                alignItems={"center"}
              >
                <Stack direction={"row"} spacing={2} alignItems={"center"}>
                  <Paper
                    elevation={1}
                    sx={{
                      p: 1,
                      m: 0,
                      alignItems: "center",
                      display: "flex",
                      justifyContent: "center",
                    }}
                  >
                    <HubOutlinedIcon sx={{ fontSize: 48 }} color="info" />
                  </Paper>
                  <Typography
                    variant="body1"
                    align="center"
                    sx={{ textTransform: "none" }}
                  >
                    Infraestrutura
                  </Typography>
                </Stack>
                <Divider sx={{ width: "100%" }} />
                {infra.map((service) => (
                  <React.Fragment key={service.name}>
                    <Stack
                      direction={"row"}
                      spacing={2}
                      alignItems={"center"}
                      width={"100%"}
                    >
                      {service.icon}
                      <Stack
                        direction={"column"}
                        spacing={0}
                        alignItems={"flex-start"}
                        width={"100%"}
                      >
                        <Typography variant="body2" color="textSecondary">
                          {service.name}
                        </Typography>
                        <Typography variant="body2" color="textSecondary">
                          {service.description}
                        </Typography>
                      </Stack>
                      <Stack
                        direction={"column"}
                        spacing={0}
                        alignItems={"flex-end"}
                        sx={{ marginLeft: "auto" }}
                      >
                        <StatusBadge status={service.status || "ERROR"} />
                        <Typography variant="body2" color="textSecondary">
                          {service.nodes}/{service.totalNodes}
                        </Typography>
                      </Stack>
                    </Stack>
                    <Divider sx={{ width: "100%" }} />
                  </React.Fragment>
                ))}
              </Stack>
            </AnimatedButton>
            <DiagramGroupButtonsAutosize diagrams={diagrams} />
          </Stack>
        </Container>
      </Fade>
    </>
  );
}

const diagrams: Diagram[] = [
  {
    id: 1,
    name: "Monitoramento",
    icon: <HubOutlinedIcon sx={{ fontSize: 32 }} color="info" />,
    nodes: {
      ...node("Frontend", "NEXT", "Monitoramento da infraestrutura"),
      nodes: [
        {
          ...node(
            "Mule EAPI",
            "MULESOFT",
            "Coleta de métricas e status dos serviços",
          ),
          sideNodes: [
            node("CACHE", "CACHE", "Cache de status para reduzir latência"),
          ],
          nodes: [
            {
              ...node(
                "Monitoring SAPI",
                "MULESOFT",
                "Processamento e armazenamento de métricas",
              ),
              nodes: [
                node(
                  "Prometheus",
                  "MONITORING",
                  "Coleta e armazenamento de métricas",
                ),
              ],
            },
          ],
        },
      ],
    },
  },
  {
    id: 2,
    name: "Login",
    icon: <LoginOutlinedIcon sx={{ fontSize: 32, color: "primary.main" }} />,
    nodes: {
      ...node("Login", "NEXT", "Criação e gestão de token"),
      nodes: [
        {
          ...node(
            "Mule EAPI",
            "MULESOFT",
            "Contrato de API e validação dos dados",
          ),
          nodes: [
            {
              ...node(
                "Users PAPI",
                "MULESOFT",
                "Validação de regras de negócio e e sincronização de dados do usuário",
              ),
              sideNodes: [
                {
                  ...node("Users SAPI", "MULESOFT", ""),
                },
              ],
              nodes: [
                {
                  ...node(
                    "Users SAPI",
                    "MULESOFT",
                    "Criação de conta e gestão de dados do usuário",
                  ),
                  nodes: [
                    node("Users Table", "POSTGRES", "Tabela de usuários"),
                  ],
                },
              ],
            },
          ],
        },
      ],
    },
  },
  {
    id: 3,
    name: "Criar Conta",
    icon: (
      <SupervisorAccountOutlinedIcon
        sx={{ fontSize: 32, color: "primary.main" }}
      />
    ),
    nodes: {
      ...node("Criar Conta", "NEXT", "Criação e gestão de token"),
      nodes: [
        {
          ...node(
            "Mule EAPI",
            "MULESOFT",
            "Contrato de API e validação dos dados",
          ),
          nodes: [
            {
              ...node(
                "Users PAPI",
                "MULESOFT",
                "Validação de regras de negócio e orquestração da criação de conta",
              ),
              nodes: [
                {
                  ...node(
                    "Users SAPI",
                    "MULESOFT",
                    "Criação de conta e gestão de dados do usuário",
                  ),
                  nodes: [
                    node("Accounts Table", "POSTGRES", "Tabela de contas"),
                    node(
                      "Clients Table",
                      "POSTGRES",
                      "Tabela de clientes como LEAD",
                    ),
                    node("Users Table", "POSTGRES", "Tabela de usuários"),
                  ],
                },
              ],
            },
          ],
        },
      ],
    },
  },
  {
    id: 4,
    name: "Pedidos",
    icon: <ShoppingCartIcon sx={{ fontSize: 32, color: "primary.main" }} />,
    nodes: {
      ...node("Pedidos", "NEXT", "Listagem e gestão de pedidos"),
      nodes: [
        {
          ...node(
            "Mule EAPI",
            "MULESOFT",
            "Contrato de API e validação dos dados",
          ),
          nodes: [
            {
              ...node(
                "Orders PAPI",
                "MULESOFT",
                "Validação de regras de negócio e orquestração dos pedidos",
              ),
              sideNodes: [
                {
                  ...node(
                    "Users SAPI",
                    "MULESOFT",
                    "Validação de dados do usuário e verificação de crédito",
                  ),
                },
              ],
              nodes: [
                {
                  ...node(
                    "Orders SAPI",
                    "MULESOFT",
                    "Cria o pedido com o status inicial",
                  ),
                  nodes: [
                    node("Orders Table", "POSTGRES", "Tabela de pedidos"),
                  ],
                },
                {
                  ...node(
                    "Orders Topic",
                    "KAFKA",
                    "Tópico de eventos de pedidos para integração com outros sistemas",
                  ),
                },
                {
                  ...node(
                    "Object Store",
                    "STORAGE",
                    "Em caso de falha no DB e Kafka, salva o pedido no object store para reprocessamento",
                  ),
                },
                {
                  ...node(
                    "Orders SAPI",
                    "MULESOFT",
                    "Em caso de falha na criação do pedido, salva o evento na tabela de eventos para reprocessamento",
                  ),
                  nodes: [
                    node("Events Table", "POSTGRES", "Tabela de eventos"),
                  ],
                },
              ],
            },
          ],
        },
      ],
    },
  },
  {
    id: 5,
    name: "Orders Events",
    icon: <Kafka sx={{ fontSize: 32, color: "black" }} />,
    nodes: {
      ...node(
        "Orders Topic",
        "KAFKA",
        "Tópico de eventos de pedidos para integração com outros sistemas",
      ),
      nodes: [
        {
          ...node(
            "Orders PAPI",
            "MULESOFT",
            "Faz a leitura dos eventos do tópico para atualizar o status do pedido e estoque dos itens",
          ),
          sideNodes: [
            {
              ...node(
                "Items SAPI",
                "MULESOFT",
                "Verifica o estoque e preço dos itens do pedido e atualiza o estoque reservado",
              ),
            },
          ],
          nodes: [
            {
              ...node(
                "Orders SAPI",
                "MULESOFT",
                "Atualiza o pedido para esperando pagamento",
              ),
              nodes: [
                node("Orders Table", "POSTGRES", "Tabela de pedidos"),
                node(
                  "Order Items Table",
                  "POSTGRES",
                  "Tabela de itens dos pedidos",
                ),
              ],
            },
            {
              ...node(
                "Orders DLQ",
                "KAFKA",
                "Em caso de falha no processamento do evento, salva o evento na DLQ para reprocessamento",
              ),
            },
            {
              ...node(
                "items SAPI",
                "MULESOFT",
                "Atualiza o estoque dos itens do pedido",
              ),
              nodes: [node("Items Table", "POSTGRES", "Tabela de itens")],
            },
          ],
        },
      ],
    },
  },
  {
    id: 6,
    name: "Itens",
    icon: <CategoryIcon sx={{ fontSize: 32, color: "primary.main" }} />,
    nodes: {
      ...node("Itens", "NEXT", "Listagem e gestão de itens"),
      nodes: [
        {
          ...node(
            "Mule EAPI",
            "MULESOFT",
            "Contrato de API e validação dos dados",
          ),
          nodes: [
            {
              ...node(
                "Items PAPI",
                "MULESOFT",
                "Validação de regras de negócio e orquestração dos itens",
              ),
              nodes: [
                {
                  ...node(
                    "Items Topic",
                    "KAFKA",
                    "Tópico de eventos de itens para integração com outros sistemas",
                  ),
                },
                {
                  ...node(
                    "Items SAPI",
                    "MULESOFT",
                    "Validação de dados dos itens e verificação de estoque",
                  ),
                  sideNodes: [
                    {
                      ...node(
                        "Gemini API",
                        "AI",
                        "Geração de descrição e imagem dos itens utilizando IA",
                      ),
                    },
                  ],
                },
              ],
            },
          ],
        },
      ],
    },
  },
  {
    id: 7,
    name: "Items Events",
    icon: <Kafka sx={{ fontSize: 32, color: "black" }} />,
    nodes: {
      ...node(
        "Items Topic",
        "KAFKA",
        "Tópico de eventos de itens para integração com outros sistemas",
      ),
      nodes: [
        {
          ...node(
            "Items PAPI",
            "MULESOFT",
            "Faz a leitura dos eventos do tópico para atualizar o estoque dos itens",
          ),
          sideNodes: [
            {
              ...node(
                "Users SAPI",
                "MULESOFT",
                "Verifica se o usuário é admin para permitir a atualização do estoque dos itens",
              ),
            },
          ],
          nodes: [
            {
              ...node("Items SAPI", "MULESOFT", "Atualiza o estoque dos itens"),
              nodes: [
                node("Items Table", "POSTGRES", "Tabela de itens"),
                node("Stock Table", "POSTGRES", "Tabela de estoque dos itens"),
              ],
            },
            {
              ...node(
                "Items DLQ",
                "KAFKA",
                "Em caso de falha no processamento do evento, salva o evento na DLQ para reprocessamento",
              ),
            },
          ],
        },
      ],
    },
  },
];
