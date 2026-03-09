'use server';
import LoginForm from "@/components/LoginForm";
import { Avatar, Button, Container, Divider, Fade, Grow, Paper, Stack, Typography } from "@mui/material";
import TerminalOutlinedIcon from '@mui/icons-material/TerminalOutlined';
import { GitHub, Google } from "@mui/icons-material";
import { signIn } from "@/lib/auth";

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
            <Paper elevation={3} sx={{ p: { xs: 3, md: 5 }, width: '100%', borderRadius: 6, gap: 1, display: 'flex', flexDirection: 'column', }}>
              <Stack direction={"row"} spacing={2} alignItems={"center"}>
                <form style={{ width: '100%' }} action={async () => {
                  "use server"
                  await signIn("google")
                }}>
                  <Button type="submit" color="inherit" variant="outlined" startIcon={<Google />} fullWidth sx={{ mb: 2 }}>
                    Google
                  </Button>
                </form>
                <form style={{ width: '100%' }} action={async () => {
                  "use server"
                  await signIn("github")
                }}>
                  <Button type="submit" color="inherit" variant="outlined" startIcon={<GitHub />} fullWidth sx={{ mb: 2 }}>
                    GitHub
                  </Button>
                </form>
              </Stack>
              <Divider sx={{ my: 1 }}><Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 700 }}>OU VIA E-MAIL</Typography></Divider>
              <LoginForm />
            </Paper>
          </Grow>
        </Stack>
      </Fade>
    </Container>
  );
}