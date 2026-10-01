import React, { useState, useMemo } from 'react';
import {
  Repeat,
  Plus,
  Clock,
  Calendar,
  Wallet as WalletIcon,
  CheckCircle2,
  AlertTriangle,
  Play,
  Edit2,
  Trash2,
  Power,
  ChevronRight,
  TrendingDown,
  TrendingUp,
  Search,
  Filter,
  ArrowDownLeft,
  ArrowUpRight,
  Sparkles,
  Info,
  X,
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { Transaction, RecurrenceInterval } from '../types';
import {
  getIntervalLabel,
  calculateNextRecurrenceDate,
  getTodayString,
} from '../utils/recurringProcessor';
import { formatCurrency } from '../utils/financeCalculations';
import { DynamicIcon } from '../utils/iconHelper';

interface RecurringSchedulesManagerProps {
  onOpenAddModal: (isRecurring?: boolean) => void;
  onEditTransaction: (tx: Transaction) => void;
  isModal?: boolean;
  onClose?: () => void;
}

// Normalize any recurrence interval amount into an estimated monthly figure
export function normalizeToMonthlyAmount(
  amount: number,
  interval?: RecurrenceInterval
): number {
  switch (interval) {
    case 'daily':
      return Math.round(amount * 30.4);
    case 'weekly':
      return Math.round(amount * 4.33);
    case 'biweekly':
      return Math.round(amount * 2.16);
    case 'yearly':
      return Math.round(amount / 12);
    case 'monthly':
    default:
      return amount;
  }
}

export const RecurringSchedulesManager: React.FC<RecurringSchedulesManagerProps> = ({
  onOpenAddModal,
  onEditTransaction,
  isModal = false,
  onClose,
}) => {
  const {
    transactions,
    wallets,
    categories,
    currency,
    updateTransaction,
    deleteTransaction,
    addTransaction,
    processRecurring,
  } = useFinance();

  const [filterType, setFilterType] = useState<'all' | 'expense' | 'income'>('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedTemplateId, setSelectedTemplateId] = useState<string | null>(null);
  const [successToast, setSuccessToast] = useState<string | null>(null);

  const todayStr = useMemo(() => getTodayString(), []);

  // All recurring templates (excluding instances created from them)
  const allTemplates = useMemo(() => {
    return transactions.filter((t) => t.isRecurring && !t.parentRecurringId);
  }, [transactions]);

  // Derived metrics
  const metrics = useMemo(() => {
    let monthlyExpenses = 0;
    let monthlyIncomes = 0;
    let expensesCount = 0;
    let incomesCount = 0;
    let nearestSchedule: Transaction | null = null;
    let nearestDays = Infinity;

    const todayDate = new Date(todayStr);

    allTemplates.forEach((t) => {
      const monthlyAmount = normalizeToMonthlyAmount(t.amount, t.recurrenceInterval);
      if (t.type === 'expense') {
        monthlyExpenses += monthlyAmount;
        expensesCount++;
      } else if (t.type === 'income') {
        monthlyIncomes += monthlyAmount;
        incomesCount++;
      }

      const nextDateStr = t.recurrenceNextDate || t.date;
      if (nextDateStr) {
        const nextDate = new Date(nextDateStr);
        const diffDays = Math.ceil((nextDate.getTime() - todayDate.getTime()) / (1000 * 3600 * 24));
        if (diffDays >= 0 && diffDays < nearestDays) {
          nearestDays = diffDays;
          nearestSchedule = t;
        }
      }
    });

    return {
      monthlyExpenses,
      monthlyIncomes,
      netMonthly: monthlyIncomes - monthlyExpenses,
      expensesCount,
      incomesCount,
      totalCount: allTemplates.length,
      nearestSchedule: nearestSchedule as Transaction | null,
      nearestDays: nearestDays === Infinity ? null : nearestDays,
    };
  }, [allTemplates, todayStr]);

  // Filtered and searched templates
  const filteredTemplates = useMemo(() => {
    return allTemplates.filter((t) => {
      if (filterType !== 'all' && t.type !== filterType) return false;
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase();
        const matchesCategory = t.category.toLowerCase().includes(q);
        const matchesNote = t.note?.toLowerCase().includes(q);
        const matchesSubcat = t.subcategory?.toLowerCase().includes(q);
        const wallet = wallets.find((w) => w.id === t.walletId);
        const matchesWallet = wallet?.name.toLowerCase().includes(q);
        if (!matchesCategory && !matchesNote && !matchesSubcat && !matchesWallet) {
          return false;
        }
      }
      return true;
    });
  }, [allTemplates, filterType, searchQuery, wallets]);

  // Trigger manual premature execution of a recurring schedule
  const handleTriggerNow = (template: Transaction) => {
    const nextDate = template.recurrenceNextDate || template.date;
    const interval = template.recurrenceInterval || 'monthly';
    const subsequentDate = calculateNextRecurrenceDate(nextDate, interval);

    // Create execution instance
    addTransaction({
      amount: template.amount,
      type: template.type,
      category: template.category,
      categoryId: template.categoryId,
      subcategory: template.subcategory,
      walletId: template.walletId,
      targetWalletId: template.targetWalletId,
      date: todayStr,
      note: template.note ? `${template.note} (Автосписание)` : `Плановый платеж «${template.category}»`,
      parentRecurringId: template.id,
      isRecurring: false,
    });

    // Advance schedule to next date
    updateTransaction({
      ...template,
      recurrenceLastProcessed: todayStr,
      recurrenceNextDate: subsequentDate,
    });

    setSuccessToast(`Операция «${template.category}» успешно проведена сегодня!`);
    setTimeout(() => setSuccessToast(null), 3500);
  };

  // Toggle pausing / resuming recurrence
  const handleToggleRecurrence = (template: Transaction) => {
    updateTransaction({
      ...template,
      isRecurring: !template.isRecurring,
    });
    setSuccessToast(
      template.isRecurring
        ? `Расписание «${template.category}» приостановлено`
        : `Расписание «${template.category}» возобновлено`
    );
    setTimeout(() => setSuccessToast(null), 3000);
  };

  // Delete template
  const handleDeleteTemplate = (id: string, name: string) => {
    if (confirm(`Удалить регулярное расписание для «${name}»? (История прошлых списаний сохранится)`)) {
      deleteTransaction(id);
      setSuccessToast(`Расписание «${name}» удалено`);
      setTimeout(() => setSuccessToast(null), 3000);
    }
  };

  // Countdown badge helper
  const getDueBadge = (nextDateStr?: string) => {
    if (!nextDateStr) return null;
    const today = new Date(todayStr);
    const nextDate = new Date(nextDateStr);
    const diffDays = Math.ceil((nextDate.getTime() - today.getTime()) / (1000 * 3600 * 24));

    if (diffDays < 0) {
      return (
        <span className="px-2 py-0.5 rounded-full text-[10px] font-black bg-rose-100 text-rose-800 border border-rose-200 flex items-center gap-1">
          <AlertTriangle size={10} />
          <span>Просрочено ({Math.abs(diffDays)} дн.)</span>
        </span>
      );
    }
    if (diffDays === 0) {
      return (
        <span className="px-2 py-0.5 rounded-full text-[10px] font-black bg-amber-100 text-amber-900 border border-amber-300 flex items-center gap-1 animate-pulse">
          <Clock size={10} />
          <span>Сегодня!</span>
        </span>
      );
    }
    if (diffDays === 1) {
      return (
        <span className="px-2 py-0.5 rounded-full text-[10px] font-black bg-emerald-100 text-emerald-800 border border-emerald-200">
          Завтра
        </span>
      );
    }
    return (
      <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-slate-100 text-slate-700 border border-slate-200">
        Через {diffDays} дн.
      </span>
    );
  };

  const content = (
    <div className="space-y-6">
      {/* Toast Banner */}
      {successToast && (
        <div className="p-3.5 rounded-2xl bg-emerald-600 text-white font-bold text-xs flex items-center justify-between shadow-lg animate-in slide-in-from-top-2 duration-200">
          <div className="flex items-center gap-2">
            <CheckCircle2 size={16} className="text-emerald-200" />
            <span>{successToast}</span>
          </div>
          <button
            onClick={() => setSuccessToast(null)}
            className="p-1 hover:bg-white/10 rounded-lg text-emerald-100"
          >
            <X size={14} />
          </button>
        </div>
      )}

      {/* Header section with Action Buttons */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-100">
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 rounded-2xl bg-gradient-to-tr from-teal-500 to-emerald-600 text-white flex items-center justify-center shadow-xs shrink-0">
            <Repeat size={22} className="stroke-[2.5]" />
          </div>
          <div>
            <div className="flex items-center gap-2 flex-wrap">
              <h2 className="text-lg sm:text-xl font-black text-slate-900 tracking-tight">
                Регулярные платежи и подписки
              </h2>
              <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-teal-50 text-teal-800 border border-teal-200">
                {allTemplates.length} активных
              </span>
            </div>
            <p className="text-xs text-slate-500 mt-0.5">
              Управление автоматическими списаниями, подписками и регулярными доходами
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2 flex-wrap">
          <button
            type="button"
            onClick={() => {
              processRecurring();
              setSuccessToast('Проверка расписания завершена!');
              setTimeout(() => setSuccessToast(null), 3000);
            }}
            className="px-3.5 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 active:bg-slate-300 text-slate-700 text-xs font-bold transition-all flex items-center gap-1.5 cursor-pointer border border-slate-200/80 shadow-2xs"
            title="Проверить и провести наступившие платежи"
          >
            <Clock size={14} className="text-slate-600" />
            <span>Проверить сейчас</span>
          </button>

          <button
            type="button"
            onClick={() => onOpenAddModal(true)}
            className="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 active:bg-emerald-800 text-white text-xs font-bold transition-all flex items-center gap-1.5 cursor-pointer shadow-xs hover:shadow-sm"
          >
            <Plus size={16} className="stroke-[2.5]" />
            <span>Новое расписание</span>
          </button>
        </div>
      </div>

      {/* 4 Summary KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        {/* Monthly Recurring Expenses */}
        <div className="p-3.5 sm:p-4 rounded-2xl bg-rose-50/70 border border-rose-200/80 flex flex-col justify-between">
          <div className="flex items-center justify-between text-[11px] font-bold text-rose-800">
            <span className="uppercase tracking-wider">Регулярные расходы</span>
            <ArrowUpRight size={14} className="text-rose-600 stroke-[2.5]" />
          </div>
          <div className="text-lg sm:text-2xl font-black text-rose-700 mt-1 whitespace-nowrap">
            -{formatCurrency(metrics.monthlyExpenses, currency.symbol)}
          </div>
          <div className="text-[10px] font-semibold text-rose-800/80 mt-1 flex justify-between">
            <span>в месяц (эквивалент)</span>
            <span>{metrics.expensesCount} подписок</span>
          </div>
        </div>

        {/* Monthly Recurring Incomes */}
        <div className="p-3.5 sm:p-4 rounded-2xl bg-emerald-50/70 border border-emerald-200/80 flex flex-col justify-between">
          <div className="flex items-center justify-between text-[11px] font-bold text-emerald-800">
            <span className="uppercase tracking-wider">Регулярные доходы</span>
            <ArrowDownLeft size={14} className="text-emerald-600 stroke-[2.5]" />
          </div>
          <div className="text-lg sm:text-2xl font-black text-emerald-700 mt-1 whitespace-nowrap">
            +{formatCurrency(metrics.monthlyIncomes, currency.symbol)}
          </div>
          <div className="text-[10px] font-semibold text-emerald-800/80 mt-1 flex justify-between">
            <span>в месяц (эквивалент)</span>
            <span>{metrics.incomesCount} источников</span>
          </div>
        </div>

        {/* Recurring Net Balance */}
        <div className="p-3.5 sm:p-4 rounded-2xl bg-teal-50/70 border border-teal-200/80 flex flex-col justify-between">
          <div className="flex items-center justify-between text-[11px] font-bold text-teal-800">
            <span className="uppercase tracking-wider">Регулярное сальдо</span>
            <Repeat size={14} className="text-teal-600" />
          </div>
          <div
            className={`text-lg sm:text-2xl font-black mt-1 whitespace-nowrap ${
              metrics.netMonthly >= 0 ? 'text-teal-700' : 'text-rose-700'
            }`}
          >
            {metrics.netMonthly >= 0 ? '+' : ''}
            {formatCurrency(metrics.netMonthly, currency.symbol)}
          </div>
          <div className="text-[10px] font-semibold text-teal-800/80 mt-1">
            чистый остаток после автоплатежей
          </div>
        </div>

        {/* Nearest Due Payment */}
        <div className="p-3.5 sm:p-4 rounded-2xl bg-slate-50 border border-slate-200/90 flex flex-col justify-between">
          <div className="flex items-center justify-between text-[11px] font-bold text-slate-500">
            <span className="uppercase tracking-wider">Ближайший платеж</span>
            <Calendar size={14} className="text-slate-500" />
          </div>
          {metrics.nearestSchedule ? (
            <div>
              <div className="text-sm font-extrabold text-slate-900 truncate">
                {metrics.nearestSchedule.category}
              </div>
              <div className="text-xs font-black text-slate-700 mt-0.5">
                {formatCurrency(metrics.nearestSchedule.amount, currency.symbol)}
              </div>
            </div>
          ) : (
            <span className="text-xs font-bold text-slate-400 mt-1">Нет предстоящих</span>
          )}
          <div className="text-[10px] font-bold text-teal-700 mt-1">
            {metrics.nearestDays === 0
              ? 'Сегодня!'
              : metrics.nearestDays === 1
              ? 'Завтра'
              : metrics.nearestDays !== null
              ? `Через ${metrics.nearestDays} дн.`
              : '—'}
          </div>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pt-1">
        {/* Type Filter Chips */}
        <div className="flex items-center gap-1.5 p-1 bg-slate-100/90 rounded-2xl w-fit">
          <button
            onClick={() => setFilterType('all')}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer ${
              filterType === 'all'
                ? 'bg-white text-slate-900 shadow-2xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Все ({allTemplates.length})
          </button>
          <button
            onClick={() => setFilterType('expense')}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer ${
              filterType === 'expense'
                ? 'bg-white text-rose-700 shadow-2xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Расходы ({metrics.expensesCount})
          </button>
          <button
            onClick={() => setFilterType('income')}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer ${
              filterType === 'income'
                ? 'bg-white text-emerald-700 shadow-2xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Доходы ({metrics.incomesCount})
          </button>
        </div>

        {/* Search Input */}
        <div className="relative flex-1 sm:max-w-xs">
          <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Поиск по категории, счету..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-9 pr-3 py-1.5 bg-slate-50 border border-slate-200/90 rounded-xl text-xs font-medium focus:bg-white focus:outline-none focus:ring-2 focus:ring-teal-500 transition-all"
          />
          {searchQuery && (
            <button
              onClick={() => setSearchQuery('')}
              className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
            >
              <X size={13} />
            </button>
          )}
        </div>
      </div>

      {/* Recurring Schedules List */}
      {filteredTemplates.length === 0 ? (
        <div className="p-8 text-center bg-slate-50/80 rounded-3xl border border-dashed border-slate-200 space-y-3">
          <div className="w-12 h-12 rounded-2xl bg-teal-50 text-teal-600 mx-auto flex items-center justify-center">
            <Repeat size={24} />
          </div>
          <div>
            <h4 className="text-sm font-bold text-slate-800">
              {searchQuery ? 'Ничего не найдено' : 'Нет активных регулярных платежей'}
            </h4>
            <p className="text-xs text-slate-400 max-w-sm mx-auto mt-0.5">
              {searchQuery
                ? 'Попробуйте изменить поисковый запрос или фильтры'
                : 'Добавьте подписки, аренду, зарплату или счета за интернет, чтобы приложение автоматически вело учет по расписанию'}
            </p>
          </div>
          <button
            onClick={() => onOpenAddModal(true)}
            className="mt-2 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold rounded-xl shadow-xs transition-colors inline-flex items-center gap-1.5 cursor-pointer"
          >
            <Plus size={14} />
            <span>Создать первое расписание</span>
          </button>
        </div>
      ) : (
        <div className="space-y-3">
          {filteredTemplates.map((template) => {
            const cat = categories.find(
              (c) => c.name === template.category || c.id === template.categoryId
            );
            const wallet = wallets.find((w) => w.id === template.walletId);
            const isExpense = template.type === 'expense';
            const monthlyNormalized = normalizeToMonthlyAmount(
              template.amount,
              template.recurrenceInterval
            );
            const isSelected = selectedTemplateId === template.id;

            // Find history of transactions generated by this schedule
            const pastExecutions = transactions.filter(
              (t) => t.parentRecurringId === template.id
            );

            return (
              <div
                key={template.id}
                className="bg-white rounded-2xl border border-slate-200/90 hover:border-slate-300 p-4 transition-all shadow-2xs space-y-3"
              >
                {/* Top Row: Category icon, titles, interval badge and Amount */}
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-start gap-3 min-w-0">
                    <div
                      className="w-10 h-10 rounded-2xl flex items-center justify-center text-white shrink-0 shadow-2xs mt-0.5"
                      style={{ backgroundColor: cat?.color || '#10B981' }}
                    >
                      <DynamicIcon name={cat?.icon || 'Tag'} size={20} />
                    </div>

                    <div className="min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <h4 className="text-sm font-extrabold text-slate-900 truncate">
                          {template.category}
                        </h4>
                        {template.subcategory && (
                          <span className="text-[10px] font-semibold text-slate-500 bg-slate-100 px-2 py-0.5 rounded-md">
                            {template.subcategory}
                          </span>
                        )}
                        <span className="px-2 py-0.5 rounded-md text-[10px] font-extrabold bg-teal-50 text-teal-800 border border-teal-200">
                          {getIntervalLabel(template.recurrenceInterval || 'monthly')}
                        </span>
                      </div>

                      {template.note && (
                        <p className="text-xs text-slate-500 mt-0.5 line-clamp-1">
                          {template.note}
                        </p>
                      )}

                      <div className="flex items-center gap-3 text-[11px] text-slate-400 mt-1 flex-wrap">
                        {wallet && (
                          <span className="inline-flex items-center gap-1 font-semibold text-slate-600">
                            <WalletIcon size={12} className="text-slate-400" />
                            {wallet.name}
                          </span>
                        )}
                        <span>•</span>
                        <span className="inline-flex items-center gap-1 font-medium">
                          След. дата: <strong className="text-slate-700 font-bold">{template.recurrenceNextDate || template.date}</strong>
                        </span>
                        {getDueBadge(template.recurrenceNextDate || template.date)}
                      </div>
                    </div>
                  </div>

                  {/* Amount and Normalized equivalent */}
                  <div className="text-right shrink-0">
                    <span
                      className={`text-base sm:text-lg font-black block tracking-tight ${
                        isExpense ? 'text-rose-600' : 'text-emerald-600'
                      }`}
                    >
                      {isExpense ? '-' : '+'}
                      {formatCurrency(template.amount, currency.symbol)}
                    </span>
                    {template.recurrenceInterval !== 'monthly' && (
                      <span className="text-[10px] font-bold text-slate-400 block">
                        ≈ {formatCurrency(monthlyNormalized, currency.symbol)} / мес
                      </span>
                    )}
                  </div>
                </div>

                {/* Bottom Row: Actions Bar */}
                <div className="pt-2 border-t border-slate-100 flex items-center justify-between flex-wrap gap-2 text-xs">
                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => handleTriggerNow(template)}
                      className="px-2.5 py-1 rounded-xl bg-teal-50 hover:bg-teal-100 text-teal-800 font-bold transition-colors inline-flex items-center gap-1 cursor-pointer border border-teal-200/80 shadow-2xs"
                      title="Провести платеж прямо сейчас и перенести дату на следующий цикл"
                    >
                      <Play size={11} className="fill-teal-700 text-teal-700" />
                      <span>Провести сейчас</span>
                    </button>

                    <button
                      type="button"
                      onClick={() => onEditTransaction(template)}
                      className="px-2.5 py-1 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold transition-colors inline-flex items-center gap-1 cursor-pointer"
                      title="Редактировать сумму, интервал или категорию"
                    >
                      <Edit2 size={11} />
                      <span>Изменить</span>
                    </button>

                    <button
                      type="button"
                      onClick={() => handleToggleRecurrence(template)}
                      className="px-2 py-1 rounded-xl text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors inline-flex items-center gap-1 cursor-pointer"
                      title="Приостановить расписание"
                    >
                      <Power size={11} />
                      <span>Пауза</span>
                    </button>

                    <button
                      type="button"
                      onClick={() => handleDeleteTemplate(template.id, template.category)}
                      className="px-2 py-1 rounded-xl text-slate-400 hover:text-rose-600 hover:bg-rose-50 transition-colors inline-flex items-center gap-1 cursor-pointer"
                      title="Удалить расписание"
                    >
                      <Trash2 size={11} />
                    </button>
                  </div>

                  {pastExecutions.length > 0 && (
                    <button
                      type="button"
                      onClick={() =>
                        setSelectedTemplateId(isSelected ? null : template.id)
                      }
                      className="text-[11px] font-bold text-slate-500 hover:text-slate-800 transition-colors inline-flex items-center gap-1 cursor-pointer"
                    >
                      <span>Проведено: {pastExecutions.length} раз</span>
                      <ChevronRight
                        size={12}
                        className={`transition-transform ${isSelected ? 'rotate-90' : ''}`}
                      />
                    </button>
                  )}
                </div>

                {/* Sub-panel: History of past executions for this schedule */}
                {isSelected && pastExecutions.length > 0 && (
                  <div className="pt-2 border-t border-slate-100/80 bg-slate-50/70 p-3 rounded-xl space-y-1.5 animate-in fade-in duration-150">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block">
                      История автоматических списаний:
                    </span>
                    <div className="divide-y divide-slate-200/60 max-h-32 overflow-y-auto">
                      {pastExecutions.slice(0, 5).map((exec) => (
                        <div
                          key={exec.id}
                          className="flex items-center justify-between text-xs py-1"
                        >
                          <span className="text-slate-600 font-medium">{exec.date}</span>
                          <span
                            className={`font-black ${
                              exec.type === 'expense' ? 'text-rose-600' : 'text-emerald-600'
                            }`}
                          >
                            {exec.type === 'expense' ? '-' : '+'}
                            {formatCurrency(exec.amount, currency.symbol)}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );

  if (isModal) {
    return (
      <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs flex items-center justify-center p-3 sm:p-4 animate-in fade-in duration-200">
        <div className="bg-white w-full max-w-3xl rounded-3xl shadow-2xl max-h-[90vh] flex flex-col overflow-hidden border border-slate-200">
          <div className="p-4 sm:p-5 border-b border-slate-100 flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-xl bg-teal-50 text-teal-700 flex items-center justify-center">
                <Repeat size={18} />
              </div>
              <h3 className="text-base font-extrabold text-slate-900">
                Расписание регулярных платежей
              </h3>
            </div>
            {onClose && (
              <button
                onClick={onClose}
                className="p-1.5 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded-xl transition-colors cursor-pointer"
              >
                <X size={18} />
              </button>
            )}
          </div>
          <div className="p-4 sm:p-6 overflow-y-auto flex-1">{content}</div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-3xl border border-slate-200/90 p-5 sm:p-6 shadow-xs">
      {content}
    </div>
  );
};
