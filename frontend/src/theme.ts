'use client';
import { createTheme, responsiveFontSizes } from '@mui/material/styles';
import Link from '@/components/Link';

const FONT_FAMILY = '"Plus Jakarta Sans", "Inter", sans-serif';

declare module '@mui/material/styles' {
  interface Palette {
    cosmic: {
      star: string;
      meteor: string;
      glow: string;
    };
  }
  interface PaletteOptions {
    cosmic?: {
      particleChannel?: string;
      streakChannel?: string;
    };
  }
}

let theme = createTheme({
  typography: {
    fontFamily: FONT_FAMILY,
  },
  cssVariables: {
    colorSchemeSelector: 'class',
    nativeColor: true,
  },
  colorSchemes: {
    light: {
      palette: {
        mode: 'light',
        primary: { main: '#3b82f6' },
        secondary: { main: '#a855f7' },
        background: { 
          default: '#f8fafc', 
          paper: '#ffffff' 
        },
        text: { 
          primary: '#0f172a', 
          secondary: '#475569' 
        },
        cosmic: {
          particleChannel: '255 255 255', 
          streakChannel: '255 255 255', 
        }
      }
    },
    dark: {
      palette: {
        mode: 'dark',
        primary: { main: '#3b82f6' },
        secondary: { main: '#a855f7' },
        background: { default: '#020617', paper: '#0f172a' },
        text: { primary: '#ffffff', secondary: '#94a3b8' },
        cosmic: {
          particleChannel: '255 255 255',
          streakChannel: '96 165 250',
        }
      }
    }
  },
  components: {
    MuiLink: {
      defaultProps: {
        component: Link
      }
    },
    MuiButtonBase: {
      defaultProps: {
        LinkComponent: Link
      }
    }
  }
});

theme = responsiveFontSizes(theme, {
  breakpoints: ["xs", "sm", "md", "lg", "xl"],
  factor: 2,
});

export default theme;
