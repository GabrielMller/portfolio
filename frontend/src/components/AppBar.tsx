import { Avatar, Box, Container, IconButton, Toolbar, Typography } from "@mui/material";
import { AppBar as MuiAppBar } from "@mui/material";
import TerminalOutlinedIcon from '@mui/icons-material/TerminalOutlined';
import { ThemeToggleButton } from "@/components/ColorModeSelect";
import { GitHub, LinkedIn } from "@mui/icons-material";
import Link from "@/components/Link";

export default function AppBar() {
  return (
    <MuiAppBar elevation={1} color="transparent" position="static">
      <Container maxWidth="xl">
        <Toolbar disableGutters sx={{ display: 'flex', justifyContent: 'space-between', height: '60px' }}>
          <Box href={'/'} component={Link} sx={{ display: 'flex', alignItems: 'center', gap: 1.5, textDecoration: 'none' }}>
            <Avatar variant="rounded"  sx={{ width: 40, height: 40, backgroundColor: 'primary.main' }}>
              <TerminalOutlinedIcon color="action" />
            </Avatar>
            <Typography color="textPrimary" variant="h6" sx={{ fontWeight: 800, letterSpacing: -1 }}>
              Gabriel Muller
            </Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <IconButton size="small" color="inherit" href="https://github.com/GabrielMller" target="_blank" ><GitHub /></IconButton>
            <IconButton size="small" color="inherit" href="https://www.linkedin.com/in/gabriel-muller-5a5136249/" target="_blank" ><LinkedIn /></IconButton>
            <ThemeToggleButton />
          </Box>
        </Toolbar>
      </Container>
    </MuiAppBar>
  );
}