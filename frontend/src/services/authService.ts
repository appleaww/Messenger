import type { LoginRequest, RegisterRequest, AuthResponse } from '../types/auth';

const API_URL = 'http://localhost:8080';

export const authService = {
    async login(data: LoginRequest): Promise<AuthResponse> {
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error);
        }

        return response.json();
    },

    async register(data: RegisterRequest): Promise<string> {
        const response = await fetch(`${API_URL}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error);
        }

        return response.text();
    },

    saveToken(token: string): void {
        localStorage.setItem('token', token);
    },

    getToken(): string | null {
        return localStorage.getItem('token');
    },

    saveUserId(userId: number): void {
        localStorage.setItem('userId', userId.toString());
    },

    getUserId(): number | null {
        const id = localStorage.getItem('userId');
        return id ? parseInt(id) : null;
    },

    logout(): void {
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
    },

    isAuthenticated(): boolean {
        return !!this.getToken();
    },
};
