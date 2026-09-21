import React, { useState, useEffect, useMemo } from 'react';
import {
  X,
  ArrowRightLeft,
  SlidersHorizontal,
  RotateCcw,
  PlusCircle,
  TrendingUp,
  MapPin,
  Check,
  Sparkles,
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import {
  EXTENDED_CURRENCIES,
  POPULAR_TRAVEL_DESTINATIONS,
  getCrossExchangeRate,
  convertCurrency,
  getCurrencyInfo,
} from '../utils/currencyRates';

interface CurrencyConverterModalProps {
  isOpen: boolean;
  onClose: () => void;
  onOpenAddTransactionWithData: (data: {
    baseAmount: number;
    originalAmount: number;
    originalCurrency: string;
    exchangeRate: number;
    country?: string;
  }) => void;
}

export const CurrencyConverterModal: React.FC<CurrencyConverterModalProps> = ({
  isOpen,
  onClose,
  onOpenAddTransactionWithData,
}) => {
  const { currency } = useFinance();

  // Selected currencies: fromCode defaults to TRY (Turkey) or USD, toCode defaults to user's base currency (e.g. RUB)
  const [fromCode, setFromCode] = useState<string>('TRY');
  const [toCode, setToCode] = useState<string>(currency.code);
  const [fromAmount, setFromAmount] = useState<string>('100');
  const [isCustomRateActive, setIsCustomRateActive] = useState<boolean>(false);
  const [customRate, setCustomRate] = useState<string>('');
  const [selectedCountryName, setSelectedCountryName] = useState<string>('Турция 🇹🇷');

  // Keep toCode in sync with base currency if not edited
  useEffect(() => {
    if (currency.code) {
      setToCode(currency.code);
    }
  }, [currency.code]);

  // Calculate default market rate
  const marketRate = useMemo(() => {
    return getCrossExchangeRate(fromCode, toCode);
  }, [fromCode, toCode]);

  // Active rate (custom or market)
  const effectiveRate = useMemo(() => {
    const parsedCustom = parseFloat(customRate);
    if (isCustomRateActive && !isNaN(parsedCustom) && parsedCustom > 0) {
      return parsedCustom;
    }
    return marketRate;
  }, [isCustomRateActive, customRate, marketRate]);

  // Converted result
  const convertedAmount = useMemo(() => {
    const num = parseFloat(fromAmount);
    if (isNaN(num) || num <= 0) return 0;
    return convertCurrency(num, fromCode, toCode, effectiveRate);
  }, [fromAmount, fromCode, toCode, effectiveRate]);

  if (!isOpen) return null;

  const fromInfo = getCurrencyInfo(fromCode);
  const toInfo = getCurrencyInfo(toCode);

  const handleSwap = () => {
    const prevFrom = fromCode;
    const prevTo = toCode;
    setFromCode(prevTo);
    setToCode(prevFrom);
    if (isCustomRateActive && effectiveRate > 0) {
      setCustomRate((1 / effectiveRate).toFixed(4));
    }
  };

  const handleSelectDestination = (dest: (typeof POPULAR_TRAVEL_DESTINATIONS)[0]) => {
    setFromCode(dest.currencyCode);
    setToCode(currency.code);
    setSelectedCountryName(`${dest.country} ${dest.flag}`);
    setIsCustomRateActive(false);
    setCustomRate('');
  };

  const handleApplyQuickAdd = (addVal: number) => {
    const current = parseFloat(fromAmount) || 0;
    setFromAmount((current + addVal).toString());
  };

  const handleCreateExpense = () => {
    const parsedAmount = parseFloat(fromAmount);
    if (isNaN(parsedAmount) || parsedAmount <= 0) return;

    onOpenAddTransactionWithData({
      baseAmount: convertedAmount,
      originalAmount: parsedAmount,
      originalCurrency: fromCode,
      exchangeRate: effectiveRate,
      country: selectedCountryName,
    });
    onClose();
  };

  // Cheat sheet comparison numbers (e.g., 10, 50, 100, 500, 1000 foreign currency units)
  const cheatSheetValues = [10, 50, 100, 250, 500, 1000];

  return (
    <div
      id="currency_converter_modal_overlay"
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4 overflow-y-auto"
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div
        id="currency_converter_modal_content"
        className="bg-white rounded-3xl shadow-2xl w-full max-w-xl overflow-hidden my-6 animate-in fade-in zoom-in-95 duration-200"
      >
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 bg-slate-50/80">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-500 flex items-center justify-center text-white shadow-xs">
              <ArrowRightLeft size={18} />
            </div>
            <div>
              <h2 className="text-base sm:text-lg font-bold text-slate-800">
                Конвертер валют для путешествий
              </h2>
              <p className="text-[11px] text-slate-500">
                Быстрый пересчет и запись расходов в поездках за рубежом
              </p>
            </div>
          </div>
          <button
            id="close_converter_modal_btn"
            onClick={onClose}
            className="p-2 text-slate-400 hover:text-slate-600 rounded-lg hover:bg-slate-200/60 transition-colors"
          >
            <X size={20} />
          </button>
        </div>

        <div className="p-6 space-y-6">
          {/* Quick Destination Chips */}
          <div>
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-bold text-slate-500 uppercase tracking-wider flex items-center gap-1.5">
                <MapPin size={13} className="text-emerald-600" />
                Куда вы поехали?
              </span>
              <span className="text-[11px] font-semibold text-slate-400">1 клик для переключения</span>
            </div>
            <div className="flex gap-1.5 overflow-x-auto pb-1.5 scrollbar-thin">
              {POPULAR_TRAVEL_DESTINATIONS.map((dest) => {
                const isActive = fromCode === dest.currencyCode;
                return (
                  <button
                    key={dest.id}
                    type="button"
                    onClick={() => handleSelectDestination(dest)}
                    className={`shrink-0 px-2.5 py-1.5 rounded-xl text-xs font-semibold flex items-center gap-1.5 transition-all ${
                      isActive
                        ? 'bg-slate-900 text-white shadow-xs scale-102'
                        : 'bg-slate-100 hover:bg-slate-200 text-slate-700'
                    }`}
                  >
                    <span>{dest.flag}</span>
                    <span>{dest.country}</span>
                    <span className="text-[10px] opacity-70">({dest.currencyCode})</span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Dual Converter Box */}
          <div className="bg-slate-50 border border-slate-200/80 rounded-2xl p-4 space-y-4">
            {/* FROM Section */}
            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label className="text-xs font-bold text-slate-500 uppercase tracking-wider">
                  Трачу в валюте страны ({fromInfo.country})
                </label>
                <span className="text-xs font-semibold text-slate-400">
                  {fromInfo.flag} {fromInfo.code}
                </span>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
                <div className="sm:col-span-2 relative">
                  <input
                    id="converter_from_amount_input"
                    type="number"
                    step="any"
                    value={fromAmount}
                    onChange={(e) => setFromAmount(e.target.value)}
                    placeholder="0"
                    autoFocus
                    className="w-full text-2xl font-black text-slate-900 px-4 py-3 bg-white border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
                  />
                  <span className="absolute right-3.5 top-1/2 -translate-y-1/2 text-lg font-bold text-slate-400">
                    {fromInfo.symbol}
                  </span>
                </div>
                <select
                  id="converter_from_select"
                  value={fromCode}
                  onChange={(e) => {
                    setFromCode(e.target.value);
                    const found = EXTENDED_CURRENCIES.find((c) => c.code === e.target.value);
                    if (found) setSelectedCountryName(`${found.country} ${found.flag}`);
                  }}
                  className="w-full text-xs font-bold px-3 py-3 bg-white border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  {EXTENDED_CURRENCIES.map((c) => (
                    <option key={c.code} value={c.code}>
                      {c.flag} {c.code} — {c.name}
                    </option>
                  ))}
                </select>
              </div>

              {/* Quick Amount Step Buttons */}
              <div className="flex gap-1.5 mt-2">
                {[50, 100, 500, 1000, 5000].map((val) => (
                  <button
                    key={val}
                    type="button"
                    onClick={() => handleApplyQuickAdd(val)}
                    className="px-2 py-1 text-[11px] font-semibold bg-white border border-slate-200 hover:bg-slate-100 text-slate-700 rounded-lg transition-colors"
                  >
                    +{val}
                  </button>
                ))}
              </div>
            </div>

            {/* Swap button Divider */}
            <div className="flex items-center justify-center my-1 relative">
              <div className="absolute inset-x-0 top-1/2 border-t border-slate-200" />
              <button
                id="swap_currencies_btn"
                type="button"
                onClick={handleSwap}
                className="relative z-10 w-9 h-9 rounded-full bg-white border border-slate-200 shadow-xs hover:border-blue-500 hover:shadow-md flex items-center justify-center text-slate-600 hover:text-blue-600 transition-all"
                title="Поменять местами"
              >
                <ArrowRightLeft size={16} />
              </button>
            </div>

            {/* TO Section */}
            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label className="text-xs font-bold text-slate-500 uppercase tracking-wider">
                  Итого спишется со счёта ({toInfo.name})
                </label>
                <span className="text-xs font-semibold text-slate-400">
                  {toInfo.flag} {toInfo.code}
                </span>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
                <div className="sm:col-span-2 relative">
                  <div
                    id="converter_result_box"
                    className="w-full text-2xl font-black text-emerald-700 px-4 py-3 bg-emerald-50/70 border border-emerald-200 rounded-xl flex items-center justify-between"
                  >
                    <span>
                      {convertedAmount.toLocaleString('ru-RU', {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 2,
                      })}
                    </span>
                    <span className="text-lg font-bold text-emerald-600">{toInfo.symbol}</span>
                  </div>
                </div>
                <select
                  id="converter_to_select"
                  value={toCode}
                  onChange={(e) => setToCode(e.target.value)}
                  className="w-full text-xs font-bold px-3 py-3 bg-white border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  {EXTENDED_CURRENCIES.map((c) => (
                    <option key={c.code} value={c.code}>
                      {c.flag} {c.code} — {c.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>

          {/* Exchange Rate Info & Custom Rate Adjuster */}
          <div className="bg-white border border-slate-200 rounded-2xl p-4 space-y-3">
            <div className="flex items-center justify-between flex-wrap gap-2">
              <div className="flex items-center gap-2">
                <TrendingUp size={16} className="text-blue-600" />
                <span className="text-xs font-bold text-slate-800">
                  1 {fromCode} = {effectiveRate.toFixed(4)} {toCode}
                </span>
                <span className="text-[11px] text-slate-400">
                  (1 {toCode} = {(1 / effectiveRate).toFixed(4)} {fromCode})
                </span>
              </div>

              <button
                type="button"
                onClick={() => {
                  if (!isCustomRateActive) {
                    setCustomRate(effectiveRate.toFixed(4));
                  }
                  setIsCustomRateActive(!isCustomRateActive);
                }}
                className={`text-xs font-semibold px-2.5 py-1 rounded-lg flex items-center gap-1 transition-colors ${
                  isCustomRateActive
                    ? 'bg-amber-100 text-amber-800'
                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                }`}
              >
                <SlidersHorizontal size={13} />
                {isCustomRateActive ? 'Свой курс включен' : 'Задать курс вручную'}
              </button>
            </div>

            {/* Custom Rate Input Field */}
            {isCustomRateActive && (
              <div className="p-3 bg-amber-50/80 border border-amber-200 rounded-xl space-y-2 animate-in fade-in duration-200">
                <div className="flex items-center justify-between">
                  <span className="text-[11px] font-bold text-amber-900">
                    Курс вашего банка / обменника (за 1 {fromCode}):
                  </span>
                  <button
                    type="button"
                    onClick={() => {
                      setCustomRate(marketRate.toFixed(4));
                      setIsCustomRateActive(false);
                    }}
                    className="text-[10px] font-semibold text-amber-700 hover:underline flex items-center gap-1"
                  >
                    <RotateCcw size={11} /> Сбросить к рыночному
                  </button>
                </div>
                <div className="flex items-center gap-2">
                  <input
                    type="number"
                    step="any"
                    value={customRate}
                    onChange={(e) => setCustomRate(e.target.value)}
                    placeholder={marketRate.toFixed(4)}
                    className="flex-1 text-xs font-bold px-3 py-2 bg-white border border-amber-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500"
                  />
                  <span className="text-xs font-semibold text-amber-800">{toCode}</span>
                </div>
              </div>
            )}
          </div>

          {/* Quick Cheat-Sheet for Supermarket / Bazaar Shopping */}
          <div className="border border-slate-100 bg-slate-50/60 rounded-2xl p-3">
            <div className="flex items-center justify-between mb-2">
              <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">
                Шпаргалка для покупок в магазине ({fromInfo.flag} {fromCode} ➔ {toInfo.symbol})
              </span>
              <span className="text-[10px] text-slate-400">Цены ориентировочно</span>
            </div>
            <div className="grid grid-cols-3 sm:grid-cols-6 gap-2">
              {cheatSheetValues.map((val) => {
                const convertedVal = convertCurrency(val, fromCode, toCode, effectiveRate);
                return (
                  <div
                    key={val}
                    onClick={() => setFromAmount(val.toString())}
                    className="bg-white border border-slate-200/80 p-2 rounded-xl text-center cursor-pointer hover:border-blue-400 hover:shadow-xs transition-all"
                  >
                    <span className="block text-[11px] font-bold text-slate-700">
                      {val} {fromInfo.symbol}
                    </span>
                    <span className="block text-xs font-black text-emerald-600">
                      ~{Math.round(convertedVal)} {toInfo.symbol}
                    </span>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Bottom Action: Create Foreign Transaction */}
          <div className="pt-2 flex flex-col sm:flex-row items-center justify-between gap-3">
            <div className="text-xs text-slate-500 text-center sm:text-left">
              Сумма: <span className="font-bold text-slate-800">{fromAmount} {fromInfo.symbol}</span> (≈{' '}
              <span className="font-bold text-emerald-600">
                {convertedAmount.toFixed(2)} {toInfo.symbol}
              </span>)
            </div>

            <button
              id="add_converted_expense_btn"
              type="button"
              onClick={handleCreateExpense}
              className="w-full sm:w-auto px-6 py-3 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 text-white text-xs sm:text-sm font-bold shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2"
            >
              <PlusCircle size={17} />
              Записать как расход за рубежом
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
