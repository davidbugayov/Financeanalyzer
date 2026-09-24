import React, { useState, useRef, useEffect } from 'react';
import { Calendar, ChevronDown, Check, X, ArrowRight, RotateCcw } from 'lucide-react';

export type DatePresetKey =
  | 'all'
  | 'today'
  | 'yesterday'
  | 'last7'
  | 'last30'
  | 'this_month'
  | 'last_month'
  | 'this_year'
  | 'custom';

export interface DateRange {
  startDate: string; // YYYY-MM-DD or empty
  endDate: string; // YYYY-MM-DD or empty
  preset: DatePresetKey;
}

interface DateRangePickerProps {
  dateRange: DateRange;
  onChange: (range: DateRange) => void;
  className?: string;
}

function formatDateToIso(d: Date): string {
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function computePresetRange(preset: DatePresetKey): { startDate: string; endDate: string } {
  const now = new Date();
  const todayIso = formatDateToIso(now);

  switch (preset) {
    case 'today':
      return { startDate: todayIso, endDate: todayIso };

    case 'yesterday': {
      const yesterday = new Date(now);
      yesterday.setDate(now.getDate() - 1);
      const yesterdayIso = formatDateToIso(yesterday);
      return { startDate: yesterdayIso, endDate: yesterdayIso };
    }

    case 'last7': {
      const start = new Date(now);
      start.setDate(now.getDate() - 6);
      return { startDate: formatDateToIso(start), endDate: todayIso };
    }

    case 'last30': {
      const start = new Date(now);
      start.setDate(now.getDate() - 29);
      return { startDate: formatDateToIso(start), endDate: todayIso };
    }

    case 'this_month': {
      const start = new Date(now.getFullYear(), now.getMonth(), 1);
      const end = new Date(now.getFullYear(), now.getMonth() + 1, 0);
      return { startDate: formatDateToIso(start), endDate: formatDateToIso(end) };
    }

    case 'last_month': {
      const start = new Date(now.getFullYear(), now.getMonth() - 1, 1);
      const end = new Date(now.getFullYear(), now.getMonth(), 0);
      return { startDate: formatDateToIso(start), endDate: formatDateToIso(end) };
    }

    case 'this_year': {
      const start = new Date(now.getFullYear(), 0, 1);
      const end = new Date(now.getFullYear(), 11, 31);
      return { startDate: formatDateToIso(start), endDate: formatDateToIso(end) };
    }

    case 'all':
    default:
      return { startDate: '', endDate: '' };
  }
}

export function formatHumanDateRange(range: DateRange): string {
  switch (range.preset) {
    case 'today':
      return 'Сегодня';
    case 'yesterday':
      return 'Вчера';
    case 'last7':
      return 'Последние 7 дней';
    case 'last30':
      return 'Последние 30 дней';
    case 'this_month':
      return 'Этот месяц';
    case 'last_month':
      return 'Прошлый месяц';
    case 'this_year':
      return 'Этот год';
    case 'custom': {
      if (range.startDate && range.endDate) {
        if (range.startDate === range.endDate) {
          return range.startDate;
        }
        return `${range.startDate} – ${range.endDate}`;
      }
      if (range.startDate) return `С ${range.startDate}`;
      if (range.endDate) return `По ${range.endDate}`;
      return 'Период';
    }
    case 'all':
    default:
      return 'Все время';
  }
}

export const DateRangePicker: React.FC<DateRangePickerProps> = ({
  dateRange,
  onChange,
  className = '',
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const [customStart, setCustomStart] = useState(dateRange.startDate);
  const [customEnd, setCustomEnd] = useState(dateRange.endDate);
  const dropdownRef = useRef<HTMLDivElement>(null);

  // Sync custom input dates when range prop changes
  useEffect(() => {
    setCustomStart(dateRange.startDate);
    setCustomEnd(dateRange.endDate);
  }, [dateRange.startDate, dateRange.endDate]);

  // Click outside to close
  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    }
    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
      return () => document.removeEventListener('mousedown', handleClickOutside);
    }
  }, [isOpen]);

  const handleSelectPreset = (preset: DatePresetKey) => {
    if (preset === 'custom') {
      // Leave custom fields open
      return;
    }
    const { startDate, endDate } = computePresetRange(preset);
    onChange({
      preset,
      startDate,
      endDate,
    });
    setIsOpen(false);
  };

  const handleApplyCustom = () => {
    onChange({
      preset: 'custom',
      startDate: customStart,
      endDate: customEnd,
    });
    setIsOpen(false);
  };

  const handleReset = () => {
    onChange({
      preset: 'all',
      startDate: '',
      endDate: '',
    });
    setCustomStart('');
    setCustomEnd('');
    setIsOpen(false);
  };

  const isFiltered = dateRange.preset !== 'all' || dateRange.startDate || dateRange.endDate;

  const presets: { key: DatePresetKey; label: string }[] = [
    { key: 'all', label: 'Все время' },
    { key: 'today', label: 'Сегодня' },
    { key: 'yesterday', label: 'Вчера' },
    { key: 'last7', label: 'Последние 7 дней' },
    { key: 'last30', label: 'Последние 30 дней' },
    { key: 'this_month', label: 'Этот месяц' },
    { key: 'last_month', label: 'Прошлый месяц' },
    { key: 'this_year', label: 'Этот год' },
  ];

  return (
    <div className={`relative inline-block text-left ${className}`} ref={dropdownRef}>
      {/* Trigger Button */}
      <button
        id="history_date_range_picker_btn"
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className={`flex items-center gap-2 px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer border shadow-2xs ${
          isFiltered
            ? 'bg-emerald-50 text-emerald-900 border-emerald-300 ring-1 ring-emerald-400'
            : 'bg-white text-slate-700 hover:bg-slate-50 border-slate-200'
        }`}
      >
        <Calendar
          size={14}
          className={isFiltered ? 'text-emerald-600 stroke-[2.2]' : 'text-slate-400'}
        />
        <span className="truncate max-w-[170px] sm:max-w-[220px]">
          {formatHumanDateRange(dateRange)}
        </span>
        {isFiltered && (
          <span className="w-1.5 h-1.5 rounded-full bg-emerald-600 shrink-0" />
        )}
        <ChevronDown
          size={13}
          className={`transition-transform text-slate-400 ${isOpen ? 'rotate-180' : ''}`}
        />
      </button>

      {/* Popover Dropdown */}
      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 sm:w-88 bg-white rounded-2xl shadow-xl border border-slate-200 p-4 z-50 animate-in fade-in zoom-in-95 duration-150">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100">
            <div className="flex items-center gap-2">
              <Calendar size={16} className="text-emerald-600" />
              <h4 className="text-xs font-black text-slate-900 uppercase tracking-wider">
                Период операций
              </h4>
            </div>
            {isFiltered && (
              <button
                type="button"
                onClick={handleReset}
                className="text-[11px] font-bold text-slate-500 hover:text-rose-600 flex items-center gap-1 transition-colors cursor-pointer"
              >
                <RotateCcw size={11} /> Сбросить
              </button>
            )}
          </div>

          {/* Quick Preset Buttons */}
          <div className="grid grid-cols-2 gap-1.5 py-3 border-b border-slate-100">
            {presets.map((p) => {
              const isSelected = dateRange.preset === p.key;
              return (
                <button
                  key={p.key}
                  id={`date_preset_${p.key}`}
                  type="button"
                  onClick={() => handleSelectPreset(p.key)}
                  className={`px-2.5 py-1.5 rounded-lg text-xs font-semibold flex items-center justify-between text-left transition-all cursor-pointer ${
                    isSelected
                      ? 'bg-emerald-600 text-white font-bold shadow-2xs'
                      : 'bg-slate-50 hover:bg-slate-100 text-slate-700'
                  }`}
                >
                  <span className="truncate">{p.label}</span>
                  {isSelected && <Check size={12} className="shrink-0" />}
                </button>
              );
            })}
          </div>

          {/* Custom Date Range Section */}
          <div className="pt-3">
            <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-2">
              Произвольный диапазон дат
            </span>
            <div className="grid grid-cols-2 gap-2 mb-3">
              <div>
                <label className="block text-[10px] font-semibold text-slate-500 mb-0.5">
                  С даты
                </label>
                <input
                  id="date_range_start_input"
                  type="date"
                  value={customStart}
                  onChange={(e) => setCustomStart(e.target.value)}
                  className="w-full text-xs px-2 py-1.5 bg-slate-50 border border-slate-200 rounded-lg focus:bg-white focus:outline-none focus:ring-1 focus:ring-emerald-500 font-medium"
                />
              </div>

              <div>
                <label className="block text-[10px] font-semibold text-slate-500 mb-0.5">
                  По дату
                </label>
                <input
                  id="date_range_end_input"
                  type="date"
                  value={customEnd}
                  onChange={(e) => setCustomEnd(e.target.value)}
                  className="w-full text-xs px-2 py-1.5 bg-slate-50 border border-slate-200 rounded-lg focus:bg-white focus:outline-none focus:ring-1 focus:ring-emerald-500 font-medium"
                />
              </div>
            </div>

            <div className="flex items-center justify-end gap-2">
              <button
                type="button"
                onClick={() => setIsOpen(false)}
                className="px-3 py-1.5 text-xs font-bold text-slate-500 hover:text-slate-700 rounded-lg hover:bg-slate-100 cursor-pointer"
              >
                Отмена
              </button>
              <button
                id="apply_custom_date_range_btn"
                type="button"
                onClick={handleApplyCustom}
                disabled={!customStart && !customEnd}
                className="px-3.5 py-1.5 text-xs font-bold bg-emerald-600 hover:bg-emerald-700 disabled:opacity-40 disabled:hover:bg-emerald-600 text-white rounded-lg transition-colors shadow-2xs flex items-center gap-1 cursor-pointer"
              >
                <span>Применить</span>
                <ArrowRight size={12} />
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
