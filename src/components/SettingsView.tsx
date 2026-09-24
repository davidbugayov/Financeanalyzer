import React, { useState } from 'react';
import {
  Globe,
  Lock,
  Unlock,
  Shield,
  FileSpreadsheet,
  Trash2,
  RefreshCw,
  Plus,
  Tag,
  Check,
  Info,
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { CURRENCIES } from '../data/initialData';
import { DynamicIcon } from '../utils/iconHelper';
import { PWAInstallSection } from './PWAInstallSection';

interface SettingsViewProps {
  onOpenImportExport: () => void;
  onOpenAchievements: () => void;
}

export const SettingsView: React.FC<SettingsViewProps> = ({
  onOpenImportExport,
  onOpenAchievements,
}) => {
  const {
    currency,
    setCurrency,
    pinCode,
    setPinCode,
    categories,
    addCategory,
    resetAllData,
    loadDemoData,
    transactions,
    wallets,
  } = useFinance();

  // PIN settings state
  const [newPin, setNewPin] = useState('');
  const [isPinModalOpen, setIsPinModalOpen] = useState(false);
  const [pinError, setPinError] = useState('');

  // New Category state
  const [isAddCatOpen, setIsAddCatOpen] = useState(false);
  const [catName, setCatName] = useState('');
  const [catType, setCatType] = useState<'expense' | 'income'>('expense');
  const [catColor, setCatColor] = useState('#10B981');
  const [catIcon, setCatIcon] = useState('Tag');

  // Reset confirmation
  const [isResetConfirmOpen, setIsResetConfirmOpen] = useState(false);

  const handleSavePin = (e: React.FormEvent) => {
    e.preventDefault();
    if (newPin.length !== 4 || !/^\d{4}$/.test(newPin)) {
      setPinError('PIN-код должен состоять ровно из 4 цифр');
      return;
    }
    setPinCode(newPin);
    setIsPinModalOpen(false);
    setNewPin('');
    setPinError('');
  };

  const handleRemovePin = () => {
    setPinCode(null);
  };

  const handleCreateCategory = (e: React.FormEvent) => {
    e.preventDefault();
    if (!catName.trim()) return;

    addCategory({
      name: catName.trim(),
      color: catColor,
      icon: catIcon,
      isExpense: catType === 'expense',
      subcategories: ['Общее'],
    });

    setCatName('');
    setIsAddCatOpen(false);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-xl sm:text-2xl font-black text-slate-900">Настройки и профиль</h2>
        <p className="text-xs text-slate-500">
          Валюта, безопасность, офлайн-режим, категории и резервное копирование
        </p>
      </div>

      {/* Currency Switcher Card */}
      <div className="bg-white rounded-3xl border border-slate-200 p-5 sm:p-6 shadow-xs space-y-3">
        <div className="flex items-center gap-2">
          <Globe size={18} className="text-emerald-600" />
          <h3 className="text-sm font-extrabold text-slate-900">Основная валюта</h3>
        </div>
        <p className="text-xs text-slate-500">
          Выберите валюту для отображения балансов, транзакций и отчетов:
        </p>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 pt-1">
          {CURRENCIES.map((curr) => {
            const isSelected = currency.code === curr.code;
            return (
              <button
                key={curr.code}
                onClick={() => setCurrency(curr.code)}
                className={`p-3 rounded-2xl border text-left transition-all overflow-hidden min-w-0 ${
                  isSelected
                    ? 'border-emerald-500 bg-emerald-50/70 shadow-xs'
                    : 'border-slate-200 bg-white hover:bg-slate-50'
                }`}
              >
                <div className="flex items-center justify-between mb-1">
                  <span className="text-base font-extrabold text-slate-900">{curr.symbol}</span>
                  <span className="text-[10px] font-black uppercase tracking-wider text-slate-400">
                    {curr.code}
                  </span>
                </div>
                <div className="text-[11px] font-medium text-slate-600 truncate">
                  {curr.name}
                </div>
              </button>
            );
          })}
        </div>
      </div>

      {/* Security & PIN Lock */}
      <div className="bg-white rounded-3xl border border-slate-200 p-5 sm:p-6 shadow-xs space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Shield size={18} className="text-indigo-600" />
            <h3 className="text-sm font-extrabold text-slate-900">Защита и PIN-код</h3>
          </div>
          {pinCode ? (
            <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-100 text-emerald-800">
              Включено
            </span>
          ) : (
            <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-slate-100 text-slate-500">
              Отключено
            </span>
          )}
        </div>

        <p className="text-xs text-slate-500 leading-relaxed">
          Установите 4-значный цифровой код для защиты ваших финансовых данных от посторонних глаз
          при входе в приложение.
        </p>

        <div className="flex gap-2">
          {pinCode ? (
            <>
              <button
                onClick={() => setIsPinModalOpen(true)}
                className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold rounded-xl transition-colors"
              >
                Изменить PIN-код
              </button>
              <button
                onClick={handleRemovePin}
                className="px-4 py-2 bg-red-50 hover:bg-red-100 text-red-600 text-xs font-bold rounded-xl transition-colors"
              >
                Отключить PIN-код
              </button>
            </>
          ) : (
            <button
              onClick={() => setIsPinModalOpen(true)}
              className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold rounded-xl shadow-xs transition-colors"
            >
              Установить PIN-код
            </button>
          )}
        </div>
      </div>

      {/* Data Import / Export / Backup */}
      <div className="bg-white rounded-3xl border border-slate-200 p-5 sm:p-6 shadow-xs space-y-4">
        <div className="flex items-center gap-2">
          <FileSpreadsheet size={18} className="text-emerald-600" />
          <h3 className="text-sm font-extrabold text-slate-900">Управление данными</h3>
        </div>
        <p className="text-xs text-slate-500">
          Импорт выписок из банков (Тинькофф, Сбер, Альфа), выгрузка в CSV и резервные копии:
        </p>

        <div className="flex flex-wrap gap-2 pt-1">
          <button
            onClick={onOpenImportExport}
            className="flex items-center gap-1.5 px-4 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold rounded-xl shadow-xs transition-colors"
          >
            <FileSpreadsheet size={15} />
            <span>Импорт / Экспорт выписок</span>
          </button>

          <button
            onClick={loadDemoData}
            className="flex items-center gap-1.5 px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold rounded-xl transition-colors"
          >
            <RefreshCw size={14} />
            <span>Восстановить демо-данные</span>
          </button>

          <button
            onClick={() => setIsResetConfirmOpen(true)}
            className="flex items-center gap-1.5 px-4 py-2.5 bg-red-50 hover:bg-red-100 text-red-600 text-xs font-bold rounded-xl transition-colors ml-auto"
          >
            <Trash2 size={14} />
            <span>Стереть все данные</span>
          </button>
        </div>
      </div>

      {/* Category Manager Section */}
      <div className="bg-white rounded-3xl border border-slate-200 p-5 sm:p-6 shadow-xs space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Tag size={18} className="text-emerald-600" />
            <h3 className="text-sm font-extrabold text-slate-900">Категории ({categories.length})</h3>
          </div>
          <button
            onClick={() => setIsAddCatOpen(true)}
            className="flex items-center gap-1 text-xs font-bold text-emerald-600 hover:text-emerald-700"
          >
            <Plus size={15} /> Создать категорию
          </button>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5 max-h-60 overflow-y-auto pr-1">
          {categories.map((c) => (
            <div
              key={c.id}
              className="p-2.5 rounded-xl border border-slate-100 bg-slate-50/60 flex items-center gap-2.5"
            >
              <div
                className="w-7 h-7 rounded-lg flex items-center justify-center text-white shrink-0 shadow-xs"
                style={{ backgroundColor: c.color }}
              >
                <DynamicIcon name={c.icon} size={14} />
              </div>
              <div className="min-w-0">
                <span className="text-xs font-bold text-slate-800 block truncate">{c.name}</span>
                <span className="text-[10px] text-slate-400">
                  {c.isExpense ? 'Расход' : 'Доход'} • {c.subcategories.length} подкат.
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* About Box */}
      <div className="p-5 rounded-3xl bg-slate-100 border border-slate-200/80 flex items-start gap-3.5">
        <Info size={20} className="text-slate-500 shrink-0 mt-0.5" />
        <div className="text-xs text-slate-600 leading-relaxed">
          <span className="font-bold text-slate-900">Finance Analyzer (Деньги под Контролем)</span>
          <br />
          Автономное и полностью приватное веб-приложение для ведения личного и семейного бюджета.
          Все транзакции и данные хранятся локально в вашем браузере без передачи на сторонние
          сервера.
        </div>
      </div>

      {/* PWA Installation & Offline Mode Section (at bottom of Settings) */}
      <PWAInstallSection />

      {/* PIN Setup Modal */}
      {isPinModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4">
          <div className="bg-white rounded-3xl p-6 w-full max-w-sm shadow-2xl space-y-4">
            <h3 className="text-base font-extrabold text-slate-900">Установка PIN-кода</h3>
            <p className="text-xs text-slate-500">
              Введите 4 цифры, которые будут запрашиваться при входе в приложение:
            </p>

            <form onSubmit={handleSavePin} className="space-y-4">
              <input
                type="password"
                maxLength={4}
                autoFocus
                placeholder="••••"
                value={newPin}
                onChange={(e) => setNewPin(e.target.value)}
                className="w-full text-center text-3xl tracking-widest font-black py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />

              {pinError && <p className="text-xs font-bold text-red-600 text-center">{pinError}</p>}

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setIsPinModalOpen(false)}
                  className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl"
                >
                  Отмена
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 text-xs font-bold text-white bg-indigo-600 hover:bg-indigo-700 rounded-xl shadow-xs"
                >
                  Сохранить PIN
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Add Custom Category Modal */}
      {isAddCatOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4">
          <div className="bg-white rounded-3xl p-6 w-full max-w-md shadow-2xl space-y-4">
            <h3 className="text-base font-extrabold text-slate-900">Новая категория</h3>

            <form onSubmit={handleCreateCategory} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-500 mb-1">
                  Название категории
                </label>
                <input
                  type="text"
                  required
                  placeholder="Например: Хобби, Авто или Подписки"
                  value={catName}
                  onChange={(e) => setCatName(e.target.value)}
                  className="w-full text-xs font-semibold px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-500 mb-1">Тип</label>
                <div className="grid grid-cols-2 gap-2 text-xs font-bold">
                  <button
                    type="button"
                    onClick={() => setCatType('expense')}
                    className={`py-2 rounded-xl border ${
                      catType === 'expense'
                        ? 'border-red-500 bg-red-50 text-red-700'
                        : 'border-slate-200'
                    }`}
                  >
                    Расход
                  </button>
                  <button
                    type="button"
                    onClick={() => setCatType('income')}
                    className={`py-2 rounded-xl border ${
                      catType === 'income'
                        ? 'border-emerald-500 bg-emerald-50 text-emerald-700'
                        : 'border-slate-200'
                    }`}
                  >
                    Доход
                  </button>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-500 mb-2">Цвет</label>
                <div className="flex gap-2">
                  {['#10B981', '#3B82F6', '#8B5CF6', '#F59E0B', '#EF4444', '#EC4899', '#06B6D4'].map(
                    (c) => (
                      <button
                        key={c}
                        type="button"
                        onClick={() => setCatColor(c)}
                        className={`w-7 h-7 rounded-full ${
                          catColor === c ? 'scale-125 ring-2 ring-slate-900 ring-offset-2' : ''
                        }`}
                        style={{ backgroundColor: c }}
                      />
                    )
                  )}
                </div>
              </div>

              <div className="flex justify-end gap-2 pt-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsAddCatOpen(false)}
                  className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl"
                >
                  Отмена
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl shadow-xs"
                >
                  Создать
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Reset Confirmation Modal */}
      {isResetConfirmOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4">
          <div className="bg-white rounded-3xl p-6 w-full max-w-sm shadow-2xl space-y-4">
            <h3 className="text-base font-extrabold text-slate-900">Удалить все данные?</h3>
            <p className="text-xs text-slate-500 leading-relaxed">
              Все {transactions.length} транзакций будут удалены, а балансы счетов обнулены. Это
              действие необратимо!
            </p>
            <div className="flex justify-end gap-2 pt-2">
              <button
                onClick={() => setIsResetConfirmOpen(false)}
                className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl"
              >
                Отмена
              </button>
              <button
                onClick={() => {
                  resetAllData();
                  setIsResetConfirmOpen(false);
                }}
                className="px-4 py-2 text-xs font-bold text-white bg-red-600 hover:bg-red-700 rounded-xl shadow-xs"
              >
                Да, стереть
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
