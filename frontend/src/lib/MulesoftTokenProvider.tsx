'use client';

import { createContext, useContext } from "react";
import React from "react";

type MulesoftTokenContextType = {
  token: string | null;
  setToken?: (token: string | null) => void;  
}

export const MulesoftContext = createContext<MulesoftTokenContextType>({
  token: null,
});

export function MulesoftTokenProvider({ children, token }: { children: React.ReactNode; token: string | null }) {
  const [tokenState, setToken] = React.useState<string | null>(token);

  return (
    <MulesoftContext.Provider value={{ token: tokenState, setToken }}>
      {children}
    </MulesoftContext.Provider>
  );
}

export const useMulesoftToken = () => useContext(MulesoftContext);