import * as React from 'react';
import { useEffect, useState, useRef, useId } from 'react';
import {
    HashIcon,
    HouseIcon,
    MailIcon,
    SearchIcon,
    UsersRound,
    BellIcon,
    User,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
    NavigationMenu,
    NavigationMenuItem,
    NavigationMenuLink,
    NavigationMenuList,
} from '@/components/ui/navigation-menu';
import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from '@/components/ui/popover';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';

const Logo = (props: React.SVGAttributes<SVGElement>) => (
    <svg width='1em' height='1em' viewBox='0 0 324 323' fill='currentColor' xmlns='http://www.w3.org/2000/svg' {...props}>
        <rect
            x='88.1023'
            y='144.792'
            width='151.802'
            height='36.5788'
            rx='18.2894'
            transform='rotate(-38.5799 88.1023 144.792)'
            fill='currentColor'
        />
        <rect
            x='85.3459'
            y='244.537'
            width='151.802'
            height='36.5788'
            rx='18.2894'
            transform='rotate(-38.5799 85.3459 244.537)'
            fill='currentColor'
        />
    </svg>
);

const HamburgerIcon = ({ className, ...props }: React.SVGAttributes<SVGElement>) => (
    <svg
        className={cn('pointer-events-none', className)}
        width={16}
        height={16}
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        xmlns="http://www.w3.org/2000/svg"
        {...props}
    >
        <path
            d="M4 12L20 12"
            className="origin-center -translate-y-[7px] transition-all duration-300 ease-[cubic-bezier(.5,.85,.25,1.1)] group-aria-expanded:translate-x-0 group-aria-expanded:translate-y-0 group-aria-expanded:rotate-[315deg]"
        />
        <path
            d="M4 12H20"
            className="origin-center transition-all duration-300 ease-[cubic-bezier(.5,.85,.25,1.8)] group-aria-expanded:rotate-45"
        />
        <path
            d="M4 12H20"
            className="origin-center translate-y-[7px] transition-all duration-300 ease-[cubic-bezier(.5,.85,.25,1.1)] group-aria-expanded:translate-y-0 group-aria-expanded:rotate-[135deg]"
        />
    </svg>
);

interface NotificationMenuProps {
    notificationCount?: number;
    onItemClick?: (item: string) => void;
}

const NotificationMenu: React.FC<NotificationMenuProps> = ({
                                                               notificationCount = 0,
                                                               onItemClick
                                                           }) => (
    <DropdownMenu>
        <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon" className="!h-8 !w-8 !relative !rounded-full">
                <BellIcon size={16} />
                {notificationCount > 0 && (
                    <Badge className="!absolute !-top-1 !-right-1 !h-5 !w-5 !flex !items-center !justify-center !p-0 !text-xs">
                        {notificationCount > 9 ? '9+' : notificationCount}
                    </Badge>
                )}
                <span className="!sr-only">Уведомления</span>
            </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" className="!w-80 !p-4">
            <DropdownMenuLabel className="!font-normal !px-6 !py-3">
                <div className="!flex !flex-col !space-y-1">
                    <p className="!text-base !font-semibold !leading-none !tracking-tight">Уведомления</p>
                    <p className="!text-sm !leading-none !text-muted-foreground">
                        {notificationCount > 0 ? `${notificationCount} новых` : 'Нет новых'}
                    </p>
                </div>
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuItem
                onClick={() => onItemClick?.('notification1')}
                className="!px-6 !py-3 !text-base !cursor-pointer !font-medium !tracking-wide"
            >
                <div className="!flex !flex-col !gap-1">
                    <p className="!text-base !font-medium">Новое сообщение</p>
                    <p className="!text-sm !text-muted-foreground">2 минуты назад</p>
                </div>
            </DropdownMenuItem>
            <DropdownMenuItem
                onClick={() => onItemClick?.('notification2')}
                className="!px-6 !py-3 !text-base !cursor-pointer !font-medium !tracking-wide"
            >
                <div className="!flex !flex-col !gap-1">
                    <p className="!text-base !font-medium">Обновление системы</p>
                    <p className="!text-sm !text-muted-foreground">1 час назад</p>
                </div>
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem
                onClick={() => onItemClick?.('view-all')}
                className="!px-6 !py-3 !text-base !cursor-pointer !font-medium !tracking-wide"
            >
                Все уведомления
            </DropdownMenuItem>
        </DropdownMenuContent>
    </DropdownMenu>
);

interface UserMenuProps {
    userName: string;
    userEmail: string;
    onLogout?: () => void;
    onItemClick?: (item: string) => void;
}

