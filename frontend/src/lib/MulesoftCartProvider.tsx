'use client';

import { createContext, useContext } from "react";
import React from "react";

type states = "on" | "off"

type MulesoftCartContextType = {
  dbState: states;
  kafkaState: states;
  setDbState: (state: states) => void;
  setKafkaState: (state: states) => void;
}

export const MulesoftCartContext = createContext<MulesoftCartContextType>({
  dbState: "on",
  kafkaState: "on",
  setDbState: () => {},
  setKafkaState: () => {},
});

export function MulesoftProvider({ children }: { children: React.ReactNode}) {
  const [dbState, setDbState] = React.useState<states>("on");
  const [kafkaState, setKafkaState] = React.useState<states>("on");

  return (
    <MulesoftCartContext.Provider value={{ dbState, kafkaState, setDbState, setKafkaState }}>
      {children}
    </MulesoftCartContext.Provider>
  );
}

export const useCartMulesoft = () => useContext(MulesoftCartContext);