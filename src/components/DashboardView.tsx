import React, { useState, useMemo } from 'react';
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
  Globe,
  Repeat,
  Clock,
  Eye,
  EyeOff,
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { formatCurrency, formatTransactionDate, calculateTotals, getSmartTips } from '../utils/financeCalculations';
import { DynamicIcon } from '../utils/iconHelper';
import { Transaction, TransactionType } from '../types';
import { getIntervalLabel } from '../utils/recurringProcessor';
import { BudgetDashboardWidget } from './BudgetDashboardWidget';
import { BudgetModal } from './BudgetModal';
import { AiInsightsSection } from './AiInsightsSection';
import { MonthlySpendingTrendChart } from './MonthlySpendingTrendChart';

interface DashboardViewProps {
  onOpenAddModal: (type?: TransactionType) => void;
  onEditTransaction: (tx: Transaction) => void;
  onOpenConverterModal?: () => void;
  onOpenRecurringManager?: () => void;
}

export const DashboardView: React.FC<DashboardViewProps> = ({
  onOpenAddModal,
  onEditTransaction,
  onOpenConverterModal,
  onOpenRecurringManager,
}) => {
  const { transactions, wallets, categories, currency, setActiveTab, processRecurring } = useFinance();
  const [filterType, setFilterType] = useState<'all' | 'expense' | 'income'>('all');
  const [isBudgetModalOpen, setIsBudgetModalOpen] = useState(false);
  const [selectedBudgetCategoryId, setSelectedBudgetCategoryId] = useState<string | null>(null);
  const [isBalanceHidden, setIsBalanceHidden] = useState<boolean>(() => {
    return localStorage.getItem('fa_balance_hidden') === 'true';
  });

  const toggleHideBalance = () => {
    setIsBalanceHidden((prev) => {
      const next = !prev;
      localStorage.setItem('fa_balance_hidden', String(next));
      return next;
    });
  };

  const handleOpenBudgetModal = (categoryId?: string) => {
    setSelectedBudgetCategoryId(categoryId || null);
    setIsBudgetModalOpen(true);
  };

  const recurringTemplates = useMemo(() => {
    return transactions
      .filter((t) => t.isRecurring)
      .sort((a, b) => {
        const dateA = a.recurrenceNextDate || a.date;
        const dateB = b.recurrenceNextDate || b.date;
        return dateA.localeCompare(dateB);
      });
  }, [transactions]);

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
    <div className="space-y-6">
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

      {/* Main Balance Card with enhanced UI/UX, tabular numbers, and direct actions */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-emerald-600 via-teal-700 to-emerald-800 text-white p-5 sm:p-7 shadow-xl shadow-emerald-950/20">
        <div className="relative z-10 space-y-4">
          {/* Top row: Capital label + Eye toggle + Savings & Net pills */}
          <div className="flex items-center justify-between gap-2 flex-wrap">
            <div className="flex items-center gap-2">
              <span className="text-xs font-black uppercase tracking-widest text-emerald-100">
                Текущий капитал
              </span>
              <button
                type="button"
                onClick={toggleHideBalance}
                className="p-1 rounded-lg text-emerald-200 hover:text-white hover:bg-white/10 transition-colors cursor-pointer"
                title={isBalanceHidden ? 'Показать цифры' : 'Скрыть цифры'}
                aria-label={isBalanceHidden ? 'Показать цифры' : 'Скрыть цифры'}
              >
                {isBalanceHidden ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>

            <div className="flex items-center gap-2 flex-wrap">
              <span className="px-3 py-1 rounded-full bg-white/15 text-white text-xs font-black backdrop-blur-md tabular-nums border border-white/20 whitespace-nowrap">
                Сбережения: {savingsRate}%
              </span>
              {!isBalanceHidden && net !== 0 && (
                <span
                  className={`px-2.5 py-1 rounded-full text-xs font-black backdrop-blur-md tabular-nums border whitespace-nowrap ${
                    net > 0
                      ? 'bg-emerald-400/25 text-emerald-100 border-emerald-300/30'
                      : 'bg-rose-500/25 text-rose-100 border-rose-300/30'
                  }`}
                >
                  {net > 0 ? '+' : ''}
                  {formatCurrency(net, currency.symbol)} сальдо
                </span>
              )}
            </div>
          </div>

          {/* Amount and Subtitle */}
          <div>
            <div className="flex items-baseline gap-2 flex-wrap">
              <span className="text-3xl sm:text-5xl font-black tracking-tight tabular-nums select-all">
                {isBalanceHidden ? '••••••••' : formatCurrency(totalBalance, currency.symbol)}
              </span>
            </div>
            <p className="text-xs sm:text-sm text-emerald-100/90 mt-1 font-medium">
              Суммарный остаток на всех ваших счетах ({wallets.length} {wallets.length === 1 ? 'счет' : 'счетов'})
            </p>
          </div>

          {/* Direct Income & Expense Action Buttons (clickable cards) */}
          <div className="grid grid-cols-2 gap-2.5 sm:gap-3.5 pt-3 border-t border-white/15">
            <button
              type="button"
              id="dashboard_quick_add_income_btn"
              onClick={() => onOpenAddModal('income')}
              className="flex flex-col justify-between bg-white/15 hover:bg-white/25 active:scale-[0.98] border border-white/20 p-2.5 sm:p-3.5 rounded-2xl backdrop-blur-xs transition-all text-left group cursor-pointer min-w-0 shadow-2xs"
              title={`Добавить доход (${formatCurrency(income, currency.symbol)})`}
            >
              <div className="flex items-center justify-between w-full mb-1 sm:mb-1.5">
                <div className="flex items-center gap-1.5 min-w-0">
                  <div className="w-6 h-6 rounded-lg bg-emerald-500/40 text-emerald-100 flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform shadow-2xs">
                    <ArrowDownLeft size={14} className="stroke-[2.5]" />
                  </div>
                  <span className="text-[11px] sm:text-xs font-extrabold text-emerald-100 truncate">Доходы</span>
                </div>
                <span className="w-5 h-5 rounded-md bg-white/10 flex items-center justify-center text-emerald-200 group-hover:bg-white/25 transition-colors shrink-0">
                  <Plus size={12} className="stroke-[2.5]" />
                </span>
              </div>
              <div className="w-full">
                <span className="text-[14px] xs:text-[15px] sm:text-base md:text-lg font-black text-white tabular-nums tracking-tight whitespace-nowrap block leading-tight">
                  {isBalanceHidden ? '••••••' : `+${formatCurrency(income, currency.symbol)}`}
                </span>
              </div>
            </button>

            <button
              type="button"
              id="dashboard_quick_add_expense_btn"
              onClick={() => onOpenAddModal('expense')}
              className="flex flex-col justify-between bg-white/15 hover:bg-white/25 active:scale-[0.98] border border-white/20 p-2.5 sm:p-3.5 rounded-2xl backdrop-blur-xs transition-all text-left group cursor-pointer min-w-0 shadow-2xs"
              title={`Добавить расход (${formatCurrency(expense, currency.symbol)})`}
            >
              <div className="flex items-center justify-between w-full mb-1 sm:mb-1.5">
                <div className="flex items-center gap-1.5 min-w-0">
                  <div className="w-6 h-6 rounded-lg bg-rose-500/35 text-rose-100 flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform shadow-2xs">
                    <ArrowUpRight size={14} className="stroke-[2.5]" />
                  </div>
                  <span className="text-[11px] sm:text-xs font-extrabold text-rose-100 truncate">Расходы</span>
                </div>
                <span className="w-5 h-5 rounded-md bg-white/10 flex items-center justify-center text-rose-200 group-hover:bg-white/25 transition-colors shrink-0">
                  <Plus size={12} className="stroke-[2.5]" />
                </span>
              </div>
              <div className="w-full">
                <span className="text-[14px] xs:text-[15px] sm:text-base md:text-lg font-black text-white tabular-nums tracking-tight whitespace-nowrap block leading-tight">
                  {isBalanceHidden ? '••••••' : `-${formatCurrency(expense, currency.symbol)}`}
                </span>
              </div>
            </button>
          </div>

          {/* Quick Transfer Between Wallets */}
          <div className="flex justify-end pt-0.5">
            <button
              type="button"
              id="dashboard_card_transfer_btn"
              onClick={() => onOpenAddModal('transfer')}
              className="inline-flex items-center gap-1.5 px-3 py-1 rounded-xl bg-white/10 hover:bg-white/20 active:scale-95 text-emerald-100 hover:text-white text-xs font-bold transition-all border border-white/15 cursor-pointer"
              title="Сделать перевод между своими счетами"
            >
              <ArrowLeftRight size={13} />
              <span>Перевод между счетами</span>
            </button>
          </div>
        </div>

        {/* Decorative background glow */}
        <div className="absolute -right-16 -bottom-16 w-64 h-64 bg-white/10 rounded-full blur-3xl pointer-events-none" />
      </div>

      {/* 6-Month Monthly Spending Trend Chart */}
      <MonthlySpendingTrendChart />

      {/* Monthly Budget Tracking System with Category Progress Bars */}
      <BudgetDashboardWidget onOpenBudgetModal={handleOpenBudgetModal} />

      {/* AI Insights & Spending Pattern Analysis Section */}
      <AiInsightsSection
        onOpenBudgetModal={handleOpenBudgetModal}
        onNavigateTab={setActiveTab}
      />

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

      {/* Recurring / Scheduled Transactions Widget */}
      {recurringTemplates.length > 0 && (
        <div
          id="dashboard_recurring_widget"
          className="bg-white rounded-3xl p-5 sm:p-6 shadow-xs border border-teal-200/80 space-y-3.5"
        >
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-xl bg-teal-50 text-teal-700 border border-teal-200 flex items-center justify-center shrink-0">
                <Repeat size={16} />
              </div>
              <div>
                <h3 className="text-sm sm:text-base font-extrabold text-slate-900">
                  Регулярные операции ({recurringTemplates.length})
                </h3>
                <p className="text-[11px] text-slate-500">
                  Автоматическое списание и начисление по расписанию
                </p>
              </div>
            </div>

            <div className="flex items-center gap-2 flex-wrap">
              <button
                id="dashboard_process_recurring_btn"
                onClick={() => processRecurring()}
                className="px-2.5 sm:px-3 py-1.5 rounded-xl bg-teal-50 hover:bg-teal-100 active:bg-teal-200 text-teal-800 text-xs font-bold transition-colors flex items-center gap-1.5 cursor-pointer border border-teal-200"
                title="Проверить и провести наступившие регулярные платежи"
              >
                <Clock size={13} className="text-teal-700" />
                <span className="hidden sm:inline">Проверить</span>
              </button>

              {onOpenRecurringManager && (
                <button
                  id="dashboard_open_recurring_manager_btn"
                  onClick={onOpenRecurringManager}
                  className="px-3 py-1.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-white text-xs font-bold transition-colors flex items-center gap-1 cursor-pointer shadow-2xs"
                  title="Открыть управление всеми регулярными расписаниями"
                >
                  <span>Все расписания</span>
                  <ChevronRight size={13} />
                </button>
              )}
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2.5">
            {recurringTemplates.slice(0, 3).map((item) => {
              const cat = categories.find((c) => c.name === item.category || c.id === item.categoryId);
              return (
                <div
                  key={item.id}
                  onClick={() => onEditTransaction(item)}
                  className="bg-slate-50/80 hover:bg-slate-100 p-3 rounded-2xl border border-slate-200/80 transition-colors cursor-pointer flex items-center justify-between gap-2"
                >
                  <div className="flex items-center gap-2.5 min-w-0">
                    <div
                      className="w-7 h-7 rounded-lg flex items-center justify-center text-white shrink-0"
                      style={{ backgroundColor: cat?.color || '#10B981' }}
                    >
                      <DynamicIcon name={cat?.icon || 'Tag'} size={14} />
                    </div>
                    <div className="min-w-0">
                      <div className="text-xs font-bold text-slate-800 truncate flex items-center gap-1">
                        <span>{item.category}</span>
                        {item.recurrenceInterval && (
                          <span className="text-[9px] font-extrabold bg-teal-100 text-teal-800 px-1 py-0.2 rounded">
                            {getIntervalLabel(item.recurrenceInterval)}
                          </span>
                        )}
                      </div>
                      <div className="text-[10px] text-slate-400 truncate flex items-center gap-1">
                        <span>След:</span>
                        <strong className="text-slate-700 font-semibold">
                          {item.recurrenceNextDate || item.date}
                        </strong>
                      </div>
                    </div>
                  </div>

                  <div className="text-right shrink-0">
                    <span className="text-xs font-black text-teal-700 whitespace-nowrap">
                      {formatCurrency(item.amount, currency.symbol)}
                    </span>
                  </div>
                </div>
              );
            })}
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
                  ? 'bg-emerald-600 text-white shadow-2xs'
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
              onClick={() => onOpenAddModal()}
              className="mt-3 text-xs font-bold text-emerald-600 hover:underline cursor-pointer"
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
                  className="py-3 sm:py-3.5 flex items-center justify-between gap-2.5 sm:gap-3 hover:bg-slate-50 px-2 sm:px-2.5 rounded-xl cursor-pointer transition-colors"
                >
                  <div className="flex items-center gap-2.5 sm:gap-3 min-w-0 flex-1">
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
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-1.5 flex-wrap min-w-0">
                        <span className="text-xs sm:text-sm font-bold text-slate-900 truncate max-w-[150px] sm:max-w-none">
                          {t.category}
                        </span>
                        {t.subcategory && (
                          <span className="text-[10px] font-medium text-slate-500 bg-slate-100 px-1.5 py-0.5 rounded-md truncate max-w-[120px]">
                            {t.subcategory}
                          </span>
                        )}
                        {t.isForeignCurrency && (
                          <span className="text-[10px] font-bold text-blue-700 bg-blue-50 border border-blue-200/80 px-1.5 py-0.5 rounded-md flex items-center gap-1 shrink-0">
                            <Globe size={11} className="text-blue-500" />
                            <span>{t.originalAmount}&nbsp;{t.originalCurrency}</span>
                            {t.country && <span className="opacity-80">({t.country})</span>}
                          </span>
                        )}
                        {t.isRecurring && (
                          <span
                            className="text-[10px] font-bold text-teal-800 bg-teal-50 border border-teal-200/90 px-1.5 py-0.5 rounded-md flex items-center gap-1 shrink-0"
                            title={t.recurrenceNextDate ? `Следующее списание: ${t.recurrenceNextDate}` : 'Регулярная операция'}
                          >
                            <Repeat size={10} className="text-teal-600" />
                            <span>{t.recurrenceInterval ? getIntervalLabel(t.recurrenceInterval) : 'Регулярная'}</span>
                          </span>
                        )}
                        {t.parentRecurringId && (
                          <span
                            className="text-[10px] font-semibold text-slate-600 bg-slate-100 border border-slate-200 px-1.5 py-0.5 rounded-md flex items-center gap-1 shrink-0"
                            title="Создано автоплатежом по расписанию"
                          >
                            <Repeat size={9} className="text-slate-400" />
                            <span>Автоплатеж</span>
                          </span>
                        )}
                      </div>
                      <div className="flex items-center gap-1.5 text-[11px] text-slate-400 mt-0.5 min-w-0 overflow-hidden">
                        <span className="whitespace-nowrap font-medium text-slate-500 shrink-0">
                          {formatTransactionDate(t.date)}
                        </span>
                        {wallet && (
                          <span className="truncate max-w-[110px] sm:max-w-[180px] shrink-0" title={wallet.name}>
                            • {wallet.name}
                          </span>
                        )}
                        {t.note && (
                          <span className="truncate flex-1 min-w-[50px] italic text-slate-400" title={t.note}>
                            • {t.note}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>

                  <div className="text-right shrink-0 pl-2">
                    <span
                      className={`text-xs sm:text-sm font-extrabold whitespace-nowrap block tracking-tight ${
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

      {/* Budget Configuration Modal */}
      <BudgetModal
        isOpen={isBudgetModalOpen}
        onClose={() => {
          setIsBudgetModalOpen(false);
          setSelectedBudgetCategoryId(null);
        }}
        initialCategoryId={selectedBudgetCategoryId}
      />
    </div>
  );
};
