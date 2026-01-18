import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { ArrowLeft, Send, Paperclip, Smile } from 'lucide-react';
import { MessageBubble } from '@/components/chat/MessageBubble';
import { ChatSidebar } from '@/components/chat/ChatSidebar';
import { chatService, ChatDetail, ChatListItem, statusService } from '@/services/chatService';
import { websocketService, WebSocketMessage, TypingEvent, OnlineStatus, ReadReceipt } from '@/services/websocketService';
import { authService } from '@/services/authService';
import { cn } from '@/lib/utils';
import './ChatPage.css';

interface ChatPageProps {
    chatId: number;
    onBack: () => void;
    onChatSelect: (chatId: number) => void;
}

interface DisplayMessage {
    id: number;
    content: string;
    sendingTime: string;
    senderId: number;
    senderName?: string;
    isRead: boolean;
    isMine: boolean;
}

export const ChatPage: React.FC<ChatPageProps> = ({ chatId, onBack, onChatSelect }) => {
    const [chatDetail, setChatDetail] = useState<ChatDetail | null>(null);
    const [messages, setMessages] = useState<DisplayMessage[]>([]);
    const [allChats, setAllChats] = useState<ChatListItem[]>([]);
    const [newMessage, setNewMessage] = useState('');
    const [isTyping, setIsTyping] = useState(false);
    const [companionTyping, setCompanionTyping] = useState(false);
    const [loading, setLoading] = useState(true);
    const [isOnline, setIsOnline] = useState(false);
    const [companionId, setCompanionId] = useState<number | null>(null);

    const messagesEndRef = useRef<HTMLDivElement>(null);
    const typingTimeoutRef = useRef<NodeJS.Timeout | undefined>(undefined);
    const currentUserId = authService.getUserId();

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    const loadChatData = useCallback(async () => {
        try {
            setLoading(true);
            const [detail, chats] = await Promise.all([
                chatService.openChat(chatId),
                chatService.getAllChats()
            ]);

            setChatDetail(detail);
            setMessages(detail.messages.map(msg => ({
                id: msg.id,
                content: msg.content,
                sendingTime: msg.sendingTime,
                senderId: msg.senderId,
                isRead: msg.isRead,
                isMine: msg.senderId === currentUserId
            })));
            setAllChats(chats);

            const currentChat = chats.find(c => c.chatId === chatId);
            if (currentChat) {
                setCompanionId(currentChat.companionId);
            }
        } catch (error) {
            console.error('Error loading chat:', error);
        } finally {
            setLoading(false);
        }
    }, [chatId, currentUserId]);

    useEffect(() => {
        if (!companionId) return;

        const loadOnlineStatus = async () => {
            try {
                const onlineUsers = await statusService.getOnlineUsers();
                setIsOnline(onlineUsers.includes(companionId));
            } catch (error) {
                console.error('Error loading online status:', error);
            }
        };

        loadOnlineStatus();
    }, [companionId]);

    useEffect(() => {
        if (!companionId) return;

        const unsubOnlineStatus = websocketService.onOnlineStatus((status: OnlineStatus) => {
            if (status.userId === companionId) {
                setIsOnline(status.isOnline);
            }
        });

        return () => {
            unsubOnlineStatus();
        };
    }, [companionId]);

    useEffect(() => {
        loadChatData();
    }, [loadChatData]);


    useEffect(() => {
        setAllChats(prevChats =>
            prevChats.map(chat =>
                chat.chatId === chatId
                    ? { ...chat, unreadMessagesCount: 0 }
                    : chat
            )
        );
    }, [chatId]);

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (!token) return;

        if (!websocketService.isConnected()) {
            websocketService.connect(token);
        }

        const unsubMessage = websocketService.onMessage((message: WebSocketMessage) => {
            if (message.chatId === chatId) {
                setMessages(prev => [...prev, {
                    id: message.id ?? Date.now(),
                    content: message.content,
                    sendingTime: message.sendingTime,
                    senderId: message.senderId,
                    isRead: message.isRead,
                    isMine: message.senderId === currentUserId
                }]);
                scrollToBottom();

                if (message.senderId !== currentUserId && message.id) {
                    websocketService.sendReadReceipt(chatId, [message.id]);
                }
            } else {
                setAllChats(prev => prev.map(chat => {
                    if (chat.chatId === message.chatId && message.senderId !== currentUserId) {
                        return {
                            ...chat,
                            lastMessage: message.content,
                            lastMessageSendingTime: message.sendingTime,
                            unreadMessagesCount: chat.unreadMessagesCount + 1
                        };
                    }
                    return chat;
                }));
            }
        });

        const unsubTyping = websocketService.onTyping((event: TypingEvent) => {
            if (event.chatId === chatId && event.userId !== currentUserId) {
                setCompanionTyping(event.isTyping);
            }
        });

        const unsubReadReceipt = websocketService.onReadReceipt((receipt: ReadReceipt) => {
            if (receipt.chatId === chatId) {
                setMessages(prev => prev.map(msg =>
                    receipt.messageIds.includes(msg.id) ? { ...msg, isRead: true } : msg
                ));
            }
        });

        return () => {
            unsubMessage();
            unsubTyping();
            unsubReadReceipt();
        };
    }, [chatId, currentUserId]);

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const handleSendMessage = () => {
        if (!newMessage.trim()) return;

        websocketService.sendMessage(newMessage.trim(), chatId);
        setNewMessage('');
        setIsTyping(false);
        websocketService.sendTyping(chatId, false);
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setNewMessage(e.target.value);

        if (!isTyping) {
            setIsTyping(true);
            websocketService.sendTyping(chatId, true);
        }

        if (typingTimeoutRef.current) {
            clearTimeout(typingTimeoutRef.current);
        }

        typingTimeoutRef.current = setTimeout(() => {
            setIsTyping(false);
            websocketService.sendTyping(chatId, false);
        }, 2000);
    };

    const handleKeyPress = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            handleSendMessage();
        }
    };

    const getInitials = (name: string) => {
        return name
            .split(' ')
            .map(n => n[0])
            .join('')
            .toUpperCase()
            .slice(0, 2);
    };

    const getStatusText = () => {
        if (companionTyping) return 'печатает...';
        if (isOnline) return 'в сети';
        return 'не в сети';
    };

    if (loading) {
        return (
            <div className="chat-page-loading">
                <div className="loading-spinner" />
                <p>Загрузка чата...</p>
            </div>
        );
    }

    if (!chatDetail) {
        return (
            <div className="chat-page-error">
                <p>Не удалось загрузить чат</p>
                <Button onClick={onBack}>Назад</Button>
            </div>
        );
    }

    return (
        <div className="chat-page">
            <ChatSidebar
                chats={allChats}
                activeChatId={chatId}
                onChatSelect={onChatSelect}
            />
            <div className="chat-main-area">
                <header className="chat-header">
                    <Button
                        variant="ghost"
                        size="icon"
                        onClick={onBack}
                        className="!mr-2"
                    >
                        <ArrowLeft className="!h-5 !w-5" />
                    </Button>

                    <div className="!relative">
                        <Avatar className="!h-10 !w-10 !mr-3">
                            <AvatarFallback className="!bg-gradient-to-br !from-violet-800 !to-purple-300 !text-white">
                                {getInitials(chatDetail.companionName)}
                            </AvatarFallback>
                        </Avatar>
                        <span
                            className={cn(
                                '!absolute !bottom-0 !right-2 !h-3 !w-3 !rounded-full !border-2 !border-white',
                                isOnline ? '!bg-green-500' : '!bg-gray-400'
                            )}
                        />
                    </div>

                    <div className="flex-1">
                        <h2 className="!font-semibold !text-white !text-lg">{chatDetail.companionName}</h2>
                        <p className="!text-xs !text-gray-300">@{chatDetail.companionUsername}</p>
                        <p className={cn(
                            '!text-sm !font-medium',
                            companionTyping ? '!text-blue-400' : (isOnline ? '!text-green-400' : '!text-gray-400')
                        )}>
                            {getStatusText()}
                        </p>
                    </div>
                </header>

                <div className="chat-messages">
                    {messages.length === 0 ? (
                        <div className="chat-empty-messages">
                            <p className="!text-gray-500">Начните диалог!</p>
                        </div>
                    ) : (
                        messages.map(message => (
                            <MessageBubble
                                key={message.id}
                                content={message.content}
                                sendingTime={message.sendingTime}
                                isMine={message.isMine}
                                isRead={message.isRead}
                                senderName={!message.isMine ? chatDetail.companionName : undefined}
                            />
                        ))
                    )}
                    <div ref={messagesEndRef} />
                </div>

                <div className="chat-input-area">
                    <Button variant="ghost" size="icon" className="!text-green-500 !hover:text-violet-500">
                        <Paperclip className="!h-5 !w-5" />
                    </Button>
                    <Button variant="ghost" size="icon" className="!text-green-500 !hover:text-violet-500">
                        <Smile className="!h-5 !w-5" />
                    </Button>

                    <Input
                        value={newMessage}
                        onChange={handleInputChange}
                        onKeyPress={handleKeyPress}
                        placeholder="Написать сообщение..."
                        className="!flex-1 !border-0 !bg-gray-100 !focus-visible:ring-1 !focus-visible:ring-violet-500 !pl-5  !text-base !font-sans"
                    />

                    <Button
                        onClick={handleSendMessage}
                        disabled={!newMessage.trim()}
                        className="!bg-gradient-to-r !from-violet-500 !to-purple-600 !hover:from-violet-600 hover:to-purple-700 !text-white !rounded-full !h-10 !w-10 !p-0"
                    >
                        <Send className="!h-5 !w-5" />
                    </Button>
                </div>
            </div>
        </div>
    );
};
