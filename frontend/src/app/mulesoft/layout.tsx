import MulesoftAppBar from "@/components/MulesoftAppBar";
import { MulesoftCartProvider } from "@/lib/MulesoftCartProvider";
import { MulesoftProvider } from "@/lib/MulesoftProvider";

export default async function MulesoftLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <MulesoftProvider>
      <MulesoftCartProvider>
        <MulesoftAppBar />
        {children}
      </MulesoftCartProvider>
    </MulesoftProvider>
  );
}