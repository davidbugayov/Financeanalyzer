import React, { useState } from 'react';
import {
  X,
  Upload,
  Download,
  FileSpreadsheet,
  Check,
  AlertCircle,
  FileText,
  Database,
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { exportTransactionsToCSV, parseBankStatement } from '../utils/csvParser';
import { formatCurrency } from '../utils/financeCalculations';
import { Transaction } from '../types';

interface ImportExportModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const ImportExportModal: React.FC<ImportExportModalProps> = ({ isOpen, onClose }) => {
  const {
    transactions,
    wallets,
    categories,
    achievements,
    currency,
    importTransactions,
    loadDemoData,
  } = useFinance();

  const [activeTab, setActiveTab] = useState<'import' | 'export' | 'backup'>('import');
  const [bankType, setBankType] = useState<'tinkoff' | 'sberbank' | 'alfabank' | 'generic'>('tinkoff');
  const [targetWalletId, setTargetWalletId] = useState(wallets[0]?.id || '');
  const [csvContent, setCsvContent] = useState('');
  const [parsedTxs, setParsedTxs] = useState<Transaction[]>([]);
  const [parseErrors, setParseErrors] = useState<string[]>([]);
  const [successMessage, setSuccessMessage] = useState('');

  if (!isOpen) return null;

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      const text = event.target?.result as string;
      setCsvContent(text);
      previewParse(text);
    };
    reader.readAsText(file);
  };

  const previewParse = (content: string) => {
    const { transactions: parsed, errors } = parseBankStatement(content, bankType, targetWalletId);
    setParsedTxs(parsed);
    setParseErrors(errors);
  };

  const handleConfirmImport = () => {
    if (parsedTxs.length === 0) return;
    importTransactions(parsedTxs);
    setSuccessMessage(`Успешно импортировано ${parsedTxs.length} транзакций!`);
    setTimeout(() => {
      onClose();
    }, 1500);
  };

  const handleDownloadCSV = () => {
    const csvData = exportTransactionsToCSV(transactions);
    const blob = new Blob(['\uFEFF' + csvData], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `FinanceAnalyzer_Transactions_${new Date().toISOString().split('T')[0]}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handleDownloadJSONBackup = () => {
    const backup = {
      version: '1.0',
      date: new Date().toISOString(),
      transactions,
      wallets,
      categories,
      achievements,
    };
    const blob = new Blob([JSON.stringify(backup, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `FinanceAnalyzer_Backup_${new Date().toISOString().split('T')[0]}.json`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4 overflow-y-auto">
      <div className="bg-white rounded-3xl w-full max-w-xl shadow-2xl overflow-hidden my-6 animate-in fade-in zoom-in-95 duration-200">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 bg-slate-50">
          <div className="flex items-center gap-2">
            <FileSpreadsheet size={20} className="text-emerald-600" />
            <h2 className="text-base font-extrabold text-slate-900">Импорт и экспорт данных</h2>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-slate-600 rounded-lg"
          >
            <X size={18} />
          </button>
        </div>

        {/* Tab switcher */}
        <div className="flex border-b border-slate-100 bg-slate-50/50 text-xs font-bold text-slate-600">
          <button
            onClick={() => setActiveTab('import')}
            className={`flex-1 py-3 border-b-2 text-center transition-colors ${
              activeTab === 'import'
                ? 'border-emerald-600 text-emerald-600 bg-white font-black'
                : 'border-transparent hover:text-slate-900'
            }`}
          >
            Импорт выписки банка
          </button>
          <button
            onClick={() => setActiveTab('export')}
            className={`flex-1 py-3 border-b-2 text-center transition-colors ${
              activeTab === 'export'
                ? 'border-emerald-600 text-emerald-600 bg-white font-black'
                : 'border-transparent hover:text-slate-900'
            }`}
          >
            Экспорт в CSV
          </button>
          <button
            onClick={() => setActiveTab('backup')}
            className={`flex-1 py-3 border-b-2 text-center transition-colors ${
              activeTab === 'backup'
                ? 'border-emerald-600 text-emerald-600 bg-white font-black'
                : 'border-transparent hover:text-slate-900'
            }`}
          >
            Резервная копия
          </button>
        </div>

        <div className="p-6 space-y-5 max-h-[70vh] overflow-y-auto">
          {successMessage && (
            <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-xl text-xs font-bold text-emerald-800 flex items-center gap-2">
              <Check size={16} /> {successMessage}
            </div>
          )}

          {/* TAB 1: BANK IMPORT */}
          {activeTab === 'import' && (
            <div className="space-y-4">
              <p className="text-xs text-slate-500 leading-relaxed">
                Вы можете загрузить CSV-выписку по счету из мобильного банка. Приложение
                автоматически распознает суммы, категории и даты.
              </p>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-500 mb-1">
                    Формат банка
                  </label>
                  <select
                    value={bankType}
                    onChange={(e) => {
                      setBankType(e.target.value as any);
                      if (csvContent) previewParse(csvContent);
                    }}
                    className="w-full text-xs font-semibold px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
                  >
                    <option value="tinkoff">Тинькофф (Т-Банк)</option>
                    <option value="sberbank">Сбербанк</option>
                    <option value="alfabank">Альфа-Банк</option>
                    <option value="generic">Стандартный CSV</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-500 mb-1">
                    Зачислить на счет
                  </label>
                  <select
                    value={targetWalletId}
                    onChange={(e) => setTargetWalletId(e.target.value)}
                    className="w-full text-xs font-semibold px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl truncate"
                  >
                    {wallets.map((w) => (
                      <option key={w.id} value={w.id}>
                        {w.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Upload Drop area */}
              <div className="border-2 border-dashed border-slate-200 rounded-2xl p-6 text-center hover:border-emerald-500 transition-colors bg-slate-50/50">
                <input
                  type="file"
                  accept=".csv,.txt"
                  id="csv_file_input"
                  onChange={handleFileUpload}
                  className="hidden"
                />
                <label
                  htmlFor="csv_file_input"
                  className="cursor-pointer flex flex-col items-center justify-center gap-2"
                >
                  <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
                    <Upload size={20} />
                  </div>
                  <div>
                    <span className="text-xs font-bold text-slate-700">
                      Выберите или перетащите файл .CSV
                    </span>
                    <span className="text-[11px] text-slate-400 block mt-0.5">
                      Выгрузите выписку из онлайн-банка
                    </span>
                  </div>
                </label>
              </div>

              {/* Textarea for direct paste */}
              <div>
                <label className="block text-xs font-semibold text-slate-500 mb-1">
                  Или вставьте текст выписки:
                </label>
                <textarea
                  rows={3}
                  value={csvContent}
                  onChange={(e) => {
                    setCsvContent(e.target.value);
                    previewParse(e.target.value);
                  }}
                  placeholder="Вставьте строки CSV сюда..."
                  className="w-full text-[11px] font-mono p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none"
                />
              </div>

              {/* Parse Preview */}
              {parsedTxs.length > 0 && (
                <div className="p-3 bg-slate-50 border border-slate-200 rounded-xl space-y-2">
                  <div className="flex items-center justify-between text-xs font-bold text-slate-700">
                    <span>Найдено транзакций для импорта: {parsedTxs.length}</span>
                    <button
                      onClick={handleConfirmImport}
                      className="px-3 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-bold shadow-xs"
                    >
                      Импортировать все
                    </button>
                  </div>
                  <div className="max-h-32 overflow-y-auto divide-y divide-slate-200/60 text-[11px]">
                    {parsedTxs.slice(0, 5).map((t, idx) => (
                      <div key={idx} className="py-1 flex justify-between text-slate-600">
                        <span>
                          {t.date} • {t.category} ({t.note})
                        </span>
                        <span className="font-bold">
                          {formatCurrency(t.amount, currency.symbol)}
                        </span>
                      </div>
                    ))}
                    {parsedTxs.length > 5 && (
                      <div className="py-1 text-slate-400 italic">
                        И еще {parsedTxs.length - 5} транзакций...
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* TAB 2: EXPORT CSV */}
          {activeTab === 'export' && (
            <div className="space-y-4">
              <div className="p-4 bg-emerald-50/50 border border-emerald-200 rounded-2xl flex items-center justify-between">
                <div>
                  <h4 className="text-xs font-bold text-slate-900">
                    Экспорт всех операций в CSV
                  </h4>
                  <p className="text-[11px] text-slate-500 mt-0.5">
                    Файл содержит {transactions.length} записей с суммами, категориями и датами
                  </p>
                </div>
                <button
                  onClick={handleDownloadCSV}
                  className="flex items-center gap-1.5 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold rounded-xl shadow-xs"
                >
                  <Download size={15} /> Скачать CSV
                </button>
              </div>
            </div>
          )}

          {/* TAB 3: BACKUP / RESTORE */}
          {activeTab === 'backup' && (
            <div className="space-y-4">
              <div className="p-4 bg-slate-50 border border-slate-200 rounded-2xl flex items-center justify-between">
                <div>
                  <h4 className="text-xs font-bold text-slate-900">
                    Резервная копия данных (JSON)
                  </h4>
                  <p className="text-[11px] text-slate-500 mt-0.5">
                    Сохраняет все транзакции, счета, категории и достижения
                  </p>
                </div>
                <button
                  onClick={handleDownloadJSONBackup}
                  className="flex items-center gap-1.5 px-4 py-2 bg-slate-800 hover:bg-slate-900 text-white text-xs font-bold rounded-xl shadow-xs"
                >
                  <Database size={15} /> Скачать бэкап
                </button>
              </div>

              <div className="pt-2 border-t border-slate-100">
                <h4 className="text-xs font-bold text-slate-700 mb-2">Демонстрационные данные</h4>
                <p className="text-[11px] text-slate-500 mb-3">
                  Вы можете восстановить пример данных с готовыми категориями, счетами и графиками:
                </p>
                <button
                  onClick={() => {
                    loadDemoData();
                    setSuccessMessage('Демо-данные успешно загружены!');
                  }}
                  className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold rounded-xl transition-colors"
                >
                  Загрузить пример данных
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
