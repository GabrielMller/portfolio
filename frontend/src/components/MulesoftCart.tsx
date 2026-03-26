"use client";
import { useCartMulesoft } from "@/lib/MulesoftCartProvider";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";
import { Alert, Avatar, Badge, Box, Button, IconButton, Menu, Snackbar, Stack, Typography } from "@mui/material";
import React from "react";
import AddIcon from '@mui/icons-material/Add';
import RemoveIcon from '@mui/icons-material/Remove';
import { useMulesoftToken } from "@/lib/MulesoftTokenProvider";
import { ordersApi, type OrderData } from "@/lib/mulesoft-client";
import { useMulesoft } from "@/lib/MulesoftProvider";
export default function MulesoftCart() {
  const { items, addToCart, removeFromCart, resetCart } = useCartMulesoft();
  const { dbState, kafkaState } = useMulesoft();
  const [open, setOpen] = React.useState(false);
  const [notification, setNotification] = React.useState<{ type: "success" | "error"; message: string } | null>(null);
  const [loading, setLoading] = React.useState(false);
  const { token } = useMulesoftToken();
  const [anchorElConfig, setAnchorElConfig] =
    React.useState<null | HTMLElement>(null);

  const handleOpenConfigMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorElConfig(event.currentTarget);
  };

  const handleCloseUserMenu = () => {
    setAnchorElConfig(null);
  };

  const handleClick = async () => {
    setLoading(true);
    console.log(token)
    try {
      const orderData : OrderData = {
        paymentMethod: "PIX",
        items: items.map(item => ({
          id: item.id,
          quantity: item.quantity,
          price: item.price
        }))
      }
      const response = await ordersApi.createOrder(token!, orderData, { kafkaActive: kafkaState === "on", dbActive: dbState === "on" });
      setNotification({ type: "success", message: response.description });
      setOpen(true);
      resetCart();
    } catch (error) {
      console.error('Error creating order:', error);
      setNotification({ type: "error", message: "Erro ao criar pedido. Tente novamente." });
      setOpen(true);
    } finally {
      setLoading(false);
      handleCloseUserMenu();
    }

  }

  const handleClose = (event?: React.SyntheticEvent | Event, reason?: string) => {
    setOpen(false);
  }

  const quantity = items.reduce((acc, item) => acc + item.quantity, 0);
  return (
    <Box sx={{ flexGrow: 0 }}>
      <Badge badgeContent={quantity} color="primary">
        <IconButton onClick={handleOpenConfigMenu} size="small" color="inherit">
          <ShoppingCartIcon />
        </IconButton>
      </Badge>
        <Menu
          sx={{ mt: "30px" }}
          slotProps={{
            paper: {
              sx: {
                minWidth: 150,
                maxHeight: 300,
                display: "flex",
                flexDirection: "column",
                p: 2,
                gap: 1,
              },
            },
          }}
          id="menu-appbar"
          anchorEl={anchorElConfig}
          anchorOrigin={{
            vertical: "top",
            horizontal: "right",
          }}
          keepMounted
          transformOrigin={{
            vertical: "top",
            horizontal: "right",
          }}
          open={Boolean(anchorElConfig)}
          onClose={handleCloseUserMenu}
        >
          <Stack spacing={2}>
            {items.map((item) => (
              <Stack key={item.id} sx={{ display: "flex", flexDirection: "row", justifyContent: "space-between", gap: 1, px: 2 }}>
                <Avatar variant="rounded" src={item.image} sx={{ width: 50, height: 50 }} />
                <Box sx={{ display: "flex", flexDirection: "column", gap: 0.5, width: 200 }}>
                  <Typography variant="body2" noWrap>
                    {item.name}
                  </Typography>
                  <Box sx={{ display: "flex", justifyContent: 'end', alignItems: 'center', gap: 1, border: "1px solid", borderColor: "divider", borderRadius: 1, ml: "auto" }}>
                    <IconButton size="small" onClick={() => removeFromCart(item.id)}>
                      <RemoveIcon fontSize="inherit" />
                    </IconButton>
                    <Typography variant="body2">
                      {item.quantity}
                    </Typography>
                    <IconButton size="small" disabled={item.quantity >= item.stock} onClick={() => addToCart(item)}>
                      <AddIcon fontSize="inherit" />
                    </IconButton>
                  </Box>
                </Box>
              </Stack>
            ))}
          </Stack>
          <Stack sx={{ display: "flex", justifyContent: 'center', mt: 2 }} spacing={2}>
            <Button onClick={handleClick} loading={loading} variant="contained" size="small">
              Finalizar Pedido
            </Button>
            <Button href="/mulesoft/demo/view/orders" variant="contained" size="small">
              Meus Pedidos
            </Button>
          </Stack>
        </Menu>
      <Snackbar open={open} autoHideDuration={6000} onClose={handleClose}>
        <Alert
          onClose={handleClose}
          severity={notification?.type}
          variant="filled"
          sx={{ width: '100%' }}
        >
          {notification?.message}
        </Alert>
      </Snackbar>
    </Box>
  );
}
