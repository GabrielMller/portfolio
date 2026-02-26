'use server';
import { Container, Divider, Fade, Paper, Stack, Typography } from "@mui/material";
import ArrowForwardOutlinedIcon from '@mui/icons-material/ArrowForwardOutlined';
import Image from "next/image";
import AnimatedButton from "@/components/AnimatedButton";
import HubOutlinedIcon from '@mui/icons-material/HubOutlined';
import Kafka from "@/components/Kafka";
import StatusBadge from "@/components/StatusBadge";
import StorageOutlinedIcon from '@mui/icons-material/StorageOutlined';
import React from "react";
import CloudOutlinedIcon from '@mui/icons-material/CloudOutlined';

type Infra = {
  status?: 'OK' | 'WARNING' | 'ERROR';
  nodes?: number;
  totalNodes?: number;
  path?: string;
  name: string;
  description: string;
  icon: React.ReactNode;
  error: boolean;
}

export default async function Mulesoft() {
  let infra : Infra[] = [
    {
      path: 'kafka',
      name: 'Kafka',
      description: 'Brokers',
      totalNodes: 3,
      icon: <Kafka sx={{ fontSize: 32, color: 'black' }} />,
      error: false
    },
    {
      path: 'postgres',
      name: 'PostgreSQL',
      description: 'Nodes',
      totalNodes: 3,
      icon: <StorageOutlinedIcon sx={{ fontSize: 32, color: 'primary.main' }} />,
      error: false
    },
    {
      name: 'Cloudflare Tunnel',
      description: 'Status',
      icon: <CloudOutlinedIcon sx={{ fontSize: 32, color: 'warning.main' }} />,
      status: 'OK',
      totalNodes: 1,
      nodes: 1,
      error: false
    }
  ];

  await Promise.all(infra.map(async (service) => {
    if (!service.path) return;
    try {
      const res = await fetch(`${process.env.MULESOFT_URL}/api/monitoring/v1/${service.path}`, {
        cache: 'no-store'
      });
      if (res.ok) {
        const data = await res.json();
        service.status = data.status;
        service.nodes = data.nodes;
      } else {
        throw new Error(`Failed to fetch ${service.name} status`);
      }
    } catch (error) {
      console.error(error);
      service.status = 'ERROR';
      service.nodes = 0;
      service.error = true;
    }
  }));

  infra = infra.map(service => {
    if (service.name === 'Cloudflare Tunnel' && infra.filter(s => s.error).length >= infra.length - 1) {
      return {
        ...service,
        status: 'ERROR',
        nodes: 0
      };
    }
    return service;
  });
  
  return (
    <Fade in timeout={800}>
      <Container fixed sx={{ mt: 4, mb: 4 }}>
        <Stack spacing={4} alignItems={'center'}>
          <Image src="https://developer.salesforce.com/resources2/certification-site/images/2024/2024-02_SF-Cert-Badge_MuleSoft-Developer-I.svg" alt="Mulesoft Developer I" width={100} height={100} />
          <AnimatedButton color="primary" href="/mulesoft/demo" >
            <Stack sx={{ padding: 3}} direction={'row'} spacing={2} alignItems={'center'}>
              <Typography variant="h5">
                Ver sistema
              </Typography>
              <ArrowForwardOutlinedIcon />
            </Stack>
          </AnimatedButton>
          <AnimatedButton sx={{width:{
            xs: '100%',
            md: '80%',
            lg: '50%'
          }}}>
            <Stack p={3} direction={'column'} spacing={2} alignItems={'center'}>
              <Stack direction={'row'} spacing={2} alignItems={'center'}>
                <Paper elevation={1} sx={{ p: 1, m: 0, alignItems: 'center', display: 'flex', justifyContent: 'center' }}>
                  <HubOutlinedIcon sx={{ fontSize: 48}} color="info" />
                </Paper>
                <Typography variant="body1" align="center" sx={{ textTransform: 'none' }}>
                  Infraestrutura
                </Typography>
              </Stack>
              <Divider sx={{ width: '100%' }} />
              { infra.map((service) => (
                <React.Fragment key={service.name}>
                  <Stack direction={'row'} spacing={2} alignItems={'center'} width={'100%'}>
                    {service.icon}
                    <Stack direction={'column'} spacing={0} alignItems={'flex-start'} width={'100%'}>
                      <Typography variant="body2" color="textSecondary">
                        {service.name}
                      </Typography>
                      <Typography variant="body2" color="textSecondary">
                        {service.description}
                      </Typography>
                    </Stack>
                    <Stack direction={'column'} spacing={0} alignItems={'flex-end'} sx={{ marginLeft: 'auto' }}>
                      <StatusBadge status={service.status || 'ERROR'} />
                      <Typography variant="body2" color="textSecondary">
                        {service.nodes}/{service.totalNodes}
                      </Typography>
                    </Stack>
                  </Stack>
                  <Divider sx={{ width: '100%' }} />
                </React.Fragment>
              ))}
            </Stack>
          </AnimatedButton>
        </Stack>
      </Container>
    </Fade>
  );
}