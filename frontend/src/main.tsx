import { StrictMode, useState } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import AuthPage from './pages/AuthPage';

function Root() {
    const [isAuthenticated, setIsAuthenticated] = useState(false);

    if (!isAuthenticated) {
        return <AuthPage onLoginSuccess={() => setIsAuthenticated(true)} />;
    }

    return <div>Чат будет здесь</div>;
}

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <Root />
    </StrictMode>,
)
