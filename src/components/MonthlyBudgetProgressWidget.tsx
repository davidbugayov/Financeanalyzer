import React, { useState, useMemo, useEffect } from 'react';
import {
  Target,
  TrendingDown,
  TrendingUp,
  AlertTriangle,
  AlertCircle,
  CheckCircle2,
  Calendar,
  SlidersHorizontal,
  Flame,
  ArrowRight,
  Edit3,
  Check,
  X,
  Sparkles,
  Info,
  ShieldCheck,
  Zap,
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { formatCurrency } from '../utils/financeCalculations';
import { DynamicIcon } from '../utils/iconHelper';

interface MonthlyBudgetProgressWidgetProps {
  onOpenBudgetModal?: (categoryId?: string) => void;
  className?: string;
}

export const MonthlyBudgetProgressWidget: React.FC<MonthlyBudgetProgressWidgetProps> = ({
  onOpenBudgetModal,
  className = '',
}) => {
  const { categories, categoryBudgets, transactions, currency } = useFinance();

  // Custom monthly goal settings stored in localStorage
  const [goalMode, setGoalMode] = useState<'sum' | 'custom'>(() => {
    return (localStorage.getItem('fa_budget_goal_mode') as 'sum' | 'custom') || 'sum';
  });

  const [customGoalAmount, setCustomGoalAmount] = useState<number>(() => {
    const saved = localStorage.getItem('fa_custom_monthly_budget_goal');
    return saved ? Number(saved) : 120000;
  });

  const [isEditingGoal, setIsEditingGoal] = useState(false);
  const [tempGoalInput, setTempGoalInput] = useState(String(customGoalAmount));
  const [showDetails, setShowDetails] = useState(false);

  // Sync state to localStorage
  useEffect(() => {
    localStorage.setItem('fa_budget_goal_mode', goalMode);
  }, [goalMode]);

  useEffect(() => {
    localStorage.setItem('fa_custom_monthly_budget_goal', String(customGoalAmount));
  }, [customGoalAmount]);

  // Current calendar month stats
  const now = useMemo(() => new Date(), []);
  const currentYear = now.getFullYear();
  const currentMonthNum = now.getMonth(); // 0-indexed
  const currentDay = now.getDate();
  const currentMonthPrefix = `${currentYear}-${String(currentMonthNum + 1).padStart(2, '0')}`;

  // Days calculations
  const daysInCurrentMonth = new Date(currentYear, currentMonthNum + 1, 0).getDate();
  const daysPassed = Math.max(1, currentDay);
  const daysRemaining = Math.max(0, daysInCurrentMonth - daysPassed);
  const calendarPacePercent = Math.min(100, Math.round((daysPassed / daysInCurrentMonth) * 100));

  const monthNamesRu = [
    'Январь',
    'Февраль',
    'Март',
    'Апрель',
    'Май',
    'Июнь',
    'Июль',
    'Август',
    'Сентябрь',
    'Октябрь',
    'Ноябрь',
    'Декабрь',
  ];
  const currentMonthName = `${monthNamesRu[currentMonthNum]} ${currentYear}`;

  // Filter only expense categories
  const expenseCategories = useMemo(() => {
    return categories.filter((c) => c.isExpense);
  }, [categories]);

  // Sum of limits from all category budgets
  const sumOfCategoryLimits = useMemo(() => {
    let sum = 0;
    expenseCategories.forEach((c) => {
      const limit =
        (categoryBudgets && categoryBudgets[c.id]) ||
        (c.key && categoryBudgets && categoryBudgets[c.key]) ||
        0;
      if (limit > 0) {
        sum += limit;
      }
    });
    return sum;
  }, [expenseCategories, categoryBudgets]);

  // Active Total Monthly Goal: either sum of category limits (if > 0) or custom goal
  const totalMonthlyGoal = useMemo(() => {
    if (goalMode === 'custom' && customGoalAmount > 0) {
      return customGoalAmount;
    }
    return sumOfCategoryLimits > 0 ? sumOfCategoryLimits : customGoalAmount || 100000;
  }, [goalMode, customGoalAmount, sumOfCategoryLimits]);

  // Compute actual expenses for the current month
  const { totalSpentThisMonth, currentMonthExpensesCount, categorySpendMap } = useMemo(() => {
    let totalSpent = 0;
    let count = 0;
    const spendMap: Record<string, number> = {};

    transactions.forEach((tx) => {
      if (tx.type !== 'expense') return;
      if (!tx.date || !tx.date.startsWith(currentMonthPrefix)) return;

      totalSpent += tx.amount;
      count += 1;

      // Group by category
      const matchedCat = expenseCategories.find(
        (c) => (tx.categoryId && c.id === tx.categoryId) || c.name === tx.category
      );
      const catKey = matchedCat ? matchedCat.id : tx.category || 'other';
      spendMap[catKey] = (spendMap[catKey] || 0) + tx.amount;
    });

    return {
      totalSpentThisMonth: totalSpent,
      currentMonthExpensesCount: count,
      categorySpendMap: spendMap,
    };
  }, [transactions, currentMonthPrefix, expenseCategories]);

  // Core Progress Metrics
  const spentPercent = totalMonthlyGoal > 0 ? Math.round((totalSpentThisMonth / totalMonthlyGoal) * 100) : 0;
  const remainingBudget = totalMonthlyGoal - totalSpentThisMonth;
  const isOverBudget = remainingBudget < 0;
  const overBudgetAmount = isOverBudget ? Math.abs(remainingBudget) : 0;

  // Burn Rates
  const actualDailyAvg = daysPassed > 0 ? Math.round(totalSpentThisMonth / daysPassed) : 0;
  const recommendedDailyAllowance =
    daysRemaining > 0 && !isOverBudget ? Math.round(remainingBudget / daysRemaining) : 0;

  // Projected Month-End Spend based on actual daily rate
  const projectedMonthEndSpend = Math.round(actualDailyAvg * daysInCurrentMonth);
  const projectedDifferenceFromGoal = projectedMonthEndSpend - totalMonthlyGoal;

  // Pace comparison: how spending pace compares to calendar timeline
  const paceDifference = spentPercent - calendarPacePercent; // positive = spending faster, negative = spending slower

  const paceStatus = useMemo(() => {
    if (isOverBudget) {
      return {
        label: 'Лимит превышен',
        desc: `Превышение цели на ${formatCurrency(overBudgetAmount, currency.symbol)}`,
        badgeClass: 'bg-rose-100 text-rose-800 border-rose-200',
        textClass: 'text-rose-600',
        icon: AlertTriangle,
        type: 'danger' as const,
      };
    }
    if (spentPercent >= 90) {
      return {
        label: 'Критическая зона (>90%)',
        desc: `Осталось всего ${formatCurrency(remainingBudget, currency.symbol)} на ${daysRemaining} дн.`,
        badgeClass: 'bg-orange-100 text-orange-900 border-orange-200',
        textClass: 'text-orange-600',
        icon: AlertCircle,
        type: 'warning' as const,
      };
    }
    if (paceDifference > 8) {
      return {
        label: 'Опережение графика',
        desc: `Расход на ${paceDifference}% быстрее календаря (прошло ${calendarPacePercent}% месяца)`,
        badgeClass: 'bg-amber-100 text-amber-900 border-amber-200',
        textClass: 'text-amber-600',
        icon: Flame,
        type: 'caution' as const,
      };
    }
    if (paceDifference < -8) {
      return {
        label: 'Отличная экономия',
        desc: `Расход на ${Math.abs(paceDifference)}% ниже календаря. Запас бюджета в норме`,
        badgeClass: 'bg-emerald-100 text-emerald-800 border-emerald-200',
        textClass: 'text-emerald-600',
        icon: CheckCircle2,
        type: 'success' as const,
      };
    }
    return {
      label: 'В рамках графика',
      desc: `Темп трат соответствует календарю (прошло ${calendarPacePercent}% месяца)`,
      badgeClass: 'bg-teal-100 text-teal-800 border-teal-200',
      textClass: 'text-teal-600',
      icon: ShieldCheck,
      type: 'normal' as const,
    };
  }, [
    isOverBudget,
    spentPercent,
    paceDifference,
    calendarPacePercent,
    remainingBudget,
    overBudgetAmount,
    currency.symbol,
    daysRemaining,
  ]);

  // Top spending categories for this month
  const topCategories = useMemo(() => {
    return expenseCategories
      .map((cat) => {
        const spent = categorySpendMap[cat.id] || 0;
        const limit =
          (categoryBudgets && categoryBudgets[cat.id]) ||
          (cat.key && categoryBudgets && categoryBudgets[cat.key]) ||
          0;
        const catPercent = limit > 0 ? Math.round((spent / limit) * 100) : null;
        const shareOfTotalSpent =
          totalSpentThisMonth > 0 ? Math.round((spent / totalSpentThisMonth) * 100) : 0;

        return {
          cat,
          spent,
          limit,
          catPercent,
          shareOfTotalSpent,
        };
      })
      .filter((item) => item.spent > 0)
      .sort((a, b) => b.spent - a.spent)
      .slice(0, 4);
  }, [expenseCategories, categorySpendMap, categoryBudgets, totalSpentThisMonth]);

  const handleSaveCustomGoal = () => {
    const parsed = parseFloat(tempGoalInput.replace(/\s+/g, ''));
    if (!isNaN(parsed) && parsed > 0) {
      setCustomGoalAmount(parsed);
      setGoalMode('custom');
      setIsEditingGoal(false);
    }
  };

  // Progress bar styling
  const clampedProgress = Math.min(100, Math.max(0, spentPercent));
  const progressGradient = useMemo(() => {
    if (spentPercent > 100) return 'from-rose-500 via-red-500 to-rose-600';
    if (spentPercent >= 90) return 'from-amber-500 via-orange-500 to-rose-500';
    if (spentPercent >= 75) return 'from-amber-400 via-amber-500 to-orange-400';
    return 'from-emerald-400 via-teal-500 to-emerald-500';
  }, [spentPercent]);

  return (
    <div
      className={`bg-white rounded-3xl border border-slate-200/90 p-5 sm:p-6 shadow-xs space-y-5 transition-all ${className}`}
    >
      {/* Header with Title, Month Badge and Goal Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-slate-100">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-emerald-500 to-teal-400 text-white flex items-center justify-center shadow-xs shrink-0">
            <Target size={22} className="stroke-[2.5]" />
          </div>
          <div>
            <div className="flex items-center gap-2 flex-wrap">
              <h3 className="text-base sm:text-lg font-black text-slate-900 tracking-tight">
                Прогресс месячного бюджета
              </h3>
              <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-slate-100 text-slate-700 border border-slate-200/80 inline-flex items-center gap-1">
                <Calendar size={11} className="text-slate-500" />
                {currentMonthName}
              </span>
            </div>
            <p className="text-xs text-slate-500 mt-0.5">
              Исполнение трат относительно общей финансовой цели на месяц
            </p>
          </div>
        </div>

        {/* Action Controls & Settings */}
        <div className="flex items-center gap-2 self-start sm:self-auto flex-wrap">
          <button
            onClick={() => setIsEditingGoal((prev) => !prev)}
            className="px-3 py-1.5 rounded-xl bg-slate-50 hover:bg-slate-100 border border-slate-200/80 text-slate-700 font-bold text-xs inline-flex items-center gap-1.5 transition-colors cursor-pointer"
            title="Настроить общую цель"
          >
            <Edit3 size={13} className="text-slate-500" />
            <span>Цель: {goalMode === 'sum' ? 'Сумма лимитов' : 'Своя'}</span>
          </button>

          {onOpenBudgetModal && (
            <button
              onClick={() => onOpenBudgetModal()}
              className="px-3 py-1.5 rounded-xl bg-emerald-50 hover:bg-emerald-100/80 border border-emerald-200/80 text-emerald-800 font-bold text-xs inline-flex items-center gap-1.5 transition-colors cursor-pointer"
            >
              <SlidersHorizontal size={13} className="text-emerald-700" />
              <span>Лимиты статей</span>
            </button>
          )}
        </div>
      </div>

      {/* Goal Edit Panel (Collapsible) */}
      {isEditingGoal && (
        <div className="p-4 rounded-2xl bg-gradient-to-r from-slate-50 to-emerald-50/40 border border-emerald-200/80 space-y-3 animate-in fade-in duration-200">
          <div className="flex items-center justify-between">
            <span className="text-xs font-black uppercase tracking-wider text-slate-700 flex items-center gap-1.5">
              <Sparkles size={14} className="text-emerald-600" />
              Настройка целевого бюджета на месяц
            </span>
            <button
              onClick={() => setIsEditingGoal(false)}
              className="text-slate-400 hover:text-slate-600 p-1"
            >
              <X size={15} />
            </button>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-1">
            {/* Mode 1: Sum of category budgets */}
            <div
              onClick={() => setGoalMode('sum')}
              className={`p-3 rounded-xl border text-left cursor-pointer transition-all ${
                goalMode === 'sum'
                  ? 'bg-white border-emerald-500 ring-2 ring-emerald-500/20 shadow-xs'
                  : 'bg-white/60 border-slate-200 hover:bg-white'
              }`}
            >
              <div className="flex items-center justify-between mb-1">
                <span className="text-xs font-extrabold text-slate-800">
                  Сумма лимитов категорий
                </span>
                {goalMode === 'sum' && <Check size={14} className="text-emerald-600" />}
              </div>
              <p className="text-[11px] text-slate-500 mb-1.5">
                Автоматически складывает лимиты всех категорий расходов ({expenseCategories.length})
              </p>
              <span className="text-sm font-black text-emerald-700">
                {formatCurrency(sumOfCategoryLimits, currency.symbol)}
              </span>
            </div>

            {/* Mode 2: Custom goal */}
            <div
              onClick={() => setGoalMode('custom')}
              className={`p-3 rounded-xl border text-left cursor-pointer transition-all ${
                goalMode === 'custom'
                  ? 'bg-white border-emerald-500 ring-2 ring-emerald-500/20 shadow-xs'
                  : 'bg-white/60 border-slate-200 hover:bg-white'
              }`}
            >
              <div className="flex items-center justify-between mb-1">
                <span className="text-xs font-extrabold text-slate-800">
                  Фиксированная общая цель
                </span>
                {goalMode === 'custom' && <Check size={14} className="text-emerald-600" />}
              </div>
              <p className="text-[11px] text-slate-500 mb-1.5">
                Задать единую глобальную сумму на месяц независимо от категорий
              </p>
              <div className="flex items-center gap-2 mt-1" onClick={(e) => e.stopPropagation()}>
                <input
                  type="text"
                  value={tempGoalInput}
                  onChange={(e) => setTempGoalInput(e.target.value)}
                  placeholder="Сумма цели..."
                  className="w-full px-2.5 py-1 text-xs font-bold rounded-lg border border-slate-300 focus:outline-hidden focus:border-emerald-500 bg-white"
                />
                <button
                  onClick={handleSaveCustomGoal}
                  className="px-2.5 py-1 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs shrink-0 cursor-pointer"
                >
                  OK
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Main Budget Progress Section: Headline Figures & Status */}
      <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-3">
        <div>
          <span className="text-xs font-bold uppercase tracking-wider text-slate-500 block mb-1">
            Потрачено из месячной цели
          </span>
          <div className="flex items-baseline gap-2.5 flex-wrap">
            <span
              className={`text-2xl sm:text-4xl font-black tracking-tight whitespace-nowrap ${
                isOverBudget ? 'text-rose-600' : 'text-slate-900'
              }`}
            >
              {formatCurrency(totalSpentThisMonth, currency.symbol)}
            </span>
            <span className="text-slate-400 font-bold text-base sm:text-lg">/</span>
            <span className="text-slate-600 font-extrabold text-base sm:text-xl whitespace-nowrap">
              {formatCurrency(totalMonthlyGoal, currency.symbol)}
            </span>
            <span className="text-[11px] font-bold text-slate-400">
              ({currentMonthExpensesCount} операций)
            </span>
          </div>
        </div>

        {/* Dynamic Status Badge */}
        <div className="flex items-center gap-2 flex-wrap">
          <span
            className={`px-3 py-1.5 rounded-xl border text-xs font-black inline-flex items-center gap-1.5 shadow-2xs ${paceStatus.badgeClass}`}
          >
            <paceStatus.icon size={15} className="shrink-0 stroke-[2.5]" />
            <span>{spentPercent}% израсходовано</span>
          </span>
          <span
            className={`px-2.5 py-1.5 rounded-xl text-xs font-bold border ${
              isOverBudget
                ? 'bg-rose-50 text-rose-700 border-rose-200'
                : 'bg-slate-100 text-slate-700 border-slate-200'
            }`}
          >
            {isOverBudget
              ? `Перерасход: ${formatCurrency(overBudgetAmount, currency.symbol)}`
              : `Остаток: ${formatCurrency(remainingBudget, currency.symbol)}`}
          </span>
        </div>
      </div>

      {/* Interactive & Detailed Progress Bar */}
      <div className="space-y-2 pt-1">
        {/* Progress Bar Container */}
        <div className="relative">
          {/* Main Track */}
          <div className="h-5 sm:h-6 w-full bg-slate-100 rounded-full overflow-hidden p-0.5 border border-slate-200/90 shadow-inner">
            <div
              className={`h-full rounded-full bg-gradient-to-r ${progressGradient} transition-all duration-700 relative`}
              style={{ width: `${clampedProgress}%` }}
            >
              {/* Subtle light shimmer highlight */}
              <div className="absolute inset-0 bg-white/20 rounded-full" />
            </div>
          </div>

          {/* Current Day / Calendar Pace Marker */}
          {calendarPacePercent > 0 && calendarPacePercent < 100 && (
            <div
              className="absolute top-0 bottom-0 pointer-events-none transition-all duration-500 z-10 flex flex-col items-center"
              style={{ left: `${calendarPacePercent}%` }}
            >
              {/* Vertical line indicator through bar */}
              <div className="w-0.5 h-full bg-slate-800 shadow-sm" />
            </div>
          )}
        </div>

        {/* Scale & Timeline Legend Below Bar */}
        <div className="flex items-center justify-between text-[11px] font-bold text-slate-500 px-1 pt-0.5">
          <span className="flex items-center gap-1 text-slate-400">
            <span>0%</span>
            <span className="hidden sm:inline">(День 1)</span>
          </span>

          {/* Calendar Day Pace Indicator */}
          <span className="inline-flex items-center gap-1 bg-slate-100/90 text-slate-700 px-2 py-0.5 rounded-md border border-slate-200/60 shadow-2xs">
            <span className="w-1.5 h-1.5 rounded-full bg-slate-800 animate-pulse" />
            <span>Календарный темп: {calendarPacePercent}%</span>
            <span className="text-slate-400 hidden sm:inline">
              (День {daysPassed} из {daysInCurrentMonth})
            </span>
          </span>

          <span
            className={`font-black ${
              spentPercent > 100
                ? 'text-rose-600'
                : spentPercent >= 90
                ? 'text-amber-600'
                : 'text-slate-600'
            }`}
          >
            {spentPercent}% цели
            {spentPercent > 100 && ' (+ ' + (spentPercent - 100) + '% сверх)'}
          </span>
        </div>

        {/* Pace Status Description Alert */}
        <div
          className={`flex items-start gap-2.5 p-3 rounded-2xl border text-xs ${
            paceStatus.type === 'danger'
              ? 'bg-rose-50/70 border-rose-200/90 text-rose-900'
              : paceStatus.type === 'warning'
              ? 'bg-orange-50/70 border-orange-200/90 text-orange-900'
              : paceStatus.type === 'caution'
              ? 'bg-amber-50/70 border-amber-200/90 text-amber-900'
              : 'bg-emerald-50/60 border-emerald-200/90 text-emerald-950'
          }`}
        >
          <paceStatus.icon size={16} className={`shrink-0 mt-0.5 stroke-[2.5] ${paceStatus.textClass}`} />
          <div className="flex-1 flex flex-col sm:flex-row sm:items-center justify-between gap-1">
            <div>
              <span className="font-extrabold mr-1.5">{paceStatus.label}:</span>
              <span className="font-medium text-slate-700">{paceStatus.desc}</span>
            </div>
            {daysRemaining > 0 && !isOverBudget && (
              <span className="text-[11px] font-black text-slate-600 bg-white/80 px-2 py-0.5 rounded-lg border border-slate-200/60 shrink-0 self-start sm:self-auto">
                Осталось {daysRemaining}{' '}
                {daysRemaining === 1
                  ? 'день'
                  : daysRemaining > 1 && daysRemaining < 5
                  ? 'дня'
                  : 'дней'}
              </span>
            )}
          </div>
        </div>
      </div>

      {/* Key Supporting Metrics Grid (4 Cards) */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-2.5 sm:gap-3">
        {/* Metric 1: Budget Balance Left / Overrun */}
        <div className="p-3 sm:p-3.5 rounded-2xl bg-slate-50/80 border border-slate-200/80 flex flex-col justify-between">
          <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">
            {isOverBudget ? 'Перерасход цели' : 'Доступно до конца месяца'}
          </span>
          <div
            className={`text-base sm:text-xl font-black mt-1 ${
              isOverBudget ? 'text-rose-600' : 'text-emerald-700'
            }`}
          >
            {isOverBudget ? '-' : '+'}
            {formatCurrency(isOverBudget ? overBudgetAmount : remainingBudget, currency.symbol)}
          </div>
          <span className="text-[10px] font-semibold text-slate-400 mt-1">
            {isOverBudget
              ? 'Рекомендуется пауза трат'
              : `${100 - spentPercent}% свободного бюджета`}
          </span>
        </div>

        {/* Metric 2: Recommended Daily Allowance */}
        <div className="p-3 sm:p-3.5 rounded-2xl bg-slate-50/80 border border-slate-200/80 flex flex-col justify-between">
          <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500 flex items-center gap-1">
            <Zap size={12} className="text-amber-500" />
            <span>Лимит в день (норма)</span>
          </span>
          <div className="text-base sm:text-xl font-black text-slate-900 mt-1">
            {daysRemaining > 0 && !isOverBudget
              ? formatCurrency(recommendedDailyAllowance, currency.symbol)
              : isOverBudget
              ? '0 ' + currency.symbol
              : '—'}
          </div>
          <span className="text-[10px] font-semibold text-slate-400 mt-1">
            {daysRemaining > 0 && !isOverBudget
              ? `на оставшиеся ${daysRemaining} дн.`
              : isOverBudget
              ? 'Бюджет исчерпан'
              : 'Последний день месяца'}
          </span>
        </div>

        {/* Metric 3: Actual Daily Burn Rate */}
        <div className="p-3 sm:p-3.5 rounded-2xl bg-slate-50/80 border border-slate-200/80 flex flex-col justify-between">
          <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">
            Факт. расход в день
          </span>
          <div className="text-base sm:text-xl font-black text-slate-900 mt-1">
            {formatCurrency(actualDailyAvg, currency.symbol)}
          </div>
          <span className="text-[10px] font-semibold text-slate-400 mt-1">
            в среднем за {daysPassed} дн.
          </span>
        </div>

        {/* Metric 4: Projected Month-End Total */}
        <div className="p-3 sm:p-3.5 rounded-2xl bg-slate-50/80 border border-slate-200/80 flex flex-col justify-between">
          <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">
            Прогноз итога месяца
          </span>
          <div
            className={`text-base sm:text-xl font-black mt-1 ${
              projectedDifferenceFromGoal > 0 ? 'text-rose-600' : 'text-emerald-700'
            }`}
          >
            {formatCurrency(projectedMonthEndSpend, currency.symbol)}
          </div>
          <span className="text-[10px] font-semibold text-slate-400 mt-1">
            {projectedDifferenceFromGoal > 0
              ? `Превышение на ${formatCurrency(projectedDifferenceFromGoal, currency.symbol)}`
              : `Экономия ${formatCurrency(Math.abs(projectedDifferenceFromGoal), currency.symbol)}`}
          </span>
        </div>
      </div>

      {/* Top Consuming Categories & Deep Dive Toggle */}
      {topCategories.length > 0 && (
        <div className="pt-2 border-t border-slate-100 space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-500 flex items-center gap-1.5">
              <span>Главные статьи расхода этого месяца</span>
            </span>
            <button
              onClick={() => setShowDetails((prev) => !prev)}
              className="text-xs font-bold text-emerald-700 hover:text-emerald-800 transition-colors inline-flex items-center gap-1 cursor-pointer"
            >
              <span>{showDetails ? 'Скрыть статьи' : 'Показать долю категорий'}</span>
              <ArrowRight
                size={13}
                className={`transition-transform duration-200 ${showDetails ? 'rotate-90' : ''}`}
              />
            </button>
          </div>

          {showDetails && (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5 pt-1 animate-in fade-in duration-200">
              {topCategories.map(({ cat, spent, limit, catPercent, shareOfTotalSpent }) => {
                const isCatOver = limit > 0 && spent > limit;
                const catBarPercent = limit > 0 ? Math.min(100, Math.round((spent / limit) * 100)) : 100;

                return (
                  <div
                    key={cat.id}
                    onClick={() => onOpenBudgetModal && onOpenBudgetModal(cat.id)}
                    className="p-3 rounded-2xl bg-slate-50/70 hover:bg-slate-100/70 border border-slate-200/70 transition-all cursor-pointer space-y-2"
                  >
                    <div className="flex items-center justify-between gap-2">
                      <div className="flex items-center gap-2 min-w-0">
                        <div
                          className="w-7 h-7 rounded-xl flex items-center justify-center shrink-0 shadow-2xs"
                          style={{
                            backgroundColor: cat.color ? `${cat.color}20` : '#10B98120',
                            color: cat.color || '#10B981',
                          }}
                        >
                          <DynamicIcon name={cat.icon} size={15} />
                        </div>
                        <div className="truncate">
                          <span className="text-xs font-bold text-slate-900 block truncate">
                            {cat.name}
                          </span>
                          <span className="text-[10px] text-slate-400 block">
                            {shareOfTotalSpent}% от всех расходов
                          </span>
                        </div>
                      </div>

                      <div className="text-right shrink-0">
                        <span className="text-xs font-black text-slate-900 block">
                          {formatCurrency(spent, currency.symbol)}
                        </span>
                        {limit > 0 ? (
                          <span
                            className={`text-[10px] font-bold block ${
                              isCatOver ? 'text-rose-600' : 'text-slate-500'
                            }`}
                          >
                            из {formatCurrency(limit, currency.symbol)} ({catPercent}%)
                          </span>
                        ) : (
                          <span className="text-[10px] text-slate-400 block">без лимита</span>
                        )}
                      </div>
                    </div>

                    {/* Category mini bar */}
                    {limit > 0 && (
                      <div className="h-1.5 w-full bg-slate-200 rounded-full overflow-hidden">
                        <div
                          className={`h-full rounded-full ${
                            isCatOver
                              ? 'bg-rose-500'
                              : catPercent && catPercent >= 80
                              ? 'bg-amber-500'
                              : 'bg-emerald-500'
                          }`}
                          style={{ width: `${catBarPercent}%` }}
                        />
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}
    </div>
  );
};
