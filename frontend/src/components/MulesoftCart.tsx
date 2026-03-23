"use client";
import { useCartMulesoft } from "@/lib/MulesoftCartProvider";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";
import { Avatar, Badge, Box, IconButton, Menu, Stack, Typography } from "@mui/material";
import React from "react";
import AddIcon from '@mui/icons-material/Add';
import RemoveIcon from '@mui/icons-material/Remove';

export default function MulesoftCart() {
  const { items, addToCart, removeFromCart } = useCartMulesoft();
  const [anchorElConfig, setAnchorElConfig] =
    React.useState<null | HTMLElement>(null);

  const handleOpenConfigMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorElConfig(event.currentTarget);
  };

  const handleCloseUserMenu = () => {
    setAnchorElConfig(null);
  };

  const quantity = items.reduce((acc, item) => acc + item.quantity, 0);
  return (
    <Box sx={{ flexGrow: 0 }}>
      <Badge badgeContent={quantity} color="primary">
        <IconButton onClick={handleOpenConfigMenu} size="small" color="inherit">
          <ShoppingCartIcon />
        </IconButton>
      </Badge>
      {quantity > 0 && (
        <Menu
          sx={{ mt: "30px" }}
          slotProps={{
            paper: {
              sx: {
                minWidth: 150,
                maxHeight: 300,
                display: "flex",
                flexDirection: "column",
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
        </Menu>
      )}
    </Box>
  );
}
