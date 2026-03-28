"use client";

import React from "react";
import { SnackbarProvider} from 'notistack';

export function NotificationsProvider({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <SnackbarProvider maxSnack={5}>
      {children}
    </SnackbarProvider>
  );
}
