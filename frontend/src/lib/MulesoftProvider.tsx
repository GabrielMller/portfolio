'use client';

import { createContext, useContext } from "react";
import React from "react";

type states = "on" | "off"

type MulesoftContextType = {
  dbState: states;
  kafkaState: states;
  setDbState: (state: states) => void;
  setKafkaState: (state: states) => void;
}

export const MulesoftContext = createContext<MulesoftContextType>({
  dbState: "on",
  kafkaState: "on",
  setDbState: () => {},
  setKafkaState: () => {},
});

export function MulesoftProvider({ children }: { children: React.ReactNode}) {
  const [dbState, setDbState] = React.useState<states>("on");
  const [kafkaState, setKafkaState] = React.useState<states>("on");

  return (
    <MulesoftContext.Provider value={{ dbState, kafkaState, setDbState, setKafkaState }}>
      {children}
    </MulesoftContext.Provider>
  );
}

export const useMulesoft = () => useContext(MulesoftContext);