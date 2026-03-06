import LoginForm from "@/components/LoginForm";
import { Avatar, Box, Container, Fade, Grow, Paper, Stack, Typography } from "@mui/material";
import { cookies, headers } from "next/headers";
import TerminalOutlinedIcon from '@mui/icons-material/TerminalOutlined';

export default async function Page() {
  return (
    <Container maxWidth="sm" sx={{ minHeight: "calc(100vh - 70px)", display: "flex", alignItems: "center", justifyContent: "center" }}>
      <Fade in timeout={800}>
        <Stack spacing={3} alignItems="center" justifyContent="center" sx={{ width: "100%" }}>
          <Avatar variant="circular"  sx={{ width: 70, height: 70, fontSize: 40, backgroundColor: 'primary.main' }}>
            <TerminalOutlinedIcon color="action" fontSize="inherit" />
          </Avatar>
          <Typography variant="h5" color="textPrimary" sx={{ fontWeight: 800 }}>
            Acesso a Demonstração
          </Typography>
          <Grow in timeout={1000}>
            <Paper elevation={3} sx={{ p: { xs: 3, md: 5 }, width: '100%', borderRadius: 6 }}>
              <LoginForm />
            </Paper>
          </Grow>
        </Stack>
      </Fade>
    </Container>
  );
}