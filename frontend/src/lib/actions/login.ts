"use server";
import { redirect } from "next/navigation";
import { signIn } from "../auth";
import { z } from "zod";
import { mulesoftAPI } from "../mulesoft-client";

const loginSchema = z.object({
  email: z.email(),
  password: z.string().min(1, "A senha é obrigatória"),
});

const registerSchema = z.object({
  username: z.string().min(3, "O nome de usuário deve ter pelo menos 3 caracteres"),
  email: z.email(),
  password: z.string().min(8, "A senha deve ter pelo menos 8 caracteres").regex(/[A-Z]/, "A senha deve conter pelo menos uma letra maiúscula").regex(/[a-z]/, "A senha deve conter pelo menos uma letra minúscula").regex(/[0-9]/, "A senha deve conter pelo menos um número").regex(/[@$!%*?&]/, "A senha deve conter pelo menos um caractere especial"),
});

export async function login(initialState: ActionState, formData: FormData) : Promise<ActionState> {
  const input = Object.fromEntries(formData.entries());

  if (input.type === "register") {
    const validatedFields = registerSchema.safeParse(input);
    if (!validatedFields.success) {
      return {
        ...initialState,
        error: null,
        errors: z.treeifyError(validatedFields.error).properties,
        data: input,
      };
    }
    try {
      await mulesoftAPI.register({
        username: input.username,
        email: input.email,
        password: input.password,
      });
    } catch (error) {
      console.error("Registration error:", error);
      return {
        ...initialState,
        error: error instanceof Error ? error.message : "An unexpected error occurred during registration",
        errors: {},
        data: input,
      };
    }
  } else {
    const validatedFields = loginSchema.safeParse(input);

    if (!validatedFields.success) {
      return {
        ...initialState,
        error: null,
        errors: z.treeifyError(validatedFields.error).properties,
        data: input,
      };
    }
  }

  try {
    await signIn("credentials", formData);
  } catch (error: any) {
    if (error.message == "NEXT_REDIRECT" ) {
      throw error; // Let the redirect happen
    }
    console.error("Login error:", error.cause.err.message);
    return {
      error: error.cause.err.message || "An unexpected error occurred",
      data: input,
      errors: {},
      success: false,
      message: null,
    };
  }
  return initialState;
}