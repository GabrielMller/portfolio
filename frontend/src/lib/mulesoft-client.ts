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
        throw new Error(errorData.details || `MuleSoft Error: ${response.status}`);
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

  public async suggestion(title: string, description: string): Promise<{ message: string }> {
    return this.request<{ message: string }>('/suggestion', {
      method: 'POST',
      body: JSON.stringify({ title, description })
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


class ItemsClient extends MuleBaseClient {
  private static instance: ItemsClient;
  protected basePath = "items/v1";

  private constructor() {
    super();
  }

  public static getInstance(): ItemsClient {
    if (!ItemsClient.instance) {
      ItemsClient.instance = new ItemsClient();
    }
    return ItemsClient.instance;
  }

  public async getItems(token: string, page: number = 1, pageSize: number = 15): Promise<PageableResponse<ItemData>> {
    return this.request<PageableResponse<ItemData>>(`/?page=${page}&pageSize=${pageSize}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      },
    });
  }
}


export type ItemData = {
  id: string;
  name: string;
  description: string;
  sku: string;
  image: string;
  price: number;
  stock: number;
};

type PageableResponse<T> = {
  data: T[];
  metadata: {
    totalItems: number;
    page: number;
    pageSize: number;
  };
};

class OrdersClient extends MuleBaseClient {
  private static instance: OrdersClient
  protected basePath = "orders/v1";

  private constructor() {
    super();
  }

  public static getInstance(): OrdersClient {
    if (!OrdersClient.instance) {
      OrdersClient.instance = new OrdersClient();
    }
    return OrdersClient.instance;
  }

  public async createOrder(token: string, orderData: OrderData, query: ServicesQuery): Promise<OrderResponse> {
    return this.request<OrderResponse>(`/?dbActive=${query.dbActive}&kafkaActive=${query.kafkaActive}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(orderData)
    });
  }

  public async getOrders(token: string, page: number = 1, pageSize: number = 15): Promise<PageableResponse<OrderSummary>> {
    return this.request<PageableResponse<OrderSummary>>(`/?page=${page}&pageSize=${pageSize}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      },
    });
  }

  public async payment(token: string, orderId: string, amount: number, status: string, method: string): Promise<OrderResponse> {
    return this.request<OrderResponse>(`/${orderId}/payment`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ amount, status, method })
    });
  }

  public async delivery(token: string, orderId: string, status: string): Promise<OrderResponse> {
    return this.request<OrderResponse>(`/${orderId}/delivery`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ status })
    });
  }
}

export type OrderSummary = {
  id: string;
  created_at: string;
  status: string;
  total: number;
  order_number: number;
  items: OrderItem[];
};

export type OrderItem = {
  id: string;
  name: string;
  image: string;
  description: string;
  quantity: number;
  price: number;
};

export type OrderData = {
  paymentMethod: string;
  items: {
    id: string;
    quantity: number;
    price: number;
  }[];
};

type OrderResponse = {
  description: string;
};

type ServicesQuery = {
  kafkaActive?: boolean;
  dbActive?: boolean;
}

export const usersApi = UsersClient.getInstance();
export const monitoringApi = MonitoringClient.getInstance();
export const itemsApi = ItemsClient.getInstance();
export const ordersApi = OrdersClient.getInstance();