/**
 * MuleClient - SDK Interno para comunicação com o ecossistema MuleSoft.
 * Centraliza autenticação, gestão de headers e logs de integração.
 */

const MULE_API_URL = process.env.NEXT_PUBLIC_MULE_API_URL || "https://api.mule-gabriel-muller.online";

class MuleClient {
  private static instance: MuleClient;
  private clientId: string;
  private clientSecret: string;

  private constructor() {
    this.clientId = process.env.CF_CLIENT_ID || "";
    this.clientSecret = process.env.CF_CLIENT_SECRET || "";
  }

  public static getInstance(): MuleClient {
    if (!MuleClient.instance) {
      MuleClient.instance = new MuleClient();
    }
    return MuleClient.instance;
  }

  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const url = `${MULE_API_URL}/api${endpoint}`;
    
    const headers = {
      'Content-Type': 'application/json',
      'CF-Access-Client-Id': this.clientId,
      'CF-Access-Client-Secret': this.clientSecret,
      ...options.headers,
    };

    try {
      const response = await fetch(url, { ...options, headers });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        console.error(`[MuleClient Error] [${endpoint}]:`, errorData);
        throw new Error(errorData.message || `MuleSoft Error: ${response.status}`);
      }

      return await response.json();
    } catch (error: any) {
      console.error(`[MuleClient Error] [${endpoint}]:`, error.message);
      throw error;
    }
  }

  public async login(credentials: any): Promise<UserData> {
    return this.request<UserData>('/users/v1/public/accounts/auth', {
      method: 'POST',
      body: JSON.stringify(credentials)
    });
  }

  public async register(userData: any): Promise<UserData> {
    return this.request<UserData>('/users/v1/public/accounts', {
      method: 'POST',
      body: JSON.stringify(userData)
    });
  }

  // Chamadas de Negócio
  public async getMonitoring(service: string): Promise<MonitoringData> {
    return this.request<MonitoringData>(`/monitoring/v1/${service}`);
  }
}

type MonitoringData = {
  status: "OK" | "WARNING" | "ERROR";
  nodes: number;
  totalNodes: number;
};

type UserData = {
  id: string;
  username: string;
  email: string;
  user_id: number
};

export const mulesoftAPI = MuleClient.getInstance();