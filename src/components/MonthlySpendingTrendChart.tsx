import React, { useState, useMemo } from 'react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ReferenceLine,
} from 'recharts';
import {
  TrendingUp,
  TrendingDown,
  Activity,
  Calendar,
  Layers,
  Sparkles,
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { formatCurrency } from '../utils/financeCalculations';

const MONTH_NAMES_SHORT = [
  'Янв',
  'Фев',
  'Мар',
  'Апр',
  'Май',
  'Июн',
  'Июл',
  'Авг',
  'Сен',
  'Окт',
  'Ноя',
  'Дек',
];

const MONTH_NAMES_FULL = [
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

interface MonthlyDataPoint {
  monthKey: string;
  shortName: string;
  fullName: string;
  year: number;
  monthIndex: number;
  expense: number;
  income: number;
  net: number;
  txCount: number;
  isCurrentMonth: boolean;
  momChangePercent: number;
}

interface CustomTooltipProps {
  active?: boolean;
  payload?: any[];
  label?: string;
  currencySymbol: string;
  avgExpense: number;
  showIncome: boolean;
}

const CustomTooltip: React.FC<CustomTooltipProps> = ({
  active,
  payload,
  currencySymbol,
  avgExpense,
  showIncome,
}) => {
  if (!active || !payload || !payload.length) return null;

  const data: MonthlyDataPoint = payload[0].payload;
  const deviationFromAvg =
    avgExpense > 0
      ? Math.round(((data.expense - avgExpense) / avgExpense) * 100)
      : 0;

  return (
    <div className="bg-white/95 backdrop-blur-md p-3.5 rounded-2xl shadow-xl border border-slate-200/90 text-xs min-w-[200px] z-50 animate-in fade-in zoom-in-95 duration-150">
      <div className="flex items-center justify-between border-b border-slate-100 pb-2 mb-2">
        <span className="font-extrabold text-slate-900 text-sm">{data.fullName}</span>
        {data.isCurrentMonth && (
          <span className="text-[10px] font-bold px-1.5 py-0.5 rounded-md bg-emerald-50 text-emerald-700 border border-emerald-200">
            Текущий
          </span>
        )}
      </div>

      <div className="space-y-1.5">
        {/* Expense row */}
        <div className="flex items-center justify-between">
          <span className="text-slate-500 flex items-center gap-1.5">
            <span className="w-2.5 h-2.5 rounded-full bg-rose-500 shrink-0" />
            Расход:
          </span>
          <strong className="text-slate-900 font-extrabold">
            {formatCurrency(data.expense, currencySymbol)}
          </strong>
        </div>

        {/* Income row (if enabled) */}
        {showIncome && (
          <div className="flex items-center justify-between">
            <span className="text-slate-500 flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 shrink-0" />
              Доход:
            </span>
            <strong className="text-emerald-700 font-extrabold">
              +{formatCurrency(data.income, currencySymbol)}
            </strong>
          </div>
        )}

        {/* Deviation from 6m average */}
        <div className="flex items-center justify-between pt-1 border-t border-slate-100 text-[11px]">
          <span className="text-slate-400">Отклонение от ср.:</span>
          <span
            className={`font-bold flex items-center gap-0.5 ${
              deviationFromAvg > 0
                ? 'text-rose-600'
                : deviationFromAvg < 0
                ? 'text-emerald-600'
                : 'text-slate-600'
            }`}
          >
            {deviationFromAvg > 0 ? '+' : ''}
            {deviationFromAvg}%
          </span>
        </div>

        {/* Transaction count */}
        <div className="flex items-center justify-between text-[11px] text-slate-400">
          <span>Операций расхода:</span>
          <span className="font-semibold text-slate-600">{data.txCount}</span>
        </div>
      </div>
    </div>
  );
};

export const MonthlySpendingTrendChart: React.FC = () => {
  const { transactions, currency } = useFinance();
  const [showIncome, setShowIncome] = useState(false);

  // Compute 6-month historical data points dynamically
  const {
    monthlyData,
    avgExpense,
    currentMonthData,
    previousMonthData,
    momTrendPercent,
    maxExpenseMonth,
    minExpenseMonth,
    trendDirection,
  } = useMemo(() => {
    const now = new Date();
    const points: MonthlyDataPoint[] = [];

    for (let i = 5; i >= 0; i--) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      const year = d.getFullYear();
      const monthIndex = d.getMonth();
      const monthKey = `${year}-${String(monthIndex + 1).padStart(2, '0')}`;
      const shortName = MONTH_NAMES_SHORT[monthIndex];
      const fullName = `${MONTH_NAMES_FULL[monthIndex]} ${year}`;
      const isCurrentMonth = i === 0;

      // Filter transactions for this specific month
      const monthTxs = transactions.filter((t) => {
        if (!t.date) return false;
        // Compare year and month
        return t.date.startsWith(monthKey);
      });

      let expense = 0;
      let income = 0;
      let txCount = 0;

      for (const t of monthTxs) {
        if (t.type === 'expense') {
          expense += t.amount;
          txCount++;
        } else if (t.type === 'income') {
          income += t.amount;
        }
      }

      points.push({
        monthKey,
        shortName,
        fullName,
        year,
        monthIndex,
        expense,
        income,
        net: income - expense,
        txCount,
        isCurrentMonth,
        momChangePercent: 0,
      });
    }

    // Calculate month-over-month percent change for each point
    for (let i = 1; i < points.length; i++) {
      const prev = points[i - 1].expense;
      const curr = points[i].expense;
      if (prev > 0) {
        points[i].momChangePercent = Math.round(((curr - prev) / prev) * 100);
      } else {
        points[i].momChangePercent = curr > 0 ? 100 : 0;
      }
    }

    const totalExpense6m = points.reduce((acc, p) => acc + p.expense, 0);
    const avg = Math.round(totalExpense6m / 6);

    const currentMonth = points[points.length - 1];
    const previousMonth = points[points.length - 2];

    const momPercent =
      previousMonth && previousMonth.expense > 0
        ? Math.round(
            ((currentMonth.expense - previousMonth.expense) /
              previousMonth.expense) *
              100
          )
        : 0;

    // Find max and min spending months
    let max = points[0];
    let min = points[0];
    for (const p of points) {
      if (p.expense > max.expense) max = p;
      if (p.expense < min.expense) min = p;
    }

    // Determine overall trend: compare last 3 months vs previous 3 months
    const firstHalfAvg = (points[0].expense + points[1].expense + points[2].expense) / 3;
    const secondHalfAvg = (points[3].expense + points[4].expense + points[5].expense) / 3;
    let direction: 'up' | 'down' | 'stable' = 'stable';
    if (secondHalfAvg > firstHalfAvg * 1.05) {
      direction = 'up';
    } else if (secondHalfAvg < firstHalfAvg * 0.95) {
      direction = 'down';
    }

    return {
      monthlyData: points,
      avgExpense: avg,
      currentMonthData: currentMonth,
      previousMonthData: previousMonth,
      momTrendPercent: momPercent,
      maxExpenseMonth: max,
      minExpenseMonth: min,
      trendDirection: direction,
    };
  }, [transactions]);

  // Format compact numbers for Y-axis (e.g. 50k, 100k, 1.2M)
  const formatYAxis = (value: number): string => {
    if (value === 0) return '0';
    if (value >= 1_000_000) {
      return `${(value / 1_000_000).toFixed(1)}M`;
    }
    if (value >= 1_000) {
      return `${Math.round(value / 1_000)}k`;
    }
    return String(value);
  };

  const hasAnyExpenses = monthlyData.some((m) => m.expense > 0);

  return (
    <div
      id="dashboard_spending_trend_card"
      className="bg-white rounded-3xl border border-slate-200/90 p-5 sm:p-6 shadow-xs relative overflow-hidden space-y-4"
    >
      {/* Header with Title and Mode Switcher */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-rose-50 text-rose-600 border border-rose-100 flex items-center justify-center shrink-0 shadow-2xs">
            <Activity size={20} className="stroke-[2.3]" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-base font-extrabold text-slate-900">
                Тренд расходов за 6 месяцев
              </h3>
              {trendDirection === 'down' && (
                <span className="hidden sm:inline-flex items-center gap-1 text-[11px] font-bold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full border border-emerald-200">
                  <TrendingDown size={13} />
                  Снижение трат
                </span>
              )}
              {trendDirection === 'up' && (
                <span className="hidden sm:inline-flex items-center gap-1 text-[11px] font-bold text-rose-700 bg-rose-50 px-2 py-0.5 rounded-full border border-rose-200">
                  <TrendingUp size={13} />
                  Рост расходов
                </span>
              )}
            </div>
            <p className="text-xs text-slate-400">
              Колебания и ежемесячная динамика семейного бюджета
            </p>
          </div>
        </div>

        {/* Right side: Income compare toggle & MoM badge */}
        <div className="flex items-center gap-2 self-start sm:self-auto flex-wrap">
          <button
            id="toggle_trend_compare_income"
            type="button"
            onClick={() => setShowIncome((prev) => !prev)}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5 cursor-pointer border ${
              showIncome
                ? 'bg-emerald-50 text-emerald-800 border-emerald-300 shadow-2xs'
                : 'bg-slate-50 text-slate-600 border-slate-200 hover:bg-slate-100'
            }`}
            title="Показать линию доходов для сравнения"
          >
            <Layers size={13} />
            <span>{showIncome ? 'С доходами' : '+ Сравнить с доходом'}</span>
          </button>

          {momTrendPercent !== 0 && (
            <div
              className={`flex items-center gap-1 px-2.5 py-1.5 rounded-xl text-xs font-bold border ${
                momTrendPercent < 0
                  ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                  : 'bg-rose-50 text-rose-700 border-rose-200'
              }`}
              title="Изменение трат текущего месяца по отношению к предыдущему"
            >
              {momTrendPercent < 0 ? (
                <TrendingDown size={14} className="stroke-[2.5]" />
              ) : (
                <TrendingUp size={14} className="stroke-[2.5]" />
              )}
              <span>
                {momTrendPercent > 0 ? '+' : ''}
                {momTrendPercent}% к пред. мес.
              </span>
            </div>
          )}
        </div>
      </div>

      {/* Summary KPI Badges Row */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 pt-1">
        {/* 1. Average spending */}
        <div className="bg-slate-50/80 rounded-2xl p-3 border border-slate-100">
          <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-0.5">
            Средний расход
          </span>
          <span className="text-sm sm:text-base font-black text-slate-900 whitespace-nowrap block">
            {formatCurrency(avgExpense, currency.symbol)}
          </span>
          <span className="text-[10px] text-slate-400 font-medium">в месяц (за 6 мес)</span>
        </div>

        {/* 2. Current Month */}
        <div className="bg-rose-50/60 rounded-2xl p-3 border border-rose-100/80">
          <span className="text-[10px] font-bold uppercase tracking-wider text-rose-700 block mb-0.5">
            Текущий месяц
          </span>
          <span className="text-sm sm:text-base font-black text-rose-600 whitespace-nowrap block">
            {formatCurrency(currentMonthData.expense, currency.symbol)}
          </span>
          <span className="text-[10px] text-rose-600/80 font-medium">
            {avgExpense > 0
              ? `${Math.round((currentMonthData.expense / avgExpense) * 100)}% от среднего`
              : 'текущий период'}
          </span>
        </div>

        {/* 3. Peak Month */}
        <div className="bg-slate-50/80 rounded-2xl p-3 border border-slate-100">
          <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-0.5">
            Пик расходов
          </span>
          <span className="text-sm sm:text-base font-black text-slate-800 whitespace-nowrap block">
            {formatCurrency(maxExpenseMonth.expense, currency.symbol)}
          </span>
          <span className="text-[10px] text-slate-500 font-semibold truncate block">
            {maxExpenseMonth.fullName}
          </span>
        </div>

        {/* 4. Minimum Month */}
        <div className="bg-slate-50/80 rounded-2xl p-3 border border-slate-100">
          <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-0.5">
            Минимум трат
          </span>
          <span className="text-sm sm:text-base font-black text-emerald-700 whitespace-nowrap block">
            {formatCurrency(minExpenseMonth.expense, currency.symbol)}
          </span>
          <span className="text-[10px] text-slate-500 font-semibold truncate block">
            {minExpenseMonth.fullName}
          </span>
        </div>
      </div>

      {/* Main Recharts Trend Line / Area Chart */}
      <div className="h-60 sm:h-72 w-full pt-2">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart
            data={monthlyData}
            margin={{ top: 12, right: 12, left: -16, bottom: 0 }}
          >
            <defs>
              {/* Soft Rose Gradient for Expenses Area */}
              <linearGradient id="spendingGradient" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#F43F5E" stopOpacity={0.28} />
                <stop offset="65%" stopColor="#F43F5E" stopOpacity={0.06} />
                <stop offset="100%" stopColor="#F43F5E" stopOpacity={0.0} />
              </linearGradient>

              {/* Soft Emerald Gradient for Income Area (when enabled) */}
              <linearGradient id="incomeGradient" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#10B981" stopOpacity={0.2} />
                <stop offset="100%" stopColor="#10B981" stopOpacity={0.0} />
              </linearGradient>
            </defs>

            <CartesianGrid
              strokeDasharray="3 3"
              stroke="#f1f5f9"
              vertical={false}
            />

            <XAxis
              dataKey="shortName"
              tick={{ fill: '#64748b', fontSize: 11, fontWeight: 600 }}
              axisLine={{ stroke: '#e2e8f0' }}
              tickLine={false}
              dy={6}
            />

            <YAxis
              tick={{ fill: '#94a3b8', fontSize: 10 }}
              tickFormatter={formatYAxis}
              axisLine={false}
              tickLine={false}
              dx={-4}
            />

            <Tooltip
              content={
                <CustomTooltip
                  currencySymbol={currency.symbol}
                  avgExpense={avgExpense}
                  showIncome={showIncome}
                />
              }
            />

            {/* Reference Line for 6-Month Average Spending */}
            {avgExpense > 0 && (
              <ReferenceLine
                y={avgExpense}
                stroke="#94a3b8"
                strokeDasharray="4 4"
                strokeWidth={1.5}
                label={{
                  value: `Ср. ${formatYAxis(avgExpense)}`,
                  position: 'insideTopRight',
                  fill: '#94a3b8',
                  fontSize: 10,
                  fontWeight: 600,
                }}
              />
            )}

            {/* Optional Income Area & Line */}
            {showIncome && (
              <Area
                type="monotone"
                dataKey="income"
                stroke="#10B981"
                strokeWidth={2}
                strokeDasharray="4 3"
                fill="url(#incomeGradient)"
                name="Доход"
                activeDot={{
                  r: 5,
                  fill: '#10B981',
                  stroke: '#ffffff',
                  strokeWidth: 2,
                }}
              />
            )}

            {/* Primary Spending Trend Line and Smooth Area */}
            <Area
              type="monotone"
              dataKey="expense"
              stroke="#E11D48"
              strokeWidth={3}
              fill="url(#spendingGradient)"
              name="Расход"
              activeDot={{
                r: 6,
                fill: '#E11D48',
                stroke: '#ffffff',
                strokeWidth: 2.5,
                className: 'drop-shadow-md',
              }}
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      {/* Analytical Footer Note */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pt-2 border-t border-slate-100 text-xs text-slate-500">
        <div className="flex items-center gap-1.5">
          <Calendar size={13} className="text-slate-400 shrink-0" />
          <span>
            Период:{' '}
            <strong className="text-slate-700 font-semibold">
              {monthlyData[0]?.fullName} — {monthlyData[5]?.fullName}
            </strong>
          </span>
        </div>

        <div className="flex items-center gap-1 text-[11px] text-slate-400">
          <Sparkles size={12} className="text-amber-500 shrink-0" />
          <span>
            {hasAnyExpenses
              ? `Размах вариаций: ${formatCurrency(
                  Math.max(0, maxExpenseMonth.expense - minExpenseMonth.expense),
                  currency.symbol
                )}`
              : 'Для отображения тренда записывайте расходы'}
          </span>
        </div>
      </div>
    </div>
  );
};
