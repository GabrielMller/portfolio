'use client';
import { type ItemData } from "@/lib/mulesoft-client";
import { useCartMulesoft } from "@/lib/MulesoftCartProvider";
import AddShoppingCartIcon from '@mui/icons-material/AddShoppingCart';
import { IconButton } from "@mui/material";

export default function MulesoftAddToCartButton({ item }: { item: ItemData }) {
  const { addToCart } = useCartMulesoft();
  return (
    <IconButton size="small" aria-label="add to shopping cart" onClick={() => addToCart(item)}>
      <AddShoppingCartIcon />
    </IconButton>
  );
}