import React, { useState } from 'react';
import {
  ArrowUpRight,
  ArrowDownLeft,
  ArrowLeftRight,
  Plus,
  Sparkles,
  ChevronRight,
  ShieldAlert,
  Wallet as WalletIcon,
  TrendingUp,
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { formatCurrency, calculateTotals, getSmartTips } from '../utils/financeCalculations';
import { DynamicIcon } from '../utils/iconHelper';
import { Transaction } from '../types';

interface DashboardViewProps {
  onOpenAddModal: () => void;
  onEditTransaction: (tx: Transaction) => void;
}

export const DashboardView: React.FC<DashboardViewProps> = ({
  onOpenAddModal,
  onEditTransaction,
}) => {
  const { transactions, wallets, categories, currency, setActiveTab } = useFinance();
  const [filterType, setFilterType] = useState<'all' | 'expense' | 'income'>('all');

  const { income, expense, net, savingsRate } = calculateTotals(transactions);
  const totalBalance = wallets.reduce((sum, w) => sum + w.balance, 0);

  // Check budget overruns
  const overLimitWallets = wallets.filter((w) => w.limit > 0 && w.balance < 0);

  // Filtered recent transactions
  const filteredTxs = transactions
    .filter((t) => {
      if (filterType === 'all') return true;
      return t.type === filterType;
    })
    .slice(0, 8);

  const smartTips = getSmartTips(transactions);
  const currentTip = smartTips[0];

  return (
    <div className="space-y-6 pb-20">
      {/* Over-limit warning banner if any */}
      {overLimitWallets.length > 0 && (
        <div className="p-4 bg-amber-50 border border-amber-200 rounded-2xl flex items-start gap-3 text-amber-900">
          <ShieldAlert size={20} className="text-amber-600 shrink-0 mt-0.5" />
          <div className="text-xs">
            <span className="font-bold">Превышение лимита бюджета:</span> Внимание, баланс кошелька{' '}
            <span className="font-semibold">{overLimitWallets.map((w) => w.name).join(', ')}</span> ниже
            допустимого лимита.
          </div>
        </div>
      )}

      {/* Main Balance Card */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-slate-900 via-slate-800 to-emerald-950 text-white p-6 sm:p-8 shadow-xl">
        <div className="relative z-10">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-widest text-emerald-400">
              Текущий капитал
            </span>
            <span className="px-3 py-1 rounded-full bg-white/10 text-emerald-300 text-xs font-bold backdrop-blur-md">
              Сбережения: {savingsRate}%
            </span>
          </div>

          <div className="mt-2 mb-6">
            <div className="text-3xl sm:text-5xl font-extrabold tracking-tight">
              {formatCurrency(totalBalance, currency.symbol)}
            </div>
            <p className="text-xs sm:text-sm text-slate-400 mt-1">
              Суммарный остаток на всех ваших счетах
            </p>
          </div>

          {/* Income & Expense pill grid */}
          <div className="grid grid-cols-2 gap-3 sm:gap-4 pt-4 border-t border-white/10">
            <div className="flex items-center gap-3 bg-white/5 p-3 rounded-2xl backdrop-blur-xs">
              <div className="w-10 h-10 rounded-xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center shrink-0">
                <ArrowDownLeft size={20} className="stroke-[2.5]" />
              </div>
              <div>
                <span className="text-[11px] font-medium text-slate-400 block">Доходы</span>
                <span className="text-sm sm:text-base font-bold text-emerald-400">
                  +{formatCurrency(income, currency.symbol)}
                </span>
              </div>
            </div>

            <div className="flex items-center gap-3 bg-white/5 p-3 rounded-2xl backdrop-blur-xs">
              <div className="w-10 h-10 rounded-xl bg-red-500/20 text-red-400 flex items-center justify-center shrink-0">
                <ArrowUpRight size={20} className="stroke-[2.5]" />
              </div>
              <div>
                <span className="text-[11px] font-medium text-slate-400 block">Расходы</span>
                <span className="text-sm sm:text-base font-bold text-red-400">
                  -{formatCurrency(expense, currency.symbol)}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Decorative background glow */}
        <div className="absolute -right-16 -bottom-16 w-64 h-64 bg-emerald-500/20 rounded-full blur-3xl pointer-events-none" />
      </div>

      {/* Quick Action Buttons */}
      <div className="grid grid-cols-4 gap-2 sm:gap-3">
        <button
          id="action_add_tx_btn"
          onClick={onOpenAddModal}
          className="flex flex-col items-center justify-center p-3 sm:p-4 rounded-2xl bg-white border border-slate-200/80 shadow-xs hover:border-emerald-500 hover:shadow-md transition-all group"
        >
          <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center mb-1.5 group-hover:scale-110 transition-transform">
            <Plus size={20} className="stroke-[2.5]" />
          </div>
          <span className="text-[11px] font-bold text-slate-700">Транзакция</span>
        </button>

        <button
          id="action_view_wallets_btn"
          onClick={() => setActiveTab('wallets')}
          className="flex flex-col items-center justify-center p-3 sm:p-4 rounded-2xl bg-white border border-slate-200/80 shadow-xs hover:border-blue-500 hover:shadow-md transition-all group"
        >
          <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center mb-1.5 group-hover:scale-110 transition-transform">
            <WalletIcon size={20} className="stroke-[2]" />
          </div>
          <span className="text-[11px] font-bold text-slate-700">Кошельки</span>
        </button>

        <button
          id="action_view_analytics_btn"
          onClick={() => setActiveTab('stats')}
          className="flex flex-col items-center justify-center p-3 sm:p-4 rounded-2xl bg-white border border-slate-200/80 shadow-xs hover:border-purple-500 hover:shadow-md transition-all group"
        >
          <div className="w-10 h-10 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center mb-1.5 group-hover:scale-110 transition-transform">
            <TrendingUp size={20} className="stroke-[2]" />
          </div>
          <span className="text-[11px] font-bold text-slate-700">Аналитика</span>
        </button>

        <button
          id="action_transfer_btn"
          onClick={onOpenAddModal}
          className="flex flex-col items-center justify-center p-3 sm:p-4 rounded-2xl bg-white border border-slate-200/80 shadow-xs hover:border-amber-500 hover:shadow-md transition-all group"
        >
          <div className="w-10 h-10 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center mb-1.5 group-hover:scale-110 transition-transform">
            <ArrowLeftRight size={20} className="stroke-[2]" />
          </div>
          <span className="text-[11px] font-bold text-slate-700">Перевод</span>
        </button>
      </div>

      {/* Smart Financial Advice Card */}
      {currentTip && (
        <div className="p-4 sm:p-5 rounded-2xl bg-gradient-to-r from-emerald-50 via-teal-50 to-emerald-100/50 border border-emerald-200 flex items-start gap-4 shadow-xs">
          <div className="w-10 h-10 rounded-xl bg-emerald-600 text-white flex items-center justify-center shrink-0 shadow-sm">
            <Sparkles size={20} />
          </div>
          <div className="flex-1">
            <div className="flex items-center gap-2">
              <span className="text-[10px] font-extrabold uppercase tracking-wider text-emerald-800 bg-emerald-200/60 px-2 py-0.5 rounded-md">
                Совет дня
              </span>
              <h4 className="text-xs sm:text-sm font-bold text-slate-900">{currentTip.title}</h4>
            </div>
            <p className="text-xs text-slate-600 mt-1 leading-relaxed">{currentTip.desc}</p>
          </div>
        </div>
      )}

      {/* Recent Transactions Section */}
      <div className="bg-white rounded-3xl border border-slate-200/90 p-5 sm:p-6 shadow-xs">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h3 className="text-base font-extrabold text-slate-900">Недавние транзакции</h3>
            <p className="text-xs text-slate-400">Последние операции по всем счетам</p>
          </div>

          <button
            id="view_all_history_btn"
            onClick={() => setActiveTab('history')}
            className="flex items-center gap-1 text-xs font-bold text-emerald-600 hover:text-emerald-700"
          >
            Все <ChevronRight size={14} />
          </button>
        </div>

        {/* Filter chips */}
        <div className="flex gap-2 mb-4">
          {(['all', 'expense', 'income'] as const).map((mode) => (
            <button
              key={mode}
              onClick={() => setFilterType(mode)}
              className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-colors ${
                filterType === mode
                  ? 'bg-slate-900 text-white'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {mode === 'all' ? 'Все' : mode === 'expense' ? 'Расходы' : 'Доходы'}
            </button>
          ))}
        </div>

        {/* Transactions list */}
        {filteredTxs.length === 0 ? (
          <div className="text-center py-10">
            <p className="text-sm font-semibold text-slate-500">Нет транзакций</p>
            <button
              onClick={onOpenAddModal}
              className="mt-3 text-xs font-bold text-emerald-600 hover:underline"
            >
              Добавить первую операцию
            </button>
          </div>
        ) : (
          <div className="divide-y divide-slate-100">
            {filteredTxs.map((t) => {
              const cat = categories.find((c) => c.name === t.category || c.id === t.categoryId);
              const wallet = wallets.find((w) => w.id === t.walletId);
              const isIncome = t.type === 'income';
              const isTransfer = t.type === 'transfer';

              return (
                <div
                  key={t.id}
                  id={`tx_item_${t.id}`}
                  onClick={() => onEditTransaction(t)}
                  className="py-3 sm:py-3.5 flex items-center justify-between gap-3 hover:bg-slate-50 px-2 rounded-xl cursor-pointer transition-colors"
                >
                  <div className="flex items-center gap-3">
                    <div
                      className="w-10 h-10 rounded-xl flex items-center justify-center text-white shrink-0 shadow-xs"
                      style={{
                        backgroundColor: isTransfer ? '#3B82F6' : cat?.color || '#10B981',
                      }}
                    >
                      <DynamicIcon
                        name={isTransfer ? 'ArrowLeftRight' : cat?.icon || 'Tag'}
                        size={18}
                      />
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-xs sm:text-sm font-bold text-slate-900">
                          {t.category}
                        </span>
                        {t.subcategory && (
                          <span className="text-[10px] font-medium text-slate-500 bg-slate-100 px-1.5 py-0.5 rounded-md">
                            {t.subcategory}
                          </span>
                        )}
                      </div>
                      <div className="flex items-center gap-2 text-[11px] text-slate-400 mt-0.5">
                        <span>{t.date}</span>
                        {wallet && <span>• {wallet.name}</span>}
                        {t.note && <span className="line-clamp-1 italic">• {t.note}</span>}
                      </div>
                    </div>
                  </div>

                  <div className="text-right shrink-0">
                    <span
                      className={`text-xs sm:text-sm font-extrabold ${
                        isIncome
                          ? 'text-emerald-600'
                          : isTransfer
                          ? 'text-blue-600'
                          : 'text-slate-900'
                      }`}
                    >
                      {isIncome ? '+' : isTransfer ? '' : '-'}
                      {formatCurrency(t.amount, currency.symbol)}
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};
