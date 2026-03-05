declare interface ActionState {
  success?: boolean;
  message?: string | null;
  error?: string | null;
  errors?: {
    [key: string]: {
      errors: string[];
    } | undefined;
  };
  data?: any;
}