'use client';

import { Collapse, Stack, Typography } from "@mui/material";
import { AnimatedBox } from "../AnimatedButton";
import React from "react";

export default function InformationBox() {
  const [open, setOpen] = React.useState(false);
  return (
    <AnimatedBox onClick={() => setOpen(prev => !prev)} sx={{ cursor: 'pointer', transition: "all 0.6s cubic-bezier(0.4, 0, 0.2, 1)" }} width={open ? '100%' : '300px'}>
      <Stack p={3} spacing={2} alignItems="center">
        <Typography variant="h6">Informações Adicionais</Typography>
        <Collapse in={open} timeout="auto" unmountOnExit>
          <Typography variant="body1">
            - O sistema é uma simulação de um e-commerce integrado com MuleSoft, utilizando autenticação JWT e endpoints REST para gerenciamento de itens e pedidos. 
          </Typography>
          <Typography variant="body1">
            - Utilizei o runtime free do Mulesoft rodando de forma containerizada no kubernetes em uma máquina local
          </Typography>
          <Typography variant="body1">
            - Como esse runtime tem muitas limitações, não pude usar os conectores do kafka e nem gemini, também não pude usar transform message e nem componentes EE como o cache
          </Typography>
          <Typography variant="body1">
            - Para contornar essas limitações, criei um conector para o kafka, utilizei a API do Gemini e implementei uma solução de cache simples utilizando Object Store
          </Typography>
          <Typography variant="body1">
            - Caso o DB esteja indisponível, enviamos os dados para o kafka
          </Typography>
          <Typography variant="body1">
            - Caso o Kafka esteja indisponível, enviamos os dados para o DB
          </Typography>
          <Typography variant="body1">
            - Caso os dois serviços estejam indisponíveis, armazenamos os dados em um cache no object store e tentamos enviar novamente a cada 5 minutos
          </Typography>
          <Typography variant="body1">
            - Acabei não implementando um websocket para atualizar o frontend em tempo real, mas a cada ação o frontend busca os dados mais recentes, então a atualização é quase em tempo real
          </Typography>
          <Typography variant="body1">
            - Internacionalização em desenvolvimento, mas a maioria dos textos estão em português
          </Typography>
        </Collapse>
      </Stack>
    </AnimatedBox>
  );
}