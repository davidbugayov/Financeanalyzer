import React, { useState, useMemo } from 'react';
import {
  ResponsiveContainer,
  ComposedChart,
  Line,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ReferenceLine,
} from 'recharts';
import {
  TrendingUp,
  TrendingDown,
  Calendar,
  Sparkles,
  AlertTriangle,
  CheckCircle2,
  Sliders,
  ArrowRight,
  Info,
  DollarSign,
  PieChart as PieIcon,
  ShieldAlert,
  Flame,
  Clock,
  Target,
  RefreshCw,
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { formatCurrency } from '../utils/financeCalculations';
import { DynamicIcon } from '../utils/iconHelper';
import { Transaction } from '../types';

type ForecastScenario = 'base' | 'frugal' | 'stress';

interface DayTrajectoryPoint {
  day: number;
  label: string;
  actual?: number;
  forecast?: number;
  lowerBound?: number;
  upperBound?: number;
  budgetRunway?: number;
}

interface CategoryForecast {
  id: string;
  name: string;
  color: string;
  icon: string;
  spentSoFar: number;
  projectedEndOfMonth: number;
  budgetLimit: number;
  histAvg: number;
  status: 'ok' | 'warning' | 'danger';
}

export const SpendingForecast: React.FC = () => {
  const { transactions, categoryBudgets, categories, currency } = useFinance();
  const [scenario, setScenario] = useState<ForecastScenario>('base');
  const [showConfidenceBand, setShowConfidenceBand] = useState<boolean>(true);

  // Determine current active date context
  const now = useMemo(() => new Date(), []);
  const currentYear = now.getFullYear();
  const currentMonthIdx = now.getMonth(); // 0-indexed
  const daysInCurrentMonth = useMemo(
    () => new Date(currentYear, currentMonthIdx + 1, 0).getDate(),
    [currentYear, currentMonthIdx]
  );
  const currentDay = Math.min(now.getDate(), daysInCurrentMonth);
  const daysPassed = Math.max(1, currentDay);
  const daysRemaining = Math.max(0, daysInCurrentMonth - currentDay);
  const monthProgress = Math.round((daysPassed / daysInCurrentMonth) * 100);

  const monthNames = [
    'январе',
    'феврале',
    'марте',
    'апреле',
    'мае',
    'июне',
    'июле',
    'августе',
    'сентябре',
    'октябре',
    'ноябре',
    'декабре',
  ];
  const monthNameNominative = [
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
  ][currentMonthIdx];

  const currentMonthKey = `${currentYear}-${String(currentMonthIdx + 1).padStart(2, '0')}`;

  // 1. Current Month Expenses Data
  const currentMonthExpenses = useMemo(() => {
    return transactions.filter(
      (t) =>
        t.type === 'expense' &&
        t.amount > 0 &&
        t.date &&
        t.date.startsWith(currentMonthKey)
    );
  }, [transactions, currentMonthKey]);

  const spentSoFar = useMemo(() => {
    return currentMonthExpenses.reduce((sum, t) => sum + t.amount, 0);
  }, [currentMonthExpenses]);

  // Daily spend distribution so far
  const dailySpendMap = useMemo(() => {
    const map: Record<number, number> = {};
    for (let d = 1; d <= daysInCurrentMonth; d++) {
      map[d] = 0;
    }
    currentMonthExpenses.forEach((t) => {
      const d = parseInt(t.date.split('-')[2], 10);
      if (d && d >= 1 && d <= daysInCurrentMonth) {
        map[d] = (map[d] || 0) + t.amount;
      }
    });
    return map;
  }, [currentMonthExpenses, daysInCurrentMonth]);

  // Current burn rate per day
  const currentDailyBurnRate = Math.round(spentSoFar / daysPassed);

  // 2. Historical Months Trend Analysis (last 3 completed months)
  const historicalStats = useMemo(() => {
    const pastMonthsMap: Record<string, number> = {};
    const pastMonthsDailyMap: Record<string, { total: number; days: number }> = {};

    transactions.forEach((tx) => {
      if (tx.type === 'expense' && tx.amount > 0 && tx.date) {
        const ym = tx.date.substring(0, 7);
        if (ym < currentMonthKey) {
          pastMonthsMap[ym] = (pastMonthsMap[ym] || 0) + tx.amount;
        }
      }
    });

    const pastMonthsList: { monthKey: string; total: number; days: number }[] = [];
    for (let offset = 1; offset <= 3; offset++) {
      const d = new Date(currentYear, currentMonthIdx - offset, 1);
      const ym = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
      const total = pastMonthsMap[ym] || 0;
      const days = new Date(d.getFullYear(), d.getMonth() + 1, 0).getDate();
      if (total > 0 || pastMonthsList.length > 0) {
        pastMonthsList.push({ monthKey: ym, total, days });
      }
    }

    let weightedAvgMonthly = 0;
    let avgDailyPast = 0;

    if (pastMonthsList.length > 0) {
      const weights = [0.5, 0.3, 0.2].slice(0, pastMonthsList.length);
      const weightSum = weights.reduce((s, w) => s + w, 0);
      weightedAvgMonthly = Math.round(
        pastMonthsList.reduce((sum, m, idx) => sum + m.total * weights[idx], 0) / weightSum
      );
      const totalDays = pastMonthsList.reduce((sum, m) => sum + m.days, 0);
      const totalSpentPast = pastMonthsList.reduce((sum, m) => sum + m.total, 0);
      avgDailyPast = Math.round(totalSpentPast / Math.max(1, totalDays));
    } else {
      weightedAvgMonthly = spentSoFar > 0 ? currentDailyBurnRate * daysInCurrentMonth : 45000;
      avgDailyPast = Math.round(weightedAvgMonthly / daysInCurrentMonth);
    }

    return {
      pastMonthsCount: pastMonthsList.length,
      weightedAvgMonthly,
      avgDailyPast,
    };
  }, [transactions, currentMonthKey, currentYear, currentMonthIdx, spentSoFar, currentDailyBurnRate, daysInCurrentMonth]);

  // 3. Upcoming Known Recurring Expenses before month ends
  const upcomingRecurringCommitments = useMemo(() => {
    let sum = 0;
    transactions.forEach((t) => {
      if (t.isRecurring && t.type === 'expense' && !t.parentRecurringId) {
        const nextDate = t.recurrenceNextDate || t.date;
        if (nextDate && nextDate.startsWith(currentMonthKey)) {
          const dayNum = parseInt(nextDate.split('-')[2], 10);
          if (dayNum > currentDay) {
            sum += t.amount;
          }
        }
      }
    });
    return sum;
  }, [transactions, currentMonthKey, currentDay]);

  // 4. Monthly Budget Anchor
  const totalBudgetLimit = useMemo(() => {
    return Object.values(categoryBudgets).reduce((sum, b) => sum + (b || 0), 0);
  }, [categoryBudgets]);

  // 5. End-of-Month Forecast Engine (Blending current run-rate, historical velocity, recurring commitments)
  const forecastResults = useMemo(() => {
    // Blended daily rate for remaining days
    // Weight 60% on current month's actual velocity + 40% on historical trend
    const blendedDailyRate = Math.round(
      currentDailyBurnRate * 0.6 + historicalStats.avgDailyPast * 0.4
    );

    // Scenario multipliers
    let scenarioMultiplier = 1.0;
    if (scenario === 'frugal') scenarioMultiplier = 0.82; // -18% mindful reduction
    if (scenario === 'stress') scenarioMultiplier = 1.22; // +22% weekend/unexpected surge

    const projectedRemainingNonRecurring = Math.round(
      blendedDailyRate * daysRemaining * scenarioMultiplier
    );
    const estimatedEndOfMonth = Math.round(
      spentSoFar + projectedRemainingNonRecurring + upcomingRecurringCommitments
    );

    // Bounds for confidence cone
    const lowerBoundEndOfMonth = Math.round(
      spentSoFar + blendedDailyRate * daysRemaining * 0.85 + upcomingRecurringCommitments
    );
    const upperBoundEndOfMonth = Math.round(
      spentSoFar + blendedDailyRate * daysRemaining * 1.25 + upcomingRecurringCommitments * 1.05
    );

    // Comparison to budget
    const hasBudget = totalBudgetLimit > 1000;
    const projectedBudgetDiff = hasBudget ? totalBudgetLimit - estimatedEndOfMonth : 0;
    const projectedOverspend = hasBudget && estimatedEndOfMonth > totalBudgetLimit;
    const overspendAmount = projectedOverspend ? estimatedEndOfMonth - totalBudgetLimit : 0;
    const remainingSafeDailyBudget =
      hasBudget && daysRemaining > 0 && totalBudgetLimit > spentSoFar
        ? Math.max(0, Math.round((totalBudgetLimit - spentSoFar) / daysRemaining))
        : 0;

    // Comparison to historical average
    const diffVsHistorical = estimatedEndOfMonth - historicalStats.weightedAvgMonthly;
    const percentVsHistorical =
      historicalStats.weightedAvgMonthly > 0
        ? Math.round((diffVsHistorical / historicalStats.weightedAvgMonthly) * 100)
        : 0;

    return {
      blendedDailyRate,
      estimatedEndOfMonth,
      lowerBoundEndOfMonth,
      upperBoundEndOfMonth,
      hasBudget,
      projectedBudgetDiff,
      projectedOverspend,
      overspendAmount,
      remainingSafeDailyBudget,
      diffVsHistorical,
      percentVsHistorical,
      projectedRemaining: estimatedEndOfMonth - spentSoFar,
    };
  }, [
    currentDailyBurnRate,
    historicalStats.avgDailyPast,
    historicalStats.weightedAvgMonthly,
    scenario,
    daysRemaining,
    spentSoFar,
    upcomingRecurringCommitments,
    totalBudgetLimit,
  ]);

  // 6. Day-by-Day Trajectory Data for Chart
  const trajectoryChartData = useMemo(() => {
    const points: DayTrajectoryPoint[] = [];
    let runningActual = 0;
    const budgetPerDay = totalBudgetLimit > 0 ? totalBudgetLimit / daysInCurrentMonth : 0;

    // Track actual spending up to currentDay
    for (let d = 1; d <= currentDay; d++) {
      runningActual += dailySpendMap[d] || 0;
      points.push({
        day: d,
        label: `${d} ${monthNames[currentMonthIdx].slice(0, 3)}`,
        actual: runningActual,
        forecast: d === currentDay ? runningActual : undefined,
        lowerBound: d === currentDay ? runningActual : undefined,
        upperBound: d === currentDay ? runningActual : undefined,
        budgetRunway: budgetPerDay > 0 ? Math.round(budgetPerDay * d) : undefined,
      });
    }

    // Project from currentDay + 1 to daysInCurrentMonth
    let runningForecast = runningActual;
    let runningLower = runningActual;
    let runningUpper = runningActual;

    const remainingDaysCount = Math.max(1, daysInCurrentMonth - currentDay);
    const dailyForecastIncrement =
      forecastResults.projectedRemaining / remainingDaysCount;
    const dailyLowerIncrement =
      (forecastResults.lowerBoundEndOfMonth - runningActual) / remainingDaysCount;
    const dailyUpperIncrement =
      (forecastResults.upperBoundEndOfMonth - runningActual) / remainingDaysCount;

    for (let d = currentDay + 1; d <= daysInCurrentMonth; d++) {
      runningForecast += dailyForecastIncrement;
      runningLower += dailyLowerIncrement;
      runningUpper += dailyUpperIncrement;

      points.push({
        day: d,
        label: `${d} ${monthNames[currentMonthIdx].slice(0, 3)}`,
        forecast: Math.round(runningForecast),
        lowerBound: Math.round(runningLower),
        upperBound: Math.round(runningUpper),
        budgetRunway: budgetPerDay > 0 ? Math.round(budgetPerDay * d) : undefined,
      });
    }

    return points;
  }, [
    currentDay,
    daysInCurrentMonth,
    dailySpendMap,
    totalBudgetLimit,
    monthNames,
    currentMonthIdx,
    forecastResults,
  ]);

  // 7. Category-level End-of-Month Estimates
  const categoryForecasts: CategoryForecast[] = useMemo(() => {
    const catSpentMap: Record<string, number> = {};
    currentMonthExpenses.forEach((t) => {
      const catId = t.categoryId || categories.find((c) => c.name === t.category)?.id || 'other';
      catSpentMap[catId] = (catSpentMap[catId] || 0) + t.amount;
    });

    const expenseCategories = categories.filter((c) => c.isExpense);

    return expenseCategories
      .map((cat) => {
        const spent = catSpentMap[cat.id] || 0;
        const limit = categoryBudgets[cat.id] || 0;
        // Project end of month: current spent + (daily category velocity * daysRemaining)
        const dailyCatRate = spent / daysPassed;
        const projected = Math.round(spent + dailyCatRate * daysRemaining);

        let status: 'ok' | 'warning' | 'danger' = 'ok';
        if (limit > 0) {
          if (spent > limit || projected > limit * 1.05) {
            status = 'danger';
          } else if (projected > limit * 0.85) {
            status = 'warning';
          }
        }

        return {
          id: cat.id,
          name: cat.name,
          color: cat.color || '#10B981',
          icon: cat.icon || 'Tag',
          spentSoFar: spent,
          projectedEndOfMonth: projected,
          budgetLimit: limit,
          histAvg: Math.round(projected * 0.95), // baseline
          status,
        };
      })
      .filter((c) => c.spentSoFar > 0 || c.budgetLimit > 0)
      .sort((a, b) => b.projectedEndOfMonth - a.projectedEndOfMonth);
  }, [currentMonthExpenses, categories, categoryBudgets, daysPassed, daysRemaining]);

  // Custom Recharts Tooltip
  const CustomTrajectoryTooltip = ({ active, payload, label }: any) => {
    if (!active || !payload || !payload.length) return null;
    const pt: DayTrajectoryPoint = payload[0]?.payload;
    if (!pt) return null;

    return (
      <div className="bg-white/95 backdrop-blur-md p-3.5 rounded-2xl shadow-xl border border-slate-200 text-xs min-w-[210px] z-50">
        <div className="flex items-center justify-between border-b border-slate-100 pb-1.5 mb-2">
          <span className="font-extrabold text-slate-900 text-sm">
            День {pt.day} ({pt.label})
          </span>
          {pt.actual !== undefined && pt.forecast === undefined ? (
            <span className="px-1.5 py-0.5 rounded text-[10px] font-bold bg-emerald-50 text-emerald-700">
              Факт
            </span>
          ) : (
            <span className="px-1.5 py-0.5 rounded text-[10px] font-bold bg-indigo-50 text-indigo-700">
              Прогноз
            </span>
          )}
        </div>

        <div className="space-y-1.5">
          {pt.actual !== undefined && (
            <div className="flex justify-between items-center">
              <span className="text-slate-500 flex items-center gap-1.5">
                <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 inline-block" />
                Факт на день:
              </span>
              <span className="font-black text-slate-900">
                {formatCurrency(pt.actual, currency.symbol)}
              </span>
            </div>
          )}

          {pt.forecast !== undefined && (
            <div className="flex justify-between items-center">
              <span className="text-slate-500 flex items-center gap-1.5">
                <span className="w-2.5 h-2.5 rounded-full bg-indigo-600 inline-block" />
                Ожидаемая сумма:
              </span>
              <span className="font-black text-indigo-700">
                {formatCurrency(pt.forecast, currency.symbol)}
              </span>
            </div>
          )}

          {pt.budgetRunway !== undefined && (
            <div className="flex justify-between items-center text-slate-500">
              <span className="flex items-center gap-1.5">
                <span className="w-2 h-0.5 bg-slate-400 inline-block" />
                Лимит на день:
              </span>
              <span className="font-bold text-slate-600">
                {formatCurrency(pt.budgetRunway, currency.symbol)}
              </span>
            </div>
          )}

          {pt.lowerBound !== undefined && pt.upperBound !== undefined && pt.actual === undefined && (
            <div className="mt-1 pt-1 border-t border-slate-100 text-[10px] text-slate-400 flex justify-between">
              <span>Диапазон:</span>
              <span className="font-semibold text-slate-600">
                {formatCurrency(pt.lowerBound, currency.symbol)} — {formatCurrency(pt.upperBound, currency.symbol)}
              </span>
            </div>
          )}
        </div>
      </div>
    );
  };

  return (
    <div className="bg-white rounded-3xl border border-slate-200/90 p-5 sm:p-6 shadow-xs space-y-6">
      {/* Header with Title and Mode Switcher */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-slate-100">
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 rounded-2xl bg-gradient-to-tr from-indigo-600 via-indigo-700 to-purple-600 text-white flex items-center justify-center shadow-xs shrink-0">
            <Sparkles size={22} className="stroke-[2.2]" />
          </div>
          <div>
            <div className="flex items-center gap-2 flex-wrap">
              <h3 className="text-base sm:text-lg font-black text-slate-900 tracking-tight">
                Прогноз расходов до конца месяца
              </h3>
              <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-indigo-50 text-indigo-800 border border-indigo-200 flex items-center gap-1">
                <Calendar size={12} />
                <span>{monthNameNominative} ({monthProgress}%)</span>
              </span>
            </div>
            <p className="text-xs text-slate-500 mt-0.5">
              Аналитическая оценка итоговых расходов на основе исторической скорости трат и темпа
            </p>
          </div>
        </div>

        {/* Scenario simulation tabs */}
        <div className="flex items-center gap-1 p-1 bg-slate-100/90 rounded-2xl self-start sm:self-auto">
          <button
            type="button"
            onClick={() => setScenario('base')}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer ${
              scenario === 'base'
                ? 'bg-white text-indigo-700 shadow-2xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
            title="Базовый тренд: текущая динамика + история"
          >
            Базовый тренд
          </button>
          <button
            type="button"
            onClick={() => setScenario('frugal')}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer ${
              scenario === 'frugal'
                ? 'bg-white text-emerald-700 shadow-2xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
            title="Экономный режим: снижение необязательных трат на 18%"
          >
            Экономный (-18%)
          </button>
          <button
            type="button"
            onClick={() => setScenario('stress')}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer ${
              scenario === 'stress'
                ? 'bg-white text-rose-700 shadow-2xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
            title="Пессимистичный: учет всплесков и непредвиденных чеков"
          >
            Пессимистичный (+22%)
          </button>
        </div>
      </div>

      {/* 4 Main Forecast Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        {/* Card 1: Estimated End of Month */}
        <div className="p-4 rounded-2xl bg-gradient-to-br from-indigo-50/90 to-purple-50/60 border border-indigo-200/90 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between text-[11px] font-bold text-indigo-800">
              <span className="uppercase tracking-wider">Прогноз к концу месяца</span>
              <Target size={15} className="text-indigo-600 stroke-[2.5]" />
            </div>
            <div className="text-xl sm:text-2xl font-black text-indigo-950 mt-1 whitespace-nowrap">
              {formatCurrency(forecastResults.estimatedEndOfMonth, currency.symbol)}
            </div>
          </div>
          <div className="mt-2.5 pt-2 border-t border-indigo-200/60 text-[10px] font-semibold text-indigo-800/80 flex items-center justify-between">
            <span>Фактически: {formatCurrency(spentSoFar, currency.symbol)}</span>
            <span className="font-bold">+{formatCurrency(forecastResults.projectedRemaining, currency.symbol)}</span>
          </div>
        </div>

        {/* Card 2: Current Daily Burn Rate */}
        <div className="p-4 rounded-2xl bg-slate-50 border border-slate-200/90 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between text-[11px] font-bold text-slate-500">
              <span className="uppercase tracking-wider">Скорость расходов</span>
              <Flame size={15} className="text-amber-500 stroke-[2.5]" />
            </div>
            <div className="text-xl sm:text-2xl font-black text-slate-900 mt-1 whitespace-nowrap">
              {formatCurrency(currentDailyBurnRate, currency.symbol)}
              <span className="text-xs font-semibold text-slate-400"> / день</span>
            </div>
          </div>
          <div className="mt-2.5 pt-2 border-t border-slate-200 text-[10px] font-semibold text-slate-500 flex items-center justify-between">
            <span>Прошло {daysPassed} из {daysInCurrentMonth} дн.</span>
            <span>Осталось {daysRemaining} дн.</span>
          </div>
        </div>

        {/* Card 3: Safe Daily Runway (to stay in budget) */}
        <div
          className={`p-4 rounded-2xl border flex flex-col justify-between ${
            forecastResults.projectedOverspend
              ? 'bg-rose-50/70 border-rose-200/90'
              : 'bg-emerald-50/70 border-emerald-200/90'
          }`}
        >
          <div>
            <div
              className={`flex items-center justify-between text-[11px] font-bold ${
                forecastResults.projectedOverspend ? 'text-rose-800' : 'text-emerald-800'
              }`}
            >
              <span className="uppercase tracking-wider">
                {forecastResults.projectedOverspend ? 'Риск перерасхода' : 'Безопасный лимит'}
              </span>
              {forecastResults.projectedOverspend ? (
                <ShieldAlert size={15} className="text-rose-600 stroke-[2.5]" />
              ) : (
                <CheckCircle2 size={15} className="text-emerald-600 stroke-[2.5]" />
              )}
            </div>
            <div
              className={`text-xl sm:text-2xl font-black mt-1 whitespace-nowrap ${
                forecastResults.projectedOverspend ? 'text-rose-700' : 'text-emerald-700'
              }`}
            >
              {forecastResults.hasBudget ? (
                forecastResults.projectedOverspend ? (
                  `+${formatCurrency(forecastResults.overspendAmount, currency.symbol)}`
                ) : (
                  `${formatCurrency(forecastResults.remainingSafeDailyBudget, currency.symbol)}`
                )
              ) : (
                `${formatCurrency(forecastResults.blendedDailyRate, currency.symbol)}`
              )}
              {forecastResults.hasBudget && !forecastResults.projectedOverspend && (
                <span className="text-xs font-semibold text-emerald-600"> / день</span>
              )}
            </div>
          </div>
          <div
            className={`mt-2.5 pt-2 border-t text-[10px] font-semibold flex items-center justify-between ${
              forecastResults.projectedOverspend
                ? 'border-rose-200/60 text-rose-800'
                : 'border-emerald-200/60 text-emerald-800'
            }`}
          >
            {forecastResults.hasBudget ? (
              forecastResults.projectedOverspend ? (
                <span>Превышение установленного лимита</span>
              ) : (
                <span>Дневной лимит на оставшиеся {daysRemaining} дн.</span>
              )
            ) : (
              <span>Среднедневной прогноз без лимита</span>
            )}
            {forecastResults.hasBudget && (
              <span className="font-bold">
                {Math.round((forecastResults.estimatedEndOfMonth / totalBudgetLimit) * 100)}% бюджета
              </span>
            )}
          </div>
        </div>

        {/* Card 4: Historical Velocity Delta */}
        <div className="p-4 rounded-2xl bg-slate-50 border border-slate-200/90 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between text-[11px] font-bold text-slate-500">
              <span className="uppercase tracking-wider">К прошлому месяцу</span>
              {forecastResults.percentVsHistorical > 0 ? (
                <TrendingUp size={15} className="text-rose-500 stroke-[2.5]" />
              ) : (
                <TrendingDown size={15} className="text-emerald-500 stroke-[2.5]" />
              )}
            </div>
            <div
              className={`text-xl sm:text-2xl font-black mt-1 whitespace-nowrap ${
                forecastResults.percentVsHistorical > 0 ? 'text-rose-600' : 'text-emerald-600'
              }`}
            >
              {forecastResults.percentVsHistorical > 0 ? '+' : ''}
              {forecastResults.percentVsHistorical}%
            </div>
          </div>
          <div className="mt-2.5 pt-2 border-t border-slate-200 text-[10px] font-semibold text-slate-500 flex items-center justify-between">
            <span>В среднем: {formatCurrency(historicalStats.weightedAvgMonthly, currency.symbol)}</span>
            <span>{historicalStats.pastMonthsCount} мес. базы</span>
          </div>
        </div>
      </div>

      {/* Trajectory Cumulative Spending Chart */}
      <div className="space-y-2">
        <div className="flex items-center justify-between flex-wrap gap-2">
          <div className="flex items-center gap-2">
            <span className="text-xs font-extrabold text-slate-800">
              Траектория накопленных расходов: День 1 → День {daysInCurrentMonth}
            </span>
          </div>
          <div className="flex items-center gap-3 text-xs flex-wrap">
            <div className="flex items-center gap-1.5 font-bold text-slate-700">
              <span className="w-3 h-1 bg-emerald-500 rounded-full inline-block" />
              <span>Факт (Дни 1–{currentDay})</span>
            </div>
            <div className="flex items-center gap-1.5 font-bold text-indigo-700">
              <span className="w-3 h-1 bg-indigo-600 rounded-full border-b border-dotted inline-block" />
              <span>Прогноз (Дни {currentDay + 1}–{daysInCurrentMonth})</span>
            </div>
            {totalBudgetLimit > 0 && (
              <div className="flex items-center gap-1.5 font-bold text-slate-400">
                <span className="w-3 h-0.5 bg-slate-400 inline-block" />
                <span>Равномерный лимит</span>
              </div>
            )}
            <button
              type="button"
              onClick={() => setShowConfidenceBand(!showConfidenceBand)}
              className="text-[11px] font-bold text-slate-500 hover:text-slate-800 cursor-pointer ml-1 underline decoration-dotted"
            >
              {showConfidenceBand ? 'Скрыть коридор' : 'Коридор погрешности'}
            </button>
          </div>
        </div>

        <div className="h-64 sm:h-72 w-full pt-2">
          <ResponsiveContainer width="100%" height="100%">
            <ComposedChart
              data={trajectoryChartData}
              margin={{ top: 10, right: 15, left: -10, bottom: 5 }}
            >
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
              <XAxis
                dataKey="day"
                tickLine={false}
                axisLine={{ stroke: '#CBD5E1' }}
                tick={{ fontSize: 11, fill: '#64748B', fontWeight: 600 }}
                interval={Math.floor(daysInCurrentMonth / 7)}
                tickFormatter={(d) => `${d} дн.`}
              />
              <YAxis
                tickLine={false}
                axisLine={false}
                tick={{ fontSize: 11, fill: '#64748B', fontWeight: 600 }}
                tickFormatter={(val) => `${Math.round(val / 1000)}k`}
              />
              <Tooltip content={<CustomTrajectoryTooltip />} />

              {/* Confidence Band (Upper and Lower bounds) */}
              {showConfidenceBand && (
                <Area
                  type="monotone"
                  dataKey="upperBound"
                  stroke="none"
                  fill="#EEF2FF"
                  fillOpacity={0.7}
                />
              )}

              {/* Budget Ceiling Reference Line */}
              {totalBudgetLimit > 0 && (
                <ReferenceLine
                  y={totalBudgetLimit}
                  stroke="#F43F5E"
                  strokeDasharray="4 4"
                  strokeWidth={1.5}
                  label={{
                    value: `Бюджет: ${formatCurrency(totalBudgetLimit, currency.symbol)}`,
                    position: 'top',
                    fill: '#E11D48',
                    fontSize: 10,
                    fontWeight: 800,
                  }}
                />
              )}

              {/* Transition Anchor Marker */}
              <ReferenceLine
                x={currentDay}
                stroke="#6366F1"
                strokeDasharray="2 2"
                strokeWidth={1.5}
                label={{
                  value: 'Сегодня',
                  position: 'insideTopLeft',
                  fill: '#4F46E5',
                  fontSize: 10,
                  fontWeight: 800,
                }}
              />

              {/* Ideal Budget Runway Line */}
              {totalBudgetLimit > 0 && (
                <Line
                  type="linear"
                  dataKey="budgetRunway"
                  stroke="#94A3B8"
                  strokeWidth={1.2}
                  strokeDasharray="2 4"
                  dot={false}
                />
              )}

              {/* Forecast Trajectory Line (Dotted) */}
              <Line
                type="monotone"
                dataKey="forecast"
                stroke="#4F46E5"
                strokeWidth={2.5}
                strokeDasharray="4 3"
                dot={false}
                activeDot={{ r: 5, fill: '#4F46E5', stroke: '#fff', strokeWidth: 2 }}
              />

              {/* Actual Spending Line (Solid) */}
              <Line
                type="monotone"
                dataKey="actual"
                stroke="#10B981"
                strokeWidth={3}
                dot={{ r: 2.5, fill: '#10B981', strokeWidth: 0 }}
                activeDot={{ r: 5, fill: '#10B981', stroke: '#fff', strokeWidth: 2 }}
              />
            </ComposedChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Smart Analytical Takeaways & Advice */}
      <div className="p-4 rounded-2xl bg-slate-50 border border-slate-200/80 space-y-2.5">
        <div className="flex items-center gap-2">
          <Sparkles size={16} className="text-indigo-600" />
          <h4 className="text-xs font-extrabold uppercase tracking-wider text-slate-700">
            Аналитические выводы модели
          </h4>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs text-slate-600">
          <div className="flex items-start gap-2">
            <CheckCircle2 size={15} className="text-emerald-600 shrink-0 mt-0.5" />
            <span>
              Текущий темп трат составляет <strong>{formatCurrency(currentDailyBurnRate, currency.symbol)}/день</strong>.
              {forecastResults.hasBudget && (
                <>
                  {' '}Чтобы гарантированно не превысить лимит, рекомендуемый расход на оставшиеся {daysRemaining} дн. не должен превышать{' '}
                  <strong className="text-emerald-700">
                    {formatCurrency(forecastResults.remainingSafeDailyBudget, currency.symbol)}/день
                  </strong>.
                </>
              )}
            </span>
          </div>

          <div className="flex items-start gap-2">
            {forecastResults.projectedOverspend ? (
              <AlertTriangle size={15} className="text-rose-600 shrink-0 mt-0.5" />
            ) : (
              <CheckCircle2 size={15} className="text-emerald-600 shrink-0 mt-0.5" />
            )}
            <span>
              {forecastResults.projectedOverspend ? (
                <>
                  Внимание: при сохранении скорости трат ожидается перерасход общего бюджета на{' '}
                  <strong className="text-rose-700">
                    +{formatCurrency(forecastResults.overspendAmount, currency.symbol)}
                  </strong>.
                </>
              ) : (
                <>
                  При текущем профиле трат вы укладываетесь в бюджет с запасом около{' '}
                  <strong className="text-emerald-700">
                    {formatCurrency(forecastResults.projectedBudgetDiff, currency.symbol)}
                  </strong>.
                </>
              )}
              {upcomingRecurringCommitments > 0 && (
                <> В прогноз уже включены обязательные регулярные списания на сумму{' '}
                <strong>{formatCurrency(upcomingRecurringCommitments, currency.symbol)}</strong>.</>
              )}
            </span>
          </div>
        </div>
      </div>

      {/* Category Breakdown Forecast Table */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <div>
            <h4 className="text-sm font-extrabold text-slate-900">
              Прогноз по ключевым категориям к концу месяца
            </h4>
            <p className="text-[11px] text-slate-400">
              Ожидаемые суммы расходов на основе динамики и остатка дней
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2.5">
          {categoryForecasts.slice(0, 6).map((cat) => {
            const hasLimit = cat.budgetLimit > 0;
            const projectedRatio = hasLimit
              ? Math.min(100, Math.round((cat.projectedEndOfMonth / cat.budgetLimit) * 100))
              : 0;

            return (
              <div
                key={cat.id}
                className="p-3 rounded-2xl bg-white border border-slate-200/90 hover:border-slate-300 transition-all shadow-2xs space-y-2"
              >
                <div className="flex items-center justify-between gap-2">
                  <div className="flex items-center gap-2 min-w-0">
                    <div
                      className="w-7 h-7 rounded-lg flex items-center justify-center text-white shrink-0"
                      style={{ backgroundColor: cat.color }}
                    >
                      <DynamicIcon name={cat.icon} size={14} />
                    </div>
                    <span className="text-xs font-bold text-slate-800 truncate">
                      {cat.name}
                    </span>
                  </div>

                  <span
                    className={`px-2 py-0.5 rounded-md text-[10px] font-black shrink-0 ${
                      cat.status === 'danger'
                        ? 'bg-rose-100 text-rose-800'
                        : cat.status === 'warning'
                        ? 'bg-amber-100 text-amber-900'
                        : 'bg-emerald-50 text-emerald-800'
                    }`}
                  >
                    {cat.status === 'danger'
                      ? 'Риск перерасхода'
                      : cat.status === 'warning'
                      ? 'Около лимита'
                      : 'В норме'}
                  </span>
                </div>

                <div className="flex items-baseline justify-between text-xs pt-1 border-t border-slate-100">
                  <span className="text-slate-500 text-[11px]">
                    Факт: <strong className="text-slate-700">{formatCurrency(cat.spentSoFar, currency.symbol)}</strong>
                  </span>
                  <div className="text-right">
                    <span className="text-[10px] text-slate-400 block font-medium">Прогноз:</span>
                    <span className="font-black text-slate-900">
                      {formatCurrency(cat.projectedEndOfMonth, currency.symbol)}
                    </span>
                  </div>
                </div>

                {hasLimit && (
                  <div>
                    <div className="flex justify-between text-[10px] font-semibold text-slate-500 mb-1">
                      <span>Лимит: {formatCurrency(cat.budgetLimit, currency.symbol)}</span>
                      <span
                        className={
                          cat.projectedEndOfMonth > cat.budgetLimit
                            ? 'text-rose-600 font-bold'
                            : 'text-slate-600'
                        }
                      >
                        {Math.round((cat.projectedEndOfMonth / cat.budgetLimit) * 100)}%
                      </span>
                    </div>
                    <div className="h-1.5 w-full bg-slate-100 rounded-full overflow-hidden">
                      <div
                        className={`h-full rounded-full ${
                          cat.projectedEndOfMonth > cat.budgetLimit
                            ? 'bg-rose-500'
                            : projectedRatio > 85
                            ? 'bg-amber-500'
                            : 'bg-emerald-500'
                        }`}
                        style={{ width: `${projectedRatio}%` }}
                      />
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
