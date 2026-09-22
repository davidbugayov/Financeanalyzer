import React, { useState, useMemo } from 'react';
import {
  X,
  PiggyBank,
  Check,
  AlertTriangle,
  RotateCcw,
  Sparkles,
  Search,
  Trash2,
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { formatCurrency } from '../utils/financeCalculations';
import { DynamicIcon } from '../utils/iconHelper';

interface BudgetModalProps {
  isOpen: boolean;
  onClose: () => void;
  initialCategoryId?: string | null;
}

export const BudgetModal: React.FC<BudgetModalProps> = ({
  isOpen,
  onClose,
  initialCategoryId,
}) => {
  const {
    categories,
    categoryBudgets,
    setCategoryBudget,
    removeCategoryBudget,
    transactions,
    currency,
  } = useFinance();

  const [searchQuery, setSearchQuery] = useState('');
  const [localLimits, setLocalLimits] = useState<Record<string, string>>({});
  const [savedSuccess, setSavedSuccess] = useState<string | null>(null);

  // Filter only expense categories
  const expenseCategories = useMemo(() => {
    return categories.filter((c) => c.isExpense);
  }, [categories]);

  // Current month string (YYYY-MM)
  const currentMonthPrefix = useMemo(() => {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    return `${year}-${month}`;
  }, []);

  // Calculate current month spending per category
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

  // Total summary of set budgets
  const summary = useMemo(() => {
    let totalLimit = 0;
    let totalSpentInBudgeted = 0;
    let activeBudgetCount = 0;
    let overBudgetCount = 0;

    expenseCategories.forEach((c) => {
      const limit = categoryBudgets[c.id] || 0;
      if (limit > 0) {
        totalLimit += limit;
        const spent = spendingPerCategory[c.id] || 0;
        totalSpentInBudgeted += spent;
        activeBudgetCount += 1;
        if (spent > limit) {
          overBudgetCount += 1;
        }
      }
    });

    const percent = totalLimit > 0 ? Math.round((totalSpentInBudgeted / totalLimit) * 100) : 0;
    const remaining = totalLimit - totalSpentInBudgeted;

    return {
      totalLimit,
      totalSpentInBudgeted,
      activeBudgetCount,
      overBudgetCount,
      percent,
      remaining,
    };
  }, [expenseCategories, categoryBudgets, spendingPerCategory]);

  if (!isOpen) return null;

  const handleInputChange = (catId: string, val: string) => {
    setLocalLimits((prev) => ({ ...prev, [catId]: val }));
  };

  const handleSaveSingle = (catId: string) => {
    const rawVal = localLimits[catId] !== undefined ? localLimits[catId] : (categoryBudgets[catId]?.toString() || '');
    const num = parseFloat(rawVal);
    if (!isNaN(num) && num > 0) {
      setCategoryBudget(catId, Math.round(num));
    } else {
      removeCategoryBudget(catId);
    }
    setSavedSuccess(catId);
    setTimeout(() => setSavedSuccess(null), 1500);
  };

  const handleQuickAdd = (catId: string, increment: number) => {
    const current = categoryBudgets[catId] || 0;
    const newLimit = current + increment;
    setCategoryBudget(catId, newLimit);
    setLocalLimits((prev) => ({ ...prev, [catId]: newLimit.toString() }));
    setSavedSuccess(catId);
    setTimeout(() => setSavedSuccess(null), 1500);
  };

  const handleRemove = (catId: string) => {
    removeCategoryBudget(catId);
    setLocalLimits((prev) => {
      const next = { ...prev };
      delete next[catId];
      return next;
    });
  };

  // Smart Autofill: set limits based on actual expenses + 20%
  const handleSmartAutofill = () => {
    expenseCategories.forEach((c) => {
      const spent = spendingPerCategory[c.id] || 0;
      if (spent > 0) {
        // Round up to nearest 500 or 1000
        const suggested = Math.max(1000, Math.ceil((spent * 1.2) / 500) * 500);
        setCategoryBudget(c.id, suggested);
        setLocalLimits((prev) => ({ ...prev, [c.id]: suggested.toString() }));
      }
    });
  };

  const filteredCategories = expenseCategories.filter((c) => {
    if (!searchQuery.trim()) return true;
    const q = searchQuery.toLowerCase();
    return c.name.toLowerCase().includes(q) || c.subcategories.some((s) => s.toLowerCase().includes(q));
  });

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-slate-900/60 backdrop-blur-xs animate-in fade-in duration-200">
      <div
        id="budget_management_modal"
        className="bg-white w-full max-w-2xl rounded-3xl shadow-2xl border border-slate-100 max-h-[90vh] flex flex-col overflow-hidden"
      >
        {/* Header */}
        <div className="p-5 sm:p-6 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-emerald-100 text-emerald-700 flex items-center justify-center shadow-xs">
              <PiggyBank size={22} className="stroke-[2.5]" />
            </div>
            <div>
              <h2 className="text-base sm:text-lg font-extrabold text-slate-900">
                Ежемесячные бюджеты
              </h2>
              <p className="text-xs text-slate-500">
                Установите плановые лимиты трат по категориям на текущий месяц
              </p>
            </div>
          </div>
          <button
            id="close_budget_modal_btn"
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-500 hover:text-slate-700 flex items-center justify-center transition-colors cursor-pointer"
          >
            <X size={16} />
          </button>
        </div>

        {/* Total Month Overview Bar */}
        <div className="p-4 sm:p-5 bg-gradient-to-r from-slate-900 via-slate-800 to-emerald-950 text-white border-b border-slate-800">
          <div className="flex items-center justify-between text-xs mb-2">
            <span className="font-bold text-emerald-400">Общий прогресс бюджетов</span>
            <span className="text-slate-300 font-semibold">
              Категорий с лимитом: <strong className="text-white">{summary.activeBudgetCount}</strong> из {expenseCategories.length}
            </span>
          </div>

          <div className="grid grid-cols-3 gap-3 mb-3 text-center sm:text-left">
            <div className="bg-white/5 p-2.5 rounded-xl backdrop-blur-xs border border-white/5">
              <span className="text-[10px] text-slate-400 block font-medium">Запланировано</span>
              <span className="text-xs sm:text-sm font-bold text-white">
                {formatCurrency(summary.totalLimit, currency.symbol)}
              </span>
            </div>
            <div className="bg-white/5 p-2.5 rounded-xl backdrop-blur-xs border border-white/5">
              <span className="text-[10px] text-slate-400 block font-medium">Потрачено в них</span>
              <span className="text-xs sm:text-sm font-bold text-emerald-300">
                {formatCurrency(summary.totalSpentInBudgeted, currency.symbol)}
              </span>
            </div>
            <div className="bg-white/5 p-2.5 rounded-xl backdrop-blur-xs border border-white/5">
              <span className="text-[10px] text-slate-400 block font-medium">
                {summary.remaining >= 0 ? 'Остаток' : 'Превышение'}
              </span>
              <span
                className={`text-xs sm:text-sm font-bold ${
                  summary.remaining >= 0 ? 'text-emerald-400' : 'text-rose-400'
                }`}
              >
                {summary.remaining >= 0 ? '' : '+'}
                {formatCurrency(Math.abs(summary.remaining), currency.symbol)}
              </span>
            </div>
          </div>

          {/* Progress Bar */}
          {summary.totalLimit > 0 && (
            <div className="space-y-1">
              <div className="w-full h-2.5 bg-white/15 rounded-full overflow-hidden">
                <div
                  className={`h-full transition-all duration-500 rounded-full ${
                    summary.percent >= 100
                      ? 'bg-rose-500'
                      : summary.percent >= 80
                      ? 'bg-amber-400'
                      : 'bg-emerald-400'
                  }`}
                  style={{ width: `${Math.min(100, summary.percent)}%` }}
                />
              </div>
              <div className="flex items-center justify-between text-[10px] text-slate-400">
                <span>Использовано: {summary.percent}%</span>
                {summary.overBudgetCount > 0 && (
                  <span className="text-rose-300 font-bold flex items-center gap-1">
                    <AlertTriangle size={11} /> {summary.overBudgetCount} катег. превышено
                  </span>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Toolbar & Search */}
        <div className="p-3 sm:p-4 border-b border-slate-100 flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-2.5 bg-slate-50/60">
          <div className="relative flex-1">
            <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              placeholder="Поиск категории..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full text-xs pl-8 pr-3 py-2 bg-white border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500"
            />
          </div>

          <div className="flex items-center gap-2 shrink-0">
            <button
              type="button"
              onClick={handleSmartAutofill}
              className="flex items-center gap-1.5 px-3 py-2 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 text-xs font-bold rounded-xl border border-emerald-200 transition-colors"
              title="Заполнить лимиты с запасом 20% на основе текущих трат"
            >
              <Sparkles size={13} />
              <span>Автозаполнение +20%</span>
            </button>
          </div>
        </div>

        {/* Categories List */}
        <div className="flex-1 overflow-y-auto p-4 sm:p-5 space-y-3 divide-y divide-slate-100">
          {filteredCategories.map((c) => {
            const currentLimit = categoryBudgets[c.id] || 0;
            const spent = spendingPerCategory[c.id] || 0;
            const inputValue =
              localLimits[c.id] !== undefined
                ? localLimits[c.id]
                : currentLimit > 0
                ? currentLimit.toString()
                : '';

            const percent = currentLimit > 0 ? Math.round((spent / currentLimit) * 100) : 0;
            const isOver = currentLimit > 0 && spent > currentLimit;
            const isHighlighted = initialCategoryId === c.id;

            return (
              <div
                key={c.id}
                id={`budget_item_${c.id}`}
                className={`pt-3 first:pt-0 rounded-2xl p-3 transition-colors ${
                  isHighlighted ? 'bg-emerald-50/70 border border-emerald-200' : 'hover:bg-slate-50/60'
                }`}
              >
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                  {/* Category Info */}
                  <div className="flex items-center gap-3 min-w-0">
                    <div
                      className="w-10 h-10 rounded-xl flex items-center justify-center text-white shrink-0 shadow-xs"
                      style={{ backgroundColor: c.color || '#10B981' }}
                    >
                      <DynamicIcon name={c.icon || 'Tag'} size={18} />
                    </div>
                    <div className="min-w-0">
                      <div className="flex items-center gap-2">
                        <span className="text-xs sm:text-sm font-bold text-slate-900 truncate">
                          {c.name}
                        </span>
                        {currentLimit > 0 && (
                          <span
                            className={`text-[10px] font-extrabold px-2 py-0.5 rounded-md flex items-center gap-1 ${
                              isOver
                                ? 'bg-rose-100 text-rose-700'
                                : percent >= 80
                                ? 'bg-amber-100 text-amber-800'
                                : 'bg-emerald-100 text-emerald-800'
                            }`}
                          >
                            {isOver && <AlertTriangle size={10} />}
                            {percent}%
                          </span>
                        )}
                      </div>
                      <div className="text-[11px] text-slate-400 mt-0.5 flex items-center gap-2">
                        <span>
                          Потрачено в этом месяце:{' '}
                          <strong className="text-slate-700 font-semibold">
                            {formatCurrency(spent, currency.symbol)}
                          </strong>
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* Limit input & Quick controls */}
                  <div className="flex items-center gap-2 shrink-0 self-end sm:self-auto">
                    <div className="relative">
                      <input
                        type="number"
                        min="0"
                        step="500"
                        placeholder="Лимит"
                        value={inputValue}
                        onChange={(e) => handleInputChange(c.id, e.target.value)}
                        onKeyDown={(e) => {
                          if (e.key === 'Enter') handleSaveSingle(c.id);
                        }}
                        className="w-28 sm:w-32 text-xs font-bold pl-3 pr-7 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
                      />
                      <span className="absolute right-2.5 top-1/2 -translate-y-1/2 text-xs font-bold text-slate-400 pointer-events-none">
                        {currency.symbol}
                      </span>
                    </div>

                    <button
                      type="button"
                      onClick={() => handleSaveSingle(c.id)}
                      className="p-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold transition-colors cursor-pointer shadow-xs"
                      title="Сохранить лимит"
                    >
                      {savedSuccess === c.id ? <Check size={14} /> : <Check size={14} />}
                    </button>

                    {currentLimit > 0 && (
                      <button
                        type="button"
                        onClick={() => handleRemove(c.id)}
                        className="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-xl transition-colors cursor-pointer"
                        title="Удалить лимит"
                      >
                        <Trash2 size={14} />
                      </button>
                    )}
                  </div>
                </div>

                {/* Quick Increment Chips */}
                <div className="flex items-center gap-1.5 mt-2 pt-2 border-t border-slate-100/80">
                  <span className="text-[10px] text-slate-400 font-medium">Быстро:</span>
                  <button
                    type="button"
                    onClick={() => handleQuickAdd(c.id, 1000)}
                    className="text-[10px] font-bold px-2 py-0.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg transition-colors cursor-pointer"
                  >
                    +1 000 {currency.symbol}
                  </button>
                  <button
                    type="button"
                    onClick={() => handleQuickAdd(c.id, 5000)}
                    className="text-[10px] font-bold px-2 py-0.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg transition-colors cursor-pointer"
                  >
                    +5 000 {currency.symbol}
                  </button>
                  <button
                    type="button"
                    onClick={() => handleQuickAdd(c.id, 10000)}
                    className="text-[10px] font-bold px-2 py-0.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg transition-colors cursor-pointer"
                  >
                    +10 000 {currency.symbol}
                  </button>

                  {spent > 0 && currentLimit !== spent && (
                    <button
                      type="button"
                      onClick={() => {
                        const target = Math.ceil(spent / 500) * 500;
                        setCategoryBudget(c.id, target);
                        setLocalLimits((prev) => ({ ...prev, [c.id]: target.toString() }));
                      }}
                      className="text-[10px] font-bold px-2 py-0.5 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 rounded-lg transition-colors cursor-pointer ml-auto"
                    >
                      По расходам ({formatCurrency(Math.ceil(spent / 500) * 500, currency.symbol)})
                    </button>
                  )}
                </div>

                {/* Mini progress bar if limit is set */}
                {currentLimit > 0 && (
                  <div className="mt-2">
                    <div className="w-full h-1.5 bg-slate-100 rounded-full overflow-hidden">
                      <div
                        className={`h-full transition-all duration-300 rounded-full ${
                          isOver ? 'bg-rose-500' : percent >= 80 ? 'bg-amber-400' : 'bg-emerald-500'
                        }`}
                        style={{ width: `${Math.min(100, percent)}%` }}
                      />
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>

        {/* Footer */}
        <div className="p-4 sm:p-5 border-t border-slate-100 flex items-center justify-between bg-slate-50/50">
          <span className="text-xs text-slate-500">
            Лимиты автоматически отслеживаются на главной панели
          </span>
          <button
            type="button"
            onClick={onClose}
            className="px-5 py-2.5 bg-slate-900 hover:bg-slate-800 text-white rounded-xl text-xs font-bold transition-all shadow-sm cursor-pointer"
          >
            Готово
          </button>
        </div>
      </div>
    </div>
  );
};
