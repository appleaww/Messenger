const API_BASE = 'http://localhost:8080/api';

export interface ChatListItem {
    chatId: number;
    companionId: number;
    companionName: string;
    lastMessage: string;
    lastMessageSendingTime: string;
    unreadMessagesCount: number;
}

export interface ChatDetail {
    chatId: number;
    companionName: string;
    companionUsername: string;
    messages: MessageDTO[];
}

export interface MessageDTO {
    id: number;
    content: string;
    sendingTime: string;
    senderId: number;
    senderUsername: string;
    senderName: string;
    isRead: boolean;
    isMine: boolean;
}

export interface ChatCreateRequest {
    companionUsername: string;
}

export interface ChatCreateResponse {
    id: number;
    lastMessage: string;
    companionName: string;
    participantDTOList: { id: number; name: string; username: string }[];
}

const getAuthHeaders = () => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
};

export const chatService = {
    async getAllChats(): Promise<ChatListItem[]> {
        const response = await fetch(`${API_BASE}/chats`, {
            headers: getAuthHeaders()
        });
        if (!response.ok) throw new Error('Ошибка загрузки чатов');
        return response.json();
    },

    async createChat(request: ChatCreateRequest): Promise<ChatCreateResponse> {
        const response = await fetch(`${API_BASE}/chats`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(request)
        });
        if (!response.ok) throw new Error('Ошибка создания чата');
        return response.json();
    },

    async deleteChat(chatId: number): Promise<ChatListItem[]> {
        const response = await fetch(`${API_BASE}/chats/${chatId}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        if (!response.ok) throw new Error('Ошибка удаления чата');
        return response.json();
    },

    async openChat(chatId: number): Promise<ChatDetail> {
        const response = await fetch(`${API_BASE}/chats/${chatId}`, {
            headers: getAuthHeaders()
        });
        if (!response.ok) throw new Error('Ошибка открытия чата');
        return response.json();
    }
};

export const statusService = {
    async getOnlineUsers(): Promise<number[]> {
        const response = await fetch(`${API_BASE}/status/online`, {
            headers: getAuthHeaders()
        });
        if (!response.ok) throw new Error('Ошибка загрузки статусов');
        return response.json();
    }
};
