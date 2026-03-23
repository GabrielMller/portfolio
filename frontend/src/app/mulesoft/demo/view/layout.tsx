import { MulesoftTokenProvider } from "@/lib/MulesoftTokenProvider";
import { cookies } from "next/headers";

export default async function Layout({ children }: { children: React.ReactNode }) {
  const token = (await cookies()).get('authjs.session-token')?.value || null;
  return (
    <MulesoftTokenProvider token={token}>
        {children}
    </MulesoftTokenProvider>
  )
}