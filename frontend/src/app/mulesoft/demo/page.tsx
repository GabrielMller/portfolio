import LoginForm from "@/components/LoginForm";
import { Box, Container, Fade, Grow, Paper, Stack } from "@mui/material";
import { cookies, headers } from "next/headers";

export default async function Page() {
  const a = await cookies();
  console.log("Headers:", a.get("authjs.session-token"));
  return (
    <Container maxWidth="sm" sx={{ minHeight: "calc(100vh - 60px)", display: "flex", alignItems: "center", justifyContent: "center" }}>
      <Fade in timeout={800}>
        <Stack spacing={4} alignItems="center" justifyContent="center" sx={{ width: "100%" }}>
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