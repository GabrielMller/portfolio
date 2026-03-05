import theme from "@/theme";
import { AppRouterCacheProvider } from "@mui/material-nextjs/v16-appRouter";
import InitColorSchemeScript from "@mui/material/InitColorSchemeScript";
import type { Metadata } from "next";
import React from "react";
import LinearProgress from "@mui/material/LinearProgress";
import { ThemeProvider } from "@mui/material/styles";
import CssBaseline from "@mui/material/CssBaseline";
import AdaptiveBackground from "@/components/AdaptiveBackground";
import AppBar from "@/components/AppBar";
import './globals.css';

export const metadata: Metadata = {
  title: "Portfolio - Home",
  description: "Welcome to my portfolio website! Explore my projects, skills, and experience in software development. Discover how I can contribute to your team and bring innovative solutions to life.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="pt-BR" suppressHydrationWarning>
      <body>
        <React.Suspense fallback={<LinearProgress />} >
          <InitColorSchemeScript attribute="class" />
          <AppRouterCacheProvider>
            <ThemeProvider theme={theme}>
              <CssBaseline enableColorScheme/>
              <AdaptiveBackground />
              <AppBar />
              {children}
            </ThemeProvider>
          </AppRouterCacheProvider>
        </React.Suspense>
      </body>
    </html>
  );
}
