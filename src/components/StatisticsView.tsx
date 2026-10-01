import React, { useState, useMemo } from 'react';
import {
  PieChart as PieChartIcon,
  TrendingUp,
  TrendingDown,
  Minus,
  Award,
  Calendar,
  AlertCircle,
  CheckCircle2,
  HelpCircle,
  Globe,
  MapPin,
  ArrowDownLeft,
  ArrowUpRight,
  SlidersHorizontal,
  AlertTriangle,
  ShieldCheck,
  Plus,
} from 'lucide-react';
import {
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
  Tooltip,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Legend,
} from 'recharts';
import { useFinance } from '../context/FinanceContext';
import { PeriodType } from '../types';
import {
  calculateTotals,
  calculateFinancialHealth,
  formatCurrency,
} from '../utils/financeCalculations';
import { DynamicIcon } from '../utils/iconHelper';
import { MonthlyExpensesAnalytics } from './MonthlyExpensesAnalytics';
import { FinancialBalanceWidget } from './FinancialBalanceWidget';
import { MonthlyBudgetProgressWidget } from './MonthlyBudgetProgressWidget';
import { IncomeExpenseTrendLineChart } from './IncomeExpenseTrendLineChart';
import { CategoryDonutChart } from './CategoryDonutChart';
import { BudgetModal } from './BudgetModal';
import { PersonalizedFinancialAdvice } from './PersonalizedFinancialAdvice';
import { SpendingForecastLineChart } from './SpendingForecastLineChart';
import { SpendingForecast } from './SpendingForecast';
import {
  DateRangePicker,
  DateRange,
  computePresetRange,
  formatHumanDateRange,
} from './DateRangePicker';