const UserMenu: React.FC<UserMenuProps> = ({
                                               userName,
                                               userEmail,
                                               onLogout,
                                               onItemClick
                                           }) => (
    <DropdownMenu>
        <DropdownMenuTrigger asChild>
            <button className="!flex !items-center !gap-2 !rounded-full !focus:outline-none !focus:ring-2 !focus:ring-offset-2 !focus:ring-gray-500">
                <Avatar className="!h-8 !w-8">
                    <AvatarFallback>
                        <User className="!h-4 !w-4" />
                    </AvatarFallback>
                </Avatar>
            </button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" className="!w-80 !p-4">
            <DropdownMenuLabel className="!font-normal !px-6 !py-3">
                <div className="!flex !flex-col !space-y-1">
                    <p className="!text-base !font-semibold !leading-none !tracking-tight">{userName}</p>
                    <p className="!text-sm !leading-none !text-muted-foreground">{userEmail}</p>
                </div>
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuItem
                onClick={() => onItemClick?.('profile')}
                className="!px-6 !py-3 !text-base !cursor-pointer !font-medium !tracking-wide"
            >
                Профиль
            </DropdownMenuItem>
            <DropdownMenuItem
                onClick={() => onItemClick?.('settings')}
                className="!px-6 !py-3 !text-base !cursor-pointer !font-medium !tracking-wide"
            >
                Настройки
            </DropdownMenuItem>
            <DropdownMenuItem
                onClick={() => onItemClick?.('devtools')}
                className="!px-6 !py-3 !text-base !cursor-pointer !font-medium !tracking-wide"
            >
                Инструменты разработчика
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem
                onClick={() => {
                    onItemClick?.('logout');
                    onLogout?.();
                }}
                className="!px-6 !py-3 !text-base !cursor-pointer !font-medium !tracking-wide"
            >
                Выйти
            </DropdownMenuItem>
        </DropdownMenuContent>
    </DropdownMenu>
);

export interface NavbarNavItem {
    href?: string;
    label: string;
    icon: React.ComponentType<{ size?: number; className?: string; 'aria-hidden'?: boolean }>;
}

export interface NavbarProps extends React.HTMLAttributes<HTMLElement> {
    logo?: React.ReactNode;
    logoHref?: string;
    navigationLinks?: NavbarNavItem[];
    searchPlaceholder?: string;
    userName?: string;
    userEmail?: string;
    notificationCount?: number;
    messageIndicator?: boolean;
    onLogout?: () => void;
    onNavItemClick?: (href: string) => void;
    onSearch?: (query: string) => void;
    onMessageClick?: () => void;
    onNotificationItemClick?: (item: string) => void;
    onUserItemClick?: (item: string) => void;
}

const defaultNavigationLinks: NavbarNavItem[] = [
    { href: '#home', label: 'Главная', icon: HouseIcon },
    { href: '#chats', label: 'Чаты', icon: HashIcon },
    { href: '#groups', label: 'Группы', icon: UsersRound },
];

