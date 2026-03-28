'use client';

import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Stack, TextField } from "@mui/material";
import { AnimatedButton } from "../AnimatedButton";
import React from "react";
import { usersApi } from "@/lib/mulesoft-client";
import { useSnackbar } from "notistack";

export default function SuggestionButton() {
  const [open, setOpen] = React.useState(false);
  const [loading, setLoading] = React.useState(false);
  const [title, setTitle] = React.useState('');
  const [description, setDescription] = React.useState('');

  const { enqueueSnackbar } = useSnackbar();

  const handleSubmit = async () => {
    setLoading(true);
    try {
      const response = await usersApi.suggestion(title, description);
      enqueueSnackbar('Sugestão enviada com sucesso!', { variant: 'success' });
    } catch (error) {
      enqueueSnackbar( error instanceof Error ? error.message : 'Erro ao enviar sugestão. Tente novamente.', { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };


  return (
    <>
      <AnimatedButton onClick={() => setOpen(true)} >
        <Stack p={3}>
          Envie uma sugestão
        </Stack>
      </AnimatedButton>
      <Dialog fullWidth open={open} onClose={() => setOpen(false)}>
        <DialogTitle>Enviar sugestão</DialogTitle>
        <DialogContent dividers>
          <Stack spacing={2}>
            <TextField
              label="Título"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />
            <TextField
              label="Descrição"
              multiline
              rows={4}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />

          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)} disabled={loading}>Cancelar</Button>
          <Button onClick={handleSubmit} disabled={loading}>
            Enviar
          </Button>
        </DialogActions>
      </Dialog>
    </>
  )
}