function getPreviousPeriodRange(range: DateRange): { startDate: string; endDate: string } {
  const now = new Date();
  const formatIso = (d: Date) =>
    `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;

  if (range.preset === 'today') {
    const d = new Date(now);
    d.setDate(d.getDate() - 1);
    const y = formatIso(d);
    return { startDate: y, endDate: y };
  }
  if (range.preset === 'yesterday') {
    const d = new Date(now);
    d.setDate(d.getDate() - 2);
    const y = formatIso(d);
    return { startDate: y, endDate: y };
  }
  if (range.preset === 'last7') {
    const end = new Date(now);
    end.setDate(now.getDate() - 7);
    const start = new Date(end);
    start.setDate(end.getDate() - 6);
    return { startDate: formatIso(start), endDate: formatIso(end) };
  }
  if (range.preset === 'last30') {
    const end = new Date(now);
    end.setDate(now.getDate() - 30);
    const start = new Date(end);
    start.setDate(end.getDate() - 29);
    return { startDate: formatIso(start), endDate: formatIso(end) };
  }
  if (range.preset === 'this_month') {
    const prevYear = now.getMonth() === 0 ? now.getFullYear() - 1 : now.getFullYear();
    const prevMonth = now.getMonth() === 0 ? 11 : now.getMonth() - 1;
    const start = new Date(prevYear, prevMonth, 1);
    const end = new Date(prevYear, prevMonth + 1, 0);
    return { startDate: formatIso(start), endDate: formatIso(end) };
  }
  if (range.preset === 'last_month') {
    const prevYear = now.getMonth() <= 1 ? now.getFullYear() - 1 : now.getFullYear();
    const prevMonth = now.getMonth() === 0 ? 10 : now.getMonth() === 1 ? 11 : now.getMonth() - 2;
    const start = new Date(prevYear, prevMonth, 1);
    const end = new Date(prevYear, prevMonth + 1, 0);
    return { startDate: formatIso(start), endDate: formatIso(end) };
  }
  if (range.preset === 'this_year') {
    const prevYear = now.getFullYear() - 1;
    return {
      startDate: `${prevYear}-01-01`,
      endDate: `${prevYear}-12-31`,
    };
  }
  if (range.preset === 'all') {
    const sixMonthsAgo = new Date(now);
    sixMonthsAgo.setMonth(now.getMonth() - 6);
    const twelveMonthsAgo = new Date(now);
    twelveMonthsAgo.setMonth(now.getMonth() - 12);
    return {
      startDate: formatIso(twelveMonthsAgo),
      endDate: formatIso(sixMonthsAgo),
    };
  }

  // Custom date range: shift backwards by exact duration in days
  if (range.startDate && range.endDate) {
    const [sy, sm, sd] = range.startDate.split('-').map(Number);
    const [ey, em, ed] = range.endDate.split('-').map(Number);
    const startD = new Date(sy, sm - 1, sd);
    const endD = new Date(ey, em - 1, ed);
    const diffDays = Math.max(1, Math.round((endD.getTime() - startD.getTime()) / (1000 * 60 * 60 * 24))) + 1;

    const prevEnd = new Date(startD);
    prevEnd.setDate(startD.getDate() - 1);
    const prevStart = new Date(prevEnd);
    prevStart.setDate(prevEnd.getDate() - (diffDays - 1));

    return {
      startDate: formatIso(prevStart),
      endDate: formatIso(prevEnd),
    };
  }

  return { startDate: '', endDate: '' };
}

export const StatisticsView: React.FC = () => {
  const { transactions, wallets, categories, categoryBudgets, currency } = useFinance();
  const [dateRange, setDateRange] = useState<DateRange>(() => {
    const { startDate, endDate } = computePresetRange('this_month');
    return {
      preset: 'this_month',
      startDate,
      endDate,
    };
  });
  const [isBudgetModalOpen, setIsBudgetModalOpen] = useState(false);
  const [selectedBudgetCategoryId, setSelectedBudgetCategoryId] = useState<string | null>(null);
  const [hoveredChartCategoryId, setHoveredChartCategoryId] = useState<string | null>(null);
  const [budgetFilter, setBudgetFilter] = useState<'all' | 'with_budget' | 'over_budget'>('all');

  const handleOpenBudgetModal = (categoryId?: string) => {
    setSelectedBudgetCategoryId(categoryId || null);
    setIsBudgetModalOpen(true);
  };

  // Map date range to coarse period type for widgets expecting PeriodType
  const effectivePeriod: PeriodType = useMemo(() => {
    if (dateRange.preset === 'today' || dateRange.preset === 'yesterday') return 'day';
    if (dateRange.preset === 'last7') return 'week';
    if (
      dateRange.preset === 'this_month' ||
      dateRange.preset === 'last_month' ||
      dateRange.preset === 'last30'
    )
      return 'month';
    if (dateRange.preset === 'this_year') return 'year';
    if (dateRange.preset === 'all') return 'all';

    // Custom date range
    if (dateRange.startDate && dateRange.endDate) {
      const s = new Date(dateRange.startDate).getTime();
      const e = new Date(dateRange.endDate).getTime();
      const days = Math.max(1, Math.round(Math.abs(e - s) / (1000 * 60 * 60 * 24))) + 1;
      if (days <= 1) return 'day';
      if (days <= 10) return 'week';
      if (days <= 45) return 'month';
      if (days <= 400) return 'year';
      return 'all';
    }
    return 'month';
  }, [dateRange]);

  // Filter transactions dynamically by the selected date range
  const filteredTxs = useMemo(() => {
    return transactions.filter((t) => {
      if (dateRange.startDate && t.date < dateRange.startDate) return false;
      if (dateRange.endDate && t.date > dateRange.endDate) return false;
      return true;
    });
  }, [transactions, dateRange]);

  // Calculate previous date range for dynamic comparison
  const prevDateRange = useMemo(() => {
    return getPreviousPeriodRange(dateRange);
  }, [dateRange]);

  // Filter transactions for previous period (for comparison)
  const prevFilteredTxs = useMemo(() => {
    if (!prevDateRange.startDate && !prevDateRange.endDate) {
      return [];
    }
    return transactions.filter((t) => {
      if (prevDateRange.startDate && t.date < prevDateRange.startDate) return false;
      if (prevDateRange.endDate && t.date > prevDateRange.endDate) return false;
      return true;
    });
  }, [transactions, prevDateRange]);

  const { income, expense, net, savingsRate } = calculateTotals(filteredTxs);
  const { income: prevIncome, expense: prevExpense } = useMemo(
    () => calculateTotals(prevFilteredTxs),
    [prevFilteredTxs]
  );
  const health = calculateFinancialHealth(filteredTxs, wallets);

  // Human comparison text based on active dateRange
  const comparisonPeriodDesc = useMemo(() => {
    switch (dateRange.preset) {
      case 'today':
        return { compareText: 'чем вчера', periodLabel: 'вчера' };
      case 'yesterday':
        return { compareText: 'чем позавчера', periodLabel: 'позавчера' };
      case 'last7':
        return { compareText: 'чем за пред. 7 дней', periodLabel: 'пред. 7 дней' };
      case 'last30':
        return { compareText: 'чем за пред. 30 дней', periodLabel: 'пред. 30 дней' };
      case 'this_month':
        return { compareText: 'чем в прошлом месяце', periodLabel: 'прошлым месяцем' };
      case 'last_month':
        return { compareText: 'чем в позапрошлом месяце', periodLabel: 'позапрошлым месяцем' };
      case 'this_year':
        return { compareText: 'чем в прошлом году', periodLabel: 'прошлым годом' };
      case 'all':
        return { compareText: 'чем в прошлом периоде', periodLabel: 'прошлым периодом' };
      case 'custom':
      default:
        return { compareText: 'чем за прошлый период', periodLabel: 'прошлым периодом' };
    }
  }, [dateRange.preset]);

  // Indicators comparing expenses with previous period
  const expenseComparison = useMemo(() => {
    const desc = comparisonPeriodDesc;

    if (prevExpense === 0) {
      if (expense === 0) {
        return {
          hasData: false,
          label: 'Нет расходов в периодах',
          shortLabel: 'Без трат',
          badgeText: '0%',
          badgeClass: 'bg-slate-100 text-slate-600 border border-slate-200',
          textClass: 'text-slate-500',
          direction: 'same' as const,
          detailText: null,
        };
      }
      return {
        hasData: false,
        label: `Новые расходы (${formatCurrency(expense, currency.symbol)})`,
        shortLabel: 'Новые траты',
        badgeText: 'Новые',
        badgeClass: 'bg-rose-50 text-rose-700 border border-rose-200',
        textClass: 'text-rose-600',
        direction: 'up' as const,
        detailText: `+${formatCurrency(expense, currency.symbol)} к прошлому периоду`,
      };
    }

    const diff = expense - prevExpense;
    const absDiff = Math.abs(diff);
    const percent = Math.round((absDiff / prevExpense) * 100);

    if (diff < 0) {
      // Expenses are LOWER -> POSITIVE financial outcome
      return {
        hasData: true,
        percent,
        diff,
        absDiff,
        direction: 'down' as const,
        label: `на ${percent}% меньше, ${desc.compareText}`,
        shortLabel: `-${percent}% ${desc.compareText}`,
        detailText: `экономия ${formatCurrency(absDiff, currency.symbol)}`,
        badgeText: `↓ ${percent}%`,
        badgeClass: 'bg-emerald-50 text-emerald-800 border border-emerald-200/90 shadow-2xs',
        textClass: 'text-emerald-700',
        indicatorTone: 'positive',
      };
    } else if (diff > 0) {
      // Expenses are HIGHER
      return {
        hasData: true,
        percent,
        diff,
        absDiff,
        direction: 'up' as const,
        label: `на ${percent}% больше, ${desc.compareText}`,
        shortLabel: `+${percent}% ${desc.compareText}`,
        detailText: `+${formatCurrency(absDiff, currency.symbol)} к расходам`,
        badgeText: `↑ ${percent}%`,
        badgeClass: 'bg-rose-50 text-rose-800 border border-rose-200/90 shadow-2xs',
        textClass: 'text-rose-700',
        indicatorTone: 'negative',
      };
    } else {
      return {
        hasData: true,
        percent: 0,
        diff: 0,
        absDiff: 0,
        direction: 'same' as const,
        label: `на том же уровне, ${desc.compareText}`,
        shortLabel: `0% ${desc.compareText}`,
        detailText: 'без изменений к прошлому периоду',
        badgeText: '0%',
        badgeClass: 'bg-slate-100 text-slate-700 border border-slate-200',
        textClass: 'text-slate-600',
        indicatorTone: 'neutral',
      };
    }
  }, [expense, prevExpense, comparisonPeriodDesc, currency]);

  // Indicators comparing incomes with previous period
  const incomeComparison = useMemo(() => {
    const desc = comparisonPeriodDesc;

    if (prevIncome === 0) {
      return {
        hasData: false,
        label: income > 0 ? 'Новые поступления' : 'Без поступлений',
        badgeText: income > 0 ? '+100%' : '0%',
        badgeClass: 'bg-slate-100 text-slate-600 border border-slate-200',
        textClass: 'text-slate-500',
        direction: 'same' as const,
      };
    }

    const diff = income - prevIncome;
    const absDiff = Math.abs(diff);
    const percent = Math.round((absDiff / prevIncome) * 100);

    if (diff > 0) {
      return {
        hasData: true,
        percent,
        diff,
        direction: 'up' as const,
        label: `на ${percent}% больше, ${desc.compareText}`,
        badgeText: `↑ ${percent}%`,
        badgeClass: 'bg-emerald-50 text-emerald-800 border border-emerald-200/90 shadow-2xs',
        textClass: 'text-emerald-700',
      };
    } else if (diff < 0) {
      return {
        hasData: true,
        percent,
        diff,
        direction: 'down' as const,
        label: `на ${percent}% меньше, ${desc.compareText}`,
        badgeText: `↓ ${percent}%`,
        badgeClass: 'bg-amber-50 text-amber-800 border border-amber-200/90 shadow-2xs',
        textClass: 'text-amber-700',
      };
    } else {
      return {
        hasData: true,
        percent: 0,
        diff: 0,
        direction: 'same' as const,
        label: `на том же уровне, ${desc.compareText}`,
        badgeText: '0%',
        badgeClass: 'bg-slate-100 text-slate-700 border border-slate-200',
        textClass: 'text-slate-600',
      };
    }
  }, [income, prevIncome, comparisonPeriodDesc]);

  // Helper to adjust monthly budget limit dynamically to the selected date range
  const getPeriodBudgetLimit = (monthlyLimit: number): number => {
    if (!monthlyLimit || monthlyLimit <= 0) return 0;
    if (dateRange.preset === 'this_month' || dateRange.preset === 'last_month') return monthlyLimit;
    if (dateRange.preset === 'this_year') return monthlyLimit * 12;
    if (dateRange.preset === 'today' || dateRange.preset === 'yesterday')
      return Math.round(monthlyLimit / 30.4);
    if (dateRange.preset === 'last7') return Math.round((monthlyLimit * 7) / 30.4);
    if (dateRange.preset === 'last30') return monthlyLimit;
    if (dateRange.preset === 'all') return monthlyLimit;

    // Custom date range: proportional to number of days
    if (dateRange.startDate && dateRange.endDate) {
      const s = new Date(dateRange.startDate).getTime();
      const e = new Date(dateRange.endDate).getTime();
      const days = Math.max(1, Math.round(Math.abs(e - s) / (1000 * 60 * 60 * 24))) + 1;
      return Math.round((monthlyLimit * days) / 30.4);
    }
    return monthlyLimit;
  };

  const periodBudgetLabel = useMemo(() => {
    switch (dateRange.preset) {
      case 'today':
        return 'на сегодня';
      case 'yesterday':
        return 'за вчера';
      case 'last7':
        return 'на 7 дней';
      case 'last30':
        return 'на 30 дней';
      case 'this_month':
        return 'на месяц';
      case 'last_month':
        return 'за прошлый месяц';
      case 'this_year':
        return 'на год';
      case 'all':
        return 'за всё время';
      case 'custom':
      default:
        if (dateRange.startDate && dateRange.endDate) {
          return 'за выбранный период';
        }
        return 'за период';
    }
  }, [dateRange]);

  // Category distribution for expenses with comparison to previous period and budget limits
  const categoryData = useMemo(() => {
    const map: {
      [name: string]: {
        amount: number;
        color: string;
        icon: string;
        categoryId: string;
      };
    } = {};
    const prevMap: { [name: string]: number } = {};

    filteredTxs
      .filter((t) => t.type === 'expense')
      .forEach((t) => {
        const cat = categories.find(
          (c) => (t.categoryId && c.id === t.categoryId) || c.name === t.category
        );
        const name = cat?.name || t.category;
        const color = cat?.color || '#94A3B8';
        const icon = cat?.icon || 'Tag';
        const categoryId = cat?.id || t.categoryId || name;

        if (!map[name]) {
          map[name] = { amount: 0, color, icon, categoryId };
        }
        map[name].amount += t.amount;
      });

    prevFilteredTxs
      .filter((t) => t.type === 'expense')
      .forEach((t) => {
        const cat = categories.find(
          (c) => (t.categoryId && c.id === t.categoryId) || c.name === t.category
        );
        const name = cat?.name || t.category;
        prevMap[name] = (prevMap[name] || 0) + t.amount;
      });

    // If filter is 'with_budget', also include expense categories that have a budget even if spent is 0
    if (budgetFilter === 'with_budget') {
      categories
        .filter((c) => c.isExpense)
        .forEach((c) => {
          const rawLimit =
            (categoryBudgets && categoryBudgets[c.id]) ||
            (c.key && categoryBudgets && categoryBudgets[c.key]) ||
            0;
          if (rawLimit > 0 && !map[c.name]) {
            map[c.name] = {
              amount: 0,
              color: c.color,
              icon: c.icon,
              categoryId: c.id,
            };
          }
        });
    }

    return Object.keys(map)
      .map((name) => {
        const currentAmount = map[name].amount;
        const previousAmount = prevMap[name] || 0;
        const catId = map[name].categoryId;
        const catObj = categories.find((c) => c.id === catId || c.name === name);

        // Budget resolution
        const rawMonthlyLimit =
          (categoryBudgets && catId && categoryBudgets[catId]) ||
          (catObj?.id && categoryBudgets && categoryBudgets[catObj.id]) ||
          (catObj?.key && categoryBudgets && categoryBudgets[catObj.key]) ||
          0;

        const periodLimit = getPeriodBudgetLimit(rawMonthlyLimit);
        const budgetPercent =
          periodLimit > 0 ? Math.round((currentAmount / periodLimit) * 100) : null;
        const budgetDiff = periodLimit > 0 ? periodLimit - currentAmount : null;
        const isOverBudget = periodLimit > 0 && currentAmount > periodLimit;
        const isNearBudget =
          periodLimit > 0 && budgetPercent !== null && budgetPercent >= 80 && budgetPercent < 100;

        let comparison = null;
        if (previousAmount > 0) {
          const diff = currentAmount - previousAmount;
          const absDiff = Math.abs(diff);
          const percent = Math.round((absDiff / previousAmount) * 100);
          comparison = {
            direction:
              diff > 0 ? ('up' as const) : diff < 0 ? ('down' as const) : ('same' as const),
            percent,
            diff,
            label:
              diff < 0
                ? `на ${percent}% меньше`
                : diff > 0
                ? `на ${percent}% больше`
                : 'без изм.',
          };
        } else if (currentAmount > 0) {
          comparison = {
            direction: 'new' as const,
            percent: 100,
            diff: currentAmount,
            label: 'новая статья',
          };
        }

        return {
          name,
          categoryId: catId,
          value: currentAmount,
          color: map[name].color,
          icon: map[name].icon,
          percentage: expense > 0 ? Math.round((currentAmount / expense) * 100) : 0,
          comparison,
          rawMonthlyLimit,
          periodLimit,
          budgetPercent,
          budgetDiff,
          isOverBudget,
          isNearBudget,
        };
      })
      .filter((cat) => {
        if (budgetFilter === 'with_budget') return cat.periodLimit > 0;
        if (budgetFilter === 'over_budget') return cat.isOverBudget;
        return true;
      })
      .sort((a, b) => {
        if (budgetFilter === 'over_budget') {
          return (b.budgetPercent || 0) - (a.budgetPercent || 0);
        }
        return b.value - a.value;
      });
  }, [
    filteredTxs,
    prevFilteredTxs,
    categories,
    categoryBudgets,
    expense,
    dateRange,
    budgetFilter,
  ]);

  // Overall budget summary metrics
  const budgetSummary = useMemo(() => {
    let budgetedCount = 0;
    let safeCount = 0;
    let nearCount = 0;
    let overCount = 0;
    let totalPeriodBudget = 0;
    let totalSpentInBudgeted = 0;

    categories
      .filter((c) => c.isExpense)
      .forEach((c) => {
        const rawLimit =
          (categoryBudgets && categoryBudgets[c.id]) ||
          (c.key && categoryBudgets && categoryBudgets[c.key]) ||
          0;
        if (rawLimit > 0) {
          budgetedCount++;
          const periodLimit = getPeriodBudgetLimit(rawLimit);
          totalPeriodBudget += periodLimit;

          // Find spent amount in this category
          const found = categoryData.find(
            (item) => item.categoryId === c.id || item.name === c.name
          );
          const spent = found ? found.value : 0;
          totalSpentInBudgeted += spent;

          if (periodLimit > 0 && spent > periodLimit) {
            overCount++;
          } else if (
            periodLimit > 0 &&
            Math.round((spent / periodLimit) * 100) >= 80 &&
            spent <= periodLimit
          ) {
            nearCount++;
          } else {
            safeCount++;
          }
        }
      });

    const overallPercent =
      totalPeriodBudget > 0
        ? Math.round((totalSpentInBudgeted / totalPeriodBudget) * 100)
        : 0;

    return {
      budgetedCount,
      safeCount,
      nearCount,
      overCount,
      totalPeriodBudget,
      totalSpentInBudgeted,
      overallPercent,
    };
  }, [categories, categoryBudgets, categoryData, dateRange]);

  // Dynamics Bar Chart Data (grouped by date or week)
  const dynamicsData = useMemo(() => {
    const datesMap: { [dateStr: string]: { income: number; expense: number } } = {};

    // Sort by timestamp asc
    const sorted = [...filteredTxs].sort((a, b) => a.timestamp - b.timestamp);

    sorted.forEach((t) => {
      const label = t.date.length > 5 ? t.date.slice(5) : t.date; // MM-DD
      if (!datesMap[label]) {
        datesMap[label] = { income: 0, expense: 0 };
      }
      if (t.type === 'income') datesMap[label].income += t.amount;
      if (t.type === 'expense') datesMap[label].expense += t.amount;
    });

    return Object.keys(datesMap).map((date) => ({
      date,
      Доход: datesMap[date].income,
      Расход: datesMap[date].expense,
    }));
  }, [filteredTxs]);

  // Foreign currency / Travel expenses breakdown
  const travelStats = useMemo(() => {
    const foreignTxs = filteredTxs.filter((t) => t.isForeignCurrency);
    if (foreignTxs.length === 0) return null;

    const totalBaseSpent = foreignTxs.reduce(
      (sum, t) => sum + (t.type === 'expense' ? t.amount : 0),
      0
    );

    const countryMap: {
      [country: string]: { baseTotal: number; count: number; currencies: Set<string> };
    } = {};

    foreignTxs.forEach((t) => {
      const c = t.country || 'За рубежом';
      if (!countryMap[c]) {
        countryMap[c] = { baseTotal: 0, count: 0, currencies: new Set() };
      }
      countryMap[c].baseTotal += t.amount;
      countryMap[c].count += 1;
      if (t.originalCurrency) countryMap[c].currencies.add(t.originalCurrency);
    });

    const countries = Object.keys(countryMap)
      .map((c) => ({
        country: c,
        baseTotal: countryMap[c].baseTotal,
        count: countryMap[c].count,
        currencies: Array.from(countryMap[c].currencies).join(', '),
      }))
      .sort((a, b) => b.baseTotal - a.baseTotal);

    return {
      totalBaseSpent,
      txCount: foreignTxs.length,
      countries,
    };
  }, [filteredTxs]);

  // Score color helper
  const getScoreColor = (score: number) => {
    if (score >= 80) return 'text-emerald-600 border-emerald-500 bg-emerald-50';
    if (score >= 60) return 'text-blue-600 border-blue-500 bg-blue-50';
    if (score >= 40) return 'text-amber-600 border-amber-500 bg-amber-50';
    return 'text-red-600 border-red-500 bg-red-50';
  };

  return (
    <div className="space-y-6">
      {/* Header & Date Range Filter Section */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-3">
        <div>
          <h2 className="text-xl sm:text-2xl font-black text-slate-900">Финансовая аналитика</h2>
          <p className="text-xs text-slate-500">
            Динамика, структура расходов и индекс здоровья • Фильтрация по датам
          </p>
        </div>

        {/* Date Filter Controls: Quick presets + DateRangePicker */}
        <div className="flex items-center gap-2 self-start md:self-auto flex-wrap">
          {/* Quick Presets */}
          <div className="flex items-center gap-1 bg-slate-200/70 p-1 rounded-xl text-xs font-bold text-slate-600">
            {[
              { preset: 'last7' as const, label: 'Неделя' },
              { preset: 'this_month' as const, label: 'Месяц' },
              { preset: 'this_year' as const, label: 'Год' },
              { preset: 'all' as const, label: 'Все время' },
            ].map((item) => {
              const isActive = dateRange.preset === item.preset;
              return (
                <button
                  key={item.preset}
                  onClick={() => {
                    const { startDate, endDate } = computePresetRange(item.preset);
                    setDateRange({
                      preset: item.preset,
                      startDate,
                      endDate,
                    });
                  }}
                  className={`px-3 py-1.5 rounded-lg transition-all cursor-pointer ${
                    isActive
                      ? 'bg-white text-slate-900 shadow-xs font-black'
                      : 'hover:text-slate-900'
                  }`}
                >
                  {item.label}
                </button>
              );
            })}
          </div>

          {/* Date Range Picker Popover (supports custom date range & all presets) */}
          <DateRangePicker
            dateRange={dateRange}
            onChange={(newRange) => setDateRange(newRange)}
          />
        </div>
      </div>

      {/* Active Date Filter Alert / Indicator Bar */}
      {(dateRange.preset === 'custom' || (dateRange.startDate && dateRange.preset !== 'this_month')) && (
        <div className="flex items-center justify-between gap-3 px-4 py-2.5 rounded-2xl bg-gradient-to-r from-emerald-50 via-teal-50/50 to-slate-50 border border-emerald-200/90 text-xs shadow-2xs">
          <div className="flex items-center gap-2 flex-wrap min-w-0">
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse shrink-0" />
            <span className="font-extrabold text-slate-800">
              Выбранный период:
            </span>
            <span className="font-black text-emerald-900 bg-white px-2.5 py-0.5 rounded-lg border border-emerald-200/80 shadow-2xs">
              {formatHumanDateRange(dateRange)}
            </span>
            {dateRange.startDate && dateRange.endDate && (
              <span className="text-[11px] font-semibold text-emerald-800 bg-emerald-100/60 px-2 py-0.5 rounded-md">
                {dateRange.startDate} — {dateRange.endDate}
              </span>
            )}
            <span className="text-slate-500 text-[11px]">
              • {filteredTxs.length}{' '}
              {filteredTxs.length === 1
                ? 'операция'
                : filteredTxs.length > 1 && filteredTxs.length < 5
                ? 'операции'
                : 'операций'}
            </span>
          </div>

          <button
            onClick={() => {
              const { startDate, endDate } = computePresetRange('this_month');
              setDateRange({
                preset: 'this_month',
                startDate,
                endDate,
              });
            }}
            className="text-[11px] font-bold text-emerald-700 hover:text-emerald-950 underline decoration-dotted transition-colors shrink-0 cursor-pointer"
          >
            Сбросить к текущему месяцу
          </button>
        </div>
      )}

      {/* Summary KPI Section - Redesigned UI/UX: No Truncation, No Spilling, Friendly Light Theme */}
      <div className="bg-white rounded-3xl border border-slate-200/90 p-4 sm:p-5 shadow-xs space-y-3.5">
        {/* Top: Net Result (Сальдо) */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2.5 pb-3 border-b border-slate-100">
          <div>
            <div className="flex items-center gap-1.5 mb-1">
              <div
                className={`w-6 h-6 rounded-lg flex items-center justify-center shrink-0 ${
                  net >= 0 ? 'bg-emerald-100 text-emerald-700' : 'bg-rose-100 text-rose-700'
                }`}
              >
                <TrendingUp size={14} className="stroke-[2.5]" />
              </div>
              <span className="text-xs font-bold uppercase tracking-wider text-slate-500">
                Сальдо за период (Чистый результат)
              </span>
            </div>
            <div
              className={`text-2xl sm:text-3xl font-black whitespace-nowrap ${
                net >= 0 ? 'text-emerald-600' : 'text-rose-600'
              }`}
            >
              {net >= 0 ? '+' : ''}
              {formatCurrency(net, currency.symbol)}
            </div>
          </div>

          <div className="flex items-center gap-2 self-start sm:self-auto">
            {income > 0 ? (
              <div className="px-3 py-1.5 rounded-xl bg-emerald-50 border border-emerald-200/80 text-left sm:text-right">
                <span className="text-[10px] font-bold text-emerald-700 block uppercase tracking-wider">
                  Норма сбережений
                </span>
                <span className="text-sm font-black text-emerald-800">
                  {savingsRate}% от доходов
                </span>
              </div>
            ) : (
              <div className="px-3 py-1.5 rounded-xl bg-slate-100 border border-slate-200 text-slate-600 text-xs font-semibold">
                {formatHumanDateRange(dateRange)}
              </div>
            )}
          </div>
        </div>

        {/* Comparison Executive Highlight Strip */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 px-3 py-2 rounded-2xl bg-slate-50 border border-slate-200/80 text-xs">
          <div className="flex items-center gap-2">
            <div
              className={`w-6 h-6 rounded-lg flex items-center justify-center shrink-0 ${
                expenseComparison.direction === 'down'
                  ? 'bg-emerald-100 text-emerald-800'
                  : expenseComparison.direction === 'up'
                  ? 'bg-rose-100 text-rose-800'
                  : 'bg-slate-200 text-slate-700'
              }`}
            >
              {expenseComparison.direction === 'down' ? (
                <TrendingDown size={14} className="stroke-[2.5]" />
              ) : expenseComparison.direction === 'up' ? (
                <TrendingUp size={14} className="stroke-[2.5]" />
              ) : (
                <Minus size={14} className="stroke-[2.5]" />
              )}
            </div>
            <div className="flex items-center gap-1.5 flex-wrap">
              <span className="font-extrabold text-slate-700">Расходы к прошлому периоду:</span>
              <span className={`font-black ${expenseComparison.textClass}`}>
                {expenseComparison.label}
              </span>
            </div>
          </div>
          {expenseComparison.detailText && (
            <span className="text-[11px] font-bold text-slate-500 bg-white px-2.5 py-0.5 rounded-lg border border-slate-200 shadow-2xs self-start sm:self-auto">
              {expenseComparison.detailText}
            </span>
          )}
        </div>

        {/* Bottom: Incomes and Expenses side-by-side with 50% width each */}
        <div className="grid grid-cols-2 gap-3">
          {/* Доходы */}
          <div className="p-3 sm:p-4 rounded-2xl bg-emerald-50/70 border border-emerald-200/80 flex flex-col justify-between">
            <div>
              <div className="flex items-center gap-1.5 mb-1.5">
                <div className="w-6 h-6 rounded-lg bg-emerald-200/80 text-emerald-800 flex items-center justify-center shrink-0">
                  <ArrowDownLeft size={14} className="stroke-[2.5]" />
                </div>
                <span className="text-[11px] sm:text-xs font-bold uppercase tracking-wider text-emerald-800">
                  Доходы
                </span>
              </div>
              <div className="text-base sm:text-2xl font-black text-emerald-700 whitespace-nowrap">
                +{formatCurrency(income, currency.symbol)}
              </div>
            </div>

            {/* Income Comparison Indicator with Previous Period */}
            <div className="mt-2.5 pt-2 border-t border-emerald-200/60 flex flex-wrap items-center gap-1.5">
              <span
                className={`inline-flex items-center gap-0.5 px-1.5 py-0.5 rounded-md text-[10px] font-black shrink-0 ${incomeComparison.badgeClass}`}
              >
                {incomeComparison.direction === 'up' && (
                  <TrendingUp size={11} className="stroke-[2.5]" />
                )}
                {incomeComparison.direction === 'down' && (
                  <TrendingDown size={11} className="stroke-[2.5]" />
                )}
                {incomeComparison.badgeText}
              </span>
              <span className={`text-[11px] font-bold leading-tight ${incomeComparison.textClass}`}>
                {incomeComparison.label}
              </span>
            </div>
          </div>

          {/* Расходы */}
          <div className="p-3 sm:p-4 rounded-2xl bg-rose-50/70 border border-rose-200/80 flex flex-col justify-between">
            <div>
              <div className="flex items-center gap-1.5 mb-1.5">
                <div className="w-6 h-6 rounded-lg bg-rose-200/80 text-rose-800 flex items-center justify-center shrink-0">
                  <ArrowUpRight size={14} className="stroke-[2.5]" />
                </div>
                <span className="text-[11px] sm:text-xs font-bold uppercase tracking-wider text-rose-800">
                  Расходы
                </span>
              </div>
              <div className="text-base sm:text-2xl font-black text-rose-700 whitespace-nowrap">
                -{formatCurrency(expense, currency.symbol)}
              </div>
            </div>

            {/* Expense Comparison Indicator with Previous Period */}
            <div className="mt-2.5 pt-2 border-t border-rose-200/60 flex flex-wrap items-center gap-1.5">
              <span
                className={`inline-flex items-center gap-0.5 px-1.5 py-0.5 rounded-md text-[10px] font-black shrink-0 ${expenseComparison.badgeClass}`}
              >
                {expenseComparison.direction === 'down' && (
                  <TrendingDown size={11} className="stroke-[2.5]" />
                )}
                {expenseComparison.direction === 'up' && (
                  <TrendingUp size={11} className="stroke-[2.5]" />
                )}
                {expenseComparison.badgeText}
              </span>
              <span className={`text-[11px] font-bold leading-tight ${expenseComparison.textClass}`}>
                {expenseComparison.label}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Financial Balance Graphical Assessment (Income vs Expenses) */}
      <FinancialBalanceWidget
        income={income}
        expense={expense}
        net={net}
        savingsRate={savingsRate}
        currencySymbol={currency.symbol}
        period={effectivePeriod}
      />

      {/* Monthly Budget vs Goal Progress Bar Widget */}
      <MonthlyBudgetProgressWidget
        onOpenBudgetModal={handleOpenBudgetModal}
      />

      {/* Personalized Financial Advice Block (Smart Spending Insights) */}
      <PersonalizedFinancialAdvice
        period={effectivePeriod}
        filteredTxs={filteredTxs}
        prevFilteredTxs={prevFilteredTxs}
        onOpenBudgetModal={handleOpenBudgetModal}
      />

      {/* 6-Month Income and Expenses Trend Line Chart with Financial Pattern Analysis */}
      <IncomeExpenseTrendLineChart />

      {/* Monthly Expenses Recharts Visualization & Deep-Dive Analytics */}
      <MonthlyExpensesAnalytics />

      {/* End-of-Month Spending Forecast & Trajectory Analysis */}
      <SpendingForecast />

      {/* 3-Month Spending Forecast Line Chart */}
      <SpendingForecastLineChart />

      {/* Financial Health Score Meter */}
      <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-xs">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <Award size={20} className="text-emerald-600" />
            <h3 className="text-base font-extrabold text-slate-900">
              Индекс финансового здоровья
            </h3>
          </div>
          <span
            className={`px-3 py-1 rounded-full text-xs font-black border ${getScoreColor(
              health.score
            )}`}
          >
            {health.score} / 100 баллов
          </span>
        </div>

        {/* Breakdown bars */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
          <div className="space-y-3">
            <div>
              <div className="flex justify-between text-xs font-bold text-slate-700 mb-1">
                <span>Норма сбережений (таргет 20%+)</span>
                <span>{health.savingsRateScore} / 35</span>
              </div>
              <div className="h-2 w-full bg-slate-100 rounded-full overflow-hidden">
                <div
                  className="h-full bg-emerald-500 rounded-full transition-all duration-500"
                  style={{ width: `${(health.savingsRateScore / 35) * 100}%` }}
                />
              </div>
            </div>

            <div>
              <div className="flex justify-between text-xs font-bold text-slate-700 mb-1">
                <span>Контроль расходов и лимиты</span>
                <span>{health.expenseControlScore} / 25</span>
              </div>
              <div className="h-2 w-full bg-slate-100 rounded-full overflow-hidden">
                <div
                  className="h-full bg-blue-500 rounded-full transition-all duration-500"
                  style={{ width: `${(health.expenseControlScore / 25) * 100}%` }}
                />
              </div>
            </div>
          </div>

          <div className="space-y-3">
            <div>
              <div className="flex justify-between text-xs font-bold text-slate-700 mb-1">
                <span>Регулярность учета доходов</span>
                <span>{health.stabilityScore} / 20</span>
              </div>
              <div className="h-2 w-full bg-slate-100 rounded-full overflow-hidden">
                <div
                  className="h-full bg-indigo-500 rounded-full transition-all duration-500"
                  style={{ width: `${(health.stabilityScore / 20) * 100}%` }}
                />
              </div>
            </div>

            <div>
              <div className="flex justify-between text-xs font-bold text-slate-700 mb-1">
                <span>Диверсификация счетов</span>
                <span>{health.diversificationScore} / 20</span>
              </div>
              <div className="h-2 w-full bg-slate-100 rounded-full overflow-hidden">
                <div
                  className="h-full bg-purple-500 rounded-full transition-all duration-500"
                  style={{ width: `${(health.diversificationScore / 20) * 100}%` }}
                />
              </div>
            </div>
          </div>
        </div>

        {/* Personalized recommendations */}
        {health.recommendations.length > 0 && (
          <div className="mt-5 pt-4 border-t border-slate-100 space-y-2">
            <span className="text-[11px] font-bold uppercase tracking-wider text-slate-400 block">
              Рекомендации для повышения индекса
            </span>
            {health.recommendations.map((rec, i) => (
              <div key={i} className="flex items-start gap-2 text-xs text-slate-600">
                <CheckCircle2 size={15} className="text-emerald-600 shrink-0 mt-0.5" />
                <span>{rec}</span>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Category Expenses Breakdown with Donut Chart and Budget Progress Bars */}
      <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-xs space-y-5">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <PieChartIcon size={20} className="text-emerald-600" />
            <div>
              <h3 className="text-base font-extrabold text-slate-900">
                Структура расходов и лимиты бюджетов
              </h3>
              <p className="text-xs text-slate-500">
                Индикаторы соблюдения установленных лимитов по категориям ({periodBudgetLabel})
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2 flex-wrap">
            <button
              onClick={() => handleOpenBudgetModal()}
              className="px-3 py-1.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs inline-flex items-center gap-1.5 transition-colors cursor-pointer"
            >
              <SlidersHorizontal size={13} />
              <span>Настроить лимиты</span>
            </button>
            <span
              className={`text-xs font-bold px-2.5 py-1 rounded-xl border ${expenseComparison.badgeClass}`}
            >
              {expenseComparison.label}
            </span>
            <span className="text-xs font-black text-slate-700 bg-slate-100 px-2.5 py-1 rounded-xl">
              Всего: {formatCurrency(expense, currency.symbol)}
            </span>
          </div>
        </div>

        {/* Budget Execution Summary Banner */}
        {budgetSummary.budgetedCount > 0 && (
          <div className="p-3.5 rounded-2xl bg-gradient-to-r from-slate-50 via-emerald-50/30 to-slate-50 border border-slate-200/90 text-xs space-y-2.5">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
              <div className="flex items-center gap-2 flex-wrap">
                <ShieldCheck size={16} className="text-emerald-600 shrink-0" />
                <span className="font-extrabold text-slate-800">
                  Исполнение бюджета {periodBudgetLabel}:
                </span>
                <span className="font-black text-slate-900">
                  {formatCurrency(budgetSummary.totalSpentInBudgeted, currency.symbol)} /{' '}
                  {formatCurrency(budgetSummary.totalPeriodBudget, currency.symbol)}
                </span>
                <span
                  className={`font-black px-1.5 py-0.5 rounded-md text-[11px] ${
                    budgetSummary.overallPercent > 100
                      ? 'bg-rose-100 text-rose-800 border border-rose-200'
                      : budgetSummary.overallPercent >= 80
                      ? 'bg-amber-100 text-amber-800 border border-amber-200'
                      : 'bg-emerald-100 text-emerald-800 border border-emerald-200'
                  }`}
                >
                  {budgetSummary.overallPercent}%
                </span>
              </div>

              {/* Status Chips */}
              <div className="flex items-center gap-1.5 flex-wrap">
                <span className="px-2 py-0.5 rounded-lg bg-emerald-100/80 text-emerald-800 font-bold text-[11px] inline-flex items-center gap-1">
                  <CheckCircle2 size={11} /> {budgetSummary.safeCount} в норме
                </span>
                {budgetSummary.nearCount > 0 && (
                  <span className="px-2 py-0.5 rounded-lg bg-amber-100 text-amber-900 font-bold text-[11px] inline-flex items-center gap-1">
                    <AlertCircle size={11} /> {budgetSummary.nearCount} у лимита
                  </span>
                )}
                {budgetSummary.overCount > 0 && (
                  <span className="px-2 py-0.5 rounded-lg bg-rose-100 text-rose-800 font-bold text-[11px] inline-flex items-center gap-1">
                    <AlertTriangle size={11} /> {budgetSummary.overCount} превышено!
                  </span>
                )}
              </div>
            </div>

            {/* Total Budget Progress Bar */}
            <div className="h-2 w-full bg-slate-200/80 rounded-full overflow-hidden">
              <div
                className={`h-full rounded-full transition-all duration-500 ${
                  budgetSummary.overallPercent > 100
                    ? 'bg-gradient-to-r from-rose-500 to-red-600'
                    : budgetSummary.overallPercent >= 80
                    ? 'bg-gradient-to-r from-amber-400 to-orange-500'
                    : 'bg-gradient-to-r from-emerald-400 to-teal-500'
                }`}
                style={{ width: `${Math.min(100, budgetSummary.overallPercent)}%` }}
              />
            </div>
          </div>
        )}

        {/* Filter Pills for Categories */}
        <div className="flex items-center justify-between gap-2 border-b border-slate-100 pb-3 flex-wrap">
          <div className="flex items-center gap-1.5">
            <button
              onClick={() => setBudgetFilter('all')}
              className={`px-3 py-1 rounded-xl text-xs font-bold transition-colors cursor-pointer ${
                budgetFilter === 'all'
                  ? 'bg-emerald-600 text-white shadow-2xs'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              Все статьи ({categoryData.length})
            </button>
            <button
              onClick={() => setBudgetFilter('with_budget')}
              className={`px-3 py-1 rounded-xl text-xs font-bold transition-colors inline-flex items-center gap-1 cursor-pointer ${
                budgetFilter === 'with_budget'
                  ? 'bg-emerald-600 text-white shadow-2xs'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              С лимитами ({budgetSummary.budgetedCount})
            </button>
            {budgetSummary.overCount > 0 && (
              <button
                onClick={() => setBudgetFilter('over_budget')}
                className={`px-3 py-1 rounded-xl text-xs font-bold transition-colors inline-flex items-center gap-1 cursor-pointer ${
                  budgetFilter === 'over_budget'
                    ? 'bg-rose-600 text-white shadow-2xs'
                    : 'bg-rose-50 text-rose-700 hover:bg-rose-100 border border-rose-200'
                }`}
              >
                <AlertTriangle size={12} />
                Превышение ({budgetSummary.overCount})
              </button>
            )}
          </div>
          <span className="text-[11px] text-slate-400 font-medium hidden sm:inline">
            Нажмите на лимит категории для быстрого изменения
          </span>
        </div>

        {/* Content Body */}
        {categoryData.length === 0 ? (
          <div className="text-center py-8 text-slate-400 font-medium space-y-2">
            <p className="text-xs">
              {budgetFilter === 'over_budget'
                ? 'Отлично! Превышений лимита по категориям не обнаружено 🎉'
                : 'Нет данных о расходах за выбранный период'}
            </p>
            {budgetFilter === 'over_budget' && (
              <button
                onClick={() => setBudgetFilter('all')}
                className="text-xs text-emerald-600 font-bold hover:underline cursor-pointer"
              >
                Показать все категории
              </button>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 items-start">
            {/* Interactive Category Donut Chart */}
            <div className="w-full flex flex-col items-center justify-center p-2 rounded-2xl bg-slate-50/50 border border-slate-100">
              <CategoryDonutChart
                data={categoryData}
                totalExpense={expense}
                currencySymbol={currency.symbol}
                periodLabel={periodBudgetLabel}
                activeCategoryId={hoveredChartCategoryId}
                onSelectCategory={(catId) => handleOpenBudgetModal(catId)}
              />
            </div>

            {/* List with Progress Bars showing Expense vs Budget Limit */}
            <div className="space-y-3 max-h-[460px] overflow-y-auto pr-1">
              {categoryData.map((cat) => (
                <div
                  key={cat.name}
                  onMouseEnter={() => setHoveredChartCategoryId(cat.categoryId || cat.name)}
                  onMouseLeave={() => setHoveredChartCategoryId(null)}
                  className={`p-3 rounded-2xl border transition-all space-y-2 ${
                    hoveredChartCategoryId === (cat.categoryId || cat.name)
                      ? 'bg-slate-100 border-slate-400 shadow-xs ring-1 ring-slate-400/30'
                      : 'bg-slate-50/80 hover:bg-slate-50 border-slate-200/70 hover:border-slate-300'
                  }`}
                >
                  {/* Category Header Row */}
                  <div className="flex items-center justify-between text-xs gap-2">
                    <div className="flex items-center gap-2 flex-wrap min-w-0">
                      <div
                        className="w-7 h-7 rounded-xl flex items-center justify-center shrink-0 shadow-2xs text-white"
                        style={{ backgroundColor: cat.color }}
                      >
                        <DynamicIcon name={cat.icon} size={14} className="stroke-[2.2]" />
                      </div>
                      <div className="flex items-center gap-1.5 flex-wrap">
                        <span className="font-extrabold text-slate-900 truncate">
                          {cat.name}
                        </span>
                        <span className="text-[11px] text-slate-400 font-semibold">
                          ({cat.percentage}% от всех трат)
                        </span>
                        {cat.comparison && (
                          <span
                            className={`text-[10px] font-bold px-1.5 py-0.5 rounded-md inline-flex items-center gap-0.5 ${
                              cat.comparison.direction === 'down'
                                ? 'text-emerald-700 bg-emerald-50 border border-emerald-200/80'
                                : cat.comparison.direction === 'up'
                                ? 'text-rose-700 bg-rose-50 border border-rose-200/80'
                                : 'text-slate-600 bg-slate-100 border border-slate-200'
                            }`}
                          >
                            {cat.comparison.direction === 'down' && (
                              <TrendingDown size={10} className="stroke-[2.5]" />
                            )}
                            {cat.comparison.direction === 'up' && (
                              <TrendingUp size={10} className="stroke-[2.5]" />
                            )}
                            {cat.comparison.label}
                          </span>
                        )}
                      </div>
                    </div>
                    <div className="text-right shrink-0">
                      <span className="font-black text-sm text-slate-900 block">
                        {formatCurrency(cat.value, currency.symbol)}
                      </span>
                    </div>
                  </div>

                  {/* Budget Indicators and Progress Bar */}
                  {cat.periodLimit > 0 ? (
                    <div className="space-y-1.5 pt-0.5">
                      <div className="flex items-center justify-between text-xs gap-2 flex-wrap">
                        <div>
                          {cat.isOverBudget ? (
                            <span className="inline-flex items-center gap-1 px-1.5 py-0.5 rounded-md text-[10px] font-black bg-rose-100 text-rose-800 border border-rose-200">
                              <AlertTriangle size={11} className="text-rose-600 shrink-0" />
                              Превышение лимита: +{formatCurrency(Math.abs(cat.budgetDiff!), currency.symbol)} ({cat.budgetPercent}%)
                            </span>
                          ) : cat.isNearBudget ? (
                            <span className="inline-flex items-center gap-1 px-1.5 py-0.5 rounded-md text-[10px] font-bold bg-amber-100 text-amber-900 border border-amber-200">
                              <AlertCircle size={11} className="text-amber-700 shrink-0" />
                              Внимание: {cat.budgetPercent}% (осталось {formatCurrency(cat.budgetDiff!, currency.symbol)})
                            </span>
                          ) : (
                            <span className="inline-flex items-center gap-1 px-1.5 py-0.5 rounded-md text-[10px] font-medium bg-emerald-50 text-emerald-800 border border-emerald-200/70">
                              <CheckCircle2 size={11} className="text-emerald-600 shrink-0" />
                              Израсходовано {cat.budgetPercent}% (осталось {formatCurrency(cat.budgetDiff!, currency.symbol)})
                            </span>
                          )}
                        </div>

                        {/* Limit display and edit button */}
                        <div className="flex items-center gap-1 text-[11px] text-slate-500 shrink-0">
                          <span>Лимит:</span>
                          <button
                            onClick={() => handleOpenBudgetModal(cat.categoryId)}
                            className="font-bold text-slate-800 hover:text-emerald-700 underline decoration-dotted inline-flex items-center gap-1 transition-colors cursor-pointer"
                            title="Изменить лимит категории"
                          >
                            <span>{formatCurrency(cat.periodLimit, currency.symbol)}</span>
                            <SlidersHorizontal size={10} className="text-slate-400" />
                          </button>
                        </div>
                      </div>

                      {/* Visual Progress Bar (Отношение текущих расходов к заданному лимиту бюджета) */}
                      <div className="space-y-1">
                        <div className="relative h-2.5 w-full bg-slate-200/80 rounded-full overflow-hidden p-0.5">
                          <div
                            className={`h-full rounded-full transition-all duration-500 ${
                              cat.isOverBudget
                                ? 'bg-gradient-to-r from-rose-500 via-rose-600 to-red-600 shadow-2xs'
                                : cat.isNearBudget
                                ? 'bg-gradient-to-r from-amber-400 via-amber-500 to-orange-500 shadow-2xs'
                                : 'bg-gradient-to-r from-emerald-400 via-teal-500 to-emerald-600 shadow-2xs'
                            }`}
                            style={{ width: `${Math.min(100, cat.budgetPercent || 0)}%` }}
                          />
                        </div>
                        <div className="flex justify-between items-center text-[10px] text-slate-400 px-0.5">
                          <span>0%</span>
                          <span
                            className={
                              cat.isOverBudget
                                ? 'font-bold text-rose-600'
                                : cat.isNearBudget
                                ? 'font-bold text-amber-700'
                                : 'font-medium text-slate-500'
                            }
                          >
                            {cat.budgetPercent}% {cat.isOverBudget ? '⚠️ лимит исчерпан' : `(ост. ${formatCurrency(cat.budgetDiff!, currency.symbol)})`}
                          </span>
                          <span className={cat.isOverBudget ? 'font-bold text-rose-600' : ''}>
                            {formatCurrency(cat.periodLimit, currency.symbol)}
                          </span>
                        </div>
                      </div>
                    </div>
                  ) : (
                    <div className="space-y-1.5 pt-0.5">
                      <div className="flex items-center justify-between text-xs">
                        <span className="text-[11px] text-slate-400 font-medium">
                          Лимит бюджета не задан
                        </span>
                        <button
                          onClick={() => handleOpenBudgetModal(cat.categoryId)}
                          className="text-[11px] font-bold text-emerald-600 hover:text-emerald-700 hover:underline inline-flex items-center gap-1 cursor-pointer transition-colors"
                        >
                          <Plus size={11} />
                          <span>Задать лимит</span>
                        </button>
                      </div>
                      {/* Share of total expenses bar */}
                      <div className="space-y-1">
                        <div className="h-1.5 w-full bg-slate-100 rounded-full overflow-hidden">
                          <div
                            className="h-full rounded-full transition-all duration-500"
                            style={{
                              width: `${cat.percentage}%`,
                              backgroundColor: cat.color,
                            }}
                          />
                        </div>
                        <div className="flex justify-between items-center text-[10px] text-slate-400 px-0.5">
                          <span>Доля в расходах: {cat.percentage}%</span>
                          <span>Лимит не установлен</span>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Dynamics Chart */}
      {dynamicsData.length > 0 && (
        <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-xs">
          <div className="flex items-center gap-2 mb-4">
            <TrendingUp size={20} className="text-blue-600" />
            <h3 className="text-base font-extrabold text-slate-900">
              Динамика доходов и расходов
            </h3>
          </div>

          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={dynamicsData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                <XAxis dataKey="date" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip
                  formatter={(val: number) => [
                    formatCurrency(val, currency.symbol),
                    '',
                  ]}
                />
                <Legend wrapperStyle={{ fontSize: 12 }} />
                <Bar dataKey="Доход" fill="#10B981" radius={[4, 4, 0, 0]} />
                <Bar dataKey="Расход" fill="#EF4444" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}

      {/* Foreign Travel & Currency Spending Card */}
      {travelStats && (
        <div className="bg-gradient-to-br from-indigo-50/80 via-blue-50/40 to-white text-slate-800 rounded-3xl p-6 shadow-xs border border-indigo-100">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-5">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-2xl bg-indigo-100 text-indigo-700 border border-indigo-200 flex items-center justify-center shadow-2xs">
                <Globe size={20} />
              </div>
              <div>
                <h3 className="text-base font-extrabold text-slate-900">
                  Расходы за рубежом и в поездках
                </h3>
                <p className="text-xs text-slate-500">
                  Траты в иностранной валюте, пересчитанные по курсу
                </p>
              </div>
            </div>

            <div className="text-left sm:text-right">
              <span className="text-xs text-slate-500 block font-medium">Всего за рубежом</span>
              <span className="text-lg font-black text-indigo-900">
                {formatCurrency(travelStats.totalBaseSpent, currency.symbol)}
              </span>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
            {travelStats.countries.map((c) => (
              <div
                key={c.country}
                className="bg-white/90 hover:bg-white border border-slate-200/90 rounded-2xl p-4 transition-colors shadow-2xs"
              >
                <div className="flex items-center justify-between mb-1">
                  <span className="text-sm font-bold text-slate-800 flex items-center gap-1.5">
                    <MapPin size={14} className="text-rose-500" />
                    {c.country}
                  </span>
                  <span className="text-[11px] font-bold text-indigo-700 bg-indigo-50 border border-indigo-200 px-2 py-0.5 rounded-md">
                    {c.currencies}
                  </span>
                </div>
                <div className="flex items-baseline justify-between mt-2 pt-2 border-t border-slate-100">
                  <span className="text-xs text-slate-400 font-medium">{c.count} операций</span>
                  <span className="text-sm font-black text-emerald-700">
                    {formatCurrency(c.baseTotal, currency.symbol)}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Budget Limit Management Modal */}
      <BudgetModal
        isOpen={isBudgetModalOpen}
        onClose={() => setIsBudgetModalOpen(false)}
        initialCategoryId={selectedBudgetCategoryId}
      />
    </div>
  );
};
