import React, { useMemo, useState } from 'react';
import {
  PieChart,
  SlidersHorizontal,
  AlertTriangle,
  ChevronRight,
  TrendingDown,
  TrendingUp,
  CheckCircle2,
  Plus,
  Sparkles,
  ArrowUpDown,
  Calendar,
  Flame,
  ShieldCheck,
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { formatCurrency } from '../utils/financeCalculations';
import { DynamicIcon } from '../utils/iconHelper';

interface BudgetDashboardWidgetProps {
  onOpenBudgetModal: (categoryId?: string) => void;
}

type FilterTab = 'budgeted' | 'active' | 'over' | 'all';
type SortOption = 'percent' | 'spent' | 'remaining' | 'name';

export const BudgetDashboardWidget: React.FC<BudgetDashboardWidgetProps> = ({
  onOpenBudgetModal,
}) => {
  const { categories, categoryBudgets, transactions, currency, setCategoryBudget } = useFinance();
  const [filterTab, setFilterTab] = useState<FilterTab>('budgeted');
  const [sortBy, setSortBy] = useState<SortOption>('percent');

  // Current month formatting & pacing calculations
  const now = useMemo(() => new Date(), []);
  const currentMonthPrefix = useMemo(() => {
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    return `${year}-${month}`;
  }, [now]);

  const monthName = useMemo(() => {
    return now.toLocaleString('ru-RU', { month: 'long', year: 'numeric' });
  }, [now]);

  const currentDay = useMemo(() => now.getDate(), [now]);
  const daysInMonth = useMemo(() => {
    return new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate();
  }, [now]);

  const daysRemaining = useMemo(() => {
    return Math.max(1, daysInMonth - currentDay);
  }, [daysInMonth, currentDay]);

  const monthElapsedPercent = useMemo(() => {
    return Math.min(100, Math.round((currentDay / daysInMonth) * 100));
  }, [currentDay, daysInMonth]);

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

  // Categories with budget
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
      cat_clothing: 10000,
      cat_entertainment: 6000,
      cat_communication: 2000,
      cat_pet: 5000,
      cat_services: 4500,
      cat_credit: 25000,
      cat_travel: 15000,
      cat_other_expense: 5000,
    };
    Object.entries(defaults).forEach(([catId, lim]) => {
      setCategoryBudget(catId, lim);
    });
  };

  // Filtered and sorted category list
  const displayedCategories = useMemo(() => {
    let list = [...expenseCategories];

    if (filterTab === 'budgeted') {
      list = list.filter((c) => (categoryBudgets[c.id] || 0) > 0);
    } else if (filterTab === 'active') {
      list = list.filter((c) => (spendingPerCategory[c.id] || 0) > 0);
    } else if (filterTab === 'over') {
      list = list.filter((c) => {
        const lim = categoryBudgets[c.id] || 0;
        const sp = spendingPerCategory[c.id] || 0;
        return lim > 0 && sp > lim;
      });
    }

    return list.sort((a, b) => {
      const limitA = categoryBudgets[a.id] || 0;
      const spentA = spendingPerCategory[a.id] || 0;
      const percentA = limitA > 0 ? (spentA / limitA) * 100 : 0;
      const remainingA = limitA - spentA;

      const limitB = categoryBudgets[b.id] || 0;
      const spentB = spendingPerCategory[b.id] || 0;
      const percentB = limitB > 0 ? (spentB / limitB) * 100 : 0;
      const remainingB = limitB - spentB;

      if (sortBy === 'percent') {
        return percentB - percentA;
      }
      if (sortBy === 'spent') {
        return spentB - spentA;
      }
      if (sortBy === 'remaining') {
        return remainingA - remainingB;
      }
      if (sortBy === 'name') {
        return a.name.localeCompare(b.name, 'ru');
      }
      return 0;
    });
  }, [expenseCategories, filterTab, categoryBudgets, spendingPerCategory, sortBy]);

  return (
    <div
      id="dashboard_budget_tracking_widget"
      className="bg-white rounded-3xl border border-slate-200/90 p-4 sm:p-6 shadow-xs space-y-5"
    >
      {/* Widget Header with Month Pacing Info */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <div className="flex items-center gap-2 flex-wrap">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse shrink-0" />
            <h3 className="text-base sm:text-lg font-black text-slate-900">
              Ежемесячные бюджеты
            </h3>
            <span className="text-xs font-bold text-emerald-800 bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded-md capitalize">
              {monthName}
            </span>
          </div>
          <div className="flex items-center gap-2 text-xs text-slate-500 mt-1 flex-wrap">
            <span className="flex items-center gap-1">
              <Calendar size={13} className="text-slate-400 shrink-0" />
              <span>
                День {currentDay} из {daysInMonth} ({monthElapsedPercent}% месяца)
              </span>
            </span>
            <span className="hidden xs:inline text-slate-300">•</span>
            <span>Осталось {daysRemaining} дн.</span>
          </div>
        </div>

        <div className="flex items-center gap-2 self-start sm:self-auto shrink-0">
          <button
            id="open_budget_settings_btn"
            onClick={() => onOpenBudgetModal()}
            className="flex items-center gap-1.5 px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold transition-all shadow-xs cursor-pointer"
          >
            <SlidersHorizontal size={13} />
            <span>Настроить бюджеты</span>
          </button>
        </div>
      </div>

      {/* Overall Budget Progress Card */}
      {categoriesWithBudget.length > 0 ? (
        <div className="p-4 sm:p-5 rounded-3xl bg-gradient-to-br from-emerald-50/90 via-teal-50/40 to-white border border-emerald-200/90 text-slate-800 shadow-xs space-y-3.5">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2.5">
            <div>
              <span className="text-[11px] uppercase font-bold tracking-wider text-emerald-800 block">
                Суммарный расход по категориям с бюджетом
              </span>
              <div className="flex items-baseline gap-2 mt-0.5 flex-wrap">
                <span className="text-2xl sm:text-3xl font-black text-slate-900">
                  {formatCurrency(summary.totalSpentInBudgeted, currency.symbol)}
                </span>
                <span className="text-xs sm:text-sm text-slate-500 font-bold">
                  из {formatCurrency(summary.totalLimit, currency.symbol)}
                </span>
              </div>
            </div>

            <div className="flex items-center gap-2 self-start sm:self-auto flex-wrap">
              <span
                className={`text-xs font-black px-2.5 py-1 rounded-xl shadow-2xs border ${
                  summary.percent >= 100
                    ? 'bg-rose-100 text-rose-800 border-rose-200'
                    : summary.percent >= 80
                    ? 'bg-amber-100 text-amber-800 border-amber-200'
                    : 'bg-emerald-100 text-emerald-800 border-emerald-200'
                }`}
              >
                {summary.percent}% использовано
              </span>

              {summary.percent <= monthElapsedPercent ? (
                <span className="text-[11px] font-bold px-2 py-0.5 rounded-lg bg-emerald-100/80 text-emerald-800 border border-emerald-200 flex items-center gap-1">
                  <ShieldCheck size={12} className="text-emerald-700" />
                  <span>В рамках темпа</span>
                </span>
              ) : (
                <span className="text-[11px] font-bold px-2 py-0.5 rounded-lg bg-amber-100 text-amber-800 border border-amber-200 flex items-center gap-1">
                  <Flame size={12} className="text-amber-700" />
                  <span>Выше темпа</span>
                </span>
              )}
            </div>
          </div>

          {/* Master Visual Progress Bar with Month Pacing Line */}
          <div className="space-y-1.5">
            <div className="w-full h-3.5 bg-slate-100 rounded-full overflow-hidden p-0.5 border border-slate-200/90 relative">
              {/* Month time indicator tick */}
              <div
                className="absolute top-0 bottom-0 w-0.5 bg-slate-400 z-10 pointer-events-none"
                style={{ left: `${monthElapsedPercent}%` }}
                title={`Прошло ${monthElapsedPercent}% месяца`}
              />

              <div
                className={`h-full transition-all duration-700 rounded-full relative shadow-xs ${
                  summary.percent >= 100
                    ? 'bg-gradient-to-r from-rose-500 to-red-600'
                    : summary.percent >= 80
                    ? 'bg-gradient-to-r from-amber-400 to-orange-500'
                    : 'bg-gradient-to-r from-emerald-500 via-teal-500 to-emerald-600'
                }`}
                style={{ width: `${Math.min(100, summary.percent)}%` }}
              />
            </div>

            <div className="flex items-center justify-between text-[11px] flex-wrap gap-1">
              <span className="text-slate-600 font-medium">
                {summary.remaining >= 0 ? (
                  <span className="flex items-center gap-1 text-emerald-700">
                    <CheckCircle2 size={13} className="text-emerald-600" />
                    <span>
                      Осталось на месяц:{' '}
                      <strong className="text-emerald-800 font-extrabold">{formatCurrency(summary.remaining, currency.symbol)}</strong>
                      {daysRemaining > 0 && (
                        <span className="text-slate-500 text-[10px] ml-1">
                          (~{formatCurrency(Math.round(summary.remaining / daysRemaining), currency.symbol)}/день)
                        </span>
                      )}
                    </span>
                  </span>
                ) : (
                  <span className="flex items-center gap-1 text-rose-700 font-bold">
                    <AlertTriangle size={13} />
                    <span>
                      Превышение бюджета: +{formatCurrency(Math.abs(summary.remaining), currency.symbol)}
                    </span>
                  </span>
                )}
              </span>

              {summary.overBudgetCount > 0 && (
                <span className="text-rose-700 text-[11px] font-bold bg-rose-50 px-2 py-0.5 rounded-md border border-rose-200">
                  {summary.overBudgetCount} катег. с перерасходом
                </span>
              )}
            </div>
          </div>
        </div>
      ) : (
        /* Empty State */
        <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200/80 text-center space-y-3">
          <div className="w-12 h-12 rounded-2xl bg-emerald-100 text-emerald-700 flex items-center justify-center mx-auto shadow-2xs">
            <PieChart size={24} className="stroke-[2.5]" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-slate-800">
              Лимиты трат пока не заданы
            </h4>
            <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto leading-relaxed">
              Установите ежемесячные бюджеты на продукты, кафе, жилье и другие категории,
              чтобы видеть цветные шкалы прогресса и не выходить за рамки.
            </p>
          </div>
          <div className="flex items-center justify-center gap-2 pt-1 flex-wrap">
            <button
              onClick={handleApplyDefaults}
              className="flex items-center gap-1.5 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold transition-all shadow-xs cursor-pointer"
            >
              <Sparkles size={14} />
              <span>Задать базовые бюджеты</span>
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

      {/* Filter and Sorting Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2.5 pt-1 border-t border-slate-100">
        {/* Quick Filter Tabs */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 sm:pb-0 scrollbar-none">
          <button
            type="button"
            onClick={() => setFilterTab('budgeted')}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all whitespace-nowrap cursor-pointer ${
              filterTab === 'budgeted'
                ? 'bg-emerald-600 text-white shadow-2xs'
                : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
            }`}
          >
            С бюджетом ({categoriesWithBudget.length})
          </button>

          <button
            type="button"
            onClick={() => setFilterTab('active')}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all whitespace-nowrap cursor-pointer ${
              filterTab === 'active'
                ? 'bg-emerald-600 text-white shadow-2xs'
                : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
            }`}
          >
            С расходами
          </button>

          {summary.overBudgetCount > 0 && (
            <button
              type="button"
              onClick={() => setFilterTab('over')}
              className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all whitespace-nowrap cursor-pointer ${
                filterTab === 'over'
                  ? 'bg-rose-600 text-white shadow-2xs'
                  : 'bg-rose-50 text-rose-700 hover:bg-rose-100 border border-rose-200'
              }`}
            >
              Перерасход ({summary.overBudgetCount})
            </button>
          )}

          <button
            type="button"
            onClick={() => setFilterTab('all')}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all whitespace-nowrap cursor-pointer ${
              filterTab === 'all'
                ? 'bg-emerald-600 text-white shadow-2xs'
                : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
            }`}
          >
            Все категории ({expenseCategories.length})
          </button>
        </div>

        {/* Sort selector */}
        <div className="flex items-center gap-1.5 self-end sm:self-auto shrink-0">
          <ArrowUpDown size={13} className="text-slate-400" />
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as SortOption)}
            className="text-xs font-bold px-2.5 py-1 bg-slate-50 border border-slate-200 rounded-lg text-slate-700 focus:outline-none focus:ring-1 focus:ring-emerald-500 cursor-pointer"
          >
            <option value="percent">По % расхода</option>
            <option value="spent">По сумме трат</option>
            <option value="remaining">По остатку</option>
            <option value="name">По алфавиту</option>
          </select>
        </div>
      </div>

      {/* Visual Progress Bar Cards Grid for Each Budget Category */}
      {displayedCategories.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3 sm:gap-3.5">
          {displayedCategories.map((c) => {
            const limit = categoryBudgets[c.id] || 0;
            const spent = spendingPerCategory[c.id] || 0;
            const percent = limit > 0 ? Math.round((spent / limit) * 100) : 0;
            const isOver = limit > 0 && spent > limit;
            const isWarning = limit > 0 && percent >= 75 && !isOver;
            const remaining = limit - spent;
            const dailyRemaining =
              limit > 0 && remaining > 0 ? Math.round(remaining / daysRemaining) : 0;

            // Pacing comparison with elapsed month time
            const isAheadOfSchedule = limit > 0 && percent > monthElapsedPercent + 5;

            return (
              <div
                key={c.id}
                id={`dashboard_budget_card_${c.id}`}
                onClick={() => onOpenBudgetModal(c.id)}
                className={`p-4 rounded-2xl border transition-all cursor-pointer group relative overflow-hidden ${
                  isOver
                    ? 'border-rose-300 bg-rose-50/30 hover:border-rose-400 hover:bg-rose-50/50 shadow-xs'
                    : isWarning
                    ? 'border-amber-300 bg-amber-50/30 hover:border-amber-400 hover:bg-amber-50/50 shadow-xs'
                    : limit > 0
                    ? 'border-slate-200/90 bg-white hover:border-emerald-300 hover:shadow-xs'
                    : 'border-dashed border-slate-200 bg-slate-50/60 hover:bg-slate-50'
                }`}
              >
                {/* Header: Category Icon, Name and Status Pill */}
                <div className="flex items-center justify-between gap-2 mb-2.5">
                  <div className="flex items-center gap-2.5 min-w-0">
                    <div
                      className="w-9 h-9 rounded-xl flex items-center justify-center text-white shrink-0 shadow-2xs group-hover:scale-105 transition-transform"
                      style={{ backgroundColor: c.color || '#10B981' }}
                    >
                      <DynamicIcon name={c.icon || 'Tag'} size={17} />
                    </div>

                    <div className="min-w-0">
                      <div className="flex items-center gap-1.5">
                        <span className="text-sm font-bold text-slate-900 truncate group-hover:text-emerald-700 transition-colors">
                          {c.name}
                        </span>
                      </div>

                      {limit > 0 ? (
                        <div className="flex items-center gap-1.5 text-[11px] font-medium mt-0.5">
                          {isOver ? (
                            <span className="text-rose-600 font-bold flex items-center gap-0.5">
                              <AlertTriangle size={11} className="shrink-0" />
                              <span>Перерасход +{formatCurrency(Math.abs(remaining), currency.symbol)}</span>
                            </span>
                          ) : (
                            <span className="text-slate-500">
                              Осталось: <strong className="text-slate-700 font-bold">{formatCurrency(remaining, currency.symbol)}</strong>
                            </span>
                          )}
                        </div>
                      ) : (
                        <span className="text-[11px] text-slate-400 italic">
                          Лимит не задан
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Percentage Badge */}
                  <div className="shrink-0 text-right">
                    {limit > 0 ? (
                      <span
                        className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-xl text-xs font-black shadow-2xs ${
                          isOver
                            ? 'bg-rose-100 text-rose-700 border border-rose-200'
                            : isWarning
                            ? 'bg-amber-100 text-amber-800 border border-amber-200'
                            : 'bg-emerald-100 text-emerald-800 border border-emerald-200'
                        }`}
                      >
                        {isOver && <AlertTriangle size={12} className="stroke-[3]" />}
                        <span>{percent}%</span>
                      </span>
                    ) : (
                      <span className="text-[10px] font-bold px-2 py-0.5 rounded-lg bg-slate-200/80 text-slate-600">
                        Без лимита
                      </span>
                    )}
                  </div>
                </div>

                {/* The Visual Progress Bar for this Category */}
                {limit > 0 ? (
                  <div className="space-y-1.5 mt-2">
                    {/* Spending line: Spent / Total */}
                    <div className="flex items-baseline justify-between text-xs">
                      <span className="font-extrabold text-slate-900">
                        {formatCurrency(spent, currency.symbol)}
                      </span>
                      <span className="text-slate-400 font-semibold text-[11px]">
                        из {formatCurrency(limit, currency.symbol)}
                      </span>
                    </div>

                    {/* Progress Track & Fill */}
                    <div className="w-full h-3 bg-slate-100 rounded-full overflow-hidden p-0.5 border border-slate-200/70 relative shadow-2xs">
                      {/* Month timeline pacing marker */}
                      <div
                        className="absolute top-0 bottom-0 w-0.5 bg-slate-400/50 z-10 pointer-events-none"
                        style={{ left: `${monthElapsedPercent}%` }}
                        title={`День месяца: ${monthElapsedPercent}%`}
                      />

                      {/* Animated Fill Bar */}
                      <div
                        className={`h-full transition-all duration-700 ease-out rounded-full ${
                          isOver
                            ? 'bg-gradient-to-r from-rose-500 to-red-600 shadow-xs'
                            : isWarning
                            ? 'bg-gradient-to-r from-amber-400 to-orange-500 shadow-xs'
                            : 'bg-gradient-to-r from-emerald-500 to-teal-500 shadow-xs'
                        }`}
                        style={{ width: `${Math.min(100, percent)}%` }}
                      />
                    </div>

                    {/* Card Footer: pacing & edit link */}
                    <div className="flex items-center justify-between text-[10px] pt-0.5">
                      <div className="flex items-center gap-1.5 text-slate-500">
                        {remaining > 0 ? (
                          <span>
                            ~<strong>{formatCurrency(dailyRemaining, currency.symbol)}</strong> в день
                          </span>
                        ) : (
                          <span className="text-rose-600 font-bold">Лимит исчерпан</span>
                        )}

                        {isAheadOfSchedule && !isOver && (
                          <span className="px-1.5 py-0.2 rounded bg-amber-100 text-amber-800 font-bold text-[9px]">
                            выше темпа
                          </span>
                        )}
                      </div>

                      <span className="text-emerald-700 font-bold group-hover:text-emerald-800 transition-colors flex items-center gap-0.5">
                        Изменить <ChevronRight size={11} />
                      </span>
                    </div>
                  </div>
                ) : (
                  /* Call to Action for Categories Without Limit */
                  <div className="pt-2 mt-1 border-t border-slate-100 flex items-center justify-between">
                    <span className="text-xs text-slate-500 font-semibold">
                      Потрачено: {formatCurrency(spent, currency.symbol)}
                    </span>
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        onOpenBudgetModal(c.id);
                      }}
                      className="inline-flex items-center gap-1 px-2.5 py-1 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 border border-emerald-200 rounded-lg text-xs font-bold transition-colors cursor-pointer"
                    >
                      <Plus size={12} />
                      <span>Задать лимит</span>
                    </button>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      ) : (
        <div className="py-8 text-center text-slate-400 text-xs">
          Нет категорий, подходящих под выбранный фильтр.
        </div>
      )}
    </div>
  );
};
