import { AnimatedBox } from "@/components/AnimatedButton";
import Pagination from "@/components/Pagination";
import { ordersApi } from "@/lib/mulesoft-client";
import { ButtonBase, Chip, Container, IconButton, Stack, Typography } from "@mui/material";
import { cookies } from "next/headers";
import { unauthorized } from "next/navigation";
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';
import MulesoftOrderItems from "@/components/MulesoftOrderItems";

const PAGE_SIZE = 20;

export default async function OrdersPage({searchParams}: { searchParams: { [key: string]: string | undefined } }) {
  const token = (await cookies()).get("authjs.session-token")?.value;

  if (!token) {
    return unauthorized();
  }

  const params = await searchParams;

  const page = params.page ? parseInt(params.page) : 1;

  const orders = await ordersApi.getOrders(token, page, PAGE_SIZE);
  return (
    <Container fixed sx={{ mt: 4, mb: 4 }}>
      {
        orders.data.length === 0 ? (
          <Typography variant="h6" align="center" sx={{ mt: 4 }}>
            Nenhum pedido encontrado.
          </Typography>
        ) : (
          orders.data.map(order =>
            <AnimatedBox sx={{ cursor: 'default'}} key={order.id}>
              <Stack p={2}>
                <Stack direction={'row'} justifyContent={'space-between'} alignItems={'center'}>
                  <Typography variant="h6">
                    Pedido #{order.order_number} - {new Date(order.created_at).toLocaleDateString()}
                  </Typography>
                  <Chip label={order.status} color={statusColors[order.status] || "default"} variant="outlined" />
                </Stack>
                <MulesoftOrderItems items={order.items} />
              </Stack>
            </AnimatedBox>
          )
        )
      }
      {orders.metadata.totalItems > PAGE_SIZE && (
        <Pagination currentPage={orders.metadata.page} totalPages={Math.ceil(orders.metadata.totalItems / orders.metadata.pageSize)} />
      )}
    </Container>
  );
}

const statusColors: { [key: string]: "default" | "primary" | "secondary" | "error" | "info" | "success" | "warning" } = {
  waiting_payment: "warning",
  completed: "success",
  pending: "warning"
};