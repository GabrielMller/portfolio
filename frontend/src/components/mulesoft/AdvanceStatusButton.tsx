"use client";

import { ordersApi, type OrderSummary } from "@/lib/mulesoft-client";
import { useMulesoftToken } from "@/lib/MulesoftTokenProvider";
import { Button } from "@mui/material";
import { useSnackbar } from "notistack";
import React from "react";

export default function AdvanceStatusButton({
  order,
}: {
  order: Omit<OrderSummary, "items">;
}) {
  const [loading, setLoading] = React.useState(false);
  const { enqueueSnackbar } = useSnackbar();
  const { token } = useMulesoftToken();
  const handleAdvanceStatus = async () => {
    setLoading(true);
    try {
      if (order.status === "PYMT") {
        const response = await ordersApi.payment(
          token!,
          order.id,
          order.total,
          "CONFIRMED",
          "PIX",
        );
        enqueueSnackbar(response.description, { variant: "success", autoHideDuration: 3000 });
      } else if (order.status === "PCKG") {
        const response = await ordersApi.delivery(token!, order.id, "STARTED");
        enqueueSnackbar(response.description, { variant: "success", autoHideDuration: 3000 });
      } else if (order.status === "DLVR") {
        const response = await ordersApi.delivery(token!, order.id, "COMPLETED");
        enqueueSnackbar(response.description, { variant: "success", autoHideDuration: 3000 });
      }
    } catch (error) {
      console.error("Error advancing status:", error);
      enqueueSnackbar("Erro ao avançar status. Tente novamente.", { variant: "error", autoHideDuration: 3000 });
    } finally {
      setLoading(false);
    }
  };
  return (
    <>
    {order.status !== "CPLT" && (
      <Button
        variant="contained"
        color="primary"
        sx={{ mt: 2 }}
        onClick={handleAdvanceStatus}
        loading={loading}
      >
        Advance Status
      </Button>
    )}
    </>
  );
}
