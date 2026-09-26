import React from 'react';
import {
  Scale,
  ShieldCheck,
  AlertTriangle,
  ArrowDownLeft,
  ArrowUpRight,
  TrendingUp,
  TrendingDown,
  Percent,
  CheckCircle2,
  Info,
  Zap,
} from 'lucide-react';
import { PeriodType } from '../types';
import { formatCurrency } from '../utils/financeCalculations';

interface FinancialBalanceWidgetProps {
  income: number;
  expense: number;
  net: number;
  savingsRate: number;
  currencySymbol: string;
  period: PeriodType;
}

export const FinancialBalanceWidget: React.FC<FinancialBalanceWidgetProps> = ({
  income,
  expense,
  net,
  savingsRate,
  currencySymbol,
  period,
}) => {
  const periodLabels: Record<PeriodType, string> = {
    day: 'за выбранный день',
    week: 'за неделю',
    month: 'за месяц',
    year: 'за год',
    all: 'за все время',
  };

  const periodLabel = periodLabels[period] || 'за период';

  // Calculate balance indicators
  const totalFlow = income + expense;
  const isSurplus = net > 0;
  const isDeficit = net < 0;
  const isBalanced = net === 0 && income > 0;
  const isEmpty = income === 0 && expense === 0;

  // Ratios
  const expenseRatioOfIncome = income > 0 ? (expense / income) * 100 : expense > 0 ? 999 : 0;
  const coverageRatio = expense > 0 ? (income / expense) * 100 : income > 0 ? 999 : 0;
  const coverageMultiplier = expense > 0 ? (income / expense).toFixed(1) : income > 0 ? '∞' : '0.0';

  // Percentage distribution between income and expense in total turnover
  const incomeTurnoverPercent = totalFlow > 0 ? Math.round((income / totalFlow) * 100) : 50;
  const expenseTurnoverPercent = totalFlow > 0 ? Math.round((expense / totalFlow) * 100) : 50;

  // Maximum value for relative bar heights / widths
  const maxBarValue = Math.max(income, expense, 1);
  const incomeBarWidthPercent = (income / maxBarValue) * 100;
  const expenseBarWidthPercent = (expense / maxBarValue) * 100;

  // Balance status descriptor
  const getBalanceStatus = () => {
    if (isEmpty) {
      return {
        title: 'Нет финансовых операций',
        description: 'В выбранном периоде пока не зафиксировано доходов и расходов.',
        badge: 'Нет данных',
        badgeClass: 'bg-slate-100 text-slate-600 border-slate-200',
        tone: 'neutral' as const,
      };
    }
    if (isSurplus) {
      return {
        title: 'Профицитный бюджет (Положительный баланс)',
        description: `Доходы превышают расходы на ${formatCurrency(net, currencySymbol)}. В резерв сохраняется ${savingsRate}% от заработанных средств.`,
        badge: `Профицит +${formatCurrency(net, currencySymbol)}`,
        badgeClass: 'bg-emerald-100 text-emerald-800 border-emerald-300',
        tone: 'positive' as const,
      };
    }
    if (isDeficit) {
      const deficitAmount = Math.abs(net);
      const overspendPercent = income > 0 ? Math.round((deficitAmount / income) * 100) : 100;
      return {
        title: 'Дефицитный бюджет (Превышение расходов)',
        description: `Расходы превышают доходы на ${formatCurrency(deficitAmount, currencySymbol)} (перерасход ${overspendPercent}% от поступлений). Рекомендуется сократить необязательные траты.`,
        badge: `Дефицит -${formatCurrency(deficitAmount, currencySymbol)}`,
        badgeClass: 'bg-rose-100 text-rose-800 border-rose-300',
        tone: 'negative' as const,
      };
    }
    return {
      title: 'Равновесный бюджет (Нулевой баланс)',
      description: 'Все поступившие средства были израсходованы в ноль. Доходы и расходы полностью равны.',
      badge: 'Баланс в ноль (100%)',
      badgeClass: 'bg-blue-100 text-blue-800 border-blue-300',
      tone: 'balanced' as const,
    };
  };

  const status = getBalanceStatus();

  return (
    <div className="bg-white rounded-3xl border border-slate-200/90 p-5 sm:p-6 shadow-xs space-y-5">
      {/* Header & Status Indicator */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-100">
        <div className="flex items-center gap-2.5">
          <div
            className={`w-9 h-9 rounded-2xl flex items-center justify-center shrink-0 shadow-2xs ${
              status.tone === 'positive'
                ? 'bg-emerald-100 text-emerald-700'
                : status.tone === 'negative'
                ? 'bg-rose-100 text-rose-700'
                : 'bg-slate-100 text-slate-700'
            }`}
          >
            <Scale size={20} className="stroke-[2.5]" />
          </div>
          <div>
            <div className="flex items-center gap-2 flex-wrap">
              <h3 className="text-base font-extrabold text-slate-900">
                Финансовый баланс {periodLabel}
              </h3>
              <span
                className={`text-xs font-black px-2.5 py-0.5 rounded-full border shadow-2xs ${status.badgeClass}`}
              >
                {status.badge}
              </span>
            </div>
            <p className="text-xs text-slate-500 mt-0.5">
              Сравнительная оценка соотношения доходов и расходов
            </p>
          </div>
        </div>

        {/* Status Callout Pill */}
        <div className="flex items-center gap-1.5 self-start sm:self-auto text-xs font-bold text-slate-600 bg-slate-50 px-3 py-1.5 rounded-xl border border-slate-200/70">
          {status.tone === 'positive' ? (
            <ShieldCheck size={16} className="text-emerald-600" />
          ) : status.tone === 'negative' ? (
            <AlertTriangle size={16} className="text-rose-600" />
          ) : (
            <CheckCircle2 size={16} className="text-blue-600" />
          )}
          <span>{status.title}</span>
        </div>
      </div>

      {/* Main Graphical Comparative Element */}
      <div className="bg-slate-50/80 rounded-2xl p-4 sm:p-5 border border-slate-200/80 space-y-4">
        {/* Dynamic Descriptive Note */}
        <p className="text-xs sm:text-sm font-medium text-slate-700 leading-relaxed">
          {status.description}
        </p>

        {/* Dual Comparative Visual Bars with Equilibrium Anchors */}
        <div className="space-y-3.5 pt-1">
          {/* Income Bar */}
          <div className="space-y-1.5">
            <div className="flex items-center justify-between text-xs">
              <div className="flex items-center gap-1.5 font-bold text-emerald-800">
                <div className="w-5 h-5 rounded-md bg-emerald-100 flex items-center justify-center">
                  <ArrowDownLeft size={13} className="text-emerald-700 stroke-[2.5]" />
                </div>
                <span>Общие доходы</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-[11px] font-bold text-slate-500">
                  {totalFlow > 0 ? `${incomeTurnoverPercent}% оборота` : '0%'}
                </span>
                <span className="font-black text-emerald-700 text-sm sm:text-base">
                  +{formatCurrency(income, currencySymbol)}
                </span>
              </div>
            </div>

            {/* Income progress bar */}
            <div className="h-3 w-full bg-slate-200/70 rounded-full overflow-hidden p-0.5">
              <div
                className="h-full rounded-full bg-gradient-to-r from-emerald-500 to-teal-500 transition-all duration-700 shadow-2xs"
                style={{ width: `${incomeBarWidthPercent}%` }}
              />
            </div>
          </div>

          {/* Expense Bar */}
          <div className="space-y-1.5">
            <div className="flex items-center justify-between text-xs">
              <div className="flex items-center gap-1.5 font-bold text-rose-800">
                <div className="w-5 h-5 rounded-md bg-rose-100 flex items-center justify-center">
                  <ArrowUpRight size={13} className="text-rose-700 stroke-[2.5]" />
                </div>
                <span>Общие расходы</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-[11px] font-bold text-slate-500">
                  {totalFlow > 0 ? `${expenseTurnoverPercent}% оборота` : '0%'}
                </span>
                <span className="font-black text-rose-700 text-sm sm:text-base">
                  -{formatCurrency(expense, currencySymbol)}
                </span>
              </div>
            </div>

            {/* Expense progress bar */}
            <div className="h-3 w-full bg-slate-200/70 rounded-full overflow-hidden p-0.5">
              <div
                className={`h-full rounded-full transition-all duration-700 shadow-2xs ${
                  isDeficit
                    ? 'bg-gradient-to-r from-rose-500 to-red-600'
                    : 'bg-gradient-to-r from-rose-400 to-pink-500'
                }`}
                style={{ width: `${expenseBarWidthPercent}%` }}
              />
            </div>
          </div>
        </div>

        {/* 100% Split Proportion Bar (Income Budget Distribution) */}
        {income > 0 && (
          <div className="pt-2 border-t border-slate-200/60 space-y-2">
            <div className="flex items-center justify-between text-[11px] font-bold text-slate-500">
              <span>Распределение поступивших средств (100%)</span>
              <span>
                {isSurplus ? (
                  <span className="text-emerald-700">Сберегается {savingsRate}%</span>
                ) : isDeficit ? (
                  <span className="text-rose-700">Перерасход {Math.round(expenseRatioOfIncome - 100)}%</span>
                ) : (
                  <span className="text-slate-600">Равновесие 100%</span>
                )}
              </span>
            </div>

            {/* Proportional Segmented Bar */}
            <div className="h-4 w-full rounded-xl overflow-hidden flex bg-slate-200 p-0.5 shadow-inner">
              {isDeficit ? (
                <>
                  <div
                    title={`Покрыто доходами: 100% (${formatCurrency(income, currencySymbol)})`}
                    style={{ width: '80%' }}
                    className="h-full bg-rose-500 rounded-l-lg transition-all"
                  />
                  <div
                    title={`Непокрытый перерасход: ${formatCurrency(Math.abs(net), currencySymbol)}`}
                    style={{ width: '20%' }}
                    className="h-full bg-repeating-linear-stripes bg-rose-700 rounded-r-lg transition-all"
                  />
                </>
              ) : (
                <>
                  <div
                    title={`Расходы: ${expenseRatioOfIncome.toFixed(1)}% (${formatCurrency(expense, currencySymbol)})`}
                    style={{ width: `${Math.min(expenseRatioOfIncome, 100)}%` }}
                    className="h-full bg-rose-500 rounded-l-lg transition-all"
                  />
                  <div
                    title={`Сбережения / Чистый остаток: ${savingsRate}% (${formatCurrency(net, currencySymbol)})`}
                    style={{ width: `${Math.max(100 - expenseRatioOfIncome, 0)}%` }}
                    className="h-full bg-emerald-500 rounded-r-lg transition-all"
                  />
                </>
              )}
            </div>

            {/* Sub-bar legend */}
            <div className="flex items-center justify-between text-[10px] sm:text-[11px] font-bold text-slate-500 pt-0.5">
              <div className="flex items-center gap-1.5">
                <span className="w-2.5 h-2.5 rounded-full bg-rose-500 inline-block" />
                <span>
                  Расходы: <strong>{expenseRatioOfIncome.toFixed(0)}%</strong> от доходов
                </span>
              </div>
              <div className="flex items-center gap-1.5">
                <span
                  className={`w-2.5 h-2.5 rounded-full inline-block ${
                    isDeficit ? 'bg-rose-700' : 'bg-emerald-500'
                  }`}
                />
                <span>
                  {isDeficit ? (
                    <strong className="text-rose-700">Дефицит бюджета</strong>
                  ) : (
                    <strong className="text-emerald-700">Сбережения: {savingsRate}%</strong>
                  )}
                </span>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* 3 Key Balance Assessment KPI Metrics */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
        {/* Metric 1: Coverage Ratio */}
        <div className="p-3.5 rounded-2xl bg-white border border-slate-200/90 shadow-2xs space-y-1">
          <div className="flex items-center justify-between text-slate-500 text-xs font-bold">
            <span>Коэффициент покрытия</span>
            <Percent size={14} className="text-slate-400" />
          </div>
          <div className="text-lg sm:text-xl font-black text-slate-900">
            {coverageMultiplier}x
            <span className="text-xs font-bold text-slate-500 ml-1.5">
              ({coverageRatio > 900 ? '∞' : `${Math.round(coverageRatio)}%`})
            </span>
          </div>
          <p className="text-[11px] text-slate-500">
            {coverageRatio >= 100
              ? 'Доходы полностью покрывают расходы'
              : 'Доходы не покрывают расходы'}
          </p>
        </div>

        {/* Metric 2: Burn Rate (Доля расходов) */}
        <div className="p-3.5 rounded-2xl bg-white border border-slate-200/90 shadow-2xs space-y-1">
          <div className="flex items-center justify-between text-slate-500 text-xs font-bold">
            <span>Доля трат от доходов</span>
            <TrendingUp size={14} className="text-slate-400" />
          </div>
          <div
            className={`text-lg sm:text-xl font-black ${
              expenseRatioOfIncome > 100
                ? 'text-rose-600'
                : expenseRatioOfIncome > 80
                ? 'text-amber-600'
                : 'text-emerald-700'
            }`}
          >
            {expenseRatioOfIncome > 900 ? '>100%' : `${expenseRatioOfIncome.toFixed(0)}%`}
          </div>
          <p className="text-[11px] text-slate-500">
            {expenseRatioOfIncome <= 70
              ? 'Здоровая норма (< 70%)'
              : expenseRatioOfIncome <= 100
              ? 'Высокая нагрузка (70-100%)'
              : 'Критический перерасход (> 100%)'}
          </p>
        </div>

        {/* Metric 3: Net Cash Balance */}
        <div className="p-3.5 rounded-2xl bg-white border border-slate-200/90 shadow-2xs space-y-1">
          <div className="flex items-center justify-between text-slate-500 text-xs font-bold">
            <span>Чистое сальдо периода</span>
            {isSurplus ? (
              <TrendingUp size={14} className="text-emerald-600" />
            ) : isDeficit ? (
              <TrendingDown size={14} className="text-rose-600" />
            ) : (
              <Info size={14} className="text-slate-400" />
            )}
          </div>
          <div
            className={`text-lg sm:text-xl font-black ${
              isSurplus ? 'text-emerald-700' : isDeficit ? 'text-rose-600' : 'text-slate-900'
            }`}
          >
            {net >= 0 ? '+' : ''}
            {formatCurrency(net, currencySymbol)}
          </div>
          <p className="text-[11px] text-slate-500">
            {isSurplus
              ? `Свободный капитал (+${savingsRate}% к доходу)`
              : isDeficit
              ? 'Отрицательный финансовый итог'
              : 'Точное равенство доходов и трат'}
          </p>
        </div>
      </div>
    </div>
  );
};
