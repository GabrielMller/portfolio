"use client";
import { Alert, Box, Button, IconButton, InputAdornment, Stack, TextField } from "@mui/material";
import { auth, signIn } from "@/lib/auth";
import { getToken } from "next-auth/jwt";
import React, { useActionState } from "react";
import { initialState } from "@/lib/actions/initialState";
import EmailOutlinedIcon from '@mui/icons-material/EmailOutlined';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import { login } from "@/lib/actions/login";
import Form from 'next/form'
import VisibilityOutlinedIcon from '@mui/icons-material/VisibilityOutlined';
import VisibilityOffOutlinedIcon from '@mui/icons-material/VisibilityOffOutlined';

export default function LoginForm() {
  const [showPassword, setShowPassword] = React.useState(false);
  const [state, formAction, pending] = useActionState(login, initialState);
  return (
    <Box component={Form} action={formAction} autoComplete='on'>
      <Stack spacing={3}>
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
        <Button type="submit" variant="contained" fullWidth>
          Login
        </Button>
      </Stack>
    </Box>
  );
}
