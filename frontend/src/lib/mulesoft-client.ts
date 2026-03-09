if (!process.env.NEXT_PUBLIC_MULE_API_URL) {
  throw new Error("MuleSoft API URL must be defined in environment variables.");
}
abstract class MuleBaseClient {
  protected readonly apiURL: string;
  protected readonly clientId: string;
  protected readonly clientSecret: string;
  
  protected abstract basePath: string;

  // Construtor protegido para que apenas as subclasses possam invocar o super()
  protected constructor() {
    this.apiURL = process.env.NEXT_PUBLIC_MULE_API_URL!;
    this.clientId = process.env.CF_CLIENT_ID!;
    this.clientSecret = process.env.CF_CLIENT_SECRET!;
  }

  protected async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const url = `${this.apiURL}/api/${this.basePath}${endpoint}`;;
    
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
}

class UsersClient extends MuleBaseClient {
  private static instance: UsersClient;
  protected basePath = "users/v1/public";

  private constructor() {
    super();
  }

  public static getInstance(): UsersClient {
    if (!UsersClient.instance) {
      UsersClient.instance = new UsersClient();
    }
    return UsersClient.instance;
  }

  public async login(credentials: any): Promise<UserData> {
    return this.request<UserData>('/accounts/auth', {
      method: 'POST',
      body: JSON.stringify(credentials)
    });
  }

  public async register(userData: any): Promise<UserData> {
    return this.request<UserData>('/accounts', {
      method: 'POST',
      body: JSON.stringify(userData)
    });
  }

  public async sync(email: string, username: string): Promise<UserData> {
    return this.request<UserData>('/sync', {
      method: 'PATCH',
      body: JSON.stringify({ email, username })
    });
  }
}

class MonitoringClient extends MuleBaseClient {
  private static instance: MonitoringClient;
  protected basePath = "monitoring/v1";

  private constructor() {
    super();
  }

  public static getInstance(): MonitoringClient {
    if (!MonitoringClient.instance) {
      MonitoringClient.instance = new MonitoringClient();
    }
    return MonitoringClient.instance;
  }

  public async getMonitoring(service: string): Promise<MonitoringData> {
    return this.request<MonitoringData>(`/${service}`);
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

export const usersApi = UsersClient.getInstance();
export const monitoringApi = MonitoringClient.getInstance();