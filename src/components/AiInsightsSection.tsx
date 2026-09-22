import React, { useState, useMemo } from 'react';
import {
  Sparkles,
  TrendingDown,
  AlertTriangle,
  Lightbulb,
  ArrowRight,
  Sliders,
  CheckCircle2,
  ChevronDown,
  ChevronUp,
  Flame,
  Clock,
  PiggyBank,
  RefreshCw,
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { formatCurrency } from '../utils/financeCalculations';
import {
  analyzeSpendingPatterns,
  AiInsight,
  AiSpendingAnalysisResult,
} from '../utils/aiSpendingAnalyzer';
import { DynamicIcon } from '../utils/iconHelper';

interface AiInsightsSectionProps {
  onOpenBudgetModal: (categoryId?: string) => void;
  onNavigateTab: (tab: 'home' | 'history' | 'stats' | 'wallets' | 'profile') => void;
}

export const AiInsightsSection: React.FC<AiInsightsSectionProps> = ({
  onOpenBudgetModal,
  onNavigateTab,
}) => {
  const { transactions, categories, categoryBudgets, wallets, currency } = useFinance();

  const [timeWindow, setTimeWindow] = useState<'7d' | '30d' | 'all'>('30d');
  const [filterType, setFilterType] = useState<'all' | 'saving' | 'alert' | 'quick_win'>('all');
  const [isExpanded, setIsExpanded] = useState(false);
  const [showSimulator, setShowSimulator] = useState(false);
  const [simulatorCutPercent, setSimulatorCutPercent] = useState<number>(20);
  const [isRefreshing, setIsRefreshing] = useState(false);

  // Compute AI insights using pure analysis engine
  const analysis: AiSpendingAnalysisResult = useMemo(() => {
    return analyzeSpendingPatterns(
      transactions,
      categories,
      categoryBudgets,
      wallets,
      timeWindow
    );
  }, [transactions, categories, categoryBudgets, wallets, timeWindow]);

  // Filtered insights
  const filteredInsights = useMemo(() => {
    if (filterType === 'all') return analysis.insights;
    return analysis.insights.filter((ins) => ins.type === filterType);
  }, [analysis.insights, filterType]);

  // Interactive What-If simulation calculation
  const simulationData = useMemo(() => {
    const monthlyDiscretionary = (analysis.discretionaryTotal / (timeWindow === '7d' ? 7 : timeWindow === '30d' ? 30 : 90)) * 30;
    const monthlySimulatedSavings = Math.round(monthlyDiscretionary * (simulatorCutPercent / 100));
    const annualSimulatedSavings = monthlySimulatedSavings * 12;
    const sixMonthsSavings = monthlySimulatedSavings * 6;

    // Find first savings or goal wallet to show concrete goal achievement
    const targetWallet = wallets.find(
      (w) => w.type === 'savings' || w.type === 'goal' || w.type === 'investment'
    ) || wallets[0];

    return {
      monthlySimulatedSavings,
      annualSimulatedSavings,
      sixMonthsSavings,
      targetWallet,
    };
  }, [analysis.discretionaryTotal, timeWindow, simulatorCutPercent, wallets]);

  const handleRefresh = () => {
    setIsRefreshing(true);
    setTimeout(() => {
      setIsRefreshing(false);
    }, 450);
  };

  const getBadgeClass = (color: AiInsight['badgeColor']) => {
    switch (color) {
      case 'emerald':
        return 'bg-emerald-500/15 text-emerald-700 border-emerald-300/80';
      case 'rose':
        return 'bg-rose-500/15 text-rose-700 border-rose-300/80';
      case 'amber':
        return 'bg-amber-500/15 text-amber-700 border-amber-300/80';
      case 'purple':
        return 'bg-purple-500/15 text-purple-700 border-purple-300/80';
      case 'blue':
      default:
        return 'bg-blue-500/15 text-blue-700 border-blue-300/80';
    }
  };

  const displayedInsights = isExpanded ? filteredInsights : filteredInsights.slice(0, 3);

  return (
    <section
      id="dashboard_ai_insights_section"
      className="bg-white rounded-3xl border border-slate-200/90 p-5 sm:p-6 shadow-xs relative overflow-hidden transition-all"
    >
      {/* Subtle background ambient light */}
      <div className="absolute top-0 right-0 w-80 h-80 bg-gradient-to-br from-emerald-500/10 via-teal-500/5 to-transparent rounded-full blur-3xl pointer-events-none" />

      {/* Header with Title and Time Range Switcher */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-5 relative z-10">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-emerald-600 via-teal-600 to-cyan-600 text-white flex items-center justify-center shadow-sm shrink-0">
            <Sparkles size={20} className={isRefreshing ? 'animate-spin' : ''} />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-base sm:text-lg font-black text-slate-900 tracking-tight">
                AI Инсайты и Анализ трат
              </h3>
              <span className="hidden sm:inline-flex items-center gap-1 text-[10px] font-black uppercase tracking-wider px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">
                Авто-анализ
              </span>
            </div>
            <p className="text-xs text-slate-500 mt-0.5">
              Поиск скрытых резервов, аномалий и потенциала экономии
            </p>
          </div>
        </div>

        {/* Controls: Time Window & Refresh */}
        <div className="flex items-center gap-2 self-start sm:self-auto">
          <div className="inline-flex p-1 bg-slate-100 rounded-xl text-xs font-semibold text-slate-600">
            <button
              id="ai_window_7d_btn"
              onClick={() => setTimeWindow('7d')}
              className={`px-2.5 py-1 rounded-lg transition-all cursor-pointer ${
                timeWindow === '7d'
                  ? 'bg-white text-slate-900 shadow-2xs font-bold'
                  : 'hover:text-slate-900'
              }`}
            >
              7 дней
            </button>
            <button
              id="ai_window_30d_btn"
              onClick={() => setTimeWindow('30d')}
              className={`px-2.5 py-1 rounded-lg transition-all cursor-pointer ${
                timeWindow === '30d'
                  ? 'bg-white text-slate-900 shadow-2xs font-bold'
                  : 'hover:text-slate-900'
              }`}
            >
              30 дней
            </button>
            <button
              id="ai_window_all_btn"
              onClick={() => setTimeWindow('all')}
              className={`px-2.5 py-1 rounded-lg transition-all cursor-pointer ${
                timeWindow === 'all'
                  ? 'bg-white text-slate-900 shadow-2xs font-bold'
                  : 'hover:text-slate-900'
              }`}
            >
              Все время
            </button>
          </div>

          <button
            id="ai_refresh_insights_btn"
            onClick={handleRefresh}
            className="p-1.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-500 hover:text-slate-800 transition-colors cursor-pointer"
            title="Обновить аналитику"
          >
            <RefreshCw size={15} className={isRefreshing ? 'animate-spin' : ''} />
          </button>
        </div>
      </div>

      {/* Main Highlights Hero Banner */}
      <div className="bg-gradient-to-br from-slate-900 via-slate-800 to-emerald-950 text-white rounded-2xl p-4 sm:p-5 mb-5 shadow-inner relative overflow-hidden">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 relative z-10">
          {/* Main potential saving */}
          <div className="md:col-span-1 border-b md:border-b-0 md:border-r border-white/10 pb-4 md:pb-0 md:pr-4">
            <span className="text-[11px] font-bold uppercase tracking-wider text-emerald-300 flex items-center gap-1.5">
              <TrendingDown size={14} />
              Потенциал оптимизации
            </span>
            <div className="mt-1.5 flex items-baseline gap-2">
              <span className="text-2xl sm:text-3xl font-black text-emerald-400">
                +{formatCurrency(analysis.totalPotentialMonthlySavings, currency.symbol)}
              </span>
              <span className="text-xs text-slate-400">/ месяц</span>
            </div>
            <p className="text-[11px] text-slate-300 mt-1">
              Эквивалентно{' '}
              <strong className="text-white font-bold">
                {formatCurrency(analysis.annualPotentialSavings, currency.symbol)}
              </strong>{' '}
              в год при дисциплине
            </p>
          </div>

          {/* Savings Rate Impact */}
          <div className="border-b md:border-b-0 md:border-r border-white/10 pb-4 md:pb-0 md:pr-4 flex flex-col justify-center">
            <span className="text-[11px] font-bold uppercase tracking-wider text-slate-400 flex items-center gap-1.5">
              <PiggyBank size={14} className="text-teal-300" />
              Рост нормы сбережений
            </span>
            <div className="mt-1.5 flex items-center gap-2.5">
              <span className="text-xl sm:text-2xl font-black text-slate-300">
                {analysis.currentSavingsRate}%
              </span>
              <ArrowRight size={16} className="text-emerald-400" />
              <span className="text-xl sm:text-2xl font-black text-emerald-400">
                ~{analysis.projectedNewSavingsRate}%
              </span>
            </div>
            <p className="text-[11px] text-slate-400 mt-1">
              Удержание спонтанных покупок повысит свободный капитал
            </p>
          </div>

          {/* Speed & Daily Burn Rate */}
          <div className="flex flex-col justify-center">
            <div className="flex items-center justify-between text-[11px] text-slate-300 mb-1">
              <span className="flex items-center gap-1 text-slate-400">
                <Clock size={12} />
                Темп расходов:
              </span>
              <strong className="text-white">
                {formatCurrency(analysis.dailyBurnRate, currency.symbol)}/день
              </strong>
            </div>

            {analysis.spendingAccelerationPercent !== 0 && (
              <div className="flex items-center justify-between text-[11px] text-slate-300 mb-1">
                <span className="text-slate-400">Динамика к прошлому периоду:</span>
                <span
                  className={`font-bold ${
                    analysis.spendingAccelerationPercent > 0 ? 'text-rose-400' : 'text-emerald-400'
                  }`}
                >
                  {analysis.spendingAccelerationPercent > 0 ? '+' : ''}
                  {analysis.spendingAccelerationPercent}%
                </span>
              </div>
            )}

            {analysis.discretionaryPercent > 0 && (
              <div className="flex items-center justify-between text-[11px] text-slate-300">
                <span className="text-slate-400">Необязательные траты:</span>
                <span className="font-bold text-amber-300">
                  {analysis.discretionaryPercent}% бюджета
                </span>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Simulator Toggle Button & Interactive Simulator Drawer */}
      <div className="mb-4">
        <button
          id="ai_toggle_simulator_btn"
          onClick={() => setShowSimulator(!showSimulator)}
          className="w-full flex items-center justify-between p-3 rounded-xl bg-slate-50 hover:bg-slate-100 border border-slate-200/80 text-xs font-bold text-slate-700 transition-colors cursor-pointer"
        >
          <div className="flex items-center gap-2">
            <Sliders size={15} className="text-emerald-600" />
            <span>Интерактивный симулятор оптимизации: «Что если сократить траты на X%?»</span>
          </div>
          <span className="text-[11px] text-emerald-600 flex items-center gap-1">
            {showSimulator ? 'Скрыть калькулятор' : 'Попробовать'}
            {showSimulator ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
          </span>
        </button>

        {showSimulator && (
          <div className="mt-2.5 p-4 rounded-2xl bg-emerald-50/70 border border-emerald-200/80 text-slate-800 animate-in fade-in slide-in-from-top-2 duration-200">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-3">
              <div>
                <h4 className="text-xs sm:text-sm font-black text-slate-900">
                  Снижение спонтанных и необязательных расходов
                </h4>
                <p className="text-[11px] text-slate-600 mt-0.5">
                  Кафе, доставка, такси и мелкие импульсивные покупки
                </p>
              </div>

              {/* Preset buttons */}
              <div className="flex items-center gap-1.5">
                {[10, 20, 30, 40].map((pct) => (
                  <button
                    key={pct}
                    onClick={() => setSimulatorCutPercent(pct)}
                    className={`px-2.5 py-1 rounded-lg text-xs font-bold transition-colors cursor-pointer ${
                      simulatorCutPercent === pct
                        ? 'bg-emerald-600 text-white shadow-2xs'
                        : 'bg-white text-slate-700 hover:bg-emerald-100 border border-emerald-200'
                    }`}
                  >
                    -{pct}%
                  </button>
                ))}
              </div>
            </div>

            {/* Simulated Results Banner */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-2.5 pt-3 border-t border-emerald-200/60">
              <div className="bg-white p-3 rounded-xl border border-emerald-100 shadow-2xs">
                <span className="text-[10px] font-bold text-slate-500 uppercase tracking-wider block">
                  Экономия в месяц
                </span>
                <span className="text-base sm:text-lg font-black text-emerald-600">
                  +{formatCurrency(simulationData.monthlySimulatedSavings, currency.symbol)}
                </span>
              </div>

              <div className="bg-white p-3 rounded-xl border border-emerald-100 shadow-2xs">
                <span className="text-[10px] font-bold text-slate-500 uppercase tracking-wider block">
                  За 6 месяцев в копилку
                </span>
                <span className="text-base sm:text-lg font-black text-emerald-700">
                  +{formatCurrency(simulationData.sixMonthsSavings, currency.symbol)}
                </span>
              </div>

              <div className="bg-white p-3 rounded-xl border border-emerald-100 shadow-2xs">
                <span className="text-[10px] font-bold text-slate-500 uppercase tracking-wider block">
                  Экономия за год
                </span>
                <span className="text-base sm:text-lg font-black text-emerald-800">
                  +{formatCurrency(simulationData.annualSimulatedSavings, currency.symbol)}
                </span>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Filter Tabs for Insights */}
      <div className="flex items-center gap-1.5 overflow-x-auto pb-2 mb-3 scrollbar-none">
        <button
          id="ai_filter_all_btn"
          onClick={() => setFilterType('all')}
          className={`px-3 py-1.5 rounded-xl text-xs font-bold shrink-0 transition-colors cursor-pointer ${
            filterType === 'all'
              ? 'bg-slate-900 text-white'
              : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
          }`}
        >
          Все инсайты ({analysis.insights.length})
        </button>

        <button
          id="ai_filter_saving_btn"
          onClick={() => setFilterType('saving')}
          className={`px-3 py-1.5 rounded-xl text-xs font-bold shrink-0 transition-colors cursor-pointer flex items-center gap-1 ${
            filterType === 'saving'
              ? 'bg-emerald-600 text-white'
              : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
          }`}
        >
          <TrendingDown size={13} />
          Экономия ({analysis.insights.filter((i) => i.type === 'saving').length})
        </button>

        <button
          id="ai_filter_alert_btn"
          onClick={() => setFilterType('alert')}
          className={`px-3 py-1.5 rounded-xl text-xs font-bold shrink-0 transition-colors cursor-pointer flex items-center gap-1 ${
            filterType === 'alert'
              ? 'bg-rose-600 text-white'
              : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
          }`}
        >
          <AlertTriangle size={13} />
          Лимиты и риски ({analysis.insights.filter((i) => i.type === 'alert').length})
        </button>

        <button
          id="ai_filter_quick_win_btn"
          onClick={() => setFilterType('quick_win')}
          className={`px-3 py-1.5 rounded-xl text-xs font-bold shrink-0 transition-colors cursor-pointer flex items-center gap-1 ${
            filterType === 'quick_win'
              ? 'bg-purple-600 text-white'
              : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
          }`}
        >
          <Lightbulb size={13} />
          Быстрые победы ({analysis.insights.filter((i) => i.type === 'quick_win').length})
        </button>
      </div>

      {/* Insight Cards List */}
      <div className="space-y-3">
        {displayedInsights.map((insight) => (
          <div
            key={insight.id}
            className="p-4 rounded-2xl border border-slate-200/90 hover:border-slate-300 bg-slate-50/50 hover:bg-slate-50 transition-all flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3.5 group"
          >
            <div className="flex items-start gap-3.5 min-w-0">
              <div className="w-10 h-10 rounded-xl bg-white border border-slate-200 flex items-center justify-center text-slate-700 shrink-0 shadow-2xs group-hover:scale-105 transition-transform mt-0.5 sm:mt-0">
                <DynamicIcon name={insight.iconName} size={18} />
              </div>

              <div className="min-w-0">
                <div className="flex flex-wrap items-center gap-2">
                  <h4 className="text-xs sm:text-sm font-bold text-slate-900">
                    {insight.title}
                  </h4>
                  <span
                    className={`text-[10px] font-black uppercase tracking-wider px-2 py-0.5 rounded-md border ${getBadgeClass(
                      insight.badgeColor
                    )}`}
                  >
                    {insight.badgeText}
                  </span>
                </div>

                <p className="text-xs text-slate-600 mt-1 leading-relaxed">
                  {insight.description}
                </p>

                <div className="flex items-center gap-2 mt-2 text-[11px] text-slate-400">
                  <span className="font-medium text-slate-500">{insight.evidence}</span>
                </div>
              </div>
            </div>

            {/* Savings Pill & Action Button */}
            <div className="flex items-center gap-2.5 self-end sm:self-center shrink-0">
              {insight.potentialSavingsMonthly > 0 && (
                <div className="text-right">
                  <span className="text-[10px] text-slate-400 font-semibold block">
                    Потенциал
                  </span>
                  <span className="text-xs sm:text-sm font-black text-emerald-600 bg-emerald-50 border border-emerald-200/80 px-2.5 py-1 rounded-xl inline-block shadow-2xs">
                    +{formatCurrency(insight.potentialSavingsMonthly, currency.symbol)}
                  </span>
                </div>
              )}

              {insight.actionLabel && (
                <button
                  onClick={() => {
                    if (insight.actionType === 'budget') {
                      onOpenBudgetModal(insight.categoryId);
                    } else if (insight.actionType === 'stats') {
                      onNavigateTab('stats');
                    } else if (insight.actionType === 'wallets') {
                      onNavigateTab('wallets');
                    }
                  }}
                  className="px-3 py-1.5 rounded-xl bg-white hover:bg-slate-900 text-slate-700 hover:text-white border border-slate-200 hover:border-slate-900 text-xs font-bold transition-all shadow-2xs flex items-center gap-1 cursor-pointer"
                >
                  <span>{insight.actionLabel}</span>
                  <ArrowRight size={13} />
                </button>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Expand / Collapse Button if more than 3 insights */}
      {filteredInsights.length > 3 && (
        <div className="mt-3 text-center">
          <button
            id="ai_toggle_expand_insights_btn"
            onClick={() => setIsExpanded(!isExpanded)}
            className="inline-flex items-center gap-1.5 text-xs font-bold text-slate-600 hover:text-slate-900 px-3 py-1.5 rounded-xl hover:bg-slate-100 transition-colors cursor-pointer"
          >
            <span>
              {isExpanded
                ? 'Свернуть список'
                : `Показать еще ${filteredInsights.length - 3} инсайта(ов)`}
            </span>
            {isExpanded ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
          </button>
        </div>
      )}
    </section>
  );
};