export const Navbar = React.forwardRef<HTMLElement, NavbarProps>(
    (
        {
            className,
            logo = <Logo />,
            navigationLinks = defaultNavigationLinks,
            searchPlaceholder = 'Поиск...',
            userName = 'Пользователь',
            userEmail = 'user@example.com',
            notificationCount = 0,
            messageIndicator = false,
            onLogout,
            onNavItemClick,
            onSearch,
            onMessageClick,
            onNotificationItemClick,
            onUserItemClick,
            ...props
        },
        ref
    ) => {
        const [isMobile, setIsMobile] = useState(false);
        const [searchValue, setSearchValue] = useState('');
        const containerRef = useRef<HTMLElement>(null);
        const searchId = useId();

        useEffect(() => {
            const checkWidth = () => {
                if (containerRef.current) {
                    setIsMobile(containerRef.current.offsetWidth < 768);
                }
            };

            checkWidth();
            const resizeObserver = new ResizeObserver(checkWidth);
            if (containerRef.current) {
                resizeObserver.observe(containerRef.current);
            }

            return () => resizeObserver.disconnect();
        }, []);

        const combinedRef = React.useCallback((node: HTMLElement | null) => {
            (containerRef as React.MutableRefObject<HTMLElement | null>).current = node;
            if (typeof ref === 'function') {
                ref(node);
            } else if (ref) {
                ref.current = node;
            }
        }, [ref]);

        const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
            const value = e.target.value;
            setSearchValue(value);
            onSearch?.(value);
        };

        return (
            <header
                ref={combinedRef}
                className={cn(
                    '!sticky top-0 !z-50 !w-full border-b bg-background/95 !backdrop-blur supports-[backdrop-filter]:bg-background/60 !px-4 !md:px-6',
                    className
                )}
                {...props}
            >
                <div className="!flex !h-16 !w-full !items-center !justify-between gap-4">
                    <div className="!flex !flex-1 !items-center !gap-2">
                        {isMobile && (
                            <Popover>
                                <PopoverTrigger asChild>
                                    <Button
                                        className="!group !h-8 !w-8 !hover:bg-accent !hover:text-accent-foreground"
                                        variant="ghost"
                                        size="icon"
                                    >
                                        <HamburgerIcon />
                                    </Button>
                                </PopoverTrigger>
                                <PopoverContent align="start" className="w-48 p-1">
                                    <NavigationMenu className="max-w-none">
                                        <NavigationMenuList className="flex-col items-start gap-0">
                                            {navigationLinks.map((link, index) => {
                                                const Icon = link.icon;
                                                return (
                                                    <NavigationMenuItem key={index} className="w-full">
                                                        <button
                                                            onClick={() => link.href && onNavItemClick?.(link.href)}
                                                            className="!flex !w-full !items-center! gap-2 !rounded-md !px-3 !py-2 !text-sm !font-medium !transition-colors !hover:bg-accent !hover:text-accent-foreground cursor-pointer"
                                                        >
                                                            <Icon size={16} className="text-muted-foreground" aria-hidden />
                                                            <span>{link.label}</span>
                                                        </button>
                                                    </NavigationMenuItem>
                                                );
                                            })}
                                        </NavigationMenuList>
                                    </NavigationMenu>
                                </PopoverContent>
                            </Popover>
                        )}

                        <div className="!flex !items-center !gap-6">
                            <div className="!flex !items-center !space-x-2 !text-primary">
                                <div className="text-2xl">{logo}</div>
                                <span className=" !font-bold !text-xl !sm:inline-block">Messenger</span>
                            </div>

                            <div className="relative">
                                <Input
                                    id={searchId}
                                    value={searchValue}
                                    onChange={handleSearchChange}
                                    className="!peer !h-8 !ps-8 !pe-2 !w-40 md:w-64"
                                    placeholder={searchPlaceholder}
                                    type="search"
                                />
                                <div className="!text-muted-foreground/80 !pointer-events-none !absolute !inset-y-0 !start-0 !flex !items-center !justify-center !ps-2.5">
                                    <SearchIcon size={16} />
                                </div>
                            </div>
                        </div>
                    </div>

                    {!isMobile && (
                        <NavigationMenu className="flex">
                            <NavigationMenuList className="gap-2">
                                {navigationLinks.map((link, index) => {
                                    const Icon = link.icon;
                                    return (
                                        <NavigationMenuItem key={index}>
                                            <NavigationMenuLink
                                                href={link.href}
                                                onClick={(e) => {
                                                    e.preventDefault();
                                                    link.href && onNavItemClick?.(link.href);
                                                }}
                                                className="!flex !size-8 !items-center !justify-center !p-1.5 !rounded-md !transition-colors !hover:bg-accent !hover:text-accent-foreground !cursor-pointer"
                                                title={link.label}
                                            >
                                                <Icon aria-hidden />
                                                <span className="sr-only">{link.label}</span>
                                            </NavigationMenuLink>
                                        </NavigationMenuItem>
                                    );
                                })}
                            </NavigationMenuList>
                        </NavigationMenu>
                    )}

                    <div className="!flex !flex-1 !items-center !justify-end !gap-4">
                        <div className="!flex !items-center !gap-2">
                            <Button
                                size="icon"
                                variant="ghost"
                                className="text-muted-foreground relative !size-8 rounded-full shadow-none"
                                aria-label="Сообщения"
                                onClick={() => onMessageClick?.()}
                            >
                                <MailIcon size={16} aria-hidden />
                                {messageIndicator && (
                                    <div aria-hidden className="bg-primary absolute !top-0.5 !right-0.5 size-1 !rounded-full" />
                                )}
                            </Button>

                            <NotificationMenu
                                notificationCount={notificationCount}
                                onItemClick={onNotificationItemClick}
                            />
                        </div>

                        <UserMenu
                            userName={userName}
                            userEmail={userEmail}
                            onLogout={onLogout}
                            onItemClick={onUserItemClick}
                        />
                    </div>
                </div>
            </header>
        );
    }
);

Navbar.displayName = 'Navbar';

export { Logo, HamburgerIcon, NotificationMenu, UserMenu };
