import React from 'react';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { cn } from '@/lib/utils';
import type { ChatListItem } from '@/services/chatService';

interface ChatSidebarProps {
    chats: ChatListItem[];
    activeChatId: number;
    onChatSelect: (chatId: number) => void;
}

export const ChatSidebar: React.FC<ChatSidebarProps> = ({
                                                            chats,
                                                            activeChatId,
                                                            onChatSelect
                                                        }) => {
    const getInitials = (name: string) => {
        return name
            .split(' ')
            .map(n => n[0])
            .join('')
            .toUpperCase()
            .slice(0, 2);
    };

    return (
        <div className="!w-16 !flex !flex-col !items-center !py-4 !gap-2 !overflow-y-auto">
            {chats.map(chat => (
                <button
                    key={chat.chatId}
                    onClick={() => onChatSelect(chat.chatId)}
                    className={cn(
                        '!relative !p-1 !rounded-full !transition-all !hover:scale-110',
                        activeChatId === chat.chatId && '!ring-2 !ring-teal-500 !ring-offset-2'
                    )}
                    title={chat.companionName}
                >
                    <Avatar className="!h-10! w-10">
                        <AvatarFallback className="!bg-gradient-to-br !from-violet-800 !to-purple-300 !text-white !text-xs">
                            {getInitials(chat.companionName)}
                        </AvatarFallback>
                    </Avatar>
                    {chat.unreadMessagesCount > 0 && (
                        <span className="!absolute !-top-1 !-right-1 !h-5 !w-5 !bg-red-500 !text-white !text-xs !rounded-full !flex !items-center !justify-center">
                            {chat.unreadMessagesCount > 9 ? '9+' : chat.unreadMessagesCount}
                        </span>
                    )}
                </button>
            ))}
        </div>
    );
};
