'use client';

import { Avatar, ButtonBase, Collapse, Stack, Typography } from "@mui/material";
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';
import React from "react";
import { type OrderItem } from "@/lib/mulesoft-client";

export default function MulesoftOrderItems({items}: { items: OrderItem[] }) {
  const [open, setOpen] = React.useState(false);

  const handleClick = () => {
    setOpen(!open);
  }

  return (
    <Stack spacing={2}>
      <Collapse in={open} timeout="auto" unmountOnExit>
        <Stack spacing={2} mt={2}>
          {items.map(item => (
            <Stack key={item.id} direction={'row'} spacing={2} alignItems={'center'}>
              <Avatar variant="rounded" src={item.image} alt={item.name} sx={{ width: {
                sm: 50,
                md: 50,
                lg: 60
              }, height: {
                sm: 50,
                md: 50,
                lg: 60
              } }} />
              <Stack width={'80%'}>
                <Typography variant="body1" noWrap>{item.name}</Typography>
                <Typography variant="body2" color="text.secondary">
                  Quantidade: {item.quantity}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Preço: R$ {item.price.toFixed(2)}
                </Typography>
              </Stack>
            </Stack>
          ))}
        </Stack>
      </Collapse>
      <ButtonBase onClick={handleClick}>
        <ArrowDownwardIcon sx={{ transform: open ? 'rotate(180deg)' : 'rotate(0deg)', transition: 'transform 0.2s ease-in-out' }} />
      </ButtonBase>
    </Stack>
  );
}