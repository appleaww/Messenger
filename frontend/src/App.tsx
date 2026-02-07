import { useState, useEffect, useCallback } from 'react';
import AuthPage from './pages/AuthPage';
import ChatsPage from './pages/ChatsPage';
import { ChatPage } from './pages/ChatPage';
import { authService } from './services/authService';
import './App.css';

function App() {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [loading, setLoading] = useState(true);
    const [activeChatId, setActiveChatId] = useState<number | null>(null);
    const [refreshKey, setRefreshKey] = useState(0);

    useEffect(() => {
        const token = authService.getToken();
        if (token) {
            setIsAuthenticated(true);
        }
        setLoading(false);
    }, []);

    const handleLoginSuccess = () => {
        setIsAuthenticated(true);
    };

    const handleLogout = () => {
        authService.logout();
        setIsAuthenticated(false);
        setActiveChatId(null);
    };

    const handleOpenChat = (chatId: number) => {
        setActiveChatId(chatId);
    };

    const handleBackToChats = useCallback(() => {
        setActiveChatId(null);
        setRefreshKey(prev => prev + 1);
    }, []);

    if (loading) {
        return <div className="app-loading">Загрузка...</div>;
    }

    if (!isAuthenticated) {
        return <AuthPage onLoginSuccess={handleLoginSuccess} />;
    }

    if (activeChatId !== null) {
        return (
            <ChatPage
                chatId={activeChatId}
                onBack={handleBackToChats}
                onChatSelect={handleOpenChat}
            />
        );
    }

    return <ChatsPage key={refreshKey} onLogout={handleLogout} onOpenChat={handleOpenChat} />;
}

export default App;
