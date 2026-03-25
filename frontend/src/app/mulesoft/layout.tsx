import { MulesoftProvider } from "@/lib/MulesoftProvider";

export default async function MulesoftLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <MulesoftProvider>
      {children}
    </MulesoftProvider>
  );
}