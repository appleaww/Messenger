import { useState, useEffect } from 'react';
import { Navbar } from '../components/ui/Navbar';
import { ChatListItem } from '../components/chat/ChatListItem';
import {chatService, statusService} from '../services/chatService';
import type { ChatListItem as ChatListItemType } from '../services/chatService';
import './ChatsPage.css';
import {authService} from "@/services/authService.ts";
import {websocketService, WebSocketMessage} from "@/services/websocketService.ts";
import { subscriptionService } from '../services/chatService';


interface ChatsPageProps {
    onLogout: () => void;
    onOpenChat: (chatId: number) => void;
}

function ChatsPage({ onLogout, onOpenChat }: ChatsPageProps) {
    const [chats, setChats] = useState<ChatListItemType[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showNewChatModal, setShowNewChatModal] = useState(false);
    const [newChatUsername, setNewChatUsername] = useState('');
    const [creating, setCreating] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');
    const [onlineUsers, setOnlineUsers] = useState<Set<number>>(new Set());
    const [showSubscriptionModal, setShowSubscriptionModal] = useState(false);
    const [subscriptionTier, setSubscriptionTier] = useState<'BASIC' | 'PREMIUM'>('BASIC');
    const [subscribing, setSubscribing] = useState(false);

    const currentUserId = authService.getUserId();

    useEffect(() => {
        loadChats();

        statusService.getOnlineUsers()
            .then(users => setOnlineUsers(new Set(users)))
            .catch(err => console.error('Failed to load online users:', err));

        const unsubOnline = websocketService.onOnlineStatus(({ userId, isOnline }) => {
            setOnlineUsers(prev => {
                const next = new Set(prev);
                isOnline ? next.add(userId) : next.delete(userId);
                return next;
            });
        });

        const unsubMessage = websocketService.onMessage((message: WebSocketMessage) => {
            setChats(prevChats => {
                const updatedChats = prevChats.map(chat => {
                    if (chat.chatId === message.chatId) {
                        const isFromOther = message.senderId !== currentUserId;
                        return {
                            ...chat,
                            lastMessage: message.content,
                            lastMessageSendingTime: message.sendingTime,
                            unreadMessagesCount: isFromOther
                                ? chat.unreadMessagesCount + 1
                                : chat.unreadMessagesCount
                        };
                    }
                    return chat;
                });

                return updatedChats.sort((a, b) =>
                    new Date(b.lastMessageSendingTime).getTime() -
                    new Date(a.lastMessageSendingTime).getTime()
                );
            });
        });

        return () => {
            unsubOnline();
            unsubMessage();
        };
    }, [currentUserId]);

    const loadChats = async () => {
        try {
            setLoading(true);
            const data = await chatService.getAllChats();
            setChats(data);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Ошибка загрузки чатов');
        } finally {
            setLoading(false);
        }
    };

    const handleCreateChat = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!newChatUsername.trim()) return;

        try {
            setCreating(true);
            await chatService.createChat({ companionUsername: newChatUsername });
            setNewChatUsername('');
            setShowNewChatModal(false);
            await loadChats();
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Ошибка создания чата');
        } finally {
            setCreating(false);
        }
    };

    const handleDeleteChat = async (chatId: number) => {
        if (!confirm('Удалить этот чат?')) return;
        try {
            await chatService.deleteChat(chatId);
            await loadChats();
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Ошибка удаления чата');
        }
    };

    const handleSearch = (query: string) => {
        setSearchQuery(query);
    };

    const filteredChats = chats.filter(chat =>
        chat.companionName.toLowerCase().includes(searchQuery.toLowerCase())
    );

    return (
        <div className="chats-page">
            <Navbar
                userName= {authService.getUserName()}
                userEmail= {authService.getUserEmail()}
                onLogout={onLogout}
                onSearch={handleSearch}
                notificationCount={3}
                messageIndicator={true}
            />

            <main className="chats-main">
                <div className="chats-container">
                    <div className="chats-header">
                        <h1>Все чаты</h1>
                        <div style={{ display: 'flex', gap: '10px' }}>
                            <button
                                className="new-chat-btn"
                                onClick={() => setShowNewChatModal(true)}
                            >
                                Создать новый чат
                            </button>
                            <button
                                className="new-chat-btn"
                                style={{
                                    background: 'linear-gradient(135deg, #10b981, #34d399)',
                                    boxShadow: '0 4px 15px rgba(16, 185, 129, 0.4)'
                                }}
                                onClick={() => setShowSubscriptionModal(true)}
                            >
                                Подписка
                            </button>
                        </div>
                    </div>

                    {error && <div className="chats-error">{error}</div>}

                    {loading ? (
                        <div className="chats-loading">
                            <div className="loading-spinner"></div>
                            <p>Загрузка чатов...</p>
                        </div>
                    ) : filteredChats.length === 0 ? (
                        <div className="chats-empty">
                            <span className="empty-icon"></span>
                            <h3>Нет чатов</h3>
                            <p>Создайте новый чат, чтобы начать общение</p>
                            <button
                                className="new-chat-btn"
                                onClick={() => setShowNewChatModal(true)}
                            >
                                Создать чат
                            </button>
                        </div>
                    ) : (
                        <div className="chats-list">
                            {filteredChats.map(chat => (
                                <ChatListItem
                                    key={chat.chatId}
                                    chat={chat}
                                    isOnline={onlineUsers.has(chat.companionId)}
                                    onClick={onOpenChat}
                                    onDelete={handleDeleteChat}
                                />
                            ))}
                        </div>
                    )}
                </div>
            </main>

            {showNewChatModal && (
                <div className="modal-overlay" onClick={() => setShowNewChatModal(false)}>
                    <div className="modal-content" onClick={e => e.stopPropagation()}>
                        <h2>Новый чат</h2>
                        <form onSubmit={handleCreateChat}>
                            <input
                                type="text"
                                placeholder="Введите username собеседника"
                                value={newChatUsername}
                                onChange={e => setNewChatUsername(e.target.value)}
                                autoFocus
                            />
                            <div className="modal-actions">
                                <button
                                    type="button"
                                    className="cancel-btn"
                                    onClick={() => setShowNewChatModal(false)}
                                >
                                    Отмена
                                </button>
                                <button
                                    type="submit"
                                    className="submit-btn"
                                    disabled={creating || !newChatUsername.trim()}
                                >
                                    {creating ? 'Создание...' : 'Создать'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
            {showSubscriptionModal && (
                <div className="modal-overlay" onClick={() => setShowSubscriptionModal(false)}>
                    <div className="modal-content" onClick={e => e.stopPropagation()}>
                        <h2>Управление подпиской</h2>

                        <div style={{
                            marginBottom: '1.5rem',
                            padding: '1rem',
                            background: '#f8fafc',
                            borderRadius: '12px',
                            textAlign: 'center'
                        }}>
                            <p style={{ margin: '0 0 0.5rem 0', fontWeight: 500, color: '#64748b' }}>
                                Ваш текущий статус подписки:
                            </p>
                            <p style={{
                                fontSize: '1.25rem',
                                fontWeight: 700,
                                color: subscriptionTier === 'PREMIUM' ? '#10b981' : '#64748b'
                            }}>
                                {subscriptionTier === 'PREMIUM' ? 'Premium' : 'Basic'}
                            </p>
                        </div>

                        {subscriptionTier === 'BASIC' && (
                            <button
                                onClick={async () => {
                                    setSubscribing(true);
                                    try {
                                        await subscriptionService.activateSubscription('PREMIUM');
                                        setSubscriptionTier('PREMIUM');
                                        alert('Подписка Premium успешно активирована!');
                                    } catch (err: any) {
                                        alert(err.message || 'Ошибка активации');
                                    } finally {
                                        setSubscribing(false);
                                    }
                                }}
                                disabled={subscribing}
                                className="submit-btn"
                                style={{ width: '100%', marginBottom: '1rem' }}
                            >
                                {subscribing ? 'Активация...' : 'Активировать Premium подписку'}
                            </button>
                        )}

                        <button
                            onClick={() => setShowSubscriptionModal(false)}
                            className="cancel-btn"
                            style={{ width: '100%' }}
                        >
                            Закрыть
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}

export default ChatsPage;
