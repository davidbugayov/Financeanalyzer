import React, { useMemo, useState } from 'react';
import {
  PieChart,
  SlidersHorizontal,
  AlertTriangle,
  ChevronRight,
  TrendingDown,
  CheckCircle2,
  Plus,
  Sparkles,
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { formatCurrency } from '../utils/financeCalculations';
import { DynamicIcon } from '../utils/iconHelper';

interface BudgetDashboardWidgetProps {
  onOpenBudgetModal: (categoryId?: string) => void;
}

export const BudgetDashboardWidget: React.FC<BudgetDashboardWidgetProps> = ({
  onOpenBudgetModal,
}) => {
  const { categories, categoryBudgets, transactions, currency, setCategoryBudget } = useFinance();
  const [showAllCategories, setShowAllCategories] = useState(false);

  // Current month formatting
  const now = useMemo(() => new Date(), []);
  const currentMonthPrefix = useMemo(() => {
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    return `${year}-${month}`;
  }, [now]);

  const monthName = useMemo(() => {
    return now.toLocaleString('ru-RU', { month: 'long', year: 'numeric' });
  }, [now]);

  // Filter only expense categories
  const expenseCategories = useMemo(() => {
    return categories.filter((c) => c.isExpense);
  }, [categories]);

  // Calculate spending this month for each category
  const spendingPerCategory = useMemo(() => {
    const map: Record<string, number> = {};
    expenseCategories.forEach((c) => {
      map[c.id] = 0;
    });

    transactions.forEach((t) => {
      if (t.type !== 'expense') return;
      if (!t.date.startsWith(currentMonthPrefix)) return;

      const matchedCat = expenseCategories.find(
        (c) => (t.categoryId && c.id === t.categoryId) || c.name === t.category
      );
      if (matchedCat) {
        map[matchedCat.id] = (map[matchedCat.id] || 0) + t.amount;
      }
    });

    return map;
  }, [transactions, expenseCategories, currentMonthPrefix]);

  // Separate categories with active limits vs without
  const categoriesWithBudget = useMemo(() => {
    return expenseCategories.filter((c) => (categoryBudgets[c.id] || 0) > 0);
  }, [expenseCategories, categoryBudgets]);

  // Summary calculations
  const summary = useMemo(() => {
    let totalLimit = 0;
    let totalSpentInBudgeted = 0;
    let overBudgetCount = 0;

    categoriesWithBudget.forEach((c) => {
      const limit = categoryBudgets[c.id] || 0;
      const spent = spendingPerCategory[c.id] || 0;
      totalLimit += limit;
      totalSpentInBudgeted += spent;
      if (spent > limit) {
        overBudgetCount += 1;
      }
    });

    const percent = totalLimit > 0 ? Math.round((totalSpentInBudgeted / totalLimit) * 100) : 0;
    const remaining = totalLimit - totalSpentInBudgeted;

    return {
      totalLimit,
      totalSpentInBudgeted,
      overBudgetCount,
      percent,
      remaining,
    };
  }, [categoriesWithBudget, categoryBudgets, spendingPerCategory]);

  // Apply quick recommended defaults
  const handleApplyDefaults = () => {
    const defaults: Record<string, number> = {
      cat_food: 25000,
      cat_restaurant: 12000,
      cat_transport: 8000,
      cat_housing: 15000,
      cat_health: 7000,
      cat_entertainment: 6000,
    };
    Object.entries(defaults).forEach(([catId, lim]) => {
      setCategoryBudget(catId, lim);
    });
  };

  // Categories to display: either only budgeted or all expense categories
  const displayedCategories = showAllCategories ? expenseCategories : categoriesWithBudget;

  return (
    <div
      id="dashboard_budget_tracking_widget"
      className="bg-white rounded-3xl border border-slate-200/90 p-5 sm:p-6 shadow-xs space-y-5"
    >
      {/* Widget Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse" />
            <h3 className="text-base font-extrabold text-slate-900">Ежемесячные бюджеты</h3>
            <span className="text-[11px] font-semibold text-slate-400 capitalize">
              • {monthName}
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-0.5">
            Контроль лимитов трат и прогресс по категориям
          </p>
        </div>

        <div className="flex items-center gap-2 self-start sm:self-auto">
          {categoriesWithBudget.length > 0 && (
            <button
              id="toggle_all_categories_budget_btn"
              onClick={() => setShowAllCategories(!showAllCategories)}
              className="text-xs font-semibold px-2.5 py-1.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-600 transition-colors cursor-pointer"
            >
              {showAllCategories ? 'Только с лимитом' : 'Все категории'}
            </button>
          )}

          <button
            id="open_budget_settings_btn"
            onClick={() => onOpenBudgetModal()}
            className="flex items-center gap-1.5 px-3 py-1.5 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 rounded-xl text-xs font-bold transition-all border border-emerald-200 shadow-2xs cursor-pointer"
          >
            <SlidersHorizontal size={13} />
            <span>Настроить лимиты</span>
          </button>
        </div>
      </div>

      {/* Overall Budget Progress Card */}
      {categoriesWithBudget.length > 0 ? (
        <div className="p-4 rounded-2xl bg-gradient-to-br from-slate-900 to-slate-800 text-white shadow-xs space-y-3">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <div>
              <span className="text-[10px] uppercase font-bold tracking-wider text-emerald-400 block">
                Сводка по бюджетам
              </span>
              <div className="flex items-baseline gap-2 mt-0.5">
                <span className="text-xl sm:text-2xl font-extrabold text-white">
                  {formatCurrency(summary.totalSpentInBudgeted, currency.symbol)}
                </span>
                <span className="text-xs text-slate-400">
                  из {formatCurrency(summary.totalLimit, currency.symbol)}
                </span>
              </div>
            </div>

            <div className="flex items-center gap-2 self-start sm:self-auto">
              <span
                className={`text-xs font-bold px-2.5 py-1 rounded-lg ${
                  summary.percent >= 100
                    ? 'bg-rose-500/20 text-rose-300 border border-rose-500/30'
                    : summary.percent >= 80
                    ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                    : 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
                }`}
              >
                {summary.percent}% использовано
              </span>
            </div>
          </div>

          {/* Master Progress Bar */}
          <div className="space-y-1.5">
            <div className="w-full h-3 bg-white/15 rounded-full overflow-hidden p-0.5">
              <div
                className={`h-full transition-all duration-500 rounded-full ${
                  summary.percent >= 100
                    ? 'bg-gradient-to-r from-rose-500 to-red-600'
                    : summary.percent >= 80
                    ? 'bg-gradient-to-r from-amber-400 to-orange-500'
                    : 'bg-gradient-to-r from-emerald-400 to-teal-400'
                }`}
                style={{ width: `${Math.min(100, summary.percent)}%` }}
              />
            </div>

            <div className="flex items-center justify-between text-[11px]">
              <span className="text-slate-300 font-medium">
                {summary.remaining >= 0 ? (
                  <span className="flex items-center gap-1 text-emerald-300">
                    <CheckCircle2 size={12} />
                    <span>Осталось потратить: {formatCurrency(summary.remaining, currency.symbol)}</span>
                  </span>
                ) : (
                  <span className="flex items-center gap-1 text-rose-300 font-bold">
                    <AlertTriangle size={12} />
                    <span>
                      Превышение бюджета: +{formatCurrency(Math.abs(summary.remaining), currency.symbol)}
                    </span>
                  </span>
                )}
              </span>

              {summary.overBudgetCount > 0 && (
                <span className="text-rose-300 text-[10px] font-bold">
                  {summary.overBudgetCount} катег. превышено!
                </span>
              )}
            </div>
          </div>
        </div>
      ) : (
        /* Empty state when no budgets are defined */
        <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200/80 text-center space-y-3">
          <div className="w-12 h-12 rounded-2xl bg-emerald-100 text-emerald-700 flex items-center justify-center mx-auto shadow-2xs">
            <PieChart size={24} className="stroke-[2.5]" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-slate-800">
              Лимиты трат пока не настроены
            </h4>
            <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto leading-relaxed">
              Установите ежемесячные бюджеты на продукты, рестораны, транспорт и другие категории,
              чтобы видеть шкалу прогресса и избегать перерасходов.
            </p>
          </div>
          <div className="flex items-center justify-center gap-2 pt-1 flex-wrap">
            <button
              onClick={handleApplyDefaults}
              className="flex items-center gap-1.5 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold transition-all shadow-xs cursor-pointer"
            >
              <Sparkles size={14} />
              <span>Применить рекомендуемые</span>
            </button>
            <button
              onClick={() => onOpenBudgetModal()}
              className="flex items-center gap-1.5 px-4 py-2 bg-white hover:bg-slate-100 text-slate-700 border border-slate-200 rounded-xl text-xs font-bold transition-all cursor-pointer"
            >
              <SlidersHorizontal size={14} />
              <span>Настроить вручную</span>
            </button>
          </div>
        </div>
      )}

      {/* Category Progress Bars Grid */}
      {displayedCategories.length > 0 && (
        <div className="space-y-3 pt-1">
          <div className="flex items-center justify-between text-xs font-bold text-slate-500 px-1">
            <span>Категория и прогресс</span>
            <span>Потрачено / Лимит</span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {displayedCategories.map((c) => {
              const limit = categoryBudgets[c.id] || 0;
              const spent = spendingPerCategory[c.id] || 0;
              const percent = limit > 0 ? Math.round((spent / limit) * 100) : 0;
              const isOver = limit > 0 && spent > limit;
              const isWarning = limit > 0 && percent >= 80 && !isOver;
              const remaining = limit - spent;

              return (
                <div
                  key={c.id}
                  id={`dashboard_budget_card_${c.id}`}
                  onClick={() => onOpenBudgetModal(c.id)}
                  className="p-3.5 rounded-2xl border border-slate-200/80 bg-slate-50/50 hover:bg-slate-50 hover:border-slate-300 transition-all cursor-pointer group shadow-2xs"
                >
                  {/* Category info line */}
                  <div className="flex items-center justify-between gap-2 mb-2">
                    <div className="flex items-center gap-2.5 min-w-0">
                      <div
                        className="w-8 h-8 rounded-xl flex items-center justify-center text-white shrink-0 shadow-2xs group-hover:scale-105 transition-transform"
                        style={{ backgroundColor: c.color || '#10B981' }}
                      >
                        <DynamicIcon name={c.icon || 'Tag'} size={15} />
                      </div>
                      <div className="min-w-0">
                        <span className="text-xs sm:text-sm font-bold text-slate-800 block truncate group-hover:text-emerald-700 transition-colors">
                          {c.name}
                        </span>
                        <span className="text-[10px] text-slate-400 block truncate">
                          {limit > 0
                            ? remaining >= 0
                              ? `Осталось ${formatCurrency(remaining, currency.symbol)}`
                              : `Перерасход +${formatCurrency(Math.abs(remaining), currency.symbol)}`
                            : 'Лимит не установлен'}
                        </span>
                      </div>
                    </div>

                    {/* Amounts and Badge */}
                    <div className="text-right shrink-0">
                      <div className="text-xs sm:text-sm font-extrabold text-slate-900">
                        {formatCurrency(spent, currency.symbol)}
                      </div>
                      <div className="text-[10px] font-semibold text-slate-400">
                        {limit > 0 ? `из ${formatCurrency(limit, currency.symbol)}` : 'без лимита'}
                      </div>
                    </div>
                  </div>

                  {/* Progress Bar */}
                  {limit > 0 ? (
                    <div className="space-y-1">
                      <div className="w-full h-2.5 bg-slate-200/70 rounded-full overflow-hidden">
                        <div
                          className={`h-full transition-all duration-500 rounded-full ${
                            isOver
                              ? 'bg-rose-500'
                              : isWarning
                              ? 'bg-amber-500'
                              : 'bg-emerald-500'
                          }`}
                          style={{ width: `${Math.min(100, percent)}%` }}
                        />
                      </div>

                      <div className="flex items-center justify-between text-[10px] font-bold">
                        <span
                          className={`${
                            isOver
                              ? 'text-rose-600'
                              : isWarning
                              ? 'text-amber-600'
                              : 'text-emerald-600'
                          }`}
                        >
                          {percent}% израсходовано
                        </span>
                        <span className="text-slate-400 group-hover:text-slate-600 transition-colors flex items-center gap-0.5">
                          Изменить <ChevronRight size={11} />
                        </span>
                      </div>
                    </div>
                  ) : (
                    <div className="pt-1 flex items-center justify-between text-[11px]">
                      <span className="text-slate-400 italic text-[10px]">Лимит не задан</span>
                      <span className="text-[10px] font-bold text-emerald-600 group-hover:underline flex items-center gap-0.5">
                        <Plus size={11} /> Установить лимит
                      </span>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};
