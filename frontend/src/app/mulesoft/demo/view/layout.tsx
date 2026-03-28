import MulesoftAppBar from "@/components/MulesoftAppBar";
import MulesoftCart from "@/components/MulesoftCart";
import { MulesoftCartProvider } from "@/lib/MulesoftCartProvider";
import { MulesoftTokenProvider } from "@/lib/MulesoftTokenProvider";
import { cookies } from "next/headers";

export default async function Layout({ children }: { children: React.ReactNode }) {
  const cookiesStore = await cookies();
  const token = cookiesStore.get("authjs.session-token")?.value || cookiesStore.get("__Secure-authjs.session-token")?.value || null;
  return (
    <MulesoftTokenProvider token={token}> 
      <MulesoftCartProvider>
        <MulesoftAppBar>
          <MulesoftCart />
        </MulesoftAppBar>
        {children}
      </MulesoftCartProvider>
    </MulesoftTokenProvider>
  )
}