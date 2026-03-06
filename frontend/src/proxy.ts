import { auth, signOut } from "./lib/auth";
import { NextResponse, type NextRequest } from 'next/server'

export async function proxy(request: NextRequest) {
  if (request.nextUrl.pathname.includes("/mulesoft/demo")) {
    const session = await auth();
    if (session && session.mode === "mulesoft" && request.nextUrl.pathname === "/mulesoft/demo") {
      const url = request.nextUrl.clone()
      url.pathname = '/mulesoft/demo/teste';
      return NextResponse.redirect(url)
    }
    if (!session && request.nextUrl.pathname !== "/mulesoft/demo") {
      const url = request.nextUrl.clone()
      url.pathname = '/mulesoft/demo';
      return NextResponse.redirect(url)
    }
    if (session && session.mode !== "mulesoft") {
      try {
        await signOut();
      } catch (error) {
        const url = request.nextUrl.clone()
        url.pathname = '/mulesoft/demo';
        return NextResponse.redirect(url)
      }
    }
  }
}

export const config = {
  matcher: ["/((?!api|_next/static|_next/image|favicon.ico).*)"],
}