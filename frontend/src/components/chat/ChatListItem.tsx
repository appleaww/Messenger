import React from 'react';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Trash2Icon } from 'lucide-react';
import { cn } from '@/lib/utils';
import type { ChatListItem as ChatListItemType } from '../../services/chatService';
import './ChatListItem.css';

interface ChatListItemProps {
    chat: ChatListItemType;
    isOnline?: boolean;
    onClick: (chatId: number) => void;
    onDelete?: (chatId: number) => void;
}

export const ChatListItem: React.FC<ChatListItemProps> = ({
                                                              chat,
                                                              isOnline = false,
                                                              onClick,
                                                              onDelete
                                                          }) => {
    const formatTime = (dateString: string) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return '';

        const now = new Date();
        const diffDays = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24));

        if (diffDays === 0) {
            return date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
        } else if (diffDays === 1) {
            return 'Вчера';
        } else if (diffDays < 7) {
            return date.toLocaleDateString('ru-RU', { weekday: 'short' });
        }
        return date.toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit' });
    };

    const getInitials = (name: string) => {
        return name
            .split(' ')
            .map(n => n[0])
            .join('')
            .toUpperCase()
            .slice(0, 2);
    };

    const handleDelete = (e: React.MouseEvent) => {
        e.stopPropagation();
        onDelete?.(chat.chatId);
    };

    const timeDisplay = chat.lastMessage?.trim() ? formatTime(chat.lastMessageSendingTime) : '';

    return (
        <div
            className="chat-list-item group"
            onClick={() => onClick(chat.chatId)}
        >
            <div className="relative">
                <Avatar className="!h-12 !w-12">
                    <AvatarFallback className="!bg-gradient-to-br !from-violet-600 !to-purple-400 !text-white">
                        {getInitials(chat.companionName)}
                    </AvatarFallback>
                </Avatar>
                <span
                    className={cn(
                        '!absolute !-bottom-0.5 !-right-0.5 !h-3 !w-3 !rounded-full !border-2 !border-white',
                        isOnline ? '!bg-green-500' : '!bg-gray-400'
                    )}
                />
            </div>

            <div className="!flex-1 min-w-0 !ml-4">
                <div className="!flex items-center !justify-between">
                    <span className="!font-medium !truncate !text-white">{chat.companionName}</span>
                    {timeDisplay && (
                        <span className="message-time !text-white !font-bold">{timeDisplay}</span>
                    )}
                </div>
                <div className="!flex !items-center !justify-between">
                    <p className="!text-sm !text-white/80 !truncate">
                        {chat.lastMessage || 'Нет сообщений'}
                    </p>
                    {chat.unreadMessagesCount > 0 && (
                        <Badge className="!ml-2 !h-5 !min-w-5 !flex !items-center !justify-center !bg-white !text-violet-600">
                            {chat.unreadMessagesCount}
                        </Badge>
                    )}
                </div>
            </div>

            {onDelete && (
                <Button
                    variant="ghost"
                    size="icon"
                    className="!h-8 !w-8 !opacity-0 group-hover:!opacity-100 !transition-opacity !text-white hover:!bg-white/20"
                    onClick={handleDelete}
                >
                    <Trash2Icon className="!h-4 !w-4" />
                </Button>
            )}
        </div>
    );

};
