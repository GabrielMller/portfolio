import { jwtVerify, SignJWT } from "jose";
import NextAuth, { NextAuthConfig } from "next-auth"
import Credentials from "next-auth/providers/credentials"
import { mulesoftAPI } from "./mulesoft-client";
import Google from "next-auth/providers/google";
declare module "next-auth" {
  interface Session {
    mode: "mulesoft";
  }
}
export const config : NextAuthConfig = {
  basePath: "/api/auth/mulesoft",
  session: {
    strategy: "jwt",
  },
  secret: process.env.AUTH_SECRET,
  providers: [
    Google({
      clientId: process.env.GOOGLE_CLIENT_ID || "",
      clientSecret: process.env.GOOGLE_CLIENT_SECRET || "",
    }),
    Credentials({
      name: "Credentials",
      type: "credentials",
      credentials: {
        password: { label: "Password", type: "password" },
        email: { label: "Email", type: "email", placeholder: "john.doe@example.com" }
      },
      async authorize(credentials) {
        try {
          const userData = await mulesoftAPI.login({
            email: credentials?.email,
            password: credentials?.password
          });
          return {
            id: userData.user_id.toString(),
            username: userData.username,
            email: userData.email,
          };
        } catch (error) {
          throw error;
        }
      }
    })
  ],
  pages: {
    signIn: "/demo"
  },
  callbacks: {
    signIn: async ({ user }) => {
      console.log("User signed in:", user);
      return true;
    },
    session: async ({ session, token }) => {
      session.mode = "mulesoft";
      return session;
    },
    authorized: async ({ auth  }) => {
      console.log("Checking authorization with auth callback:", auth);
      return !!auth;
    },
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
