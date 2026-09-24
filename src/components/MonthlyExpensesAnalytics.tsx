import React, { useState, useMemo } from 'react';
import {
  ResponsiveContainer,
  ComposedChart,
  AreaChart,
  BarChart,
  Area,
  Bar,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ReferenceLine,
  Cell,
  Legend,
} from 'recharts';
import {
  TrendingUp,
  TrendingDown,
  Calendar,
  Layers,
  BarChart3,
  Activity,
  ArrowUpRight,
  ArrowDownLeft,
  PieChart as PieIcon,
  Sparkles,
  ChevronRight,
  Clock,
  Wallet,
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { formatCurrency } from '../utils/financeCalculations';
import { DynamicIcon } from '../utils/iconHelper';
import { InteractiveCategoryDistributionTable } from './InteractiveCategoryDistributionTable';

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
  monthKey: string; // YYYY-MM
  shortLabel: string;
  fullLabel: string;
  year: number;
  monthIndex: number;
  expense: number;
  income: number;
  net: number;
  txCount: number;
  isCurrentMonth: boolean;
  momPercent: number;
  savingsRate: number;
  topCategory: { name: string; amount: number } | null;
}

type ChartType = 'area' | 'bar' | 'balance';
type TimeHorizon = 6 | 12;

interface CustomTooltipProps {
  active?: boolean;
  payload?: any[];
  label?: string;
  currencySymbol: string;
  avgExpense: number;
}

const CustomAnalyticsTooltip: React.FC<CustomTooltipProps> = ({
  active,
  payload,
  currencySymbol,
  avgExpense,
}) => {
  if (!active || !payload || !payload.length) return null;

  const data: MonthlyDataPoint = payload[0].payload;
  const deviation =
    avgExpense > 0
      ? Math.round(((data.expense - avgExpense) / avgExpense) * 100)
      : 0;

  return (
    <div className="bg-white/95 backdrop-blur-md p-4 rounded-2xl shadow-xl border border-slate-200/90 text-xs min-w-[220px] z-50 animate-in fade-in zoom-in-95 duration-150">
      <div className="flex items-center justify-between border-b border-slate-100 pb-2 mb-2.5">
        <span className="font-extrabold text-slate-900 text-sm">{data.fullLabel}</span>
        {data.isCurrentMonth && (
          <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-800 border border-emerald-200">
            Текущий
          </span>
        )}
      </div>

      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <span className="text-slate-500 flex items-center gap-1.5">
            <span className="w-2.5 h-2.5 rounded-full bg-rose-500 shrink-0" />
            Расходы:
          </span>
          <strong className="text-rose-600 font-extrabold text-sm">
            -{formatCurrency(data.expense, currencySymbol)}
          </strong>
        </div>

        <div className="flex items-center justify-between">
          <span className="text-slate-500 flex items-center gap-1.5">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 shrink-0" />
            Доходы:
          </span>
          <strong className="text-emerald-600 font-extrabold">
            +{formatCurrency(data.income, currencySymbol)}
          </strong>
        </div>

        <div className="flex items-center justify-between pt-1.5 border-t border-slate-100">
          <span className="text-slate-500">Чистый баланс:</span>
          <strong
            className={`font-extrabold ${
              data.net >= 0 ? 'text-emerald-700' : 'text-rose-600'
            }`}
          >
            {data.net >= 0 ? '+' : ''}
            {formatCurrency(data.net, currencySymbol)}
          </strong>
        </div>

        <div className="flex items-center justify-between text-[11px] pt-1 border-t border-slate-100/70">
          <span className="text-slate-400">Отклонение от ср.:</span>
          <span
            className={`font-bold ${
              deviation > 0
                ? 'text-rose-600'
                : deviation < 0
                ? 'text-emerald-600'
                : 'text-slate-500'
            }`}
          >
            {deviation > 0 ? `+${deviation}% (выше ср.)` : `${deviation}% (ниже ср.)`}
          </span>
        </div>

        {data.topCategory && (
          <div className="flex items-center justify-between text-[11px] text-slate-400">
            <span>Главная статья:</span>
            <span className="font-semibold text-slate-700 truncate max-w-[120px]">
              {data.topCategory.name}
            </span>
          </div>
        )}
      </div>
    </div>
  );
};

