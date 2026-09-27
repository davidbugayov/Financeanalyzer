import React, { useState } from 'react';
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
  Calendar,
  Sparkles,
  Info,
  CheckCircle2,
  AlertCircle,
  Repeat,
  Layers,
  ArrowRight,
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { formatCurrency } from '../utils/financeCalculations';
import { SpendingChartPoint } from '../types';

export const SpendingForecastLineChart: React.FC = () => {
  const { spendingProjection, currency } = useFinance();
  const [showBounds, setShowBounds] = useState<boolean>(true);

  const {
    combinedChartData,
    projectedMonths,
    totalThreeMonthProjected,
    averageMonthlyProjected,
    trendPercentage,
    recurringCommitmentsTotal,
    methodology,
  } = spendingProjection;

  // Find the anchor point where forecast begins
  const anchorPoint = combinedChartData.find((p) => p.isAnchor);
  const anchorLabel = anchorPoint ? anchorPoint.label : '';

  // Custom Tooltip component for rich forecast details
  const CustomTooltip = ({ active, payload }: any) => {
    if (!active || !payload || !payload.length) return null;

    const data: SpendingChartPoint = payload[0]?.payload;
    if (!data) return null;

    return (
      <div className="bg-white/95 backdrop-blur-md p-3.5 rounded-2xl shadow-xl border border-slate-200 text-xs min-w-[220px] z-50">
        <div className="flex items-center justify-between border-b border-slate-100 pb-2 mb-2">
          <span className="font-extrabold text-slate-900 text-sm">{data.fullLabel}</span>
          {data.isForecast ? (
            <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-indigo-50 text-indigo-700 border border-indigo-200">
              Прогноз
            </span>
          ) : data.isAnchor ? (
            <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">
              Текущий месяц
            </span>
          ) : (
            <span className="text-[10px] font-medium px-2 py-0.5 rounded-full bg-slate-100 text-slate-600">
              Факт
            </span>
          )}
        </div>

        <div className="space-y-1.5">
          {data.actualExpense !== undefined && !data.isForecast && (
            <div className="flex justify-between items-center">
              <span className="text-slate-500 flex items-center gap-1.5">
                <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 inline-block" />
                Фактический расход:
              </span>
              <span className="font-black text-slate-900">
                {formatCurrency(data.actualExpense, currency.symbol)}
              </span>
            </div>
          )}

          {data.projectedExpense !== undefined && (
            <div className="flex justify-between items-center">
              <span className="text-slate-500 flex items-center gap-1.5">
                <span className="w-2.5 h-2.5 rounded-full bg-indigo-600 inline-block" />
                {data.isAnchor ? 'Прогноз / Факт на сегодня:' : 'Ожидаемый расход:'}
              </span>
              <span className="font-black text-indigo-700">
                {formatCurrency(data.projectedExpense, currency.symbol)}
              </span>
            </div>
          )}

          {data.isForecast && data.lowerBound && data.upperBound && (
            <div className="mt-2 pt-2 border-t border-slate-100 space-y-1">
              <div className="flex justify-between items-center text-[11px]">
                <span className="text-cyan-700">Оптимистичный сценарий:</span>
                <span className="font-bold text-cyan-800">
                  {formatCurrency(data.lowerBound, currency.symbol)}
                </span>
              </div>
              <div className="flex justify-between items-center text-[11px]">
                <span className="text-rose-600">Верхняя планка (риск):</span>
                <span className="font-bold text-rose-700">
                  {formatCurrency(data.upperBound, currency.symbol)}
                </span>
              </div>
            </div>
          )}
        </div>
      </div>
    );
  };

  return (
    <div
      id="spending_forecast_section"
      className="bg-white rounded-3xl border border-slate-200 p-5 sm:p-6 shadow-xs relative overflow-hidden transition-all space-y-5"
    >
      {/* Background ambient gradient */}
      <div className="absolute top-0 right-0 w-80 h-80 bg-gradient-to-br from-indigo-500/10 via-purple-500/5 to-transparent rounded-full blur-3xl pointer-events-none" />

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 relative z-10 border-b border-slate-100 pb-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-indigo-600 via-indigo-700 to-purple-600 text-white flex items-center justify-center shadow-sm shrink-0">
            <TrendingUp size={22} className="stroke-[2.2]" />
          </div>
          <div>
            <div className="flex items-center gap-2 flex-wrap">
              <h3 className="text-base sm:text-lg font-black text-slate-900 tracking-tight">
                Прогноз расходов на 3 месяца
              </h3>
              <span className="inline-flex items-center gap-1 text-[10px] font-black uppercase tracking-wider px-2 py-0.5 rounded-full bg-indigo-50 text-indigo-700 border border-indigo-200">
                <Sparkles size={11} className="text-indigo-600" />
                FinanceContext Модель
              </span>
            </div>
            <p className="text-xs text-slate-500 mt-0.5">
              Линейный прогноз будущих трат на основе динамики, скользящего среднего и обязательств
            </p>
          </div>
        </div>

        {/* Action Toggle */}
        <div className="flex items-center gap-2 self-start sm:self-auto">
          <button
            onClick={() => setShowBounds(!showBounds)}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all inline-flex items-center gap-1.5 cursor-pointer ${
              showBounds
                ? 'bg-indigo-50 text-indigo-700 border border-indigo-200 shadow-2xs'
                : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
            }`}
          >
            <Layers size={13} />
            <span>{showBounds ? 'Скрыть коридор диапазона' : 'Показать коридор'}</span>
          </button>
        </div>
      </div>

      {/* Top KPI Summary Metrics */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
        {/* Total 3-Month Projected */}
        <div className="p-3.5 rounded-2xl bg-gradient-to-br from-indigo-50/70 via-indigo-50/30 to-white border border-indigo-100 flex flex-col justify-between">
          <span className="text-[10px] font-bold text-indigo-800 uppercase tracking-wider block">
            Суммарно на 3 месяца
          </span>
          <div className="text-xl sm:text-2xl font-black text-indigo-900 mt-1">
            {formatCurrency(totalThreeMonthProjected, currency.symbol)}
          </div>
          <span className="text-[10px] text-slate-500 mt-1 block">
            Ожидаемый объем расходов
          </span>
        </div>

        {/* Average Monthly */}
        <div className="p-3.5 rounded-2xl bg-gradient-to-br from-purple-50/70 via-purple-50/30 to-white border border-purple-100 flex flex-col justify-between">
          <span className="text-[10px] font-bold text-purple-800 uppercase tracking-wider block">
            Средний расход в месяц
          </span>
          <div className="text-xl sm:text-2xl font-black text-purple-900 mt-1">
            {formatCurrency(averageMonthlyProjected, currency.symbol)}
          </div>
          <span className="text-[10px] text-slate-500 mt-1 block">
            Базовый уровень планирования
          </span>
        </div>

        {/* Trend Percentage */}
        <div className="p-3.5 rounded-2xl bg-gradient-to-br from-slate-50 to-white border border-slate-200 flex flex-col justify-between">
          <span className="text-[10px] font-bold text-slate-600 uppercase tracking-wider block">
            Динамика к прошлому периоду
          </span>
          <div
            className={`text-xl sm:text-2xl font-black mt-1 inline-flex items-center gap-1 ${
              trendPercentage > 0
                ? 'text-rose-600'
                : trendPercentage < 0
                ? 'text-emerald-600'
                : 'text-slate-800'
            }`}
          >
            {trendPercentage > 0 ? (
              <TrendingUp size={20} className="stroke-[2.5]" />
            ) : trendPercentage < 0 ? (
              <TrendingDown size={20} className="stroke-[2.5]" />
            ) : null}
            <span>
              {trendPercentage > 0 ? '+' : ''}
              {trendPercentage}%
            </span>
          </div>
          <span className="text-[10px] text-slate-500 mt-1 block">
            {trendPercentage > 0 ? 'Прогнозируется рост трат' : 'Стабилизация расходов'}
          </span>
        </div>

        {/* Recurring Commitments */}
        <div className="p-3.5 rounded-2xl bg-gradient-to-br from-teal-50/70 via-teal-50/30 to-white border border-teal-100 flex flex-col justify-between">
          <span className="text-[10px] font-bold text-teal-800 uppercase tracking-wider block">
            Фиксированные подписки
          </span>
          <div className="text-xl sm:text-2xl font-black text-teal-900 mt-1">
            {formatCurrency(recurringCommitmentsTotal, currency.symbol)}
          </div>
          <span className="text-[10px] text-slate-500 mt-1 block">
            Обязательные списания за 3 мес.
          </span>
        </div>
      </div>

      {/* Main Line Chart */}
      <div className="bg-slate-50/60 rounded-2xl border border-slate-200/80 p-4 pt-5">
        <div className="flex items-center justify-between mb-3 px-1 text-xs">
          <span className="font-extrabold text-slate-800 flex items-center gap-1.5">
            <Calendar size={14} className="text-indigo-600" />
            Линейный график: факт и прогноз на 3 месяца вперед
          </span>
          <span className="text-[11px] text-slate-400 font-medium hidden sm:inline">
            Пунктирная линия обозначает период прогнозирования
          </span>
        </div>

        <div className="h-72 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart
              data={combinedChartData}
              margin={{ top: 15, right: 15, left: -10, bottom: 5 }}
            >
              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" vertical={false} />
              <XAxis
                dataKey="label"
                tick={{ fontSize: 11, fill: '#64748b' }}
                axisLine={{ stroke: '#cbd5e1' }}
                tickLine={false}
              />
              <YAxis
                tick={{ fontSize: 11, fill: '#64748b' }}
                axisLine={false}
                tickLine={false}
                tickFormatter={(val) => {
                  if (val >= 1000000) return `${(val / 1000000).toFixed(1)}M`;
                  if (val >= 1000) return `${Math.round(val / 1000)}k`;
                  return `${val}`;
                }}
              />
              <Tooltip content={<CustomTooltip />} />
              <Legend
                wrapperStyle={{ fontSize: 11, paddingTop: 10 }}
                iconType="circle"
                iconSize={8}
              />

              {/* Reference line marking transition to forecast */}
              {anchorLabel && (
                <ReferenceLine
                  x={anchorLabel}
                  stroke="#94a3b8"
                  strokeDasharray="3 3"
                  label={{
                    value: 'Начало прогноза',
                    position: 'top',
                    fill: '#64748b',
                    fontSize: 10,
                  }}
                />
              )}

              {/* Historical actual expenses */}
              <Line
                type="monotone"
                dataKey="actualExpense"
                name="Фактические расходы"
                stroke="#10b981"
                strokeWidth={3}
                dot={{ r: 4, fill: '#10b981', strokeWidth: 1.5, stroke: '#fff' }}
                activeDot={{ r: 6 }}
                connectNulls={false}
              />

              {/* Projected 3-month forecast */}
              <Line
                type="monotone"
                dataKey="projectedExpense"
                name="Прогноз расходов (3 мес.)"
                stroke="#6366f1"
                strokeWidth={3}
                strokeDasharray="6 4"
                dot={{ r: 5, fill: '#6366f1', strokeWidth: 2, stroke: '#fff' }}
                activeDot={{ r: 7 }}
              />

              {/* Upper Bound (Risk/Spike) */}
              {showBounds && (
                <Line
                  type="monotone"
                  dataKey="upperBound"
                  name="Верхняя граница (риск)"
                  stroke="#f43f5e"
                  strokeWidth={1.5}
                  strokeDasharray="3 3"
                  dot={false}
                  activeDot={false}
                />
              )}

              {/* Lower Bound (Optimistic) */}
              {showBounds && (
                <Line
                  type="monotone"
                  dataKey="lowerBound"
                  name="Оптимистичный сценарий"
                  stroke="#06b6d4"
                  strokeWidth={1.5}
                  strokeDasharray="3 3"
                  dot={false}
                  activeDot={false}
                />
              )}
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Month-by-Month Forecast Breakdown Cards */}
      <div className="space-y-2">
        <span className="text-xs font-extrabold text-slate-800 uppercase tracking-wider block">
          Детализация по месяцам прогноза
        </span>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          {projectedMonths.map((m, idx) => (
            <div
              key={m.monthKey}
              className="p-4 rounded-2xl bg-gradient-to-b from-white to-slate-50 border border-slate-200/90 shadow-2xs hover:shadow-xs transition-shadow space-y-2.5"
            >
              <div className="flex items-center justify-between border-b border-slate-100 pb-2">
                <span className="font-extrabold text-slate-900 text-sm">{m.fullLabel}</span>
                <span className="text-[10px] font-bold px-2 py-0.5 rounded-md bg-indigo-50 text-indigo-700 border border-indigo-200">
                  {idx === 0 ? '+1 месяц' : idx === 1 ? '+2 месяца' : '+3 месяца'}
                </span>
              </div>

              <div className="space-y-1">
                <span className="text-[11px] text-slate-400 font-medium block">
                  Ожидаемый расход:
                </span>
                <div className="text-xl font-black text-indigo-950">
                  {formatCurrency(m.projectedExpense, currency.symbol)}
                </div>
              </div>

              {/* Confidence & Range */}
              <div className="pt-2 border-t border-slate-100 text-[11px] space-y-1 text-slate-600">
                <div className="flex justify-between items-center">
                  <span className="text-slate-400">Диапазон:</span>
                  <span className="font-bold text-slate-700">
                    {formatCurrency(m.lowerBound, currency.symbol)} –{' '}
                    {formatCurrency(m.upperBound, currency.symbol)}
                  </span>
                </div>

                <div className="flex justify-between items-center">
                  <span className="text-slate-400">Точность оценки:</span>
                  <span className="font-black text-emerald-600 inline-flex items-center gap-1">
                    <CheckCircle2 size={11} />
                    ~{m.confidenceScore}%
                  </span>
                </div>

                {m.recurringCommitment > 0 && (
                  <div className="flex justify-between items-center text-[10px] text-slate-500 pt-0.5">
                    <span className="inline-flex items-center gap-1">
                      <Repeat size={10} className="text-teal-600" />
                      Обязательные платежи:
                    </span>
                    <span className="font-bold text-teal-800">
                      {formatCurrency(m.recurringCommitment, currency.symbol)}
                    </span>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Methodology footer info */}
      <div className="p-3 rounded-2xl bg-indigo-50/50 border border-indigo-100 flex items-start gap-2.5 text-slate-600 text-xs">
        <Info size={16} className="text-indigo-600 shrink-0 mt-0.5" />
        <p className="leading-relaxed text-[11px]">
          <strong className="text-indigo-950 font-bold">Алгоритм прогнозирования: </strong>
          {methodology}. Для максимальной точности регулярно вносите расходы и настраивайте
          категории бюджета.
        </p>
      </div>
    </div>
  );
};
