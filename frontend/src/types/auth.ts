export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    username: string;
    name: string;
    password: string;
    email: string;
    role: string;
}

export interface AuthResponse {
    token: string;
    userId: number;
    role: string;
    name: string;
    email: string;
}
