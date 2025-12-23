export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    username: string;
    name: string;
    password: string;
    email: string;
}

export interface AuthResponse {
    token: string;
    userId: number;
    role: string;
}
