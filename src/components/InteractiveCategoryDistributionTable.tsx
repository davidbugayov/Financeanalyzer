import React, { useState, useMemo } from 'react';
import {
  PieChart as PieIcon,
  Table as TableIcon,
  LayoutGrid,
  ArrowUpDown,
  Search,
  Filter,
  CheckCircle2,
  TrendingUp,
  TrendingDown,
  Info,
  Calendar,
  X,
  CreditCard,
  ChevronDown,
  ChevronUp,
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { formatCurrency } from '../utils/financeCalculations';
import { DynamicIcon } from '../utils/iconHelper';

export interface MonthlyDataPoint {
  monthKey: string;
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
}

interface InteractiveCategoryDistributionTableProps {
  activeMonth?: MonthlyDataPoint | null;
  timeHorizon: 6 | 12;
  monthlyData: MonthlyDataPoint[];
  currencySymbol: string;
}

type ScopeType = 'month' | 'period';
type ViewType = 'table' | 'cards';
type SortField = 'percentage' | 'amount' | 'name' | 'count';

interface CategoryStat {
  name: string;
  categoryId?: string;
  color: string;
  icon: string;
  amount: number;
  percentage: number;
  count: number;
  avgPerTx: number;
  recentTransactions: Array<{
    id: string;
    amount: number;
    date: string;
    note?: string;
    walletName?: string;
  }>;
}

export const InteractiveCategoryDistributionTable: React.FC<
  InteractiveCategoryDistributionTableProps
> = ({ activeMonth, timeHorizon, monthlyData, currencySymbol }) => {
  const { transactions, categories, wallets } = useFinance();
  const [scope, setScope] = useState<ScopeType>('month');
  const [viewType, setViewType] = useState<ViewType>('table');
  const [searchQuery, setSearchQuery] = useState('');
  const [sortField, setSortField] = useState<SortField>('percentage');
  const [sortAsc, setSortAsc] = useState(false);
  const [selectedCategoryName, setSelectedCategoryName] = useState<string | null>(null);

  // Compute category statistics based on scope (selected month or all visible horizon)
  const { categoryStats, totalScopeExpense, totalTxCount } = useMemo(() => {
    let filteredTxs = transactions.filter((t) => t.type === 'expense');

    if (scope === 'month' && activeMonth) {
      filteredTxs = filteredTxs.filter(
        (t) => t.date && t.date.startsWith(activeMonth.monthKey)
      );
    } else {
      // Horizon filter (past 6 or 12 months in monthlyData)
      const validMonthKeys = new Set(monthlyData.map((m) => m.monthKey));
      filteredTxs = filteredTxs.filter((t) => {
        if (!t.date || t.date.length < 7) return false;
        const key = t.date.slice(0, 7);
        return validMonthKeys.has(key);
      });
    }

    const totalExpense = filteredTxs.reduce((sum, t) => sum + t.amount, 0);

    const catMap: {
      [name: string]: {
        amount: number;
        count: number;
        categoryId?: string;
        txs: Array<{
          id: string;
          amount: number;
          date: string;
          note?: string;
          walletName?: string;
        }>;
      };
    } = {};

    filteredTxs.forEach((t) => {
      const name = t.category || 'Прочее';
      if (!catMap[name]) {
        catMap[name] = {
          amount: 0,
          count: 0,
          categoryId: t.categoryId,
          txs: [],
        };
      }
      catMap[name].amount += t.amount;
      catMap[name].count += 1;

      const wallet = wallets.find((w) => w.id === t.walletId);
      catMap[name].txs.push({
        id: t.id,
        amount: t.amount,
        date: t.date,
        note: t.note,
        walletName: wallet?.name,
      });
    });

    const stats: CategoryStat[] = Object.keys(catMap).map((name) => {
      const item = catMap[name];
      const categoryObj = categories.find(
        (c) => c.name === name || c.id === item.categoryId
      );
      const percentage =
        totalExpense > 0 ? (item.amount / totalExpense) * 100 : 0;
      const avgPerTx = item.count > 0 ? Math.round(item.amount / item.count) : 0;

      // Sort recent transactions by date descending
      item.txs.sort((a, b) => b.date.localeCompare(a.date));

      return {
        name,
        categoryId: item.categoryId || categoryObj?.id,
        color: categoryObj?.color || '#94A3B8',
        icon: categoryObj?.icon || 'Tag',
        amount: item.amount,
        percentage,
        count: item.count,
        avgPerTx,
        recentTransactions: item.txs.slice(0, 4),
      };
    });

    return {
      categoryStats: stats,
      totalScopeExpense: totalExpense,
      totalTxCount: filteredTxs.length,
    };
  }, [
    transactions,
    categories,
    wallets,
    scope,
    activeMonth,
    monthlyData,
  ]);

  // Filter & Sort
  const processedCategories = useMemo(() => {
    let list = [...categoryStats];

    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase().trim();
      list = list.filter((c) => c.name.toLowerCase().includes(q));
    }

    list.sort((a, b) => {
      let cmp = 0;
      if (sortField === 'percentage' || sortField === 'amount') {
        cmp = a.amount - b.amount;
      } else if (sortField === 'count') {
        cmp = a.count - b.count;
      } else if (sortField === 'name') {
        cmp = a.name.localeCompare(b.name, 'ru');
      }
      return sortAsc ? cmp : -cmp;
    });

    return list;
  }, [categoryStats, searchQuery, sortField, sortAsc]);

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortAsc(!sortAsc);
    } else {
      setSortField(field);
      setSortAsc(false); // default desc
    }
  };

  const selectedCategoryDetails = useMemo(() => {
    if (!selectedCategoryName) return null;
    return categoryStats.find((c) => c.name === selectedCategoryName) || null;
  }, [selectedCategoryName, categoryStats]);

  return (
    <div className="pt-4 border-t border-slate-100 space-y-4">
      {/* Table & Legend Control Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <div className="flex items-center gap-2">
            <PieIcon size={18} className="text-rose-600" />
            <h4 className="text-sm sm:text-base font-extrabold text-slate-900">
              Распределение расходов по категориям
            </h4>
            <span className="text-xs font-bold text-slate-500 bg-slate-100 px-2 py-0.5 rounded-full">
              {categoryStats.length} категорий
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-0.5">
            {scope === 'month'
              ? `Данные за ${activeMonth?.fullLabel || 'выбранный месяц'}`
              : `Суммарные траты за горизонт ${timeHorizon} месяцев`}
            {' • '}Всего: {formatCurrency(totalScopeExpense, currencySymbol)}
          </p>
        </div>

        {/* Action Controls: Scope & View Switcher */}
        <div className="flex flex-wrap items-center gap-2 self-start sm:self-auto">
          {/* Scope: Month vs Period */}
          <div className="flex items-center bg-slate-100 p-1 rounded-xl text-xs font-bold text-slate-600 border border-slate-200/60">
            <button
              onClick={() => {
                setScope('month');
                setSelectedCategoryName(null);
              }}
              className={`px-2.5 py-1 rounded-lg transition-all ${
                scope === 'month'
                  ? 'bg-white text-slate-900 shadow-2xs font-extrabold'
                  : 'hover:text-slate-900 text-slate-500'
              }`}
            >
              За месяц
            </button>
            <button
              onClick={() => {
                setScope('period');
                setSelectedCategoryName(null);
              }}
              className={`px-2.5 py-1 rounded-lg transition-all ${
                scope === 'period'
                  ? 'bg-white text-slate-900 shadow-2xs font-extrabold'
                  : 'hover:text-slate-900 text-slate-500'
              }`}
            >
              За {timeHorizon} мес
            </button>
          </div>

          {/* View: Table vs Cards Legend */}
          <div className="flex items-center bg-slate-100 p-1 rounded-xl text-xs font-bold text-slate-600 border border-slate-200/60">
            <button
              onClick={() => setViewType('table')}
              title="Таблица"
              className={`p-1.5 rounded-lg transition-all ${
                viewType === 'table'
                  ? 'bg-white text-rose-600 shadow-2xs font-extrabold'
                  : 'hover:text-slate-900 text-slate-500'
              }`}
            >
              <TableIcon size={14} />
            </button>
            <button
              onClick={() => setViewType('cards')}
              title="Интерактивная легенда-плитка"
              className={`p-1.5 rounded-lg transition-all ${
                viewType === 'cards'
                  ? 'bg-white text-rose-600 shadow-2xs font-extrabold'
                  : 'hover:text-slate-900 text-slate-500'
              }`}
            >
              <LayoutGrid size={14} />
            </button>
          </div>
        </div>
      </div>

      {/* Visual Multi-Color 100% Proportion Bar */}
      {totalScopeExpense > 0 && (
        <div className="space-y-1.5 bg-slate-50 p-3 rounded-2xl border border-slate-200/70">
          <div className="flex items-center justify-between text-[11px] font-bold text-slate-500">
            <span>Пропорциональное разделение бюджета (100%)</span>
            <span className="text-slate-400">
              {totalTxCount} транзакций в {categoryStats.length} категориях
            </span>
          </div>

          {/* Multi-segment bar */}
          <div className="h-3 w-full rounded-full overflow-hidden flex bg-slate-200/70 p-0.5">
            {processedCategories.map((cat) => {
              if (cat.percentage < 0.5) return null;
              const isSelected = selectedCategoryName === cat.name;
              return (
                <div
                  key={cat.name}
                  onClick={() =>
                    setSelectedCategoryName(
                      selectedCategoryName === cat.name ? null : cat.name
                    )
                  }
                  title={`${cat.name}: ${cat.percentage.toFixed(1)}% (${formatCurrency(
                    cat.amount,
                    currencySymbol
                  )})`}
                  style={{
                    width: `${cat.percentage}%`,
                    backgroundColor: cat.color,
                  }}
                  className={`h-full transition-all cursor-pointer hover:opacity-90 hover:brightness-110 first:rounded-l-full last:rounded-r-full ${
                    isSelected ? 'ring-2 ring-slate-900 ring-offset-1 z-10' : ''
                  }`}
                />
              );
            })}
          </div>

          {/* Top category quick tags */}
          <div className="flex flex-wrap items-center gap-1.5 pt-1 overflow-x-auto no-scrollbar">
            {categoryStats.slice(0, 6).map((cat) => {
              const isSelected = selectedCategoryName === cat.name;
              return (
                <button
                  key={cat.name}
                  onClick={() =>
                    setSelectedCategoryName(
                      selectedCategoryName === cat.name ? null : cat.name
                    )
                  }
                  className={`text-[11px] font-bold px-2 py-0.5 rounded-lg flex items-center gap-1.5 transition-all cursor-pointer ${
                    isSelected
                      ? 'bg-slate-900 text-white shadow-2xs'
                      : 'bg-white hover:bg-slate-100 text-slate-700 border border-slate-200'
                  }`}
                >
                  <span
                    className="w-2 h-2 rounded-full shrink-0"
                    style={{ backgroundColor: cat.color }}
                  />
                  <span>{cat.name}</span>
                  <span
                    className={`font-black ${
                      isSelected ? 'text-rose-300' : 'text-slate-400'
                    }`}
                  >
                    {cat.percentage.toFixed(0)}%
                  </span>
                </button>
              );
            })}
          </div>
        </div>
      )}

      {/* Search and Category Highlight Banner if selected */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2.5">
        <div className="relative flex-1 max-w-sm">
          <Search
            size={14}
            className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
          />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Поиск категории в таблице..."
            className="w-full pl-8 pr-8 py-1.5 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500"
          />
          {searchQuery && (
            <button
              onClick={() => setSearchQuery('')}
              className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
            >
              <X size={12} />
            </button>
          )}
        </div>

        {selectedCategoryName && (
          <div className="flex items-center gap-2 bg-rose-50 border border-rose-200 text-rose-800 px-3 py-1 rounded-xl text-xs font-bold animate-in fade-in">
            <span>Выбрана категория: {selectedCategoryName}</span>
            <button
              onClick={() => setSelectedCategoryName(null)}
              className="hover:text-rose-950 p-0.5"
              title="Сбросить фильтр"
            >
              <X size={14} />
            </button>
          </div>
        )}
      </div>

      {/* Selected Category Highlight Detail Card */}
      {selectedCategoryDetails && (
        <div className="p-4 rounded-2xl bg-white border-2 border-rose-200 shadow-xs space-y-3 animate-in fade-in slide-in-from-top-2 duration-150">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div
                className="w-8 h-8 rounded-xl flex items-center justify-center text-white shrink-0 shadow-2xs"
                style={{ backgroundColor: selectedCategoryDetails.color }}
              >
                <DynamicIcon name={selectedCategoryDetails.icon} size={16} />
              </div>
              <div>
                <h5 className="text-sm font-extrabold text-slate-900">
                  {selectedCategoryDetails.name}
                </h5>
                <span className="text-xs text-slate-500">
                  Доля от расходов:{' '}
                  <strong className="text-rose-600 font-extrabold">
                    {selectedCategoryDetails.percentage.toFixed(1)}%
                  </strong>{' '}
                  (всего {selectedCategoryDetails.count} операций)
                </span>
              </div>
            </div>

            <div className="text-right">
              <span className="text-base sm:text-lg font-black text-rose-600 block">
                -{formatCurrency(selectedCategoryDetails.amount, currencySymbol)}
              </span>
              <span className="text-[11px] text-slate-400 font-medium">
                Ср. чек: {formatCurrency(selectedCategoryDetails.avgPerTx, currencySymbol)}
              </span>
            </div>
          </div>

          {/* Recent transactions for selected category */}
          {selectedCategoryDetails.recentTransactions.length > 0 && (
            <div className="pt-2 border-t border-slate-100 space-y-1.5">
              <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block">
                Последние операции в этой категории:
              </span>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                {selectedCategoryDetails.recentTransactions.map((tx) => (
                  <div
                    key={tx.id}
                    className="p-2 rounded-xl bg-slate-50 border border-slate-200/70 flex items-center justify-between text-xs"
                  >
                    <div>
                      <span className="font-semibold text-slate-800 block truncate max-w-[170px]">
                        {tx.note || selectedCategoryDetails.name}
                      </span>
                      <span className="text-[10px] text-slate-400">
                        {tx.date} {tx.walletName ? `• ${tx.walletName}` : ''}
                      </span>
                    </div>
                    <span className="font-extrabold text-rose-600 whitespace-nowrap">
                      -{formatCurrency(tx.amount, currencySymbol)}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Main View: Interactive Table Mode */}
      {viewType === 'table' ? (
        <div className="overflow-x-auto rounded-2xl border border-slate-200/90 shadow-2xs">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="bg-slate-50/90 border-b border-slate-200 text-slate-500 font-bold">
                <th
                  onClick={() => handleSort('name')}
                  className="py-3 px-3.5 cursor-pointer hover:text-slate-900 transition-colors select-none"
                >
                  <div className="flex items-center gap-1.5">
                    <span>Категория</span>
                    <ArrowUpDown size={12} className="text-slate-400" />
                  </div>
                </th>
                <th
                  onClick={() => handleSort('percentage')}
                  className="py-3 px-3.5 cursor-pointer hover:text-slate-900 transition-colors select-none text-right"
                >
                  <div className="flex items-center justify-end gap-1.5">
                    <span>Доля (%)</span>
                    <ArrowUpDown size={12} className="text-slate-400" />
                  </div>
                </th>
                <th
                  onClick={() => handleSort('amount')}
                  className="py-3 px-3.5 cursor-pointer hover:text-slate-900 transition-colors select-none text-right"
                >
                  <div className="flex items-center justify-end gap-1.5">
                    <span>Сумма трат</span>
                    <ArrowUpDown size={12} className="text-slate-400" />
                  </div>
                </th>
                <th
                  onClick={() => handleSort('count')}
                  className="hidden sm:table-cell py-3 px-3.5 cursor-pointer hover:text-slate-900 transition-colors select-none text-center"
                >
                  <div className="flex items-center justify-center gap-1.5">
                    <span>Операций</span>
                    <ArrowUpDown size={12} className="text-slate-400" />
                  </div>
                </th>
                <th className="hidden md:table-cell py-3 px-3.5 text-right font-bold text-slate-500">
                  Ср. чек
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 bg-white">
              {processedCategories.length === 0 ? (
                <tr>
                  <td colSpan={5} className="py-8 text-center text-slate-400 text-xs">
                    Нет данных о расходах по выбранным критериям.
                  </td>
                </tr>
              ) : (
                processedCategories.map((cat) => {
                  const isSelected = selectedCategoryName === cat.name;
                  return (
                    <tr
                      key={cat.name}
                      onClick={() =>
                        setSelectedCategoryName(
                          selectedCategoryName === cat.name ? null : cat.name
                        )
                      }
                      className={`cursor-pointer transition-colors ${
                        isSelected
                          ? 'bg-rose-50/70 font-semibold'
                          : 'hover:bg-slate-50/80'
                      }`}
                    >
                      {/* Category Name & Color Icon */}
                      <td className="py-2.5 px-3.5">
                        <div className="flex items-center gap-2.5">
                          <div
                            className="w-7 h-7 rounded-xl flex items-center justify-center text-white shrink-0 shadow-2xs"
                            style={{ backgroundColor: cat.color }}
                          >
                            <DynamicIcon name={cat.icon} size={14} />
                          </div>
                          <div>
                            <span className="font-extrabold text-slate-900 block">
                              {cat.name}
                            </span>
                            <div className="sm:hidden text-[10px] text-slate-400 font-medium">
                              {cat.count} оп. • Ср: {formatCurrency(cat.avgPerTx, currencySymbol)}
                            </div>
                          </div>
                        </div>
                      </td>

                      {/* Percentage & Mini Meter */}
                      <td className="py-2.5 px-3.5 text-right">
                        <div className="flex flex-col items-end gap-1">
                          <span className="font-black text-slate-900">
                            {cat.percentage.toFixed(1)}%
                          </span>
                          <div className="h-1.5 w-16 bg-slate-100 rounded-full overflow-hidden">
                            <div
                              className="h-full rounded-full transition-all duration-300"
                              style={{
                                width: `${cat.percentage}%`,
                                backgroundColor: cat.color,
                              }}
                            />
                          </div>
                        </div>
                      </td>

                      {/* Amount */}
                      <td className="py-2.5 px-3.5 text-right">
                        <span className="font-black text-rose-600 whitespace-nowrap">
                          -{formatCurrency(cat.amount, currencySymbol)}
                        </span>
                      </td>

                      {/* Count */}
                      <td className="hidden sm:table-cell py-2.5 px-3.5 text-center text-slate-600 font-semibold">
                        {cat.count}
                      </td>

                      {/* Avg per Transaction */}
                      <td className="hidden md:table-cell py-2.5 px-3.5 text-right text-slate-500 font-medium">
                        {formatCurrency(cat.avgPerTx, currencySymbol)}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      ) : (
        /* Alternative View: Interactive Tile Legend Cards */
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2.5">
          {processedCategories.length === 0 ? (
            <div className="col-span-full py-8 text-center text-slate-400 text-xs">
              Нет данных о расходах по выбранным критериям.
            </div>
          ) : (
            processedCategories.map((cat) => {
              const isSelected = selectedCategoryName === cat.name;
              return (
                <div
                  key={cat.name}
                  onClick={() =>
                    setSelectedCategoryName(
                      selectedCategoryName === cat.name ? null : cat.name
                    )
                  }
                  className={`p-3 rounded-2xl border transition-all cursor-pointer ${
                    isSelected
                      ? 'bg-rose-50/80 border-rose-300 shadow-xs'
                      : 'bg-white hover:bg-slate-50/90 border-slate-200/90 shadow-2xs'
                  }`}
                >
                  <div className="flex items-start justify-between gap-2 mb-2">
                    <div className="flex items-center gap-2">
                      <div
                        className="w-7 h-7 rounded-xl flex items-center justify-center text-white shrink-0 shadow-2xs"
                        style={{ backgroundColor: cat.color }}
                      >
                        <DynamicIcon name={cat.icon} size={14} />
                      </div>
                      <div>
                        <h6 className="font-extrabold text-slate-900 text-xs truncate max-w-[120px]">
                          {cat.name}
                        </h6>
                        <span className="text-[10px] text-slate-400 font-medium">
                          {cat.count} оп. • Ср: {formatCurrency(cat.avgPerTx, currencySymbol)}
                        </span>
                      </div>
                    </div>

                    <div className="text-right">
                      <span className="text-xs font-black text-rose-600 block">
                        -{formatCurrency(cat.amount, currencySymbol)}
                      </span>
                      <span className="text-[11px] font-black text-slate-900">
                        {cat.percentage.toFixed(1)}%
                      </span>
                    </div>
                  </div>

                  {/* Progress Meter Bar */}
                  <div className="h-1.5 w-full bg-slate-100 rounded-full overflow-hidden">
                    <div
                      className="h-full rounded-full transition-all duration-300"
                      style={{
                        width: `${cat.percentage}%`,
                        backgroundColor: cat.color,
                      }}
                    />
                  </div>
                </div>
              );
            })
          )}
        </div>
      )}

      {/* Summary Footer Note */}
      <div className="flex items-center justify-between text-[11px] text-slate-400 px-1 pt-1">
        <span>💡 Кликните на любую категорию для фильтрации и просмотра последних чеков</span>
        <span>{totalTxCount} операций всего</span>
      </div>
    </div>
  );
};
