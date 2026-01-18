import React from 'react';
import { cn } from '@/lib/utils';
import { Check, CheckCheck } from 'lucide-react';

interface MessageBubbleProps {
    content: string;
    sendingTime: string;
    isMine: boolean;
    isRead: boolean;
    senderName?: string;
}

export const MessageBubble: React.FC<MessageBubbleProps> = ({
                                                                content,
                                                                sendingTime,
                                                                isMine,
                                                                isRead,
                                                            }) => {
    const formatTime = (dateString: string) => {
        const date = new Date(dateString);
        return date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
    };

    return (
        <div className={cn('!flex !mb-4', isMine ? 'justify-end' : 'justify-start')}>
            <div
                className={cn(
                    '!max-w-[50%] !px-3 !py-2 !rounded-2xl !shadow-sm !overflow-hidden',
                    isMine
                        ? '!bg-gradient-to-r !from-teal-500 !to-purple-600 text-white rounded-br-sm'
                        : '!bg-gradient-to-r !from-purple-500 !to-teal-600 text-white rounded-bl-sm'
                )}
            >
                <div className={cn(
                    '!flex !items-end !gap-2 !flex-wrap',
                    isMine ? '!flex-row' : '!flex-row-reverse'
                )}>
                    <p className="!text-base !break-all !whitespace-pre-wrap !leading-tight !font-sans !text-white !overflow-hidden !min-w-0 !flex-1">
                        {content}
                    </p>
                    <div className={cn('!flex !items-center !gap-1 !shrink-0')}>
                        <span className="!text-[13px] !font-black !text-white !drop-shadow-sm">
                            {formatTime(sendingTime)}
                        </span>
                        {isMine && (
                            <div className="!text-white/80">
                                {isRead ? (
                                    <CheckCheck className="!h-4 !w-4" />
                                ) : (
                                    <Check className="!h-4 !w-4" />
                                )}
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};
