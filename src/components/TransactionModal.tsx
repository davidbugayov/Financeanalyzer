import React, { useState, useEffect, useMemo } from 'react';
import {
  X,
  Check,
  Plus,
  ArrowRightLeft,
  Globe,
  MapPin,
  SlidersHorizontal,
  RotateCcw,
  Tag,
  Repeat,
  Calendar,
  Clock,
  Sparkles,
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { Transaction, TransactionType, RecurrenceInterval } from '../types';
import { DynamicIcon } from '../utils/iconHelper';
import {
  suggestCategoryFromDescription,
  CategorySuggestion,
} from '../utils/categorySuggester';
import {
  EXTENDED_CURRENCIES,
  POPULAR_TRAVEL_DESTINATIONS,
  getCrossExchangeRate,
  convertCurrency,
  getCurrencyInfo,
} from '../utils/currencyRates';
import {
  calculateNextRecurrenceDate,
  getIntervalLabel,
} from '../utils/recurringProcessor';

export interface InitialForeignData {
  baseAmount: number;
  originalAmount: number;
  originalCurrency: string;
  exchangeRate: number;
  country?: string;
}

const PRESET_TAG_SUGGESTIONS = [
  'отпуск',
  'поездка',
  'ремонт',
  'работа',
  'проект',
  'подарок',
  'здоровье',
  'хобби',
  'авто',
  'дом',
];

interface TransactionModalProps {
  isOpen: boolean;
  onClose: () => void;
  initialTransaction?: Transaction | null;
  initialForeignData?: InitialForeignData | null;
}

export const TransactionModal: React.FC<TransactionModalProps> = ({
  isOpen,
  onClose,
  initialTransaction,
  initialForeignData,
}) => {
  const { categories, wallets, transactions, addTransaction, updateTransaction, currency, addSubcategory } = useFinance();

  const [type, setType] = useState<TransactionType>('expense');
  const [amount, setAmount] = useState<string>('');
  const [categoryId, setCategoryId] = useState<string>('');
  const [subcategory, setSubcategory] = useState<string>('');
  const [newSubcategoryInput, setNewSubcategoryInput] = useState<string>('');
  const [isAddingSubcategory, setIsAddingSubcategory] = useState<boolean>(false);
  const [walletId, setWalletId] = useState<string>('');
  const [targetWalletId, setTargetWalletId] = useState<string>('');
  const [date, setDate] = useState<string>(new Date().toISOString().split('T')[0]);
  const [note, setNote] = useState<string>('');
  const [tags, setTags] = useState<string[]>([]);
  const [tagInput, setTagInput] = useState<string>('');
  const [error, setError] = useState<string>('');
  const [userManuallySelectedCategory, setUserManuallySelectedCategory] = useState<boolean>(false);

  // Automated categorization engine: keyword analysis on transaction note/description
  const categorySuggestion: CategorySuggestion | null = useMemo(() => {
    if (type === 'transfer' || !note.trim()) return null;
    return suggestCategoryFromDescription(
      note,
      categories,
      transactions,
      type === 'expense' ? 'expense' : 'income'
    );
  }, [note, categories, transactions, type]);

  // Auto-apply suggestion if user hasn't explicitly picked a category yet
  useEffect(() => {
    if (!userManuallySelectedCategory && !initialTransaction && categorySuggestion) {
      setCategoryId(categorySuggestion.category.id);
      if (categorySuggestion.subcategory) {
        setSubcategory(categorySuggestion.subcategory);
      }
    }
  }, [categorySuggestion, userManuallySelectedCategory, initialTransaction]);

  // Recurring transaction states
  const [isRecurring, setIsRecurring] = useState<boolean>(false);
  const [recurrenceInterval, setRecurrenceInterval] = useState<RecurrenceInterval>('monthly');
  const [hasEndDate, setHasEndDate] = useState<boolean>(false);
  const [recurrenceEndDate, setRecurrenceEndDate] = useState<string>('');

  // Foreign currency / Travel states
  const [isForeign, setIsForeign] = useState<boolean>(false);
  const [foreignAmount, setForeignAmount] = useState<string>('');
  const [foreignCurrency, setForeignCurrency] = useState<string>('TRY');
  const [customRate, setCustomRate] = useState<string>('');
  const [country, setCountry] = useState<string>('Турция 🇹🇷');
  const [city, setCity] = useState<string>('');

  useEffect(() => {
    if (initialTransaction) {
      setType(initialTransaction.type);
      setAmount(initialTransaction.amount.toString());
      setCategoryId(initialTransaction.categoryId || '');
      setSubcategory(initialTransaction.subcategory || '');
      setWalletId(initialTransaction.walletId);
      setTargetWalletId(initialTransaction.targetWalletId || '');
      setDate(initialTransaction.date);
      setNote(initialTransaction.note || '');
      setTags(initialTransaction.tags || []);
      setTagInput('');

      // Recurring fields
      setIsRecurring(Boolean(initialTransaction.isRecurring));
      setRecurrenceInterval(initialTransaction.recurrenceInterval || 'monthly');
      setHasEndDate(Boolean(initialTransaction.recurrenceEndDate));
      setRecurrenceEndDate(initialTransaction.recurrenceEndDate || '');

      if (initialTransaction.isForeignCurrency) {
        setIsForeign(true);
        setForeignAmount(initialTransaction.originalAmount?.toString() || '');
        setForeignCurrency(initialTransaction.originalCurrency || 'TRY');
        setCustomRate(initialTransaction.exchangeRate?.toString() || '');
        setCountry(initialTransaction.country || '');
        setCity(initialTransaction.city || '');
      } else {
        setIsForeign(false);
        setForeignAmount('');
        setForeignCurrency('TRY');
        setCustomRate('');
        setCountry('');
        setCity('');
      }
    } else if (initialForeignData) {
      setType('expense');
      setAmount(initialForeignData.baseAmount.toString());
      const defaultCat = categories.find((c) => c.isExpense);
      setCategoryId(defaultCat?.id || '');
      setSubcategory(defaultCat?.subcategories[0] || '');
      setWalletId(wallets[0]?.id || '');
      setTargetWalletId(wallets[1]?.id || wallets[0]?.id || '');
      setDate(new Date().toISOString().split('T')[0]);
      setNote('');
      setTags(['отпуск']);
      setTagInput('');

      setIsRecurring(false);
      setRecurrenceInterval('monthly');
      setHasEndDate(false);
      setRecurrenceEndDate('');

      setIsForeign(true);
      setForeignAmount(initialForeignData.originalAmount.toString());
      setForeignCurrency(initialForeignData.originalCurrency);
      setCustomRate(initialForeignData.exchangeRate.toString());
      setCountry(initialForeignData.country || '');
      setCity('');
    } else {
      setType('expense');
      setAmount('');
      const defaultCat = categories.find((c) => c.isExpense);
      setCategoryId(defaultCat?.id || '');
      setSubcategory(defaultCat?.subcategories[0] || '');
      setWalletId(wallets[0]?.id || '');
      setTargetWalletId(wallets[1]?.id || wallets[0]?.id || '');
      setDate(new Date().toISOString().split('T')[0]);
      setNote('');
      setTags([]);
      setTagInput('');

      setIsRecurring(false);
      setRecurrenceInterval('monthly');
      setHasEndDate(false);
      setRecurrenceEndDate('');

      setIsForeign(false);
      setForeignAmount('');
      setForeignCurrency('TRY');
      const defaultRate = getCrossExchangeRate('TRY', currency.code);
      setCustomRate(defaultRate.toFixed(4));
      setCountry('Турция 🇹🇷');
      setCity('');
    }
    setError('');
  }, [initialTransaction, initialForeignData, isOpen, categories, wallets, currency.code]);

  const handleAddTag = (rawTag: string) => {
    const clean = rawTag.trim().replace(/^#+/, '').toLowerCase();
    if (clean && !tags.includes(clean)) {
      setTags((prev) => [...prev, clean]);
    }
    setTagInput('');
  };

  const handleRemoveTag = (tagToRemove: string) => {
    setTags((prev) => prev.filter((t) => t !== tagToRemove));
  };

  const suggestedTags = useMemo(() => {
    const set = new Set<string>();
    PRESET_TAG_SUGGESTIONS.forEach((s) => set.add(s));
    transactions.forEach((t) => {
      t.tags?.forEach((tag) => set.add(tag));
    });
    return Array.from(set).filter((s) => !tags.includes(s));
  }, [transactions, tags]);

  if (!isOpen) return null;

  const currentCategory = categories.find((c) => c.id === categoryId);
  const filteredCategories = categories.filter((c) => (type === 'expense' ? c.isExpense : !c.isExpense));

  const foreignCurrInfo = getCurrencyInfo(foreignCurrency);
  const marketExchangeRate = getCrossExchangeRate(foreignCurrency, currency.code);

  const effectiveRate = (() => {
    const parsed = parseFloat(customRate);
    if (!isNaN(parsed) && parsed > 0) return parsed;
    return marketExchangeRate;
  })();

  // Update base amount automatically when foreign amount or rate changes
  const handleForeignAmountChange = (val: string) => {
    setForeignAmount(val);
    const num = parseFloat(val);
    if (!isNaN(num) && num > 0) {
      const calculatedBase = convertCurrency(num, foreignCurrency, currency.code, effectiveRate);
      setAmount(calculatedBase.toString());
    } else if (!val) {
      setAmount('');
    }
  };

  const handleForeignCurrencyChange = (newCode: string) => {
    setForeignCurrency(newCode);
    const newRate = getCrossExchangeRate(newCode, currency.code);
    setCustomRate(newRate.toFixed(4));

    const num = parseFloat(foreignAmount);
    if (!isNaN(num) && num > 0) {
      const calculatedBase = convertCurrency(num, newCode, currency.code, newRate);
      setAmount(calculatedBase.toString());
    }

    const foundDest = POPULAR_TRAVEL_DESTINATIONS.find((d) => d.currencyCode === newCode);
    if (foundDest) {
      setCountry(`${foundDest.country} ${foundDest.flag}`);
    } else {
      const foundCurr = EXTENDED_CURRENCIES.find((c) => c.code === newCode);
      if (foundCurr) setCountry(`${foundCurr.country} ${foundCurr.flag}`);
    }
  };

  const handleCustomRateChange = (val: string) => {
    setCustomRate(val);
    const parsedRate = parseFloat(val);
    const num = parseFloat(foreignAmount);
    if (!isNaN(num) && num > 0 && !isNaN(parsedRate) && parsedRate > 0) {
      const calculatedBase = convertCurrency(num, foreignCurrency, currency.code, parsedRate);
      setAmount(calculatedBase.toString());
    }
  };

  const handleSelectQuickDestination = (dest: (typeof POPULAR_TRAVEL_DESTINATIONS)[0]) => {
    setForeignCurrency(dest.currencyCode);
    setCountry(`${dest.country} ${dest.flag}`);
    const newRate = getCrossExchangeRate(dest.currencyCode, currency.code);
    setCustomRate(newRate.toFixed(4));

    const num = parseFloat(foreignAmount);
    if (!isNaN(num) && num > 0) {
      const calculatedBase = convertCurrency(num, dest.currencyCode, currency.code, newRate);
      setAmount(calculatedBase.toString());
    }
  };

  const handleQuickAddAmount = (addValue: number) => {
    if (isForeign) {
      const current = parseFloat(foreignAmount) || 0;
      handleForeignAmountChange((current + addValue).toString());
    } else {
      const current = parseFloat(amount) || 0;
      setAmount((current + addValue).toString());
    }
  };

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    const parsedAmount = parseFloat(amount);

    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      setError('Пожалуйста, введите корректную сумму');
      return;
    }

    if (!walletId) {
      setError('Выберите кошелек');
      return;
    }

    const recurringData = isRecurring
      ? {
          isRecurring: true,
          recurrenceInterval,
          recurrenceNextDate: calculateNextRecurrenceDate(date, recurrenceInterval),
          recurrenceEndDate: hasEndDate && recurrenceEndDate ? recurrenceEndDate : undefined,
          recurrenceLastProcessed: date,
        }
      : {
          isRecurring: false,
          recurrenceInterval: undefined,
          recurrenceNextDate: undefined,
          recurrenceEndDate: undefined,
          recurrenceLastProcessed: undefined,
        };

    if (type === 'transfer') {
      if (!targetWalletId || targetWalletId === walletId) {
        setError('Выберите другой кошелек для перевода');
        return;
      }

      if (initialTransaction) {
        updateTransaction({
          ...initialTransaction,
          amount: parsedAmount,
          type: 'transfer',
          category: 'Перевод',
          walletId,
          targetWalletId,
          date,
          note,
          tags: tags.length > 0 ? tags : undefined,
          ...recurringData,
        });
      } else {
        addTransaction({
          amount: parsedAmount,
          type: 'transfer',
          category: 'Перевод',
          walletId,
          targetWalletId,
          date,
          note,
          tags: tags.length > 0 ? tags : undefined,
          ...recurringData,
        });
      }
      onClose();
      return;
    }

    const cat = categories.find((c) => c.id === categoryId);
    const categoryName = cat ? cat.name : type === 'expense' ? 'Расход' : 'Доход';

    const foreignData = isForeign
      ? {
          isForeignCurrency: true,
          originalAmount: parseFloat(foreignAmount) || parsedAmount,
          originalCurrency: foreignCurrency,
          exchangeRate: effectiveRate,
          country: country.trim() || undefined,
          city: city.trim() || undefined,
        }
      : {
          isForeignCurrency: false,
          originalAmount: undefined,
          originalCurrency: undefined,
          exchangeRate: undefined,
          country: undefined,
          city: undefined,
        };

    if (initialTransaction) {
      updateTransaction({
        ...initialTransaction,
        amount: parsedAmount,
        type,
        category: categoryName,
        categoryId,
        subcategory,
        walletId,
        date,
        note,
        tags: tags.length > 0 ? tags : undefined,
        ...foreignData,
        ...recurringData,
      });
    } else {
      addTransaction({
        amount: parsedAmount,
        type,
        category: categoryName,
        categoryId,
        subcategory,
        walletId,
        date,
        note,
        tags: tags.length > 0 ? tags : undefined,
        ...foreignData,
        ...recurringData,
      });
    }

    onClose();
  };

  const handleAddNewSubcategory = () => {
    if (newSubcategoryInput.trim() && categoryId) {
      addSubcategory(categoryId, newSubcategoryInput.trim());
      setSubcategory(newSubcategoryInput.trim());
      setNewSubcategoryInput('');
      setIsAddingSubcategory(false);
    }
  };

  return (
    <div
      id="transaction_modal_overlay"
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4 overflow-y-auto"
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div
        id="transaction_modal_content"
        className="bg-white rounded-2xl shadow-2xl w-full max-w-lg overflow-hidden my-6 animate-in fade-in zoom-in-95 duration-200"
      >
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 bg-slate-50/70">
          <h2 className="text-lg font-bold text-slate-800">
            {initialTransaction ? 'Редактировать транзакцию' : 'Новая транзакция'}
          </h2>
          <button
            id="close_transaction_modal_btn"
            onClick={onClose}
            className="p-2 text-slate-400 hover:text-slate-600 rounded-lg hover:bg-slate-200/60 transition-colors"
          >
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSave} className="p-6 space-y-5">
          {error && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-xl text-xs font-semibold text-red-700">
              {error}
            </div>
          )}

          {/* Type Selector */}
          <div className="grid grid-cols-3 gap-2 bg-slate-100 p-1.5 rounded-xl text-sm font-semibold">
            <button
              id="type_expense_btn"
              type="button"
              onClick={() => {
                setType('expense');
                const cat = categories.find((c) => c.isExpense);
                if (cat) {
                  setCategoryId(cat.id);
                  setSubcategory(cat.subcategories[0] || '');
                }
              }}
              className={`py-2 px-3 rounded-lg transition-all text-center ${
                type === 'expense'
                  ? 'bg-red-500 text-white shadow-sm'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-white/50'
              }`}
            >
              Расход
            </button>
            <button
              id="type_income_btn"
              type="button"
              onClick={() => {
                setType('income');
                const cat = categories.find((c) => !c.isExpense);
                if (cat) {
                  setCategoryId(cat.id);
                  setSubcategory(cat.subcategories[0] || '');
                }
              }}
              className={`py-2 px-3 rounded-lg transition-all text-center ${
                type === 'income'
                  ? 'bg-emerald-600 text-white shadow-sm'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-white/50'
              }`}
            >
              Доход
            </button>
            <button
              id="type_transfer_btn"
              type="button"
              onClick={() => setType('transfer')}
              className={`py-2 px-3 rounded-lg transition-all text-center ${
                type === 'transfer'
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-white/50'
              }`}
            >
              Перевод
            </button>
          </div>

          {/* Foreign Currency / Travel Expense Toggle (for expense & income) */}
          {type !== 'transfer' && (
            <div className="bg-slate-50 border border-slate-200/90 rounded-2xl p-3.5 space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2.5">
                  <div
                    className={`w-8 h-8 rounded-xl flex items-center justify-center transition-colors ${
                      isForeign ? 'bg-blue-600 text-white shadow-xs' : 'bg-slate-200 text-slate-600'
                    }`}
                  >
                    <Globe size={16} />
                  </div>
                  <div>
                    <span className="text-xs font-bold text-slate-900 block">
                      Траты за границей / в другой валюте
                    </span>
                    <span className="text-[11px] text-slate-500 block">
                      Конвертер пересчитает в {currency.symbol} ({currency.code})
                    </span>
                  </div>
                </div>

                <button
                  id="toggle_foreign_currency_btn"
                  type="button"
                  role="switch"
                  aria-checked={isForeign}
                  onClick={() => {
                    const next = !isForeign;
                    setIsForeign(next);
                    if (next && !foreignAmount && amount) {
                      const num = parseFloat(amount);
                      if (!isNaN(num) && num > 0 && effectiveRate > 0) {
                        setForeignAmount((num / effectiveRate).toFixed(2));
                      }
                    } else if (next && foreignAmount) {
                      handleForeignAmountChange(foreignAmount);
                    }
                  }}
                  className={`w-11 h-6 rounded-full transition-colors relative focus:outline-none ${
                    isForeign ? 'bg-blue-600' : 'bg-slate-300'
                  }`}
                >
                  <span
                    className={`block w-4 h-4 bg-white rounded-full transition-transform transform shadow-xs ${
                      isForeign ? 'translate-x-6' : 'translate-x-1'
                    }`}
                  />
                </button>
              </div>

              {/* Foreign Details (if toggled on) */}
              {isForeign && (
                <div className="pt-3 border-t border-slate-200 space-y-3 animate-in fade-in duration-200">
                  {/* Quick Travel Destination Chips */}
                  <div>
                    <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-1.5">
                      Популярные страны для поездок (1 клик)
                    </span>
                    <div className="flex gap-1.5 overflow-x-auto pb-1 scrollbar-thin">
                      {POPULAR_TRAVEL_DESTINATIONS.slice(0, 8).map((dest) => {
                        const isSelected = foreignCurrency === dest.currencyCode;
                        return (
                          <button
                            key={dest.id}
                            type="button"
                            onClick={() => handleSelectQuickDestination(dest)}
                            className={`shrink-0 px-2.5 py-1 rounded-lg text-xs font-semibold flex items-center gap-1 transition-all ${
                              isSelected
                                ? 'bg-blue-600 text-white shadow-xs'
                                : 'bg-white border border-slate-200 text-slate-700 hover:bg-slate-100'
                            }`}
                          >
                            <span>{dest.flag}</span>
                            <span>{dest.currencyCode}</span>
                          </button>
                        );
                      })}
                    </div>
                  </div>

                  {/* Currency and Foreign Amount Inputs */}
                  <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
                    <div className="sm:col-span-2">
                      <label className="block text-[11px] font-bold text-slate-600 mb-1">
                        Сумма в валюте страны ({foreignCurrInfo.symbol})
                      </label>
                      <div className="relative">
                        <input
                          id="foreign_amount_input"
                          type="number"
                          step="any"
                          placeholder="0"
                          value={foreignAmount}
                          onChange={(e) => handleForeignAmountChange(e.target.value)}
                          className="w-full text-xl font-black text-slate-900 px-3.5 py-2.5 bg-white border border-blue-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                        <span className="absolute right-3.5 top-1/2 -translate-y-1/2 text-base font-bold text-slate-400">
                          {foreignCurrInfo.symbol}
                        </span>
                      </div>
                    </div>

                    <div>
                      <label className="block text-[11px] font-bold text-slate-600 mb-1">Валюта</label>
                      <select
                        id="foreign_currency_select"
                        value={foreignCurrency}
                        onChange={(e) => handleForeignCurrencyChange(e.target.value)}
                        className="w-full text-xs font-bold px-2 py-3 bg-white border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
                      >
                        {EXTENDED_CURRENCIES.map((c) => (
                          <option key={c.code} value={c.code}>
                            {c.flag} {c.code} ({c.name})
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>

                  {/* Exchange rate info & adjustment */}
                  <div className="flex items-center justify-between text-xs bg-white p-2.5 rounded-xl border border-slate-200 gap-2 flex-wrap">
                    <div className="flex items-center gap-1.5 text-slate-700">
                      <span className="font-semibold">Курс:</span>
                      <span>1 {foreignCurrency} =</span>
                      <input
                        type="number"
                        step="any"
                        value={customRate}
                        onChange={(e) => handleCustomRateChange(e.target.value)}
                        className="w-20 px-2 py-0.5 font-bold text-blue-700 border border-slate-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500 text-xs"
                      />
                      <span className="font-semibold">{currency.symbol}</span>
                    </div>

                    <button
                      type="button"
                      onClick={() => {
                        const freshRate = getCrossExchangeRate(foreignCurrency, currency.code);
                        handleCustomRateChange(freshRate.toFixed(4));
                      }}
                      className="text-[10px] font-bold text-blue-600 hover:underline flex items-center gap-1"
                    >
                      <RotateCcw size={11} /> Сбросить к рыночному
                    </button>
                  </div>

                  {/* Location & Country tags */}
                  <div className="grid grid-cols-2 gap-2">
                    <div>
                      <label className="block text-[10px] font-bold uppercase tracking-wider text-slate-400 mb-1">
                        Страна
                      </label>
                      <input
                        id="foreign_country_input"
                        type="text"
                        placeholder="например, Турция 🇹🇷"
                        value={country}
                        onChange={(e) => setCountry(e.target.value)}
                        className="w-full text-xs px-2.5 py-1.5 bg-white border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-blue-500"
                      />
                    </div>
                    <div>
                      <label className="block text-[10px] font-bold uppercase tracking-wider text-slate-400 mb-1">
                        Город / Место (опц.)
                      </label>
                      <input
                        id="foreign_city_input"
                        type="text"
                        placeholder="например, Стамбул"
                        value={city}
                        onChange={(e) => setCity(e.target.value)}
                        className="w-full text-xs px-2.5 py-1.5 bg-white border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-blue-500"
                      />
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Amount Input */}
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider">
                {isForeign ? `Сумма к списанию со счёта (${currency.symbol})` : `Сумма (${currency.symbol})`}
              </label>
              {isForeign && foreignAmount && (
                <span className="text-xs font-bold text-blue-600">
                  ≈ {foreignAmount} {foreignCurrInfo.symbol} по курсу {effectiveRate.toFixed(2)}
                </span>
              )}
            </div>
            <div className="relative">
              <input
                id="tx_amount_input"
                type="number"
                step="any"
                required
                placeholder="0"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                autoFocus={!isForeign}
                className={`w-full text-3xl font-extrabold text-slate-900 px-4 py-3 bg-slate-50 border rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:border-transparent transition-all ${
                  isForeign
                    ? 'border-blue-300 focus:ring-blue-500 bg-blue-50/20'
                    : 'border-slate-200 focus:ring-emerald-500'
                }`}
              />
              <span className="absolute right-4 top-1/2 -translate-y-1/2 text-2xl font-bold text-slate-400">
                {currency.symbol}
              </span>
            </div>

            {/* Quick amount add buttons */}
            <div className="flex gap-2 mt-2">
              {[500, 1000, 2000, 5000].map((val) => (
                <button
                  key={val}
                  type="button"
                  onClick={() => handleQuickAddAmount(val)}
                  className="px-2.5 py-1 text-xs font-medium bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg transition-colors"
                >
                  +{val}
                </button>
              ))}
            </div>
          </div>

          {/* Category selection (for expense / income) */}
          {type !== 'transfer' ? (
            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Категория
                </label>
                {categorySuggestion && categoryId === categorySuggestion.category.id && (
                  <span className="text-[10px] font-bold text-emerald-700 bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded-full flex items-center gap-1">
                    <Sparkles size={10} className="text-emerald-600" />
                    Подобрано по описанию
                  </span>
                )}
              </div>

              {/* Automated AI Categorization Suggestion Banner */}
              {categorySuggestion && (
                <div
                  id="category_ai_suggestion_banner"
                  className="mb-2.5 p-2.5 rounded-xl bg-gradient-to-r from-emerald-50 via-teal-50 to-emerald-50 border border-emerald-200 flex items-center justify-between gap-2 shadow-2xs animate-in fade-in duration-150"
                >
                  <div className="flex items-center gap-2 min-w-0">
                    <div className="w-6 h-6 rounded-lg bg-emerald-600 text-white flex items-center justify-center shrink-0 shadow-2xs">
                      <Sparkles size={13} />
                    </div>
                    <div className="text-xs min-w-0 truncate">
                      <div className="flex items-center gap-1.5 flex-wrap">
                        <span className="font-extrabold text-emerald-950">AI подсказка:</span>
                        <strong className="text-slate-900 truncate">
                          {categorySuggestion.category.name}
                        </strong>
                        {categorySuggestion.subcategory && (
                          <span className="text-[10px] text-emerald-800 bg-white border border-emerald-200 px-1.5 py-0.2 rounded-md font-semibold">
                            {categorySuggestion.subcategory}
                          </span>
                        )}
                        <span className="text-[10px] text-slate-500">
                          (по слову «{categorySuggestion.matchedKeyword}»)
                        </span>
                      </div>
                    </div>
                  </div>

                  {categoryId !== categorySuggestion.category.id ? (
                    <button
                      id="apply_category_suggestion_btn"
                      type="button"
                      onClick={() => {
                        setCategoryId(categorySuggestion.category.id);
                        if (categorySuggestion.subcategory) {
                          setSubcategory(categorySuggestion.subcategory);
                        }
                        setUserManuallySelectedCategory(true);
                      }}
                      className="px-2.5 py-1 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-lg shrink-0 transition-colors shadow-2xs cursor-pointer flex items-center gap-1"
                    >
                      <span>Выбрать</span>
                    </button>
                  ) : (
                    <span className="text-[11px] font-bold text-emerald-700 bg-emerald-100/90 px-2 py-0.5 rounded-md flex items-center gap-1 shrink-0">
                      <Check size={12} className="stroke-[3]" /> Выбрано
                    </span>
                  )}
                </div>
              )}

              <div className="grid grid-cols-4 gap-2 max-h-48 overflow-y-auto p-1 border border-slate-100 rounded-xl bg-slate-50/50">
                {filteredCategories.map((cat) => {
                  const isSelected = categoryId === cat.id;
                  const isSuggested = categorySuggestion?.category.id === cat.id;
                  return (
                    <button
                      key={cat.id}
                      type="button"
                      id={`category_chip_${cat.id}`}
                      onClick={() => {
                        setCategoryId(cat.id);
                        setSubcategory(cat.subcategories[0] || '');
                        setUserManuallySelectedCategory(true);
                      }}
                      className={`relative flex flex-col items-center justify-center p-2.5 rounded-xl border text-center transition-all ${
                        isSelected
                          ? 'border-emerald-500 bg-emerald-50/80 shadow-xs ring-1 ring-emerald-500'
                          : isSuggested
                          ? 'border-emerald-300 bg-emerald-50/40 hover:bg-emerald-50'
                          : 'border-slate-200 bg-white hover:bg-slate-50'
                      }`}
                    >
                      {isSuggested && !isSelected && (
                        <span className="absolute -top-1 -right-1 px-1.5 py-0.2 rounded-full bg-emerald-600 text-white text-[9px] font-black uppercase tracking-wider shadow-2xs">
                          AI
                        </span>
                      )}
                      <div
                        className="w-8 h-8 rounded-lg flex items-center justify-center text-white mb-1 shadow-xs"
                        style={{ backgroundColor: cat.color }}
                      >
                        <DynamicIcon name={cat.icon} size={16} />
                      </div>
                      <span className="text-[11px] font-medium text-slate-800 line-clamp-1">
                        {cat.name}
                      </span>
                    </button>
                  );
                })}
              </div>

              {/* Subcategories */}
              {currentCategory && currentCategory.subcategories.length > 0 && (
                <div className="mt-3">
                  <div className="flex items-center justify-between mb-1.5">
                    <label className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                      Подкатегория
                    </label>
                    <button
                      type="button"
                      onClick={() => setIsAddingSubcategory(!isAddingSubcategory)}
                      className="text-xs font-medium text-emerald-600 hover:text-emerald-700 flex items-center gap-1"
                    >
                      <Plus size={13} /> Новая
                    </button>
                  </div>

                  {isAddingSubcategory ? (
                    <div className="flex gap-2 mb-2">
                      <input
                        type="text"
                        placeholder="Название подкатегории"
                        value={newSubcategoryInput}
                        onChange={(e) => setNewSubcategoryInput(e.target.value)}
                        className="flex-1 text-xs px-3 py-1.5 border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-emerald-500"
                      />
                      <button
                        type="button"
                        onClick={handleAddNewSubcategory}
                        className="px-3 py-1.5 bg-emerald-600 text-white rounded-lg text-xs font-semibold hover:bg-emerald-700"
                      >
                        <Check size={14} />
                      </button>
                    </div>
                  ) : null}

                  <div className="flex flex-wrap gap-1.5 max-h-24 overflow-y-auto">
                    {currentCategory.subcategories.map((sub) => {
                      const isSubSelected = subcategory === sub;
                      return (
                        <button
                          key={sub}
                          type="button"
                          onClick={() => setSubcategory(sub)}
                          className={`px-2.5 py-1 text-xs rounded-lg transition-colors ${
                            isSubSelected
                              ? 'bg-slate-800 text-white font-semibold shadow-xs'
                              : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
                          }`}
                        >
                          {sub}
                        </button>
                      );
                    })}
                  </div>
                </div>
              )}
            </div>
          ) : (
            /* Transfer Wallets Selection */
            <div className="space-y-3 bg-blue-50/50 p-4 rounded-xl border border-blue-100">
              <div className="flex items-center gap-2 text-xs font-bold text-blue-800">
                <ArrowRightLeft size={16} /> Направление перевода
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs text-slate-500 font-medium mb-1">Откуда</label>
                  <select
                    id="transfer_from_wallet_select"
                    value={walletId}
                    onChange={(e) => setWalletId(e.target.value)}
                    className="w-full text-xs font-semibold px-3 py-2 bg-white border border-slate-200 rounded-lg"
                  >
                    {wallets.map((w) => (
                      <option key={w.id} value={w.id}>
                        {w.name}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs text-slate-500 font-medium mb-1">Куда</label>
                  <select
                    id="transfer_to_wallet_select"
                    value={targetWalletId}
                    onChange={(e) => setTargetWalletId(e.target.value)}
                    className="w-full text-xs font-semibold px-3 py-2 bg-white border border-slate-200 rounded-lg"
                  >
                    {wallets
                      .filter((w) => w.id !== walletId)
                      .map((w) => (
                        <option key={w.id} value={w.id}>
                          {w.name}
                        </option>
                      ))}
                  </select>
                </div>
              </div>
            </div>
          )}

          {/* Wallet selection for expense/income */}
          {type !== 'transfer' && (
            <div>
              <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">
                Кошелек / Счет
              </label>
              <select
                id="tx_wallet_select"
                value={walletId}
                onChange={(e) => setWalletId(e.target.value)}
                className="w-full text-sm font-semibold px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
              >
                {wallets.map((w) => (
                  <option key={w.id} value={w.id}>
                    {w.name} ({currency.symbol}
                    {w.balance.toLocaleString('ru-RU')})
                  </option>
                ))}
              </select>
            </div>
          )}

          {/* Date & Note Grid */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">
                Дата
              </label>
              <input
                id="tx_date_input"
                type="date"
                required
                value={date}
                onChange={(e) => setDate(e.target.value)}
                className="w-full text-sm px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">
                Заметка
              </label>
              <input
                id="tx_note_input"
                type="text"
                placeholder="Например, кофе, такси, пятерочка"
                value={note}
                onChange={(e) => setNote(e.target.value)}
                className="w-full text-sm px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
              />
              {type !== 'transfer' && categorySuggestion && (
                <div className="mt-1 flex items-center justify-between text-[11px] text-emerald-850 bg-emerald-50/90 border border-emerald-200/80 px-2 py-0.5 rounded-lg">
                  <span className="truncate flex items-center gap-1">
                    <Sparkles size={10} className="text-emerald-600 shrink-0" />
                    <span>
                      {categorySuggestion.category.name}
                      {categorySuggestion.subcategory ? ` • ${categorySuggestion.subcategory}` : ''}
                    </span>
                  </span>
                  {categoryId !== categorySuggestion.category.id ? (
                    <button
                      type="button"
                      onClick={() => {
                        setCategoryId(categorySuggestion.category.id);
                        if (categorySuggestion.subcategory) setSubcategory(categorySuggestion.subcategory);
                        setUserManuallySelectedCategory(true);
                      }}
                      className="ml-1 text-[10px] font-bold text-emerald-700 hover:text-emerald-900 underline cursor-pointer shrink-0"
                    >
                      Применить
                    </button>
                  ) : (
                    <span className="text-[10px] font-bold text-emerald-700 shrink-0">
                      ✓
                    </span>
                  )}
                </div>
              )}
            </div>
          </div>

          {/* Recurring Transaction Configuration */}
          <div
            id="recurring_section_container"
            className={`p-3.5 rounded-2xl border transition-all ${
              isRecurring
                ? 'bg-gradient-to-r from-emerald-50/80 to-teal-50/80 border-emerald-300 shadow-2xs'
                : 'bg-slate-50/70 border-slate-200/80 hover:bg-slate-50'
            }`}
          >
            <div className="flex items-center justify-between">
              <label
                htmlFor="tx_recurring_toggle"
                className="flex items-center gap-2.5 cursor-pointer select-none"
              >
                <div
                  className={`w-7 h-7 rounded-xl flex items-center justify-center transition-colors ${
                    isRecurring
                      ? 'bg-emerald-600 text-white shadow-2xs'
                      : 'bg-slate-200 text-slate-500'
                  }`}
                >
                  <Repeat size={15} />
                </div>
                <div>
                  <div className="text-xs sm:text-sm font-bold text-slate-900 flex items-center gap-1.5">
                    <span>Регулярная операция</span>
                    {isRecurring && (
                      <span className="text-[10px] font-extrabold uppercase px-1.5 py-0.5 rounded-md bg-emerald-200/80 text-emerald-800">
                        {getIntervalLabel(recurrenceInterval)}
                      </span>
                    )}
                  </div>
                  <p className="text-[11px] text-slate-500">
                    Автоматически повторять списание или начисление по расписанию
                  </p>
                </div>
              </label>

              {/* Toggle switch */}
              <input
                id="tx_recurring_toggle"
                type="checkbox"
                checked={isRecurring}
                onChange={(e) => setIsRecurring(e.target.checked)}
                className="w-5 h-5 accent-emerald-600 rounded cursor-pointer shrink-0"
              />
            </div>

            {/* Recurring Settings when active */}
            {isRecurring && (
              <div className="mt-3.5 pt-3 border-t border-emerald-200/70 space-y-3 animate-in fade-in duration-200">
                {/* Interval selection buttons */}
                <div>
                  <label className="block text-[10px] font-bold uppercase tracking-wider text-slate-600 mb-1.5">
                    Периодичность повтора
                  </label>
                  <div className="grid grid-cols-3 sm:grid-cols-5 gap-1.5">
                    {(
                      [
                        { id: 'daily', label: 'Каждый день' },
                        { id: 'weekly', label: 'Еженедельно' },
                        { id: 'biweekly', label: '2 недели' },
                        { id: 'monthly', label: 'Ежемесячно' },
                        { id: 'yearly', label: 'Ежегодно' },
                      ] as const
                    ).map((item) => (
                      <button
                        key={item.id}
                        type="button"
                        onClick={() => setRecurrenceInterval(item.id)}
                        className={`py-1.5 px-2 rounded-xl text-xs font-bold transition-all text-center ${
                          recurrenceInterval === item.id
                            ? 'bg-emerald-600 text-white shadow-2xs'
                            : 'bg-white text-slate-700 border border-slate-200 hover:bg-slate-50'
                        }`}
                      >
                        {item.label}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Next scheduled run date indicator */}
                <div className="flex items-center justify-between text-xs bg-white/90 p-2.5 rounded-xl border border-emerald-200/60">
                  <div className="flex items-center gap-1.5 text-slate-700">
                    <Clock size={13} className="text-emerald-600 shrink-0" />
                    <span className="text-[11px] font-medium text-slate-600">
                      Следующее автопроведение:
                    </span>
                    <strong className="text-emerald-800 font-bold">
                      {calculateNextRecurrenceDate(date, recurrenceInterval)}
                    </strong>
                  </div>
                </div>

                {/* Duration / End Date */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 pt-1">
                  <div className="flex items-center gap-2">
                    <input
                      id="recurrence_has_end_date"
                      type="checkbox"
                      checked={hasEndDate}
                      onChange={(e) => setHasEndDate(e.target.checked)}
                      className="w-4 h-4 accent-emerald-600 rounded cursor-pointer"
                    />
                    <label
                      htmlFor="recurrence_has_end_date"
                      className="text-xs font-semibold text-slate-700 cursor-pointer select-none"
                    >
                      Ограничить дату окончания
                    </label>
                  </div>

                  {hasEndDate && (
                    <div className="flex items-center gap-2">
                      <span className="text-[11px] text-slate-500 shrink-0 font-medium">До:</span>
                      <input
                        id="tx_recurrence_end_date"
                        type="date"
                        min={date}
                        value={recurrenceEndDate}
                        onChange={(e) => setRecurrenceEndDate(e.target.value)}
                        className="w-full text-xs font-semibold px-2.5 py-1.5 bg-white border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500"
                      />
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>

          {/* Tags section for project, trip, or interest */}
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="text-xs font-semibold text-slate-500 uppercase tracking-wider flex items-center gap-1.5">
                <Tag size={13} className="text-emerald-600" />
                <span>Теги (проект, поездка, интерес)</span>
              </label>
              <span className="text-[11px] text-slate-400">Enter или запятая</span>
            </div>

            {/* Active Tags */}
            {tags.length > 0 && (
              <div className="flex flex-wrap gap-1.5 mb-2">
                {tags.map((tag) => (
                  <span
                    key={tag}
                    className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-bold bg-emerald-50 text-emerald-800 border border-emerald-200 shadow-2xs"
                  >
                    <span>#{tag}</span>
                    <button
                      type="button"
                      onClick={() => handleRemoveTag(tag)}
                      className="hover:text-rose-600 rounded-full p-0.5 transition-colors"
                      title="Удалить тег"
                    >
                      <X size={12} />
                    </button>
                  </span>
                ))}
              </div>
            )}

            {/* Tag input row */}
            <div className="flex gap-2">
              <div className="relative flex-1">
                <span className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-sm font-semibold select-none">
                  #
                </span>
                <input
                  id="tx_tag_input"
                  type="text"
                  placeholder="отпуск, ремонт, проект-х..."
                  value={tagInput}
                  onChange={(e) => setTagInput(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ',') {
                      e.preventDefault();
                      handleAddTag(tagInput);
                    }
                  }}
                  className="w-full text-sm pl-7 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>
              <button
                type="button"
                id="tx_add_tag_btn"
                onClick={() => handleAddTag(tagInput)}
                className="px-3.5 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold transition-colors shrink-0"
              >
                + Тег
              </button>
            </div>

            {/* Suggested quick tags */}
            {suggestedTags.length > 0 && (
              <div className="flex flex-wrap items-center gap-1.5 mt-2">
                <span className="text-[11px] text-slate-400 font-medium">Подсказки:</span>
                {suggestedTags.slice(0, 8).map((s) => (
                  <button
                    key={s}
                    type="button"
                    onClick={() => handleAddTag(s)}
                    className="text-[11px] font-semibold px-2 py-0.5 rounded-md bg-slate-100 text-slate-600 hover:bg-emerald-50 hover:text-emerald-700 border border-slate-200/70 transition-colors"
                  >
                    #{s}
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Actions */}
          <div className="pt-2 flex items-center justify-end gap-3">
            <button
              id="cancel_transaction_btn"
              type="button"
              onClick={onClose}
              className="px-5 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold text-slate-700 hover:bg-slate-100 transition-colors"
            >
              Отмена
            </button>
            <button
              id="save_transaction_btn"
              type="submit"
              className="px-6 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-bold shadow-md hover:shadow-lg transition-all"
            >
              {initialTransaction ? 'Сохранить' : 'Добавить'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