export const MonthlyExpensesAnalytics: React.FC = () => {
  const { transactions, categories, currency } = useFinance();
  const [chartType, setChartType] = useState<ChartType>('area');
  const [timeHorizon, setTimeHorizon] = useState<TimeHorizon>(6);
  const [selectedMonthKey, setSelectedMonthKey] = useState<string | null>(null);

  // Compute monthly data for the specified time horizon
  const {
    monthlyData,
    avgExpense,
    maxExpensePoint,
    minExpensePoint,
    momTrendPercent,
    currentMonthPoint,
  } = useMemo(() => {
    const now = new Date();
    const points: MonthlyDataPoint[] = [];

    for (let i = timeHorizon - 1; i >= 0; i--) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      const year = d.getFullYear();
      const monthIndex = d.getMonth();
      const monthKey = `${year}-${String(monthIndex + 1).padStart(2, '0')}`;
      const shortLabel = MONTH_NAMES_SHORT[monthIndex];
      const fullLabel = `${MONTH_NAMES_FULL[monthIndex]} ${year}`;
      const isCurrentMonth = i === 0;

      // Filter transactions for this month
      const monthTxs = transactions.filter((t) => t.date && t.date.startsWith(monthKey));

      let expense = 0;
      let income = 0;
      let txCount = 0;
      const catSpendMap: { [cat: string]: number } = {};

      for (const t of monthTxs) {
        if (t.type === 'expense') {
          expense += t.amount;
          txCount++;
          const catName = t.category || 'Прочее';
          catSpendMap[catName] = (catSpendMap[catName] || 0) + t.amount;
        } else if (t.type === 'income') {
          income += t.amount;
        }
      }

      // Top spending category
      let topCat: { name: string; amount: number } | null = null;
      for (const [catName, amt] of Object.entries(catSpendMap)) {
        if (!topCat || amt > topCat.amount) {
          topCat = { name: catName, amount: amt };
        }
      }

      const savingsRate =
        income > 0 ? Math.max(0, Math.round(((income - expense) / income) * 100)) : 0;

      points.push({
        monthKey,
        shortLabel,
        fullLabel,
        year,
        monthIndex,
        expense,
        income,
        net: income - expense,
        txCount,
        isCurrentMonth,
        momPercent: 0,
        savingsRate,
        topCategory: topCat,
      });
    }

    // MoM % calculation
    for (let i = 1; i < points.length; i++) {
      const prev = points[i - 1].expense;
      const curr = points[i].expense;
      if (prev > 0) {
        points[i].momPercent = Math.round(((curr - prev) / prev) * 100);
      } else {
        points[i].momPercent = curr > 0 ? 100 : 0;
      }
    }

    const totalExp = points.reduce((sum, p) => sum + p.expense, 0);
    const avg = Math.round(totalExp / points.length);

    let maxP = points[0];
    let minP = points[0];
    for (const p of points) {
      if (p.expense > maxP.expense) maxP = p;
      if (p.expense < minP.expense) minP = p;
    }

    const currentP = points[points.length - 1];
    const prevP = points[points.length - 2];
    const momTrend =
      prevP && prevP.expense > 0
        ? Math.round(((currentP.expense - prevP.expense) / prevP.expense) * 100)
        : 0;

    return {
      monthlyData: points,
      avgExpense: avg,
      maxExpensePoint: maxP,
      minExpensePoint: minP,
      momTrendPercent: momTrend,
      currentMonthPoint: currentP,
    };
  }, [transactions, timeHorizon]);

  // Selected month detail data (defaults to current month or active selection)
  const activeSelectedMonth = useMemo(() => {
    if (selectedMonthKey) {
      const found = monthlyData.find((p) => p.monthKey === selectedMonthKey);
      if (found) return found;
    }
    return currentMonthPoint;
  }, [selectedMonthKey, monthlyData, currentMonthPoint]);

  // Category breakdown for selected month
  const selectedMonthCategories = useMemo(() => {
    if (!activeSelectedMonth) return [];

    const monthTxs = transactions.filter(
      (t) =>
        t.date &&
        t.date.startsWith(activeSelectedMonth.monthKey) &&
        t.type === 'expense'
    );

    const catMap: { [key: string]: { name: string; amount: number; count: number } } = {};
    for (const t of monthTxs) {
      const name = t.category || 'Прочее';
      if (!catMap[name]) {
        catMap[name] = { name, amount: 0, count: 0 };
      }
      catMap[name].amount += t.amount;
      catMap[name].count += 1;
    }

    const totalMonthExp = activeSelectedMonth.expense || 1;

    return Object.values(catMap)
      .map((item) => {
        const catInfo = categories.find((c) => c.name === item.name);
        return {
          ...item,
          color: catInfo?.color || '#64748B',
          icon: catInfo?.icon || 'Tag',
          percentage: Math.round((item.amount / totalMonthExp) * 100),
        };
      })
      .sort((a, b) => b.amount - a.amount);
  }, [activeSelectedMonth, transactions, categories]);

  return (
    <div className="bg-white rounded-3xl border border-slate-200/90 p-5 sm:p-6 shadow-xs space-y-5">
      {/* Top Header & Chart Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3.5">
        <div className="flex items-center gap-2.5">
          <div className="w-10 h-10 rounded-2xl bg-rose-50 text-rose-600 border border-rose-100 flex items-center justify-center shrink-0 shadow-2xs">
            <Activity size={20} className="stroke-[2.5]" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-base sm:text-lg font-black text-slate-900 tracking-tight">
                Помесячная динамика расходов
              </h3>
              <span className="hidden sm:inline-flex items-center gap-1 text-[11px] font-bold text-rose-700 bg-rose-50 border border-rose-200/80 px-2 py-0.5 rounded-full">
                Recharts
              </span>
            </div>
            <p className="text-xs text-slate-500">
              Визуализация трендов трат, отклонений от среднего и финансовой активности
            </p>
          </div>
        </div>

        {/* View Switchers: Type & Horizon */}
        <div className="flex flex-wrap items-center gap-2 self-start sm:self-auto">
          {/* Horizon Selector (6m / 12m) */}
          <div className="flex items-center bg-slate-100 p-1 rounded-xl text-xs font-bold text-slate-600 border border-slate-200/60">
            <button
              onClick={() => setTimeHorizon(6)}
              className={`px-2.5 py-1 rounded-lg transition-all ${
                timeHorizon === 6
                  ? 'bg-white text-slate-900 shadow-2xs font-extrabold'
                  : 'hover:text-slate-900 text-slate-500'
              }`}
            >
              6 мес
            </button>
            <button
              onClick={() => setTimeHorizon(12)}
              className={`px-2.5 py-1 rounded-lg transition-all ${
                timeHorizon === 12
                  ? 'bg-white text-slate-900 shadow-2xs font-extrabold'
                  : 'hover:text-slate-900 text-slate-500'
              }`}
            >
              12 мес (Год)
            </button>
          </div>

          {/* Chart Style Switcher */}
          <div className="flex items-center bg-slate-100 p-1 rounded-xl text-xs font-bold text-slate-600 border border-slate-200/60">
            <button
              onClick={() => setChartType('area')}
              title="Плавная область"
              className={`px-2.5 py-1 rounded-lg transition-all flex items-center gap-1 ${
                chartType === 'area'
                  ? 'bg-white text-rose-700 shadow-2xs font-extrabold'
                  : 'hover:text-slate-900 text-slate-500'
              }`}
            >
              <TrendingUp size={13} />
              <span>Область</span>
            </button>
            <button
              onClick={() => setChartType('bar')}
              title="Столбчатая диаграмма"
              className={`px-2.5 py-1 rounded-lg transition-all flex items-center gap-1 ${
                chartType === 'bar'
                  ? 'bg-white text-rose-700 shadow-2xs font-extrabold'
                  : 'hover:text-slate-900 text-slate-500'
              }`}
            >
              <BarChart3 size={13} />
              <span>Столбцы</span>
            </button>
            <button
              onClick={() => setChartType('balance')}
              title="Сравнение с доходами"
              className={`px-2.5 py-1 rounded-lg transition-all flex items-center gap-1 ${
                chartType === 'balance'
                  ? 'bg-white text-emerald-800 shadow-2xs font-extrabold'
                  : 'hover:text-slate-900 text-slate-500'
              }`}
            >
              <Layers size={13} />
              <span>С доходами</span>
            </button>
          </div>
        </div>
      </div>

      {/* KPI Insight Strip */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 sm:gap-3">
        {/* Card 1: Средний расход */}
        <div className="p-3 sm:p-3.5 rounded-2xl bg-slate-50 border border-slate-200/80">
          <span className="text-[10px] sm:text-[11px] font-bold text-slate-400 block uppercase tracking-wider mb-0.5">
            Средний расход
          </span>
          <div className="text-base sm:text-lg font-black text-slate-900">
            {formatCurrency(avgExpense, currency.symbol)}
          </div>
          <span className="text-[10px] text-slate-400 font-medium">
            в мес (за {timeHorizon} мес)
          </span>
        </div>

        {/* Card 2: Текущий месяц & MoM */}
        <div className="p-3 sm:p-3.5 rounded-2xl bg-rose-50/60 border border-rose-200/70">
          <div className="flex items-center justify-between mb-0.5">
            <span className="text-[10px] sm:text-[11px] font-bold text-rose-700 uppercase tracking-wider">
              Текущий месяц
            </span>
            {momTrendPercent !== 0 && (
              <span
                className={`text-[10px] font-extrabold flex items-center gap-0.5 ${
                  momTrendPercent > 0 ? 'text-rose-600' : 'text-emerald-700'
                }`}
              >
                {momTrendPercent > 0 ? '+' : ''}
                {momTrendPercent}%
              </span>
            )}
          </div>
          <div className="text-base sm:text-lg font-black text-rose-600">
            {formatCurrency(currentMonthPoint?.expense || 0, currency.symbol)}
          </div>
          <span className="text-[10px] text-rose-800/80 font-medium">
            {avgExpense > 0
              ? `${Math.round(
                  ((currentMonthPoint?.expense || 0) / avgExpense) * 100
                )}% от среднего`
              : 'Расчет...'}
          </span>
        </div>

        {/* Card 3: Пик расходов */}
        <div className="p-3 sm:p-3.5 rounded-2xl bg-slate-50 border border-slate-200/80">
          <span className="text-[10px] sm:text-[11px] font-bold text-slate-400 block uppercase tracking-wider mb-0.5">
            Пик расходов
          </span>
          <div className="text-base sm:text-lg font-black text-slate-900">
            {formatCurrency(maxExpensePoint?.expense || 0, currency.symbol)}
          </div>
          <span className="text-[10px] text-slate-400 font-medium truncate block">
            {maxExpensePoint?.fullLabel}
          </span>
        </div>

        {/* Card 4: Минимум трат */}
        <div className="p-3 sm:p-3.5 rounded-2xl bg-emerald-50/60 border border-emerald-200/70">
          <span className="text-[10px] sm:text-[11px] font-bold text-emerald-800 block uppercase tracking-wider mb-0.5">
            Минимум трат
          </span>
          <div className="text-base sm:text-lg font-black text-emerald-700">
            {formatCurrency(minExpensePoint?.expense || 0, currency.symbol)}
          </div>
          <span className="text-[10px] text-emerald-800/70 font-medium truncate block">
            {minExpensePoint?.fullLabel}
          </span>
        </div>
      </div>

      {/* Main Recharts Visualization Canvas */}
      <div className="relative pt-2">
        <div className="h-64 sm:h-72 w-full">
          <ResponsiveContainer width="100%" height="100%">
            {chartType === 'area' ? (
              <AreaChart
                data={monthlyData}
                margin={{ top: 12, right: 8, left: -14, bottom: 0 }}
                onClick={(e) => {
                  if (e && e.activePayload && e.activePayload.length) {
                    setSelectedMonthKey(e.activePayload[0].payload.monthKey);
                  }
                }}
              >
                <defs>
                  <linearGradient id="statsExpenseGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#F43F5E" stopOpacity={0.4} />
                    <stop offset="60%" stopColor="#F43F5E" stopOpacity={0.12} />
                    <stop offset="100%" stopColor="#F43F5E" stopOpacity={0.0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                <XAxis
                  dataKey="shortLabel"
                  tickLine={false}
                  axisLine={{ stroke: '#CBD5E1' }}
                  tick={{ fontSize: 11, fill: '#64748B', fontWeight: 600 }}
                />
                <YAxis
                  tickLine={false}
                  axisLine={false}
                  tick={{ fontSize: 10, fill: '#94A3B8' }}
                  tickFormatter={(val) =>
                    val >= 1000 ? `${Math.round(val / 1000)}k` : `${val}`
                  }
                />
                <Tooltip
                  content={
                    <CustomAnalyticsTooltip
                      currencySymbol={currency.symbol}
                      avgExpense={avgExpense}
                    />
                  }
                />
                <ReferenceLine
                  y={avgExpense}
                  stroke="#94A3B8"
                  strokeDasharray="4 4"
                  label={{
                    value: `Ср. ${Math.round(avgExpense / 1000)}k`,
                    fill: '#64748B',
                    fontSize: 10,
                    fontWeight: 700,
                    position: 'insideTopRight',
                  }}
                />
                <Area
                  type="monotone"
                  dataKey="expense"
                  stroke="#E11D48"
                  strokeWidth={2.8}
                  fillOpacity={1}
                  fill="url(#statsExpenseGradient)"
                  activeDot={{
                    r: 6,
                    fill: '#E11D48',
                    stroke: '#FFFFFF',
                    strokeWidth: 3,
                  }}
                />
              </AreaChart>
            ) : chartType === 'bar' ? (
              <BarChart
                data={monthlyData}
                margin={{ top: 12, right: 8, left: -14, bottom: 0 }}
                onClick={(e) => {
                  if (e && e.activePayload && e.activePayload.length) {
                    setSelectedMonthKey(e.activePayload[0].payload.monthKey);
                  }
                }}
              >
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                <XAxis
                  dataKey="shortLabel"
                  tickLine={false}
                  axisLine={{ stroke: '#CBD5E1' }}
                  tick={{ fontSize: 11, fill: '#64748B', fontWeight: 600 }}
                />
                <YAxis
                  tickLine={false}
                  axisLine={false}
                  tick={{ fontSize: 10, fill: '#94A3B8' }}
                  tickFormatter={(val) =>
                    val >= 1000 ? `${Math.round(val / 1000)}k` : `${val}`
                  }
                />
                <Tooltip
                  content={
                    <CustomAnalyticsTooltip
                      currencySymbol={currency.symbol}
                      avgExpense={avgExpense}
                    />
                  }
                />
                <ReferenceLine
                  y={avgExpense}
                  stroke="#94A3B8"
                  strokeDasharray="4 4"
                  label={{
                    value: `Ср. ${Math.round(avgExpense / 1000)}k`,
                    fill: '#64748B',
                    fontSize: 10,
                    fontWeight: 700,
                    position: 'insideTopRight',
                  }}
                />
                <Bar dataKey="expense" radius={[6, 6, 0, 0]}>
                  {monthlyData.map((entry) => {
                    const isSelected = activeSelectedMonth?.monthKey === entry.monthKey;
                    const isOverAvg = entry.expense > avgExpense;
                    return (
                      <Cell
                        key={`cell-${entry.monthKey}`}
                        fill={
                          isSelected
                            ? '#BE123C'
                            : isOverAvg
                            ? '#F43F5E'
                            : '#FDA4AF'
                        }
                        className="cursor-pointer transition-all duration-150 hover:opacity-85"
                      />
                    );
                  })}
                </Bar>
              </BarChart>
            ) : (
              <ComposedChart
                data={monthlyData}
                margin={{ top: 12, right: 8, left: -14, bottom: 0 }}
                onClick={(e) => {
                  if (e && e.activePayload && e.activePayload.length) {
                    setSelectedMonthKey(e.activePayload[0].payload.monthKey);
                  }
                }}
              >
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                <XAxis
                  dataKey="shortLabel"
                  tickLine={false}
                  axisLine={{ stroke: '#CBD5E1' }}
                  tick={{ fontSize: 11, fill: '#64748B', fontWeight: 600 }}
                />
                <YAxis
                  tickLine={false}
                  axisLine={false}
                  tick={{ fontSize: 10, fill: '#94A3B8' }}
                  tickFormatter={(val) =>
                    val >= 1000 ? `${Math.round(val / 1000)}k` : `${val}`
                  }
                />
                <Tooltip
                  content={
                    <CustomAnalyticsTooltip
                      currencySymbol={currency.symbol}
                      avgExpense={avgExpense}
                    />
                  }
                />
                <Legend
                  verticalAlign="top"
                  align="right"
                  wrapperStyle={{ fontSize: 11, paddingBottom: 6 }}
                />
                <Bar
                  dataKey="income"
                  name="Доходы"
                  fill="#10B981"
                  radius={[5, 5, 0, 0]}
                  barSize={16}
                />
                <Bar
                  dataKey="expense"
                  name="Расходы"
                  fill="#F43F5E"
                  radius={[5, 5, 0, 0]}
                  barSize={16}
                />
                <Line
                  type="monotone"
                  dataKey="net"
                  name="Сальдо"
                  stroke="#3B82F6"
                  strokeWidth={2.5}
                  dot={{ r: 3, fill: '#3B82F6' }}
                />
              </ComposedChart>
            )}
          </ResponsiveContainer>
        </div>

        {/* Month selector chips */}
        <div className="flex items-center gap-1.5 overflow-x-auto py-2 px-0.5 no-scrollbar">
          <span className="text-[11px] font-bold text-slate-400 whitespace-nowrap mr-1">
            Выбрать месяц:
          </span>
          {monthlyData.map((m) => {
            const isSelected = activeSelectedMonth?.monthKey === m.monthKey;
            return (
              <button
                key={m.monthKey}
                onClick={() => setSelectedMonthKey(m.monthKey)}
                className={`px-2.5 py-1 rounded-xl text-xs font-bold transition-all whitespace-nowrap flex items-center gap-1.5 shrink-0 ${
                  isSelected
                    ? 'bg-rose-600 text-white shadow-xs scale-102'
                    : 'bg-slate-100 hover:bg-slate-200/80 text-slate-600'
                }`}
              >
                <span>{m.shortLabel}</span>
                {m.isCurrentMonth && (
                  <span
                    className={`w-1.5 h-1.5 rounded-full ${
                      isSelected ? 'bg-white' : 'bg-emerald-500'
                    }`}
                  />
                )}
              </button>
            );
          })}
        </div>
      </div>

      {/* Interactive Category Distribution Legend & Table Section */}
      <InteractiveCategoryDistributionTable
        activeMonth={activeSelectedMonth}
        timeHorizon={timeHorizon}
        monthlyData={monthlyData}
        currencySymbol={currency.symbol}
      />
    </div>
  );
};
