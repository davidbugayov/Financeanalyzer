import React from 'react';
import { Home, History, PieChart, Wallet, Settings } from 'lucide-react';
import { useFinance } from '../context/FinanceContext';

export const BottomNav: React.FC = () => {
  const { activeTab, setActiveTab } = useFinance();

  const tabs = [
    { id: 'home', label: 'Главная', icon: Home },
    { id: 'history', label: 'История', icon: History },
    { id: 'stats', label: 'Аналитика', icon: PieChart },
    { id: 'wallets', label: 'Кошельки', icon: Wallet },
    { id: 'profile', label: 'Профиль', icon: Settings },
  ] as const;

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-40 bg-white/95 backdrop-blur-md border-t border-slate-200 py-1.5 px-3">
      <div className="max-w-md mx-auto flex items-center justify-around">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              id={`nav_tab_${tab.id}`}
              onClick={() => setActiveTab(tab.id)}
              className={`flex flex-col items-center justify-center py-1 px-3 rounded-xl transition-all duration-200 ${
                isActive
                  ? 'text-emerald-600 font-bold scale-105'
                  : 'text-slate-400 hover:text-slate-600 font-medium'
              }`}
            >
              <Icon size={20} className={isActive ? 'stroke-[2.4]' : 'stroke-[1.8]'} />
              <span className="text-[10px] mt-0.5">{tab.label}</span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
