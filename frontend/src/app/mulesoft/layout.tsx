import MulesoftAppBar from "@/components/MulesoftAppBar";
import { MulesoftProvider } from "@/lib/MulesoftProvider";

export default async function MulesoftLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <MulesoftProvider>
      <MulesoftAppBar />
      {children}
    </MulesoftProvider>
  );
}