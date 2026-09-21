import React, { useState, useEffect } from 'react';
import { X, Check, Plus, ArrowRightLeft, Globe, MapPin, SlidersHorizontal, RotateCcw } from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { Transaction, TransactionType } from '../types';
import { DynamicIcon } from '../utils/iconHelper';
import {
  EXTENDED_CURRENCIES,
  POPULAR_TRAVEL_DESTINATIONS,
  getCrossExchangeRate,
  convertCurrency,
  getCurrencyInfo,
} from '../utils/currencyRates';

export interface InitialForeignData {
  baseAmount: number;
  originalAmount: number;
  originalCurrency: string;
  exchangeRate: number;
  country?: string;
}

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
  const { categories, wallets, addTransaction, updateTransaction, currency, addSubcategory } = useFinance();

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
  const [error, setError] = useState<string>('');

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
        ...foreignData,
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
        ...foreignData,
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
              <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">
                Категория
              </label>
              <div className="grid grid-cols-4 gap-2 max-h-48 overflow-y-auto p-1 border border-slate-100 rounded-xl bg-slate-50/50">
                {filteredCategories.map((cat) => {
                  const isSelected = categoryId === cat.id;
                  return (
                    <button
                      key={cat.id}
                      type="button"
                      id={`category_chip_${cat.id}`}
                      onClick={() => {
                        setCategoryId(cat.id);
                        setSubcategory(cat.subcategories[0] || '');
                      }}
                      className={`flex flex-col items-center justify-center p-2.5 rounded-xl border text-center transition-all ${
                        isSelected
                          ? 'border-emerald-500 bg-emerald-50/80 shadow-xs'
                          : 'border-slate-200 bg-white hover:bg-slate-50'
                      }`}
                    >
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
                placeholder="Например, кофе или подарок"
                value={note}
                onChange={(e) => setNote(e.target.value)}
                className="w-full text-sm px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
              />
            </div>
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
