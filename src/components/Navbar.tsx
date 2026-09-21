import React from 'react';
import { Plus, Trophy, Lock, Wallet as WalletIcon, ArrowRightLeft } from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { formatCurrency } from '../utils/financeCalculations';

interface NavbarProps {
  onOpenAddModal: () => void;
  onOpenAchievementsModal: () => void;
  onOpenConverterModal: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  onOpenAddModal,
  onOpenAchievementsModal,
  onOpenConverterModal,
}) => {
  const { wallets, achievements, currency, pinCode, lockApp } = useFinance();

  const totalBalance = wallets.reduce((sum, w) => sum + w.balance, 0);
  const unlockedCount = achievements.filter((a) => a.isUnlocked).length;

  return (
    <header className="sticky top-0 z-40 bg-white/90 backdrop-blur-md border-b border-slate-200">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between">
        {/* Brand & Logo */}
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-400 flex items-center justify-center text-white shadow-md shadow-emerald-500/20">
            <WalletIcon size={22} className="stroke-[2.2]" />
          </div>
          <div>
            <h1 className="text-base sm:text-lg font-extrabold tracking-tight text-slate-900 leading-tight">
              Finance Analyzer
            </h1>
            <span className="text-[11px] font-semibold text-emerald-600 hidden sm:inline-block">
              Деньги под контролем
            </span>
          </div>
        </div>

        {/* Center/Right Info & Actions */}
        <div className="flex items-center gap-2 sm:gap-4">
          {/* Balance pill */}
          <div className="hidden sm:flex flex-col items-end pr-2 border-r border-slate-200">
            <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">
              Общий баланс
            </span>
            <span className="text-sm font-extrabold text-slate-800">
              {formatCurrency(totalBalance, currency.symbol)}
            </span>
          </div>

          {/* Currency Converter Quick Button */}
          <button
            id="nav_currency_converter_btn"
            onClick={onOpenConverterModal}
            className="flex items-center gap-1.5 p-2 sm:px-3 sm:py-2 text-slate-600 hover:text-blue-600 hover:bg-blue-50 rounded-xl transition-colors font-semibold text-xs"
            title="Конвертер валют / Траты за рубежом"
          >
            <ArrowRightLeft size={18} className="text-blue-600" />
            <span className="hidden md:inline">Конвертер валют</span>
          </button>

          {/* Achievements button */}
          <button
            id="achievements_nav_btn"
            onClick={onOpenAchievementsModal}
            className="relative p-2 text-slate-600 hover:text-amber-600 hover:bg-amber-50 rounded-xl transition-colors"
            title="Достижения"
          >
            <Trophy size={20} />
            {unlockedCount > 0 && (
              <span className="absolute -top-1 -right-1 w-4 h-4 bg-amber-500 text-white text-[10px] font-black rounded-full flex items-center justify-center">
                {unlockedCount}
              </span>
            )}
          </button>

          {/* PIN Lock button (if pin is configured) */}
          {pinCode && (
            <button
              id="lock_app_btn"
              onClick={lockApp}
              className="p-2 text-slate-500 hover:text-indigo-600 hover:bg-indigo-50 rounded-xl transition-colors"
              title="Заблокировать приложение"
            >
              <Lock size={19} />
            </button>
          )}

          {/* Primary Quick Add Transaction Button */}
          <button
            id="quick_add_transaction_btn"
            onClick={onOpenAddModal}
            className="flex items-center gap-1.5 px-3.5 py-2 bg-emerald-600 hover:bg-emerald-700 active:scale-95 text-white text-xs sm:text-sm font-bold rounded-xl shadow-md shadow-emerald-600/25 transition-all"
          >
            <Plus size={18} className="stroke-[2.5]" />
            <span>Добавить</span>
          </button>
        </div>
      </div>
    </header>
  );
};
