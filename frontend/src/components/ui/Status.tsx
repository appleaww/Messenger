import React from 'react';
import './Status.css';

export type StatusType = 'online' | 'offline';

interface StatusProps {
    status: StatusType;
    showLabel?: boolean;
}

export const Status: React.FC<StatusProps> = ({ status, showLabel = true }) => {
    return (
        <div className={`status-container ${status}`}>
            <span className="status-indicator">
                <span className="status-ping"></span>
                <span className="status-dot"></span>
            </span>
            {showLabel && (
                <span className="status-label">
                    {status === 'online' ? 'В сети' : 'Не в сети'}
                </span>
            )}
        </div>
    );
};
