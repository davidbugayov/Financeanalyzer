import React from 'react';
import { WifiOff } from 'lucide-react';
import { useOnlineStatus } from '../hooks/usePWAInstall';

export const OfflineIndicator: React.FC = () => {
  const isOnline = useOnlineStatus();

  if (isOnline) return null;

  return (
    <div
      id="pwa_offline_indicator"
      className="fixed bottom-20 left-4 z-50 flex items-center gap-2 rounded-xl bg-amber-500/95 backdrop-blur-xs px-3.5 py-2 text-xs font-bold text-white shadow-lg animate-in slide-in-from-bottom duration-200"
    >
      <WifiOff size={16} className="animate-pulse" />
      <span>Оффлайн-режим — используются сохраненные локальные данные PWA</span>
    </div>
  );
};
