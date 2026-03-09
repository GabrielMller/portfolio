import AppBar from "@/components/AppBar";
import Mulesoft from "@/components/Mulesoft";
import { Container, Box, Typography, Stack, Chip, Button } from "@mui/material";
import { Sparkles } from "lucide-react";
import Image from "next/image";

export default async function Home() {
  return (
    <>
      <AppBar />
      <Box sx={{ pt: { xs: 8, md: 18 }, pb: 10 }}>
        <Container maxWidth="md">
          <Stack spacing={4} alignItems="center" sx={{ textAlign: 'center' }}>
            <Chip label={
              <Stack fontSize={10} direction="row" spacing={1} alignItems="center">
                <Sparkles size={14} color="var(--mui-palette-primary-main)" />
                <Typography variant="caption" sx={{ fontWeight: 800, letterSpacing: 1.5 }}>ARQUITETURA & INTEGRAÇÃO</Typography>
              </Stack>} variant="outlined" color="primary" />
              <Button size="large" variant="outlined" color="primary" href="/mulesoft" sx={{ mt: 2, textTransform: 'none' }} startIcon={<Mulesoft/>}>
                <Typography variant="h4" sx={{ fontWeight: 600, letterSpacing: -1, fontFamily: 'Roboto Mono, monospace' }}>
                  Mulesoft
                </Typography>
              </Button>
          </Stack>
        </Container>
      </Box>
    </>
  );
}
