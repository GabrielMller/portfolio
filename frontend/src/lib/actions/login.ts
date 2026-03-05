"use server";
import { signIn } from "../auth";
import { z } from "zod";

const loginSchema = z.object({
  email: z.email(),
  password: z.string().min(1, "A senha é obrigatória"),
});

export async function login(initialState: ActionState, formData: FormData) : Promise<ActionState> {
  const input = Object.fromEntries(formData.entries());
  const validatedFields = loginSchema.safeParse(input);

  if (!validatedFields.success) {
    return {
      ...initialState,
      error: null,
      errors: z.treeifyError(validatedFields.error).properties,
      data: input,
    };
  }

  try {
    await signIn("credentials", formData);
  } catch (error: any) {
    console.error("Login error:", error);
    return {
      error: "Email ou senha inválidos",
      data: input,
      errors: {},
      success: false,
      message: null,
    };
  }
  return initialState;
}