import React, { useState, useMemo } from 'react';
import {
  PieChart as PieChartIcon,
  TrendingUp,
  Award,
  Calendar,
  AlertCircle,
  CheckCircle2,
  HelpCircle,
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

export const StatisticsView: React.FC = () => {
  const { transactions, wallets, categories, currency } = useFinance();
  const [period, setPeriod] = useState<PeriodType>('month');

  // Filter transactions by period
  const filteredTxs = useMemo(() => {
    const now = new Date();
    return transactions.filter((t) => {
      const txDate = new Date(t.date);
      if (period === 'day') {
        return txDate.toDateString() === now.toDateString();
      }
      if (period === 'week') {
        const weekAgo = new Date();
        weekAgo.setDate(now.getDate() - 7);
        return txDate >= weekAgo;
      }
      if (period === 'month') {
        return (
          txDate.getMonth() === now.getMonth() && txDate.getFullYear() === now.getFullYear()
        );
      }
      if (period === 'year') {
        return txDate.getFullYear() === now.getFullYear();
      }
      return true; // 'all'
    });
  }, [transactions, period]);

  const { income, expense, net, savingsRate } = calculateTotals(filteredTxs);
  const health = calculateFinancialHealth(filteredTxs, wallets);

  // Category distribution for expenses
  const categoryData = useMemo(() => {
    const map: { [name: string]: { amount: number; color: string; icon: string } } = {};

    filteredTxs
      .filter((t) => t.type === 'expense')
      .forEach((t) => {
        const cat = categories.find((c) => c.name === t.category || c.id === t.categoryId);
        const name = t.category;
        const color = cat?.color || '#94A3B8';
        const icon = cat?.icon || 'Tag';

        if (!map[name]) {
          map[name] = { amount: 0, color, icon };
        }
        map[name].amount += t.amount;
      });

    return Object.keys(map)
      .map((name) => ({
        name,
        value: map[name].amount,
        color: map[name].color,
        icon: map[name].icon,
        percentage: expense > 0 ? Math.round((map[name].amount / expense) * 100) : 0,
      }))
      .sort((a, b) => b.value - a.value);
  }, [filteredTxs, categories, expense]);

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

  // Score color helper
  const getScoreColor = (score: number) => {
    if (score >= 80) return 'text-emerald-600 border-emerald-500 bg-emerald-50';
    if (score >= 60) return 'text-blue-600 border-blue-500 bg-blue-50';
    if (score >= 40) return 'text-amber-600 border-amber-500 bg-amber-50';
    return 'text-red-600 border-red-500 bg-red-50';
  };

  return (
    <div className="space-y-6 pb-24">
      {/* Header & Period Switcher */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h2 className="text-xl sm:text-2xl font-black text-slate-900">Финансовая аналитика</h2>
          <p className="text-xs text-slate-500">Динамика, структура расходов и индекс здоровья</p>
        </div>

        {/* Period Buttons */}
        <div className="flex items-center gap-1 bg-slate-200/70 p-1 rounded-xl text-xs font-bold text-slate-600 self-start sm:self-auto">
          {(
            [
              { id: 'week', label: 'Неделя' },
              { id: 'month', label: 'Месяц' },
              { id: 'year', label: 'Год' },
              { id: 'all', label: 'Все время' },
            ] as const
          ).map((item) => (
            <button
              key={item.id}
              onClick={() => setPeriod(item.id)}
              className={`px-3 py-1.5 rounded-lg transition-all ${
                period === item.id ? 'bg-white text-slate-900 shadow-xs' : 'hover:text-slate-900'
              }`}
            >
              {item.label}
            </button>
          ))}
        </div>
      </div>

      {/* Summary KPI Cards */}
      <div className="grid grid-cols-3 gap-3">
        <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-xs">
          <span className="text-[10px] sm:text-xs font-bold uppercase tracking-wider text-slate-400 block mb-1">
            Доходы
          </span>
          <div className="text-sm sm:text-xl font-extrabold text-emerald-600 truncate">
            +{formatCurrency(income, currency.symbol)}
          </div>
        </div>

        <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-xs">
          <span className="text-[10px] sm:text-xs font-bold uppercase tracking-wider text-slate-400 block mb-1">
            Расходы
          </span>
          <div className="text-sm sm:text-xl font-extrabold text-red-600 truncate">
            -{formatCurrency(expense, currency.symbol)}
          </div>
        </div>

        <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-xs">
          <span className="text-[10px] sm:text-xs font-bold uppercase tracking-wider text-slate-400 block mb-1">
            Сальдо
          </span>
          <div
            className={`text-sm sm:text-xl font-extrabold truncate ${
              net >= 0 ? 'text-emerald-700' : 'text-red-600'
            }`}
          >
            {net >= 0 ? '+' : ''}
            {formatCurrency(net, currency.symbol)}
          </div>
        </div>
      </div>

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

      {/* Category Expenses Breakdown with Donut Chart */}
      <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-xs">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <PieChartIcon size={20} className="text-emerald-600" />
            <h3 className="text-base font-extrabold text-slate-900">Структура расходов</h3>
          </div>
          <span className="text-xs font-bold text-slate-500">
            Всего: {formatCurrency(expense, currency.symbol)}
          </span>
        </div>

        {categoryData.length === 0 ? (
          <p className="text-xs text-center py-8 text-slate-400 font-medium">
            Нет данных о расходах за выбранный период
          </p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 items-center">
            {/* Donut Chart */}
            <div className="h-56 w-full flex items-center justify-center">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={categoryData}
                    cx="50%"
                    cy="50%"
                    innerRadius={55}
                    outerRadius={85}
                    paddingAngle={3}
                    dataKey="value"
                  >
                    {categoryData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip
                    formatter={(val: number) => [
                      formatCurrency(val, currency.symbol),
                      'Сумма',
                    ]}
                  />
                </PieChart>
              </ResponsiveContainer>
            </div>

            {/* List with progress bars */}
            <div className="space-y-3 max-h-64 overflow-y-auto pr-1">
              {categoryData.map((cat) => (
                <div key={cat.name} className="space-y-1">
                  <div className="flex items-center justify-between text-xs">
                    <div className="flex items-center gap-2">
                      <div
                        className="w-3 h-3 rounded-full shrink-0"
                        style={{ backgroundColor: cat.color }}
                      />
                      <span className="font-bold text-slate-800">{cat.name}</span>
                      <span className="text-[11px] text-slate-400 font-medium">
                        ({cat.percentage}%)
                      </span>
                    </div>
                    <span className="font-extrabold text-slate-900">
                      {formatCurrency(cat.value, currency.symbol)}
                    </span>
                  </div>

                  <div className="h-1.5 w-full bg-slate-100 rounded-full overflow-hidden">
                    <div
                      className="h-full rounded-full transition-all duration-500"
                      style={{
                        width: `${cat.percentage}%`,
                        backgroundColor: cat.color,
                      }}
                    />
                  </div>
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
    </div>
  );
};
