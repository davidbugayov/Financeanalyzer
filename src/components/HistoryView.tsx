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
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { Transaction, GroupingType, TransactionType } from '../types';
import { formatCurrency } from '../utils/financeCalculations';
import { DynamicIcon } from '../utils/iconHelper';

interface HistoryViewProps {
  onEditTransaction: (tx: Transaction) => void;
  onOpenAddModal: () => void;
}

export const HistoryView: React.FC<HistoryViewProps> = ({
  onEditTransaction,
  onOpenAddModal,
}) => {
  const { transactions, categories, wallets, currency, deleteTransaction } = useFinance();

  const [searchQuery, setSearchQuery] = useState('');
  const [selectedType, setSelectedType] = useState<TransactionType | 'all'>('all');
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [selectedWallet, setSelectedWallet] = useState<string>('all');
  const [grouping, setGrouping] = useState<GroupingType>('date');
  const [deleteConfirmId, setDeleteConfirmId] = useState<string | null>(null);

  // Filtered transactions
  const filtered = useMemo(() => {
    return transactions.filter((t) => {
      // Search
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase();
        const matchCat = t.category.toLowerCase().includes(q);
        const matchSub = (t.subcategory || '').toLowerCase().includes(q);
        const matchNote = (t.note || '').toLowerCase().includes(q);
        if (!matchCat && !matchSub && !matchNote) return false;
      }

      // Type
      if (selectedType !== 'all' && t.type !== selectedType) return false;

      // Category
      if (selectedCategory !== 'all' && t.category !== selectedCategory) return false;

      // Wallet
      if (selectedWallet !== 'all' && t.walletId !== selectedWallet && t.targetWalletId !== selectedWallet) {
        return false;
      }

      return true;
    });
  }, [transactions, searchQuery, selectedType, selectedCategory, selectedWallet]);

  // Grouped transactions
  const groupedData = useMemo(() => {
    const groups: { [key: string]: Transaction[] } = {};

    filtered.forEach((t) => {
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

        {/* Grouping switcher */}
        <div className="flex items-center gap-1 bg-slate-200/70 p-1 rounded-xl text-xs font-bold text-slate-600 self-start sm:self-auto">
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
        </div>
      </div>

      {/* Search & Filter Bar */}
      <div className="bg-white rounded-2xl border border-slate-200/90 p-4 shadow-xs space-y-3">
        <div className="relative">
          <Search size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            id="history_search_input"
            type="text"
            placeholder="Поиск по категории, подкатегории, заметке..."
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

        {/* Filters Grid */}
        <div className="grid grid-cols-3 gap-2 text-xs font-semibold">
          {/* Type Filter */}
          <select
            id="history_filter_type"
            value={selectedType}
            onChange={(e) => setSelectedType(e.target.value as any)}
            className="px-2.5 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-1 focus:ring-emerald-500"
          >
            <option value="all">Все типы</option>
            <option value="expense">Расходы</option>
            <option value="income">Доходы</option>
            <option value="transfer">Переводы</option>
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
        </div>
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
                  <Calendar size={14} className="text-slate-400" />
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
                          </div>
                          <div className="flex items-center gap-2 text-[11px] text-slate-400 mt-0.5 truncate">
                            <span>{t.date}</span>
                            <span>•</span>
                            <span>{wallet?.name || 'Счет'}</span>
                            {isTransfer && targetWallet && (
                              <span>➔ {targetWallet.name}</span>
                            )}
                            {t.note && <span className="italic">({t.note})</span>}
                          </div>
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
