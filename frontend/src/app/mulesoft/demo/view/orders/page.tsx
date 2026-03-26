import { AnimatedBox } from "@/components/AnimatedButton";
import { ordersApi } from "@/lib/mulesoft-client";
import { Chip, Container, Stack, Typography } from "@mui/material";
import { cookies } from "next/headers";
import { unauthorized } from "next/navigation";

export default async function OrdersPage() {
  const token = (await cookies()).get("authjs.session-token")?.value;

  if (!token) {
    return unauthorized();
  }

  const orders = await ordersApi.getOrders(token);
  return (
    <Container fixed>
      <AnimatedBox sx={{ cursor: 'default'}}>
        <Stack p={2}>
          <Stack direction={'row'} justifyContent={'space-between'} alignItems={'center'}>
            <Typography variant="h6">
              Pedido #12345 - 13/09/2024
            </Typography>
            <Chip label="Em Processamento" color="primary" variant="outlined" />
          </Stack>
        </Stack>
      </AnimatedBox>
    </Container>
  );
}