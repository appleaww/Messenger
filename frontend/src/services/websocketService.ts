import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_URL = 'http://localhost:8080/websocket';

export interface WebSocketMessage {
    id: number;
    sendingTime: string;
    content: string;
    isRead: boolean;
    senderId: number;
    recipientId: number;
    chatId: number;
}

export interface TypingEvent {
    chatId: number;
    userId: number;
    username: string;
    recipientId: number;
    isTyping: boolean;
}

export interface ReadReceipt {
    chatId: number;
    messageIds: number[];
    readerId: number;
    recipientId: number;
}

export interface OnlineStatus {
    userId: number;
    isOnline: boolean;
    lastSeen?: string;
}

class WebSocketService {
    private client: Client | null = null;
    private messageHandlers: ((message: WebSocketMessage) => void)[] = [];
    private typingHandlers: ((event: TypingEvent) => void)[] = [];
    private readReceiptHandlers: ((receipt: ReadReceipt) => void)[] = [];
    private onlineStatusHandlers: ((status: OnlineStatus) => void)[] = [];

    connect(token: string): Promise<void> {
        return new Promise((resolve, reject) => {
            this.client = new Client({
                webSocketFactory: () => new SockJS(WS_URL),
                connectHeaders: {
                    Authorization: `Bearer ${token}`
                },
                onConnect: () => {
                    console.log('WebSocket connected');
                    this.subscribeToMessages();
                    resolve();
                },
                onStompError: (frame) => {
                    console.error('STOMP error:', frame);
                    reject(new Error('STOMP connection error'));
                },
                reconnectDelay: 5000,
            });
            this.client.activate();
        });
    }

    private subscribeToMessages() {
        if (!this.client) return;

        this.client.subscribe('/user/queue/chat-messages', (message: IMessage) => {
            const msg: WebSocketMessage = JSON.parse(message.body);
            this.messageHandlers.forEach(handler => handler(msg));
        });

        this.client.subscribe('/user/queue/typing-events', (message: IMessage) => {
            const event: TypingEvent = JSON.parse(message.body);
            this.typingHandlers.forEach(handler => handler(event));
        });

        this.client.subscribe('/user/queue/read-receipts', (message: IMessage) => {
            const receipt: ReadReceipt = JSON.parse(message.body);
            this.readReceiptHandlers.forEach(handler => handler(receipt));
        });

        this.client.subscribe('/topic/online-status', (message: IMessage) => {
            const status: OnlineStatus = JSON.parse(message.body);
            this.onlineStatusHandlers.forEach(handler => handler(status));
        });
    }
    onOnlineStatus(handler: (status: { userId: number; isOnline: boolean }) => void) {
        this.onlineStatusHandlers.push(handler);
        return () => {
            this.onlineStatusHandlers = this.onlineStatusHandlers.filter(h => h !== handler);
        };
    }

    sendMessage(content: string, chatId: number) {
        if (!this.client?.connected) return;

        this.client.publish({
            destination: '/app/chat.sendMessage',
            body: JSON.stringify({ content, chatId })
        });
    }

    sendTyping(chatId: number, isTyping: boolean) {
        if (!this.client?.connected) return;

        this.client.publish({
            destination: '/app/chat.typing',
            body: JSON.stringify({ chatId, isTyping })
        });
    }

    sendReadReceipt(chatId: number, messageIds: number[]) {
        if (!this.client?.connected) return;

        this.client.publish({
            destination: '/app/chat.readMessages',
            body: JSON.stringify({ chatId, messageIds })
        });
    }

    onMessage(handler: (message: WebSocketMessage) => void) {
        this.messageHandlers.push(handler);
        return () => {
            this.messageHandlers = this.messageHandlers.filter(h => h !== handler);
        };
    }

    onTyping(handler: (event: TypingEvent) => void) {
        this.typingHandlers.push(handler);
        return () => {
            this.typingHandlers = this.typingHandlers.filter(h => h !== handler);
        };
    }

    onReadReceipt(handler: (receipt: ReadReceipt) => void) {
        this.readReceiptHandlers.push(handler);
        return () => {
            this.readReceiptHandlers = this.readReceiptHandlers.filter(h => h !== handler);
        };
    }

    disconnect() {
        if (this.client) {
            this.client.deactivate();
            this.client = null;
        }
    }

    isConnected(): boolean {
        return this.client?.connected ?? false;
    }
}

export const websocketService = new WebSocketService();
