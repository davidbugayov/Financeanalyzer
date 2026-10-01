import React, { useState, useMemo } from 'react';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ReferenceLine,
} from 'recharts';
import {
  TrendingUp,
  TrendingDown,
  ArrowUpRight,
  ArrowDownLeft,
  Activity,
  Calendar,
  Sparkles,
  ShieldCheck,
  Zap,
  SlidersHorizontal,
  Info,
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { formatCurrency } from '../utils/financeCalculations';

interface SixMonthDataPoint {
  monthKey: string; // YYYY-MM
  label: string; // e.g. "Апр"
  fullLabel: string; // e.g. "Апрель 2026"
  year: number;
  monthIndex: number; // 0-11
  income: number;
  expense: number;
  net: number;
  savingsRate: number;
  txCount: number;
  isCurrentMonth: boolean;
  // Chart friendly aliases
  Доходы: number;
  Расходы: number;
  Сальдо: number;
}

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

export const IncomeExpenseTrendLineChart: React.FC<{ className?: string }> = ({
  className = '',
}) => {
  const { transactions, currency } = useFinance();

  const [showNet, setShowNet] = useState(true);
  const [showAverages, setShowAverages] = useState(true);
  const [selectedMonthKey, setSelectedMonthKey] = useState<string | null>(null);

  // Compute 6-month historical data points
  const sixMonthsData = useMemo<SixMonthDataPoint[]>(() => {
    const now = new Date();
    const currentYear = now.getFullYear();
    const currentMonth = now.getMonth();
    const currentMonthKey = `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}`;

    const points: SixMonthDataPoint[] = [];

    // Generate last 6 calendar months (chronological order)
    for (let i = 5; i >= 0; i--) {
      const d = new Date(currentYear, currentMonth - i, 1);
      const y = d.getFullYear();
      const m = d.getMonth();
      const monthKey = `${y}-${String(m + 1).padStart(2, '0')}`;

      // Aggregate transactions for this month
      let income = 0;
      let expense = 0;
      let txCount = 0;

      transactions.forEach((tx) => {
        if (!tx.date || !tx.date.startsWith(monthKey)) return;
        txCount++;
        if (tx.type === 'income') {
          income += tx.amount;
        } else if (tx.type === 'expense') {
          expense += tx.amount;
        }
      });

      const net = income - expense;
      const savingsRate = income > 0 ? Math.max(0, Math.round(((income - expense) / income) * 100)) : 0;

      points.push({
        monthKey,
        label: `${MONTH_NAMES_SHORT[m]} '${String(y).slice(-2)}`,
        fullLabel: `${MONTH_NAMES_FULL[m]} ${y}`,
        year: y,
        monthIndex: m,
        income,
        expense,
        net,
        savingsRate,
        txCount,
        isCurrentMonth: monthKey === currentMonthKey,
        Доходы: income,
        Расходы: expense,
        Сальдо: net,
      });
    }

    return points;
  }, [transactions]);

  // Summary Metrics over the 6 months
  const metrics = useMemo(() => {
    let totalIncome = 0;
    let totalExpense = 0;
    let totalNet = 0;
    let surplusMonthsCount = 0;
    let peakIncomeMonth = sixMonthsData[0];
    let peakExpenseMonth = sixMonthsData[0];

    sixMonthsData.forEach((point) => {
      totalIncome += point.income;
      totalExpense += point.expense;
      totalNet += point.net;
      if (point.net >= 0) surplusMonthsCount++;

      if (!peakIncomeMonth || point.income > peakIncomeMonth.income) {
        peakIncomeMonth = point;
      }
      if (!peakExpenseMonth || point.expense > peakExpenseMonth.expense) {
        peakExpenseMonth = point;
      }
    });

    const avgIncome = Math.round(totalIncome / 6);
    const avgExpense = Math.round(totalExpense / 6);
    const avgNet = Math.round(totalNet / 6);
    const overallSavingsRate =
      totalIncome > 0 ? Math.round(((totalIncome - totalExpense) / totalIncome) * 100) : 0;

    // Pattern Detection:
    // 1. First 3 months vs last 3 months trend
    const firstHalfIncome = (sixMonthsData[0]?.income || 0) + (sixMonthsData[1]?.income || 0) + (sixMonthsData[2]?.income || 0);
    const secondHalfIncome = (sixMonthsData[3]?.income || 0) + (sixMonthsData[4]?.income || 0) + (sixMonthsData[5]?.income || 0);
    const incomeGrowthPct =
      firstHalfIncome > 0 ? Math.round(((secondHalfIncome - firstHalfIncome) / firstHalfIncome) * 100) : 0;

    const firstHalfExpense = (sixMonthsData[0]?.expense || 0) + (sixMonthsData[1]?.expense || 0) + (sixMonthsData[2]?.expense || 0);
    const secondHalfExpense = (sixMonthsData[3]?.expense || 0) + (sixMonthsData[4]?.expense || 0) + (sixMonthsData[5]?.expense || 0);
    const expenseGrowthPct =
      firstHalfExpense > 0 ? Math.round(((secondHalfExpense - firstHalfExpense) / firstHalfExpense) * 100) : 0;

    // Financial Pattern Archetype
    let patternArchetype: {
      title: string;
      description: string;
      badgeClass: string;
      type: 'positive' | 'neutral' | 'attention';
    };

    if (surplusMonthsCount === 6 && incomeGrowthPct >= 0) {
      patternArchetype = {
        title: 'Устойчивый финансовый рост',
        description:
          '100% месяцев закрыты с профицитом. Доходы стабильно перекрывают расходы, формируя надежную подушку безопасности.',
        badgeClass: 'bg-emerald-100 text-emerald-800 border-emerald-200',
        type: 'positive',
      };
    } else if (expenseGrowthPct > incomeGrowthPct && expenseGrowthPct > 15) {
      patternArchetype = {
        title: 'Опережающий рост расходов',
        description:
          `За последние 3 месяца расходы выросли на ${expenseGrowthPct}%, опережая динамику доходов (+${incomeGrowthPct}%). Рекомендуется аудит категорий.`,
        badgeClass: 'bg-amber-100 text-amber-900 border-amber-200',
        type: 'attention',
      };
    } else if (surplusMonthsCount >= 4) {
      patternArchetype = {
        title: 'Контролируемый баланс',
        description:
          `В большинстве месяцев (${surplusMonthsCount} из 6) сохраняется положительное сальдо. Средняя норма сбережений составляет ${overallSavingsRate}%.`,
        badgeClass: 'bg-teal-100 text-teal-800 border-teal-200',
        type: 'positive',
      };
    } else {
      patternArchetype = {
        title: 'Колебания денежного потока',
        description:
          `В ${6 - surplusMonthsCount} месяцах расходы превышали поступления. Обратите внимание на сезонные пики и оптимизацию обязательных платежей.`,
        badgeClass: 'bg-rose-100 text-rose-800 border-rose-200',
        type: 'attention',
      };
    }

    return {
      totalIncome,
      totalExpense,
      totalNet,
      avgIncome,
      avgExpense,
      avgNet,
      overallSavingsRate,
      surplusMonthsCount,
      peakIncomeMonth,
      peakExpenseMonth,
      incomeGrowthPct,
      expenseGrowthPct,
      patternArchetype,
    };
  }, [sixMonthsData]);

  // Selected or active month detail
  const activeMonthDetail = useMemo(() => {
    if (!selectedMonthKey) {
      return sixMonthsData[sixMonthsData.length - 1]; // Default to current month
    }
    return sixMonthsData.find((m) => m.monthKey === selectedMonthKey) || sixMonthsData[sixMonthsData.length - 1];
  }, [selectedMonthKey, sixMonthsData]);

  const formatCompact = (val: number) => {
    if (Math.abs(val) >= 1_000_000) return `${(val / 1_000_000).toFixed(1)}M`;
    if (Math.abs(val) >= 1_000) return `${Math.round(val / 1_000)}k`;
    return String(val);
  };

  return (
    <div
      className={`bg-white rounded-3xl border border-slate-200/90 p-5 sm:p-6 shadow-xs space-y-5 transition-all ${className}`}
    >
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-slate-100">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-indigo-500 to-blue-500 text-white flex items-center justify-center shadow-xs shrink-0">
            <Activity size={22} className="stroke-[2.5]" />
          </div>
          <div>
            <div className="flex items-center gap-2 flex-wrap">
              <h3 className="text-base sm:text-lg font-black text-slate-900 tracking-tight">
                Тренды доходов и расходов за 6 месяцев
              </h3>
              <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-slate-100 text-slate-700 border border-slate-200/80 inline-flex items-center gap-1">
                <Calendar size={11} className="text-slate-500" />
                {sixMonthsData[0]?.label} — {sixMonthsData[5]?.label}
              </span>
            </div>
            <p className="text-xs text-slate-500 mt-0.5">
              Линейный анализ динамики денежных потоков и выявление финансовых паттернов
            </p>
          </div>
        </div>

        {/* Interactive Controls & Toggles */}
        <div className="flex items-center gap-2 self-start sm:self-auto flex-wrap">
          <button
            onClick={() => setShowNet((prev) => !prev)}
            className={`px-3 py-1.5 rounded-xl border text-xs font-bold transition-all cursor-pointer inline-flex items-center gap-1.5 ${
              showNet
                ? 'bg-indigo-50 text-indigo-800 border-indigo-200 shadow-2xs'
                : 'bg-slate-50 text-slate-600 border-slate-200 hover:bg-slate-100'
            }`}
          >
            <span
              className={`w-2 h-2 rounded-full ${
                showNet ? 'bg-indigo-600' : 'bg-slate-400'
              }`}
            />
            <span>Сальдо (Чистый итог)</span>
          </button>

          <button
            onClick={() => setShowAverages((prev) => !prev)}
            className={`px-3 py-1.5 rounded-xl border text-xs font-bold transition-all cursor-pointer inline-flex items-center gap-1.5 ${
              showAverages
                ? 'bg-slate-900 text-white border-slate-900 shadow-2xs'
                : 'bg-slate-50 text-slate-600 border-slate-200 hover:bg-slate-100'
            }`}
          >
            <SlidersHorizontal size={12} />
            <span>Средние уровни</span>
          </button>
        </div>
      </div>

      {/* Financial Pattern Insight Banner */}
      <div className="p-4 rounded-2xl bg-gradient-to-r from-slate-50 via-indigo-50/20 to-slate-50 border border-slate-200/90 text-xs space-y-2">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
          <div className="flex items-center gap-2 flex-wrap">
            <Sparkles size={16} className="text-indigo-600 shrink-0" />
            <span className="font-extrabold text-slate-900">Выявленный паттерн:</span>
            <span
              className={`px-2.5 py-0.5 rounded-lg border font-black text-[11px] ${metrics.patternArchetype.badgeClass}`}
            >
              {metrics.patternArchetype.title}
            </span>
          </div>

          <div className="flex items-center gap-3 text-slate-600 font-bold text-[11px] flex-wrap">
            <span className="inline-flex items-center gap-1">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
              Профицит: {metrics.surplusMonthsCount} из 6 мес.
            </span>
            <span className="inline-flex items-center gap-1">
              <span className="w-1.5 h-1.5 rounded-full bg-indigo-500" />
              Ср. норма сбережений: {metrics.overallSavingsRate}%
            </span>
          </div>
        </div>

        <p className="text-slate-600 font-medium leading-relaxed pl-6">
          {metrics.patternArchetype.description}
        </p>
      </div>

      {/* 4 Summary Metric Pills */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-2.5 sm:gap-3">
        {/* Income Card */}
        <div className="p-3.5 rounded-2xl bg-emerald-50/60 border border-emerald-200/80 flex flex-col justify-between">
          <div className="flex items-center justify-between text-[11px] font-bold text-emerald-800">
            <span className="uppercase tracking-wider">Всего доходов (6 мес)</span>
            <ArrowDownLeft size={14} className="text-emerald-600 stroke-[2.5]" />
          </div>
          <div className="text-lg sm:text-2xl font-black text-emerald-700 mt-1 whitespace-nowrap">
            +{formatCurrency(metrics.totalIncome, currency.symbol)}
          </div>
          <div className="text-[10px] font-semibold text-emerald-800/80 mt-1 flex items-center justify-between">
            <span>Ср: {formatCurrency(metrics.avgIncome, currency.symbol)}/мес</span>
            <span className="font-bold">
              {metrics.incomeGrowthPct >= 0 ? `+${metrics.incomeGrowthPct}%` : `${metrics.incomeGrowthPct}%`}
            </span>
          </div>
        </div>

        {/* Expense Card */}
        <div className="p-3.5 rounded-2xl bg-rose-50/60 border border-rose-200/80 flex flex-col justify-between">
          <div className="flex items-center justify-between text-[11px] font-bold text-rose-800">
            <span className="uppercase tracking-wider">Всего расходов (6 мес)</span>
            <ArrowUpRight size={14} className="text-rose-600 stroke-[2.5]" />
          </div>
          <div className="text-lg sm:text-2xl font-black text-rose-700 mt-1 whitespace-nowrap">
            -{formatCurrency(metrics.totalExpense, currency.symbol)}
          </div>
          <div className="text-[10px] font-semibold text-rose-800/80 mt-1 flex items-center justify-between">
            <span>Ср: {formatCurrency(metrics.avgExpense, currency.symbol)}/мес</span>
            <span className="font-bold">
              {metrics.expenseGrowthPct >= 0 ? `+${metrics.expenseGrowthPct}%` : `${metrics.expenseGrowthPct}%`}
            </span>
          </div>
        </div>

        {/* Net Cash Flow Card */}
        <div className="p-3.5 rounded-2xl bg-indigo-50/60 border border-indigo-200/80 flex flex-col justify-between">
          <div className="flex items-center justify-between text-[11px] font-bold text-indigo-800">
            <span className="uppercase tracking-wider">Накоплено (Чистое сальдо)</span>
            <ShieldCheck size={14} className="text-indigo-600 stroke-[2.5]" />
          </div>
          <div
            className={`text-lg sm:text-2xl font-black mt-1 whitespace-nowrap ${
              metrics.totalNet >= 0 ? 'text-indigo-700' : 'text-rose-700'
            }`}
          >
            {metrics.totalNet >= 0 ? '+' : ''}
            {formatCurrency(metrics.totalNet, currency.symbol)}
          </div>
          <div className="text-[10px] font-semibold text-indigo-800/80 mt-1 flex items-center justify-between">
            <span>Ср: {formatCurrency(metrics.avgNet, currency.symbol)}/мес</span>
            <span className="font-bold">Сальдо</span>
          </div>
        </div>

        {/* Peak/Benchmark Card */}
        <div className="p-3.5 rounded-2xl bg-slate-50 border border-slate-200/80 flex flex-col justify-between">
          <div className="flex items-center justify-between text-[11px] font-bold text-slate-500">
            <span className="uppercase tracking-wider">Пиковые показатели</span>
            <Zap size={14} className="text-amber-500 stroke-[2.5]" />
          </div>
          <div className="text-xs font-bold text-slate-800 mt-1 space-y-0.5">
            <div className="flex justify-between">
              <span className="text-slate-400">Пик дохода:</span>
              <span className="text-emerald-700 font-extrabold truncate max-w-[120px]">
                {metrics.peakIncomeMonth?.label} ({formatCurrency(metrics.peakIncomeMonth?.income || 0, currency.symbol)})
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-slate-400">Пик трат:</span>
              <span className="text-rose-700 font-extrabold truncate max-w-[120px]">
                {metrics.peakExpenseMonth?.label} ({formatCurrency(metrics.peakExpenseMonth?.expense || 0, currency.symbol)})
              </span>
            </div>
          </div>
          <div className="text-[10px] font-semibold text-slate-400 mt-1">
            Кликните по месяцу для детализации
          </div>
        </div>
      </div>

      {/* Main Recharts Line Chart */}
      <div className="h-80 sm:h-96 w-full pt-2">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart
            data={sixMonthsData}
            margin={{ top: 15, right: 15, left: -10, bottom: 5 }}
            onClick={(e) => {
              if (e && e.activePayload && e.activePayload.length > 0) {
                // eslint-disable-next-line @typescript-eslint/no-explicit-any
                const point = e.activePayload[0].payload as SixMonthDataPoint;
                setSelectedMonthKey(point.monthKey);
              }
            }}
          >
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
            <XAxis
              dataKey="label"
              stroke="#64748B"
              fontSize={12}
              tickLine={false}
              axisLine={{ stroke: '#CBD5E1' }}
            />
            <YAxis
              stroke="#64748B"
              fontSize={11}
              tickLine={false}
              axisLine={false}
              tickFormatter={formatCompact}
            />

            <Tooltip
              content={({ active, payload }) => {
                if (!active || !payload || !payload.length) return null;
                const data = payload[0].payload as SixMonthDataPoint;
                return (
                  <div className="bg-slate-900/95 backdrop-blur-md text-white p-3.5 rounded-2xl shadow-xl border border-slate-700/80 text-xs min-w-[210px] space-y-2 z-50 animate-in fade-in zoom-in-95 duration-150">
                    <div className="flex items-center justify-between border-b border-slate-800 pb-2">
                      <span className="font-extrabold text-sm">{data.fullLabel}</span>
                      {data.isCurrentMonth && (
                        <span className="text-[10px] font-black px-1.5 py-0.5 rounded-md bg-emerald-500/20 text-emerald-300 border border-emerald-500/40">
                          Текущий
                        </span>
                      )}
                    </div>

                    <div className="space-y-1.5 pt-0.5">
                      <div className="flex items-center justify-between text-emerald-400">
                        <span className="flex items-center gap-1.5">
                          <span className="w-2 h-2 rounded-full bg-emerald-400" />
                          <span>Доходы:</span>
                        </span>
                        <span className="font-black">
                          +{formatCurrency(data.income, currency.symbol)}
                        </span>
                      </div>

                      <div className="flex items-center justify-between text-rose-400">
                        <span className="flex items-center gap-1.5">
                          <span className="w-2 h-2 rounded-full bg-rose-400" />
                          <span>Расходы:</span>
                        </span>
                        <span className="font-black">
                          -{formatCurrency(data.expense, currency.symbol)}
                        </span>
                      </div>

                      <div className="flex items-center justify-between text-indigo-300 pt-1 border-t border-slate-800">
                        <span className="flex items-center gap-1.5">
                          <span className="w-2 h-2 rounded-full bg-indigo-400" />
                          <span>Сальдо (Чистый итог):</span>
                        </span>
                        <span className={`font-black ${data.net >= 0 ? 'text-indigo-300' : 'text-rose-400'}`}>
                          {data.net >= 0 ? '+' : ''}
                          {formatCurrency(data.net, currency.symbol)}
                        </span>
                      </div>

                      <div className="flex items-center justify-between text-slate-300 text-[11px] pt-1">
                        <span>Норма сбережений:</span>
                        <span className="font-bold text-white bg-slate-800 px-2 py-0.5 rounded-md">
                          {data.savingsRate}%
                        </span>
                      </div>
                    </div>
                  </div>
                );
              }}
            />

            <Legend
              verticalAlign="top"
              height={36}
              iconType="circle"
              wrapperStyle={{ fontSize: '12px', fontWeight: 600, paddingBottom: '10px' }}
            />

            {/* Reference Average Lines (Optional) */}
            {showAverages && (
              <>
                <ReferenceLine
                  y={metrics.avgIncome}
                  stroke="#10B981"
                  strokeDasharray="4 4"
                  strokeOpacity={0.6}
                  strokeWidth={1.5}
                />
                <ReferenceLine
                  y={metrics.avgExpense}
                  stroke="#F43F5E"
                  strokeDasharray="4 4"
                  strokeOpacity={0.6}
                  strokeWidth={1.5}
                />
              </>
            )}

            {/* Income Line */}
            <Line
              type="monotone"
              dataKey="Доходы"
              stroke="#10B981"
              strokeWidth={3}
              dot={{ r: 5, fill: '#10B981', strokeWidth: 2, stroke: '#FFFFFF' }}
              activeDot={{ r: 8, stroke: '#10B981', strokeWidth: 3, fill: '#FFFFFF' }}
              name="Доходы"
            />

            {/* Expense Line */}
            <Line
              type="monotone"
              dataKey="Расходы"
              stroke="#F43F5E"
              strokeWidth={3}
              dot={{ r: 5, fill: '#F43F5E', strokeWidth: 2, stroke: '#FFFFFF' }}
              activeDot={{ r: 8, stroke: '#F43F5E', strokeWidth: 3, fill: '#FFFFFF' }}
              name="Расходы"
            />

            {/* Net Savings Line (Toggleable) */}
            {showNet && (
              <Line
                type="monotone"
                dataKey="Сальдо"
                stroke="#6366F1"
                strokeWidth={2.5}
                strokeDasharray="5 5"
                dot={{ r: 4, fill: '#6366F1', strokeWidth: 2, stroke: '#FFFFFF' }}
                activeDot={{ r: 7, stroke: '#6366F1', strokeWidth: 2, fill: '#FFFFFF' }}
                name="Сальдо (Чистый итог)"
              />
            )}
          </LineChart>
        </ResponsiveContainer>
      </div>

      {/* Month-by-Month Clickable Detail Selector Cards */}
      <div className="pt-2 border-t border-slate-100 space-y-3">
        <div className="flex items-center justify-between">
          <span className="text-xs font-bold uppercase tracking-wider text-slate-500">
            Детализация по месяцам (кликните для выбора):
          </span>
          {activeMonthDetail && (
            <span className="text-xs font-bold text-slate-700">
              Выбран: <span className="text-indigo-600">{activeMonthDetail.fullLabel}</span>
            </span>
          )}
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-2">
          {sixMonthsData.map((m) => {
            const isSelected = activeMonthDetail.monthKey === m.monthKey;
            return (
              <button
                key={m.monthKey}
                onClick={() => setSelectedMonthKey(m.monthKey)}
                className={`p-2.5 rounded-2xl border text-left transition-all cursor-pointer ${
                  isSelected
                    ? 'bg-slate-900 text-white border-slate-900 shadow-md ring-2 ring-indigo-500/30 scale-102'
                    : 'bg-slate-50 hover:bg-slate-100/80 border-slate-200/80 text-slate-800'
                }`}
              >
                <div className="flex items-center justify-between mb-1">
                  <span className={`text-xs font-extrabold ${isSelected ? 'text-white' : 'text-slate-900'}`}>
                    {m.label}
                  </span>
                  <span
                    className={`text-[10px] font-black px-1 rounded-md ${
                      m.net >= 0
                        ? isSelected
                          ? 'bg-emerald-500/30 text-emerald-300'
                          : 'bg-emerald-100 text-emerald-800'
                        : isSelected
                        ? 'bg-rose-500/30 text-rose-300'
                        : 'bg-rose-100 text-rose-800'
                    }`}
                  >
                    {m.savingsRate}%
                  </span>
                </div>

                <div className="space-y-0.5 text-[11px]">
                  <div className="flex justify-between font-bold">
                    <span className={isSelected ? 'text-slate-400' : 'text-slate-500'}>Доход:</span>
                    <span className="text-emerald-500">+{formatCompact(m.income)}</span>
                  </div>
                  <div className="flex justify-between font-bold">
                    <span className={isSelected ? 'text-slate-400' : 'text-slate-500'}>Расход:</span>
                    <span className="text-rose-500">-{formatCompact(m.expense)}</span>
                  </div>
                  <div className="flex justify-between font-extrabold pt-1 border-t border-slate-200/40">
                    <span className={isSelected ? 'text-slate-400' : 'text-slate-500'}>Итог:</span>
                    <span className={m.net >= 0 ? (isSelected ? 'text-indigo-300' : 'text-indigo-600') : 'text-rose-500'}>
                      {m.net >= 0 ? '+' : ''}
                      {formatCompact(m.net)}
                    </span>
                  </div>
                </div>
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
};
