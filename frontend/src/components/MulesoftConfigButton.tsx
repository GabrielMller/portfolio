'use client';

import { LogoutOutlined, Settings } from "@mui/icons-material";
import { Box, Divider, IconButton, ListItemIcon, Menu, MenuItem, Stack, Tooltip } from "@mui/material";
import React from "react";
import { ThemeToggleButton } from "./ColorModeSelect";
import Form from "next/form";
import { logout } from "@/lib/actions/login";
import PowerSettingsNewIcon from '@mui/icons-material/PowerSettingsNew';
import { useMulesoft } from "@/lib/MulesoftProvider";

type Props = {
  hasSession: boolean;
};

export default function MulesoftConfigButton({ hasSession }: Props) {
  const { dbState, kafkaState, setDbState, setKafkaState } = useMulesoft();
  const [anchorElConfig, setAnchorElConfig] = React.useState<null | HTMLElement>(null);

  const handleOpenConfigMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorElConfig(event.currentTarget);
  }

  const handleCloseUserMenu = () => {
    setAnchorElConfig(null);
  }

  return (
    <Box sx={{ flexGrow: 0 }}>
      <Tooltip title="Open settings">
        <IconButton onClick={handleOpenConfigMenu} size="small" color="inherit">
          <Settings />
        </IconButton>
      </Tooltip>
      <Menu
        sx={{ mt: '30px' }}
        slotProps={{
          paper: {
            sx: {
              minWidth: 150,
              display: 'flex',
              flexDirection: 'column',
              gap: 1
            }
          }
        }}
        id="menu-appbar"
        anchorEl={anchorElConfig}
        anchorOrigin={{
          vertical: 'top',
          horizontal: 'right',
        }}
        keepMounted
        transformOrigin={{
          vertical: 'top',
          horizontal: 'right',
        }}
        open={Boolean(anchorElConfig)}
        onClose={handleCloseUserMenu}
      >
        <Stack>
          <MenuItem onClick={() => {
              setDbState(dbState === "on" ? "off" : "on");
            }}>
            <ListItemIcon>
              <PowerSettingsNewIcon color={dbState === "on" ? "success" : "error"} />
            </ListItemIcon>
            Database
          </MenuItem>
          <MenuItem onClick={() => {
            setKafkaState(kafkaState === "on" ? "off" : "on");
          }}>
            <ListItemIcon>
              <PowerSettingsNewIcon color={kafkaState === "on" ? "success" : "error"} />
            </ListItemIcon>
            Kafka
          </MenuItem>
        </Stack>
        <Divider sx={{ my: 1}} />
        <Stack direction={"row"} alignItems={"center"} justifyContent={"flex-end"} sx={{ px: 1, width: '100%' }}>
          <ThemeToggleButton />
          {hasSession && (
            <Form action={logout}>
              <IconButton type="submit" color="inherit" size="small">
                <LogoutOutlined />
              </IconButton>
            </Form>
          )}
        </Stack>
      </Menu>
    </Box>
  );
}