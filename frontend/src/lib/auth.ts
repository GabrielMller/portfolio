import { jwtVerify, SignJWT } from "jose";
import NextAuth, { NextAuthConfig } from "next-auth"
import { encode } from "next-auth/jwt";
import Credentials from "next-auth/providers/credentials"
import zod from "zod";

export const config : NextAuthConfig = {
  basePath: "/api/auth/mulesoft",
  session: {
    strategy: "jwt",
  },
  secret: process.env.AUTH_SECRET,
  providers: [
    Credentials({
      name: "Credentials",
      credentials: {
        username: { label: "Username", type: "text", placeholder: "jsmith" },
        password: { label: "Password", type: "password" },
        email: { label: "Email", type: "email", placeholder: "john.doe@example.com" }
      },
      async authorize(credentials) {
        try {
          const user = await fetch(`${process.env.MULESOFT_URL}/api/users/v1/accounts/auth`, {
            method: "POST",
            headers: {
              "Content-Type": "application/json"
            },
            body: JSON.stringify(credentials)
          });
          const userData = await user.json();
          if (!userData) {
            throw new Error("Invalid credentials");
          }
          return userData;
        } catch (error) {
          console.error("Authorization error:", error);
          throw new Error("Invalid credentials");
        }
      }
    })
  ],
  pages: {
    signIn: "/mulesoft/demo",
  },
  callbacks: {
    jwt: async ({ token, user }) => {
      if (user) {
        token.id = user.id;
        token.name = user.name;
        token.email = user.email;
      }
      return token;
    }
  },
  jwt: {
    async encode({ token, secret }) {
      // Criamos um JWT assinado com HS256
      return await new SignJWT(token as any)
        .setProtectedHeader({ alg: "HS256" })
        .setIssuedAt()
        .setExpirationTime("7d")
        .sign(new TextEncoder().encode(secret as string));
    },
    async decode({ token, secret }) {
      if (!token) return null;
      const { payload } = await jwtVerify(token, new TextEncoder().encode(secret as string));
      return payload as any;
    },
  }
}
export const { handlers, signIn, signOut, auth } = NextAuth(config)