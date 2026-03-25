'use client';

import { createContext, useContext } from "react";
import React from "react";
import { type ItemData } from "./mulesoft-client";

type CartItem = ItemData & { quantity: number };

type MulesoftCartContextType = {
  items: CartItem[];
  addToCart: (item: ItemData) => void;
  removeFromCart: (itemId: string) => void;
  resetCart: () => void;
}

export const MulesoftCartContext = createContext<MulesoftCartContextType>({
  items: [],
  addToCart: () => {},
  removeFromCart: () => {},
  resetCart: () => {}
});

export function MulesoftCartProvider({ children }: { children: React.ReactNode}) {
  const [items, setItems] = React.useState<CartItem[]>([]);

  function resetCart() {
    setItems([]);
  }

  function removeFromCart(itemId: string) {
    setItems(prevItems => {
      const existingItem = prevItems.find(i => i.id === itemId);
      if (existingItem) {
        if (existingItem.quantity > 1) {
          return prevItems.map(i => i.id === itemId ? { ...i, quantity: i.quantity - 1 } : i);
        }
        return prevItems.filter(i => i.id !== itemId);
      }
      return prevItems;
    });
  }
  function addToCart(item: ItemData) {
    setItems(prevItems => {
      const existingItem = prevItems.find(i => i.id === item.id);
      if (existingItem) {
        if (existingItem.quantity >= item.stock) {
          return prevItems;
        }
        return prevItems.map(i =>
          i.id === item.id ? { ...i, quantity: i.quantity + 1 } : i
        );
      }
      return [...prevItems, { ...item, quantity: 1 }];
    });
  }

  return (
    <MulesoftCartContext.Provider value={{ items, addToCart, removeFromCart, resetCart }}>
      {children}
    </MulesoftCartContext.Provider>
  );
}

export const useCartMulesoft = () => useContext(MulesoftCartContext);