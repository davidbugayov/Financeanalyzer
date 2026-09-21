import React, { useState } from 'react';
import {
  Wallet as WalletIcon,
  Plus,
  ArrowLeftRight,
  TrendingUp,
  Target,
  PiggyBank,
  Edit2,
  Trash2,
  X,
  Check,
  ShieldCheck,
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { Wallet, WalletType } from '../types';
import { formatCurrency } from '../utils/financeCalculations';
import { DynamicIcon } from '../utils/iconHelper';

const WALLET_TYPES: { type: WalletType; label: string; icon: string }[] = [
  { type: 'card', label: 'Банковская карта', icon: 'CreditCard' },
  { type: 'cash', label: 'Наличные', icon: 'Banknote' },
  { type: 'savings', label: 'Сберегательный / Вклад', icon: 'PiggyBank' },
  { type: 'investment', label: 'Инвестиции / Брокер', icon: 'TrendingUp' },
  { type: 'goal', label: 'Цель накопления', icon: 'Target' },
  { type: 'other', label: 'Прочее', icon: 'Wallet' },
];

const PRESET_COLORS = ['#3B82F6', '#10B981', '#8B5CF6', '#F59E0B', '#EF4444', '#06B6D4', '#EC4899'];

export const WalletsView: React.FC = () => {
  const {
    wallets,
    currency,
    addWallet,
    updateWallet,
    deleteWallet,
    transferBetweenWallets,
  } = useFinance();

  const [isWalletModalOpen, setIsWalletModalOpen] = useState(false);
  const [isTransferModalOpen, setIsTransferModalOpen] = useState(false);
  const [editingWallet, setEditingWallet] = useState<Wallet | null>(null);

  // Form states
  const [name, setName] = useState('');
  const [type, setType] = useState<WalletType>('card');
  const [balance, setBalance] = useState('');
  const [limit, setLimit] = useState('');
  const [goalAmount, setGoalAmount] = useState('');
  const [goalDate, setGoalDate] = useState('');
  const [autoSavingsPercent, setAutoSavingsPercent] = useState('');
  const [color, setColor] = useState(PRESET_COLORS[0]);

  // Transfer states
  const [fromWalletId, setFromWalletId] = useState('');
  const [toWalletId, setToWalletId] = useState('');
  const [transferAmount, setTransferAmount] = useState('');
  const [transferNote, setTransferNote] = useState('');

  const openAddModal = () => {
    setEditingWallet(null);
    setName('');
    setType('card');
    setBalance('0');
    setLimit('0');
    setGoalAmount('');
    setGoalDate('');
    setAutoSavingsPercent('');
    setColor(PRESET_COLORS[0]);
    setIsWalletModalOpen(true);
  };

  const openEditModal = (w: Wallet) => {
    setEditingWallet(w);
    setName(w.name);
    setType(w.type);
    setBalance(w.balance.toString());
    setLimit(w.limit ? w.limit.toString() : '');
    setGoalAmount(w.goalAmount ? w.goalAmount.toString() : '');
    setGoalDate(w.goalDate || '');
    setAutoSavingsPercent(w.autoSavingsPercent ? w.autoSavingsPercent.toString() : '');
    setColor(w.color || PRESET_COLORS[0]);
    setIsWalletModalOpen(true);
  };

  const handleSaveWallet = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;

    const icon = WALLET_TYPES.find((wt) => wt.type === type)?.icon || 'Wallet';
    const parsedBalance = parseFloat(balance) || 0;
    const parsedLimit = parseFloat(limit) || 0;
    const parsedGoal = goalAmount ? parseFloat(goalAmount) : undefined;
    const parsedPercent = autoSavingsPercent ? parseInt(autoSavingsPercent) : undefined;

    if (editingWallet) {
      updateWallet({
        ...editingWallet,
        name: name.trim(),
        type,
        balance: parsedBalance,
        limit: parsedLimit,
        goalAmount: parsedGoal,
        goalDate: goalDate || undefined,
        autoSavingsPercent: parsedPercent,
        color,
        icon,
      });
    } else {
      addWallet({
        name: name.trim(),
        type,
        balance: parsedBalance,
        limit: parsedLimit,
        goalAmount: parsedGoal,
        goalDate: goalDate || undefined,
        autoSavingsPercent: parsedPercent,
        color,
        icon,
      });
    }

    setIsWalletModalOpen(false);
  };

  const openTransferModal = (defaultFromId?: string) => {
    setFromWalletId(defaultFromId || wallets[0]?.id || '');
    setToWalletId(wallets.find((w) => w.id !== (defaultFromId || wallets[0]?.id))?.id || '');
    setTransferAmount('');
    setTransferNote('');
    setIsTransferModalOpen(true);
  };

  const handleExecuteTransfer = (e: React.FormEvent) => {
    e.preventDefault();
    const amount = parseFloat(transferAmount);
    if (isNaN(amount) || amount <= 0 || !fromWalletId || !toWalletId || fromWalletId === toWalletId) {
      return;
    }
    transferBetweenWallets(fromWalletId, toWalletId, amount, transferNote);
    setIsTransferModalOpen(false);
  };

  const totalBalance = wallets.reduce((sum, w) => sum + w.balance, 0);

  return (
    <div className="space-y-6 pb-24">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h2 className="text-xl sm:text-2xl font-black text-slate-900">Кошельки и счета</h2>
          <p className="text-xs text-slate-500">
            Управление счетами, целевыми копилками и правилами автосбережения
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            id="wallet_quick_transfer_btn"
            onClick={() => openTransferModal()}
            className="flex items-center gap-1.5 px-3 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold rounded-xl transition-colors"
          >
            <ArrowLeftRight size={15} />
            <span>Перевод</span>
          </button>
          <button
            id="add_new_wallet_btn"
            onClick={openAddModal}
            className="flex items-center gap-1.5 px-3.5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold rounded-xl shadow-xs transition-colors"
          >
            <Plus size={16} />
            <span>Новый счет</span>
          </button>
        </div>
      </div>

      {/* Total Assets Card */}
      <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs flex items-center justify-between">
        <div>
          <span className="text-[11px] font-bold uppercase tracking-wider text-slate-400 block">
            Суммарно на {wallets.length} счетах
          </span>
          <span className="text-2xl font-black text-slate-900">
            {formatCurrency(totalBalance, currency.symbol)}
          </span>
        </div>
        <div className="w-12 h-12 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
          <WalletIcon size={24} />
        </div>
      </div>

      {/* Wallets Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {wallets.map((wallet) => {
          const isGoal = wallet.type === 'goal' && wallet.goalAmount;
          const goalProgress = isGoal ? Math.min(100, Math.round((wallet.balance / (wallet.goalAmount || 1)) * 100)) : 0;
          const hasLimit = wallet.limit > 0;

          return (
            <div
              key={wallet.id}
              className="bg-white rounded-2xl border border-slate-200/90 p-5 shadow-xs hover:border-slate-300 transition-all relative overflow-hidden"
            >
              {/* Top Accent line */}
              <div
                className="absolute top-0 left-0 right-0 h-1.5"
                style={{ backgroundColor: wallet.color }}
              />

              <div className="flex items-start justify-between">
                <div className="flex items-center gap-3">
                  <div
                    className="w-11 h-11 rounded-xl flex items-center justify-center text-white shadow-xs"
                    style={{ backgroundColor: wallet.color }}
                  >
                    <DynamicIcon name={wallet.icon || 'Wallet'} size={20} />
                  </div>
                  <div>
                    <h4 className="text-sm font-bold text-slate-900 line-clamp-1">{wallet.name}</h4>
                    <span className="text-[10px] font-semibold uppercase tracking-wider text-slate-400">
                      {WALLET_TYPES.find((wt) => wt.type === wallet.type)?.label || 'Счет'}
                    </span>
                  </div>
                </div>

                {/* Edit / Delete / Transfer actions */}
                <div className="flex items-center gap-1">
                  <button
                    onClick={() => openTransferModal(wallet.id)}
                    className="p-1.5 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                    title="Перевести с этого счета"
                  >
                    <ArrowLeftRight size={14} />
                  </button>
                  <button
                    onClick={() => openEditModal(wallet)}
                    className="p-1.5 text-slate-400 hover:text-emerald-600 hover:bg-emerald-50 rounded-lg transition-colors"
                    title="Редактировать"
                  >
                    <Edit2 size={14} />
                  </button>
                  {wallets.length > 1 && (
                    <button
                      onClick={() => deleteWallet(wallet.id)}
                      className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                      title="Удалить"
                    >
                      <Trash2 size={14} />
                    </button>
                  )}
                </div>
              </div>

              {/* Balance */}
              <div className="mt-4 mb-2">
                <span className="text-xs text-slate-400 block font-medium">Баланс</span>
                <div className="text-xl font-extrabold text-slate-900">
                  {formatCurrency(wallet.balance, currency.symbol)}
                </div>
              </div>

              {/* Auto savings rule badge */}
              {wallet.autoSavingsPercent && wallet.autoSavingsPercent > 0 && (
                <div className="mt-2 inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-purple-50 text-purple-700 text-[11px] font-bold">
                  <PiggyBank size={13} />
                  <span>Автосбережение: {wallet.autoSavingsPercent}% от доходов</span>
                </div>
              )}

              {/* Goal Progress bar if applicable */}
              {isGoal && (
                <div className="mt-3 pt-3 border-t border-slate-100 space-y-1.5">
                  <div className="flex justify-between text-xs font-bold">
                    <span className="text-slate-500">
                      Цель: {formatCurrency(wallet.goalAmount || 0, currency.symbol)}
                    </span>
                    <span className="text-emerald-600">{goalProgress}%</span>
                  </div>
                  <div className="h-2 w-full bg-slate-100 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-emerald-500 rounded-full transition-all duration-500"
                      style={{ width: `${goalProgress}%` }}
                    />
                  </div>
                  {wallet.goalDate && (
                    <span className="text-[10px] text-slate-400 font-medium block">
                      Срок цели: до {wallet.goalDate}
                    </span>
                  )}
                </div>
              )}

              {/* Budget Limit info if configured */}
              {hasLimit && !isGoal && (
                <div className="mt-3 pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
                  <span className="text-slate-400 font-medium">Лимит трат:</span>
                  <span className="font-bold text-slate-700">
                    {formatCurrency(wallet.limit, currency.symbol)}
                  </span>
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* Add / Edit Wallet Modal */}
      {isWalletModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4 overflow-y-auto">
          <div className="bg-white rounded-2xl p-6 w-full max-w-md shadow-2xl space-y-4 my-6">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-base font-extrabold text-slate-900">
                {editingWallet ? 'Редактировать счет' : 'Новый счет / кошелек'}
              </h3>
              <button
                onClick={() => setIsWalletModalOpen(false)}
                className="text-slate-400 hover:text-slate-600 p-1 rounded-lg"
              >
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleSaveWallet} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-500 mb-1">Название счета</label>
                <input
                  type="text"
                  required
                  placeholder="Например: ВТБ Зарплатная или Копилка на отпуск"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="w-full text-xs font-semibold px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-1 focus:ring-emerald-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-500 mb-1">Тип счета</label>
                  <select
                    value={type}
                    onChange={(e) => setType(e.target.value as WalletType)}
                    className="w-full text-xs font-semibold px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
                  >
                    {WALLET_TYPES.map((wt) => (
                      <option key={wt.type} value={wt.type}>
                        {wt.label}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-500 mb-1">
                    Баланс ({currency.symbol})
                  </label>
                  <input
                    type="number"
                    step="any"
                    value={balance}
                    onChange={(e) => setBalance(e.target.value)}
                    className="w-full text-xs font-semibold px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
                  />
                </div>
              </div>

              {/* Goal fields if type is goal */}
              {type === 'goal' && (
                <div className="p-3 bg-amber-50/60 border border-amber-200 rounded-xl space-y-3">
                  <div className="flex items-center gap-1.5 text-xs font-bold text-amber-800">
                    <Target size={15} /> Настройки финансовой цели
                  </div>
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="block text-[11px] font-medium text-slate-600 mb-1">Целевая сумма</label>
                      <input
                        type="number"
                        placeholder="100000"
                        value={goalAmount}
                        onChange={(e) => setGoalAmount(e.target.value)}
                        className="w-full text-xs px-2.5 py-1.5 bg-white border border-slate-200 rounded-lg"
                      />
                    </div>
                    <div>
                      <label className="block text-[11px] font-medium text-slate-600 mb-1">Дата цели</label>
                      <input
                        type="date"
                        value={goalDate}
                        onChange={(e) => setGoalDate(e.target.value)}
                        className="w-full text-xs px-2.5 py-1.5 bg-white border border-slate-200 rounded-lg"
                      />
                    </div>
                  </div>
                </div>
              )}

              {/* Auto savings option if savings or goal */}
              {(type === 'savings' || type === 'goal') && (
                <div className="p-3 bg-purple-50/60 border border-purple-200 rounded-xl">
                  <div className="flex items-center gap-1.5 text-xs font-bold text-purple-800 mb-2">
                    <PiggyBank size={15} /> Автоматическое сбережение дохода
                  </div>
                  <label className="block text-[11px] text-slate-600 mb-1">
                    Процент от любого дохода, переводящийся в эту копилку:
                  </label>
                  <div className="flex items-center gap-2">
                    <input
                      type="number"
                      min="0"
                      max="100"
                      placeholder="10"
                      value={autoSavingsPercent}
                      onChange={(e) => setAutoSavingsPercent(e.target.value)}
                      className="w-24 text-xs font-bold px-3 py-1.5 bg-white border border-slate-200 rounded-lg"
                    />
                    <span className="text-xs font-bold text-slate-600">% от поступлений</span>
                  </div>
                </div>
              )}

              {/* Budget Limit */}
              <div>
                <label className="block text-xs font-semibold text-slate-500 mb-1">
                  Лимит бюджета трат (0 - без лимита)
                </label>
                <input
                  type="number"
                  placeholder="0"
                  value={limit}
                  onChange={(e) => setLimit(e.target.value)}
                  className="w-full text-xs font-semibold px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
                />
              </div>

              {/* Color picker */}
              <div>
                <label className="block text-xs font-semibold text-slate-500 mb-2">Цвет счета</label>
                <div className="flex gap-2">
                  {PRESET_COLORS.map((c) => (
                    <button
                      key={c}
                      type="button"
                      onClick={() => setColor(c)}
                      className={`w-7 h-7 rounded-full transition-transform ${
                        color === c ? 'scale-125 ring-2 ring-slate-900 ring-offset-2' : ''
                      }`}
                      style={{ backgroundColor: c }}
                    />
                  ))}
                </div>
              </div>

              {/* Modal Buttons */}
              <div className="flex justify-end gap-2 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsWalletModalOpen(false)}
                  className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl"
                >
                  Отмена
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl shadow-sm"
                >
                  Сохранить
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Transfer Modal */}
      {isTransferModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4">
          <div className="bg-white rounded-2xl p-6 w-full max-w-sm shadow-2xl space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-base font-extrabold text-slate-900">Перевод между счетами</h3>
              <button
                onClick={() => setIsTransferModalOpen(false)}
                className="text-slate-400 hover:text-slate-600 p-1 rounded-lg"
              >
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleExecuteTransfer} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-500 mb-1">Счет списания</label>
                <select
                  value={fromWalletId}
                  onChange={(e) => setFromWalletId(e.target.value)}
                  className="w-full text-xs font-semibold px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
                >
                  {wallets.map((w) => (
                    <option key={w.id} value={w.id}>
                      {w.name} ({formatCurrency(w.balance, currency.symbol)})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-500 mb-1">Счет зачисления</label>
                <select
                  value={toWalletId}
                  onChange={(e) => setToWalletId(e.target.value)}
                  className="w-full text-xs font-semibold px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
                >
                  {wallets
                    .filter((w) => w.id !== fromWalletId)
                    .map((w) => (
                      <option key={w.id} value={w.id}>
                        {w.name} ({formatCurrency(w.balance, currency.symbol)})
                      </option>
                    ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-500 mb-1">
                  Сумма ({currency.symbol})
                </label>
                <input
                  type="number"
                  required
                  placeholder="0"
                  value={transferAmount}
                  onChange={(e) => setTransferAmount(e.target.value)}
                  className="w-full text-base font-bold px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-500 mb-1">Комментарий</label>
                <input
                  type="text"
                  placeholder="Например, пополнение копилки"
                  value={transferNote}
                  onChange={(e) => setTransferNote(e.target.value)}
                  className="w-full text-xs px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
                />
              </div>

              <div className="flex justify-end gap-2 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsTransferModalOpen(false)}
                  className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl"
                >
                  Отмена
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 text-xs font-bold text-white bg-blue-600 hover:bg-blue-700 rounded-xl shadow-sm"
                >
                  Перевести
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
