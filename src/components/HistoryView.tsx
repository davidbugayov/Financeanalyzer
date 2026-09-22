import React, { useState, useMemo } from 'react';
import {
  Search,
  Filter,
  Calendar,
  Layers,
  Trash2,
  Edit2,
  ArrowRightLeft,
  X,
  Globe,
  Tag,
  Repeat,
  Zap,
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { Transaction, GroupingType, TransactionType } from '../types';
import { formatCurrency } from '../utils/financeCalculations';
import { DynamicIcon } from '../utils/iconHelper';
import { getIntervalLabel } from '../utils/recurringProcessor';

interface HistoryViewProps {
  onEditTransaction: (tx: Transaction) => void;
  onOpenAddModal: () => void;
  onOpenConverterModal?: () => void;
}

export const HistoryView: React.FC<HistoryViewProps> = ({
  onEditTransaction,
  onOpenAddModal,
  onOpenConverterModal,
}) => {
  const { transactions, categories, wallets, currency, deleteTransaction } = useFinance();

  const [searchQuery, setSearchQuery] = useState('');
  const [selectedType, setSelectedType] = useState<TransactionType | 'all' | 'foreign' | 'recurring'>('all');
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [selectedWallet, setSelectedWallet] = useState<string>('all');
  const [selectedTag, setSelectedTag] = useState<string>('all');
  const [recurrenceFilter, setRecurrenceFilter] = useState<'all' | 'one_time' | 'recurring'>('all');
  const [recurringSubType, setRecurringSubType] = useState<'all' | 'templates' | 'auto'>('all');
  const [grouping, setGrouping] = useState<GroupingType>('date');
  const [deleteConfirmId, setDeleteConfirmId] = useState<string | null>(null);

  // Available unique tags with counts
  const availableTagsWithCount = useMemo(() => {
    const map: { [tag: string]: number } = {};
    transactions.forEach((t) => {
      t.tags?.forEach((tag) => {
        map[tag] = (map[tag] || 0) + 1;
      });
    });
    return Object.entries(map)
      .map(([tag, count]) => ({ tag, count }))
      .sort((a, b) => b.count - a.count || a.tag.localeCompare(b.tag));
  }, [transactions]);

  // Recurrence counts for quick-toggle badges
  const recurrenceCounts = useMemo(() => {
    let oneTime = 0;
    let recurring = 0;
    let templates = 0;
    let auto = 0;
    transactions.forEach((t) => {
      if (t.isRecurring || t.parentRecurringId) {
        recurring++;
        if (t.isRecurring) templates++;
        if (t.parentRecurringId) auto++;
      } else {
        oneTime++;
      }
    });
    return {
      total: transactions.length,
      oneTime,
      recurring,
      templates,
      auto,
    };
  }, [transactions]);

  // Filtered transactions
  const filtered = useMemo(() => {
    return transactions.filter((t) => {
      // Search
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase();
        const matchCat = t.category.toLowerCase().includes(q);
        const matchSub = (t.subcategory || '').toLowerCase().includes(q);
        const matchNote = (t.note || '').toLowerCase().includes(q);
        const matchCountry = (t.country || '').toLowerCase().includes(q);
        const matchCity = (t.city || '').toLowerCase().includes(q);
        const matchCurr = (t.originalCurrency || '').toLowerCase().includes(q);
        const matchTag = t.tags
          ? t.tags.some((tag) => tag.toLowerCase().includes(q) || `#${tag}`.toLowerCase().includes(q))
          : false;
        if (!matchCat && !matchSub && !matchNote && !matchCountry && !matchCity && !matchCurr && !matchTag) return false;
      }

      // Tag filter
      if (selectedTag !== 'all') {
        if (!t.tags || !t.tags.includes(selectedTag)) return false;
      }

      // Recurrence filter (One-time vs Recurring series)
      if (recurrenceFilter === 'one_time') {
        if (t.isRecurring || t.parentRecurringId) return false;
      } else if (recurrenceFilter === 'recurring') {
        if (!t.isRecurring && !t.parentRecurringId) return false;
        if (recurringSubType === 'templates' && !t.isRecurring) return false;
        if (recurringSubType === 'auto' && !t.parentRecurringId) return false;
      }

      // Type
      if (selectedType === 'foreign') {
        if (!t.isForeignCurrency) return false;
      } else if (selectedType === 'recurring') {
        if (!t.isRecurring && !t.parentRecurringId) return false;
      } else if (selectedType !== 'all' && t.type !== selectedType) {
        return false;
      }

      // Category
      if (selectedCategory !== 'all' && t.category !== selectedCategory) return false;

      // Wallet
      if (selectedWallet !== 'all' && t.walletId !== selectedWallet && t.targetWalletId !== selectedWallet) {
        return false;
      }

      return true;
    });
  }, [transactions, searchQuery, selectedTag, recurrenceFilter, recurringSubType, selectedType, selectedCategory, selectedWallet]);

  // Grouped transactions
  const groupedData = useMemo(() => {
    const groups: { [key: string]: Transaction[] } = {};

    filtered.forEach((t) => {
      if (grouping === 'tag') {
        if (t.tags && t.tags.length > 0) {
          t.tags.forEach((tag) => {
            const tagKey = `#${tag}`;
            if (!groups[tagKey]) groups[tagKey] = [];
            groups[tagKey].push(t);
          });
          return;
        } else {
          const key = 'Без тегов';
          if (!groups[key]) groups[key] = [];
          groups[key].push(t);
          return;
        }
      }

      let key = '';
      if (grouping === 'date') {
        key = t.date; // YYYY-MM-DD
      } else if (grouping === 'month') {
        key = t.date.substring(0, 7); // YYYY-MM
      } else if (grouping === 'category') {
        key = t.category;
      } else if (grouping === 'wallet') {
        const w = wallets.find((wal) => wal.id === t.walletId);
        key = w ? w.name : 'Неизвестный кошелек';
      }

      if (!groups[key]) groups[key] = [];
      groups[key].push(t);
    });

    // Convert to sorted array
    return Object.keys(groups)
      .sort((a, b) => {
        if (grouping === 'date' || grouping === 'month') return b.localeCompare(a);
        if (grouping === 'tag') {
          if (a === 'Без тегов') return 1;
          if (b === 'Без тегов') return -1;
          return a.localeCompare(b);
        }
        return a.localeCompare(b);
      })
      .map((key) => ({
        key,
        transactions: groups[key],
        totalExpense: groups[key]
          .filter((t) => t.type === 'expense')
          .reduce((sum, t) => sum + t.amount, 0),
        totalIncome: groups[key]
          .filter((t) => t.type === 'income')
          .reduce((sum, t) => sum + t.amount, 0),
      }));
  }, [filtered, grouping, wallets]);

  const handleDelete = (id: string) => {
    deleteTransaction(id);
    setDeleteConfirmId(null);
  };

  return (
    <div className="space-y-5 pb-24">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h2 className="text-xl sm:text-2xl font-black text-slate-900">История операций</h2>
          <p className="text-xs text-slate-500">Поиск, фильтры и группировка расходов и доходов</p>
        </div>

        {/* Grouping switcher and converter */}
        <div className="flex items-center gap-2 flex-wrap self-start sm:self-auto">
          {onOpenConverterModal && (
            <button
              id="history_open_converter_btn"
              onClick={onOpenConverterModal}
              className="flex items-center gap-1.5 px-3 py-1.5 bg-blue-50 text-blue-700 hover:bg-blue-100 rounded-xl text-xs font-bold transition-colors border border-blue-200 shadow-2xs"
            >
              <Globe size={14} />
              <span>Конвертер валют</span>
            </button>
          )}

          <div className="flex items-center gap-1 bg-slate-200/70 p-1 rounded-xl text-xs font-bold text-slate-600">
            <Layers size={14} className="ml-1 text-slate-400" />
            <button
              onClick={() => setGrouping('date')}
              className={`px-2.5 py-1 rounded-lg transition-all ${
                grouping === 'date' ? 'bg-white text-slate-900 shadow-xs' : 'hover:text-slate-900'
              }`}
            >
              По дням
            </button>
            <button
              onClick={() => setGrouping('month')}
              className={`px-2.5 py-1 rounded-lg transition-all ${
                grouping === 'month' ? 'bg-white text-slate-900 shadow-xs' : 'hover:text-slate-900'
              }`}
            >
              По месяцам
            </button>
            <button
              onClick={() => setGrouping('category')}
              className={`px-2.5 py-1 rounded-lg transition-all ${
                grouping === 'category' ? 'bg-white text-slate-900 shadow-xs' : 'hover:text-slate-900'
              }`}
            >
              По категории
            </button>
            <button
              onClick={() => setGrouping('wallet')}
              className={`px-2.5 py-1 rounded-lg transition-all ${
                grouping === 'wallet' ? 'bg-white text-slate-900 shadow-xs' : 'hover:text-slate-900'
              }`}
            >
              По счету
            </button>
            <button
              id="history_group_by_tag_btn"
              onClick={() => setGrouping('tag')}
              className={`px-2.5 py-1 rounded-lg transition-all ${
                grouping === 'tag' ? 'bg-white text-slate-900 shadow-xs' : 'hover:text-slate-900'
              }`}
            >
              По тегам
            </button>
          </div>
        </div>
      </div>

      {/* Search & Filter Bar */}
      <div className="bg-white rounded-2xl border border-slate-200/90 p-4 shadow-xs space-y-3">
        <div className="relative">
          <Search size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            id="history_search_input"
            type="text"
            placeholder="Поиск по категории, подкатегории, заметке, стране, #тегу..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full text-xs sm:text-sm pl-10 pr-9 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
          />
          {searchQuery && (
            <button
              onClick={() => setSearchQuery('')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
            >
              <X size={16} />
            </button>
          )}
        </div>

        {/* Recurrence Mode Filter Bar (One-time vs Recurring Series) */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pt-1 pb-1">
          <div className="inline-flex p-1 bg-slate-100 rounded-xl text-xs font-bold text-slate-600">
            <button
              id="history_filter_recurrence_all"
              onClick={() => {
                setRecurrenceFilter('all');
                setRecurringSubType('all');
              }}
              className={`px-3 py-1.5 rounded-lg transition-all flex items-center gap-1.5 cursor-pointer ${
                recurrenceFilter === 'all'
                  ? 'bg-white text-slate-900 shadow-2xs font-extrabold'
                  : 'hover:text-slate-900'
              }`}
            >
              <span>Все операции</span>
              <span
                className={`text-[10px] px-1.5 py-0.5 rounded-full ${
                  recurrenceFilter === 'all'
                    ? 'bg-slate-200 text-slate-800 font-bold'
                    : 'bg-slate-200/60 text-slate-500'
                }`}
              >
                {recurrenceCounts.total}
              </span>
            </button>

            <button
              id="history_filter_recurrence_onetime"
              onClick={() => {
                setRecurrenceFilter('one_time');
                setRecurringSubType('all');
              }}
              className={`px-3 py-1.5 rounded-lg transition-all flex items-center gap-1.5 cursor-pointer ${
                recurrenceFilter === 'one_time'
                  ? 'bg-white text-blue-900 shadow-2xs font-extrabold'
                  : 'hover:text-slate-900'
              }`}
            >
              <Zap
                size={13}
                className={recurrenceFilter === 'one_time' ? 'text-blue-600' : 'text-slate-400'}
              />
              <span>Разовые</span>
              <span
                className={`text-[10px] px-1.5 py-0.5 rounded-full ${
                  recurrenceFilter === 'one_time'
                    ? 'bg-blue-100 text-blue-800 font-bold'
                    : 'bg-slate-200/60 text-slate-500'
                }`}
              >
                {recurrenceCounts.oneTime}
              </span>
            </button>

            <button
              id="history_filter_recurrence_recurring"
              onClick={() => {
                setRecurrenceFilter('recurring');
              }}
              className={`px-3 py-1.5 rounded-lg transition-all flex items-center gap-1.5 cursor-pointer ${
                recurrenceFilter === 'recurring'
                  ? 'bg-teal-700 text-white shadow-2xs font-extrabold'
                  : 'hover:text-slate-900'
              }`}
            >
              <Repeat
                size={13}
                className={recurrenceFilter === 'recurring' ? 'text-teal-200' : 'text-slate-400'}
              />
              <span>Регулярные серии</span>
              <span
                className={`text-[10px] px-1.5 py-0.5 rounded-full ${
                  recurrenceFilter === 'recurring'
                    ? 'bg-teal-800 text-teal-100 font-bold'
                    : 'bg-slate-200/60 text-slate-500'
                }`}
              >
                {recurrenceCounts.recurring}
              </span>
            </button>
          </div>

          {/* Sub-pills when recurring series is selected */}
          {recurrenceFilter === 'recurring' && (
            <div className="flex items-center gap-1 text-[11px] font-semibold text-slate-600 self-start sm:self-auto animate-in fade-in duration-150">
              <span className="text-slate-400 text-[10px] uppercase font-bold tracking-wider mr-1">
                Серии:
              </span>
              <button
                id="history_recurring_sub_all"
                onClick={() => setRecurringSubType('all')}
                className={`px-2.5 py-1 rounded-lg transition-colors cursor-pointer ${
                  recurringSubType === 'all'
                    ? 'bg-teal-100 text-teal-900 font-bold'
                    : 'bg-slate-100 hover:bg-slate-200 text-slate-600'
                }`}
              >
                Все ({recurrenceCounts.recurring})
              </button>
              <button
                id="history_recurring_sub_templates"
                onClick={() => setRecurringSubType('templates')}
                className={`px-2.5 py-1 rounded-lg transition-colors cursor-pointer ${
                  recurringSubType === 'templates'
                    ? 'bg-teal-100 text-teal-900 font-bold'
                    : 'bg-slate-100 hover:bg-slate-200 text-slate-600'
                }`}
                title="Только исходные шаблоны расписаний"
              >
                Шаблоны ({recurrenceCounts.templates})
              </button>
              <button
                id="history_recurring_sub_auto"
                onClick={() => setRecurringSubType('auto')}
                className={`px-2.5 py-1 rounded-lg transition-colors cursor-pointer ${
                  recurringSubType === 'auto'
                    ? 'bg-teal-100 text-teal-900 font-bold'
                    : 'bg-slate-100 hover:bg-slate-200 text-slate-600'
                }`}
                title="Только выполненные автоплатежи"
              >
                Автоплатежи ({recurrenceCounts.auto})
              </button>
            </div>
          )}
        </div>

        {/* Filters Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-xs font-semibold">
          {/* Type Filter */}
          <select
            id="history_filter_type"
            value={selectedType}
            onChange={(e) => setSelectedType(e.target.value as any)}
            className="px-2.5 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-1 focus:ring-emerald-500 truncate"
          >
            <option value="all">Все типы</option>
            <option value="expense">Расходы</option>
            <option value="income">Доходы</option>
            <option value="transfer">Переводы</option>
            <option value="recurring">🔄 Регулярные</option>
            <option value="foreign">🌐 За границей (в валюте)</option>
          </select>

          {/* Category Filter */}
          <select
            id="history_filter_category"
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
            className="px-2.5 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-1 focus:ring-emerald-500 truncate"
          >
            <option value="all">Все категории</option>
            {categories.map((c) => (
              <option key={c.id} value={c.name}>
                {c.name}
              </option>
            ))}
          </select>

          {/* Wallet Filter */}
          <select
            id="history_filter_wallet"
            value={selectedWallet}
            onChange={(e) => setSelectedWallet(e.target.value)}
            className="px-2.5 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-1 focus:ring-emerald-500 truncate"
          >
            <option value="all">Все кошельки</option>
            {wallets.map((w) => (
              <option key={w.id} value={w.id}>
                {w.name}
              </option>
            ))}
          </select>

          {/* Tag Filter */}
          <select
            id="history_filter_tag"
            value={selectedTag}
            onChange={(e) => setSelectedTag(e.target.value)}
            className={`px-2.5 py-2 border rounded-xl focus:outline-none focus:ring-1 focus:ring-emerald-500 truncate ${
              selectedTag !== 'all'
                ? 'bg-emerald-50 border-emerald-300 text-emerald-800 font-bold'
                : 'bg-slate-50 border-slate-200 text-slate-700'
            }`}
          >
            <option value="all">Все теги ({availableTagsWithCount.length})</option>
            {availableTagsWithCount.map(({ tag, count }) => (
              <option key={tag} value={tag}>
                #{tag} ({count})
              </option>
            ))}
          </select>
        </div>

        {/* Quick Tag Chips Selector */}
        {availableTagsWithCount.length > 0 && (
          <div className="flex items-center gap-1.5 pt-2 border-t border-slate-100 overflow-x-auto pb-1 text-xs">
            <div className="flex items-center gap-1 text-slate-400 font-semibold shrink-0 mr-1 select-none">
              <Tag size={13} className="text-emerald-600" />
              <span>Теги:</span>
            </div>
            <button
              id="history_tag_chip_all"
              onClick={() => setSelectedTag('all')}
              className={`px-2.5 py-1 rounded-lg text-xs font-bold shrink-0 transition-all ${
                selectedTag === 'all'
                  ? 'bg-slate-900 text-white shadow-2xs'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              Все ({transactions.length})
            </button>
            {availableTagsWithCount.map(({ tag, count }) => {
              const isSelected = selectedTag === tag;
              return (
                <button
                  key={tag}
                  id={`history_tag_chip_${tag}`}
                  onClick={() => setSelectedTag(isSelected ? 'all' : tag)}
                  className={`px-2.5 py-1 rounded-lg text-xs font-bold shrink-0 flex items-center gap-1 transition-all cursor-pointer ${
                    isSelected
                      ? 'bg-emerald-600 text-white shadow-xs'
                      : 'bg-emerald-50/80 hover:bg-emerald-100 text-emerald-800 border border-emerald-200/70'
                  }`}
                >
                  <span>#{tag}</span>
                  <span
                    className={`text-[10px] px-1 rounded-full ${
                      isSelected
                        ? 'bg-emerald-700 text-emerald-100'
                        : 'bg-emerald-200/70 text-emerald-800'
                    }`}
                  >
                    {count}
                  </span>
                  {isSelected && <X size={12} className="ml-0.5" />}
                </button>
              );
            })}
          </div>
        )}

        {/* Active Tag Filter Status Banner */}
        {selectedTag !== 'all' && (
          <div className="flex items-center justify-between px-3 py-2 bg-emerald-50 border border-emerald-200 rounded-xl text-xs">
            <div className="flex items-center gap-2">
              <span className="font-semibold text-emerald-900">Фильтр по тегу:</span>
              <span className="px-2 py-0.5 bg-emerald-600 text-white rounded-md font-bold">
                #{selectedTag}
              </span>
              <span className="text-emerald-700 font-medium">
                (найдено: {filtered.length})
              </span>
            </div>
            <button
              onClick={() => setSelectedTag('all')}
              className="text-xs font-bold text-emerald-800 hover:text-emerald-950 underline cursor-pointer"
            >
              Сбросить
            </button>
          </div>
        )}

        {/* Active Recurrence Filter Status Banner */}
        {recurrenceFilter !== 'all' && (
          <div className="flex items-center justify-between px-3 py-2 bg-teal-50/90 border border-teal-200 rounded-xl text-xs">
            <div className="flex items-center gap-2">
              {recurrenceFilter === 'one_time' ? (
                <Zap size={14} className="text-blue-600" />
              ) : (
                <Repeat size={14} className="text-teal-700" />
              )}
              <span className="font-semibold text-teal-950">
                {recurrenceFilter === 'one_time' ? 'Фильтр по периодичности:' : 'Режим серий:'}
              </span>
              <span
                className={`px-2 py-0.5 rounded-md font-bold text-white ${
                  recurrenceFilter === 'one_time' ? 'bg-blue-600' : 'bg-teal-700'
                }`}
              >
                {recurrenceFilter === 'one_time'
                  ? '⚡ Только разовые операции'
                  : recurringSubType === 'templates'
                  ? '🔁 Только шаблоны расписаний'
                  : recurringSubType === 'auto'
                  ? '🔁 Только автоплатежи'
                  : '🔁 Все регулярные серии'}
              </span>
              <span className="text-teal-800 font-medium">
                (найдено: {filtered.length})
              </span>
            </div>
            <button
              onClick={() => {
                setRecurrenceFilter('all');
                setRecurringSubType('all');
              }}
              className="text-xs font-bold text-teal-800 hover:text-teal-950 underline cursor-pointer"
            >
              Сбросить
            </button>
          </div>
        )}
      </div>

      {/* Grouped Transaction Lists */}
      {groupedData.length === 0 ? (
        <div className="bg-white rounded-3xl border border-slate-200 p-12 text-center">
          <p className="text-sm font-bold text-slate-500">Ничего не найдено</p>
          <p className="text-xs text-slate-400 mt-1">Попробуйте изменить параметры поиска или фильтров</p>
        </div>
      ) : (
        <div className="space-y-4">
          {groupedData.map((group) => (
            <div
              key={group.key}
              className="bg-white rounded-2xl border border-slate-200/90 shadow-xs overflow-hidden"
            >
              {/* Group Header */}
              <div className="px-4 py-3 bg-slate-50/80 border-b border-slate-100 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  {grouping === 'tag' ? (
                    <Tag size={14} className="text-emerald-600" />
                  ) : grouping === 'category' || grouping === 'wallet' ? (
                    <Layers size={14} className="text-slate-400" />
                  ) : (
                    <Calendar size={14} className="text-slate-400" />
                  )}
                  <span className="text-xs sm:text-sm font-extrabold text-slate-800">
                    {group.key}
                  </span>
                  <span className="text-[11px] font-semibold text-slate-400">
                    ({group.transactions.length})
                  </span>
                </div>

                <div className="flex items-center gap-3 text-xs font-bold">
                  {group.totalExpense > 0 && (
                    <span className="text-red-600">
                      -{formatCurrency(group.totalExpense, currency.symbol)}
                    </span>
                  )}
                  {group.totalIncome > 0 && (
                    <span className="text-emerald-600">
                      +{formatCurrency(group.totalIncome, currency.symbol)}
                    </span>
                  )}
                </div>
              </div>

              {/* Transactions in group */}
              <div className="divide-y divide-slate-100">
                {group.transactions.map((t) => {
                  const cat = categories.find((c) => c.name === t.category || c.id === t.categoryId);
                  const wallet = wallets.find((w) => w.id === t.walletId);
                  const targetWallet = wallets.find((w) => w.id === t.targetWalletId);
                  const isIncome = t.type === 'income';
                  const isTransfer = t.type === 'transfer';

                  return (
                    <div
                      key={t.id}
                      className="px-4 py-3 flex items-center justify-between gap-3 hover:bg-slate-50/70 transition-colors group"
                    >
                      <div className="flex items-center gap-3 min-w-0">
                        <div
                          className="w-9 h-9 rounded-xl flex items-center justify-center text-white shrink-0 shadow-xs"
                          style={{
                            backgroundColor: isTransfer ? '#3B82F6' : cat?.color || '#10B981',
                          }}
                        >
                          <DynamicIcon
                            name={isTransfer ? 'ArrowLeftRight' : cat?.icon || 'Tag'}
                            size={16}
                          />
                        </div>

                        <div className="min-w-0">
                          <div className="flex items-center gap-2 flex-wrap">
                            <span className="text-xs sm:text-sm font-bold text-slate-900 truncate">
                              {t.category}
                            </span>
                            {t.subcategory && (
                              <span className="text-[10px] font-medium text-slate-500 bg-slate-100 px-1.5 py-0.5 rounded-md">
                                {t.subcategory}
                              </span>
                            )}
                            {t.isForeignCurrency && (
                              <span className="text-[10px] font-bold text-blue-700 bg-blue-50 border border-blue-200/80 px-1.5 py-0.5 rounded-md flex items-center gap-1">
                                <Globe size={11} className="text-blue-500" />
                                <span>{t.originalAmount} {t.originalCurrency}</span>
                                {t.country && <span className="opacity-80">({t.country})</span>}
                              </span>
                            )}
                            {t.isRecurring && (
                              <span
                                className="text-[10px] font-bold text-teal-800 bg-teal-50 border border-teal-200/90 px-1.5 py-0.5 rounded-md flex items-center gap-1"
                                title={t.recurrenceNextDate ? `Следующее списание: ${t.recurrenceNextDate}` : 'Регулярная операция'}
                              >
                                <Repeat size={10} className="text-teal-600" />
                                <span>{t.recurrenceInterval ? getIntervalLabel(t.recurrenceInterval) : 'Регулярный'}</span>
                              </span>
                            )}
                            {t.parentRecurringId && (
                              <span
                                className="text-[10px] font-semibold text-slate-600 bg-slate-100 border border-slate-200 px-1.5 py-0.5 rounded-md flex items-center gap-1"
                                title="Создано автоплатежом по расписанию"
                              >
                                <Repeat size={9} className="text-slate-400" />
                                <span>Автоплатеж</span>
                              </span>
                            )}
                          </div>
                          <div className="flex items-center gap-2 text-[11px] text-slate-400 mt-0.5 truncate">
                            <span>{t.date}</span>
                            <span>•</span>
                            <span>{wallet?.name || 'Счет'}</span>
                            {isTransfer && targetWallet && (
                              <span>➔ {targetWallet.name}</span>
                            )}
                            {t.city && <span>• 📍 {t.city}</span>}
                            {t.isForeignCurrency && t.exchangeRate && (
                              <span className="text-slate-400">
                                • курс {t.exchangeRate.toFixed(2)}
                              </span>
                            )}
                            {t.note && <span className="italic">({t.note})</span>}
                          </div>

                          {/* Tags badges */}
                          {t.tags && t.tags.length > 0 && (
                            <div className="flex flex-wrap gap-1 mt-1">
                              {t.tags.map((tag) => {
                                const isTagActive = selectedTag === tag;
                                return (
                                  <button
                                    key={tag}
                                    type="button"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      setSelectedTag(isTagActive ? 'all' : tag);
                                    }}
                                    className={`text-[10px] font-bold px-1.5 py-0.5 rounded-md transition-colors cursor-pointer flex items-center gap-0.5 ${
                                      isTagActive
                                        ? 'bg-emerald-600 text-white shadow-2xs'
                                        : 'bg-emerald-50 hover:bg-emerald-100 text-emerald-800 border border-emerald-200/70'
                                    }`}
                                    title={`Фильтровать по тегу #${tag}`}
                                  >
                                    <span>#{tag}</span>
                                  </button>
                                );
                              })}
                            </div>
                          )}
                        </div>
                      </div>

                      {/* Amount & Quick Actions */}
                      <div className="flex items-center gap-2 shrink-0">
                        <span
                          className={`text-xs sm:text-sm font-extrabold ${
                            isIncome
                              ? 'text-emerald-600'
                              : isTransfer
                              ? 'text-blue-600'
                              : 'text-slate-900'
                          }`}
                        >
                          {isIncome ? '+' : isTransfer ? '' : '-'}
                          {formatCurrency(t.amount, currency.symbol)}
                        </span>

                        {/* Action buttons (Edit / Delete) */}
                        <div className="flex items-center gap-1 opacity-80 sm:opacity-0 group-hover:opacity-100 transition-opacity">
                          <button
                            onClick={() => onEditTransaction(t)}
                            className="p-1.5 text-slate-400 hover:text-emerald-600 hover:bg-emerald-50 rounded-lg transition-colors"
                            title="Редактировать"
                          >
                            <Edit2 size={14} />
                          </button>
                          <button
                            onClick={() => setDeleteConfirmId(t.id)}
                            className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                            title="Удалить"
                          >
                            <Trash2 size={14} />
                          </button>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {deleteConfirmId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4">
          <div className="bg-white rounded-2xl p-6 max-w-sm w-full shadow-2xl space-y-4">
            <h3 className="text-base font-bold text-slate-900">Удалить транзакцию?</h3>
            <p className="text-xs text-slate-500">
              Это действие отменит операцию и вернет баланс кошелька в исходное состояние.
            </p>
            <div className="flex justify-end gap-2 pt-2">
              <button
                onClick={() => setDeleteConfirmId(null)}
                className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl"
              >
                Отмена
              </button>
              <button
                onClick={() => handleDelete(deleteConfirmId)}
                className="px-4 py-2 text-xs font-bold text-white bg-red-600 hover:bg-red-700 rounded-xl shadow-sm"
              >
                Удалить
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
