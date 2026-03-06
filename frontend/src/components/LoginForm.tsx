"use client";
import { Alert, Box, Button, IconButton, InputAdornment, Stack, TextField, Typography } from "@mui/material";
import React, { useActionState } from "react";
import { initialState } from "@/lib/actions/initialState";
import EmailOutlinedIcon from '@mui/icons-material/EmailOutlined';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import { login } from "@/lib/actions/login";
import Form from 'next/form'
import VisibilityOutlinedIcon from '@mui/icons-material/VisibilityOutlined';
import VisibilityOffOutlinedIcon from '@mui/icons-material/VisibilityOffOutlined';
import ArrowForwardOutlinedIcon from '@mui/icons-material/ArrowForwardOutlined';
import PersonOutlineOutlinedIcon from '@mui/icons-material/PersonOutlineOutlined';

export default function LoginForm() {
  const [showPassword, setShowPassword] = React.useState(false);
  const [type, setType] = React.useState("login");
  const [state, formAction, pending] = useActionState(login, initialState);
  return (
    <Box component={Form} action={formAction} autoComplete='on'>
      <input type="hidden" name="type" value={type} />
      <Stack spacing={3}>
        {type === "register" && (
          <TextField
            variant="outlined"
            autoComplete='username'
            name="username"
            label="Username"
            type="text"
            defaultValue={state.data?.username || ""}
            required
            placeholder="john_doe"
            fullWidth
            error={!!state?.errors?.username}
            helperText={state?.errors?.username?.errors.join(', ')}
            slotProps={{
              input: {
                startAdornment: <InputAdornment position="start"><PersonOutlineOutlinedIcon fontSize="small" /></InputAdornment>
              }
            }}
          />
        )}
        <TextField
          variant="outlined"
          autoComplete='username'
          name="email"
          label="Email"
          type="email"
          defaultValue={state.data?.email || ""}
          required
          placeholder="your@email.com"
          fullWidth
          error={!!state?.errors?.email}
          helperText={state?.errors?.email?.errors.join(', ')}
          slotProps={{
            input: {
              startAdornment: <InputAdornment position="start"><EmailOutlinedIcon fontSize="small" /></InputAdornment>
            }
          }}
        />
        <TextField
          name="password"
          label="Password"
          type={showPassword ? "text" : "password"}
          autoComplete="password"
          required
          error={!!state?.errors?.password}
          helperText={state?.errors?.password?.errors.join(', ')}
          defaultValue={state.data?.password || ""}
          fullWidth
          placeholder="*******"
          slotProps={{
            input: {
              startAdornment: <InputAdornment position="start"><LockOutlinedIcon fontSize="small" /></InputAdornment>,
              endAdornment: (<InputAdornment position="end">
                <IconButton onClick={() => setShowPassword(!showPassword)} size="small">
                  {showPassword ? <VisibilityOffOutlinedIcon fontSize="small" /> : <VisibilityOutlinedIcon fontSize="small" />}
                </IconButton>
              </InputAdornment>)
            }
          }}
        />
        {state.error && (
          <Alert severity="error" >
            {state.error}
          </Alert>
        )}
        <Button type="submit" variant="contained" fullWidth size="large" loading={pending} endIcon={<ArrowForwardOutlinedIcon />} sx={{ py: 1.8, borderRadius: 3, fontWeight: 800, fontSize: '1rem' }}> 
          {type === "login" ? "Login" : "Criar Conta"}
        </Button>
        <Stack direction="row" justifyContent="center" alignItems="center" spacing={1}>
          <Typography variant="body2" color="textSecondary" align="center">
            {type === "login" ? "Não tem uma conta?" : "Já tem uma conta?"}
          </Typography>
          <Button variant="text" onClick={() => setType(type === "login" ? "register" : "login")}>
            {type === "login" ? "registre-se" : "faça login"}
          </Button>
        </Stack>
      </Stack>
    </Box>
  );
}
