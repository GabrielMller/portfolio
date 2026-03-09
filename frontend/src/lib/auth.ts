import { jwtVerify, SignJWT } from "jose";
import NextAuth, { NextAuthConfig } from "next-auth"
import Credentials from "next-auth/providers/credentials"
import Google from "next-auth/providers/google";
import { usersApi } from "./mulesoft-client";
import GitHub from "next-auth/providers/github";
declare module "next-auth" {
  interface Session {
    mode: "mulesoft";
  }
  interface User {
    user_id: number;
  }
}
export const config : NextAuthConfig = {
  basePath: "/api/auth/mulesoft",
  session: {
    strategy: "jwt",
  },
  secret: process.env.AUTH_SECRET,
  providers: [
    GitHub,
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
          const userData = await usersApi.login({
            email: credentials?.email,
            password: credentials?.password
          });
          return {
            id: userData.user_id.toString(),
            username: userData.username,
            email: userData.email,
            user_id: userData.user_id,
          };
        } catch (error) {
          throw error;
        }
      }
    })
  ],
  pages: {
    signIn: "/mulesoft/demo"
  },
  callbacks: {
    session: async ({ session, token }) => {
      session.mode = "mulesoft";
      return session;
    },
    authorized: async ({ auth  }) => {
      return !!auth;
    },
    jwt: async ({ token, user, session, account }) => {
      if (user && !user?.user_id) {
        const userData = await usersApi.sync(user.email!, user.name!);
        user.user_id = Number(userData.id);  
      }

      if (!token?.user_id && user?.user_id) {
        token.user_id = user.user_id;
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
