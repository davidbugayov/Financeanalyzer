import React, { useState, useRef, useEffect, useCallback } from 'react';
import { Plus, ArrowDownLeft, ArrowUpRight, ArrowRightLeft, X } from 'lucide-react';
import { TransactionType } from '../types';

interface ExpandableFABProps {
  onOpenDefault: () => void;
  onSelectType: (type: TransactionType) => void;
}

export const ExpandableFAB: React.FC<ExpandableFABProps> = ({
  onOpenDefault,
  onSelectType,
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const [isPressing, setIsPressing] = useState(false);

  const longPressTimerRef = useRef<NodeJS.Timeout | null>(null);
  const isLongPressTriggeredRef = useRef<boolean>(false);
  const pointerStartPosRef = useRef<{ x: number; y: number }>({ x: 0, y: 0 });

  const clearLongPressTimer = useCallback(() => {
    if (longPressTimerRef.current) {
      clearTimeout(longPressTimerRef.current);
      longPressTimerRef.current = null;
    }
    setIsPressing(false);
  }, []);

  // Handle pointer down (mouse, pen, or touch)
  const handlePointerDown = (e: React.PointerEvent<HTMLButtonElement>) => {
    // Only primary button triggers long-press (right click is handled by onContextMenu)
    if (e.button !== 0) return;

    isLongPressTriggeredRef.current = false;
    pointerStartPosRef.current = { x: e.clientX, y: e.clientY };
    setIsPressing(true);

    longPressTimerRef.current = setTimeout(() => {
      isLongPressTriggeredRef.current = true;
      setIsPressing(false);

      // Trigger light haptic feedback on mobile if supported
      if (typeof navigator !== 'undefined' && 'vibrate' in navigator) {
        try {
          navigator.vibrate(40);
        } catch {
          // Ignore vibration errors
        }
      }

      setIsOpen((prev) => !prev);
    }, 450); // 450ms long-press threshold
  };

  // If user moves pointer (e.g. scrolls), cancel long-press
  const handlePointerMove = (e: React.PointerEvent<HTMLButtonElement>) => {
    if (!longPressTimerRef.current) return;
    const distance = Math.hypot(
      e.clientX - pointerStartPosRef.current.x,
      e.clientY - pointerStartPosRef.current.y
    );
    if (distance > 10) {
      clearLongPressTimer();
    }
  };

  // Pointer release / cancel
  const handlePointerUp = () => {
    clearLongPressTimer();
  };

  const handlePointerCancel = () => {
    clearLongPressTimer();
  };

  // Right-click desktop context menu handler
  const handleContextMenu = (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();
    clearLongPressTimer();
    isLongPressTriggeredRef.current = true;
    setIsOpen((prev) => !prev);
  };

  // Standard click handler
  const handleClick = (e: React.MouseEvent<HTMLButtonElement>) => {
    // If long-press just fired, ignore the standard click event
    if (isLongPressTriggeredRef.current) {
      isLongPressTriggeredRef.current = false;
      return;
    }

    // If menu is currently open, clicking the main button simply toggles/closes it
    if (isOpen) {
      setIsOpen(false);
      return;
    }

    // Otherwise standard click opens default modal
    onOpenDefault();
  };

  // Close on Escape key
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isOpen) {
        setIsOpen(false);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen]);

  // Clean up timer on unmount
  useEffect(() => {
    return () => {
      if (longPressTimerRef.current) {
        clearTimeout(longPressTimerRef.current);
      }
    };
  }, []);

  const handleActionSelect = (type: TransactionType) => {
    setIsOpen(false);
    onSelectType(type);
  };

  return (
    <>
      {/* Backdrop overlay when menu is expanded */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/25 backdrop-blur-[2px] transition-opacity animate-in fade-in duration-200"
          onClick={() => setIsOpen(false)}
          aria-hidden="true"
        />
      )}

      {/* Speed Dial / Expandable Menu Container */}
      <div className="fixed bottom-20 right-4 sm:bottom-20 sm:right-6 z-40 flex flex-col items-end gap-3 pointer-events-none">
        {/* Expanded Options List */}
        <div
          className={`flex flex-col items-end gap-2.5 transition-all duration-200 ease-out origin-bottom-right ${
            isOpen
              ? 'opacity-100 scale-100 translate-y-0 pointer-events-auto'
              : 'opacity-0 scale-90 translate-y-4 pointer-events-none'
          }`}
          role="menu"
          aria-orientation="vertical"
          aria-label="Быстрый выбор типа операции"
        >
          {/* Quick Header Badge */}
          <div className="text-[11px] font-extrabold uppercase tracking-wider text-slate-500 bg-white/90 backdrop-blur-md px-3 py-1 rounded-full shadow-xs border border-slate-200/80 mb-0.5 select-none">
            Тип операции
          </div>

          {/* Option: Add Expense (Расход) */}
          <button
            type="button"
            role="menuitem"
            id="fab_add_expense_btn"
            onClick={() => handleActionSelect('expense')}
            className="group flex items-center gap-2.5 cursor-pointer active:scale-95 transition-all"
            title="Добавить расход"
          >
            <span className="bg-white/95 backdrop-blur-md text-slate-800 text-xs font-bold px-3 py-1.5 rounded-xl shadow-md border border-slate-100 group-hover:bg-rose-50 group-hover:text-rose-700 transition-colors select-none">
              Расход
            </span>
            <div className="w-11 h-11 rounded-full bg-rose-600 hover:bg-rose-500 text-white shadow-lg shadow-rose-600/35 flex items-center justify-center transition-transform group-hover:scale-110">
              <ArrowUpRight size={20} className="stroke-[2.5]" />
            </div>
          </button>

          {/* Option: Add Income (Доход) */}
          <button
            type="button"
            role="menuitem"
            id="fab_add_income_btn"
            onClick={() => handleActionSelect('income')}
            className="group flex items-center gap-2.5 cursor-pointer active:scale-95 transition-all"
            title="Добавить доход"
          >
            <span className="bg-white/95 backdrop-blur-md text-slate-800 text-xs font-bold px-3 py-1.5 rounded-xl shadow-md border border-slate-100 group-hover:bg-emerald-50 group-hover:text-emerald-700 transition-colors select-none">
              Доход
            </span>
            <div className="w-11 h-11 rounded-full bg-emerald-600 hover:bg-emerald-500 text-white shadow-lg shadow-emerald-600/35 flex items-center justify-center transition-transform group-hover:scale-110">
              <ArrowDownLeft size={20} className="stroke-[2.5]" />
            </div>
          </button>

          {/* Option: Transfer (Перевод) */}
          <button
            type="button"
            role="menuitem"
            id="fab_add_transfer_btn"
            onClick={() => handleActionSelect('transfer')}
            className="group flex items-center gap-2.5 cursor-pointer active:scale-95 transition-all"
            title="Перевод между счетами"
          >
            <span className="bg-white/95 backdrop-blur-md text-slate-800 text-xs font-bold px-3 py-1.5 rounded-xl shadow-md border border-slate-100 group-hover:bg-blue-50 group-hover:text-blue-700 transition-colors select-none">
              Перевод
            </span>
            <div className="w-11 h-11 rounded-full bg-blue-600 hover:bg-blue-500 text-white shadow-lg shadow-blue-600/35 flex items-center justify-center transition-transform group-hover:scale-110">
              <ArrowRightLeft size={18} className="stroke-[2.5]" />
            </div>
          </button>
        </div>

        {/* Master FAB Trigger Button */}
        <div className="relative pointer-events-auto">
          {/* Long-press holding indicator ring animation */}
          {isPressing && (
            <div className="absolute inset-0 -m-1.5 rounded-full border-2 border-emerald-400 animate-ping pointer-events-none" />
          )}

          <button
            id="global_fab_add_tx"
            type="button"
            onPointerDown={handlePointerDown}
            onPointerMove={handlePointerMove}
            onPointerUp={handlePointerUp}
            onPointerCancel={handlePointerCancel}
            onContextMenu={handleContextMenu}
            onClick={handleClick}
            aria-expanded={isOpen}
            aria-haspopup="true"
            aria-label={isOpen ? 'Закрыть меню' : 'Добавить операцию (зажмите для выбора)'}
            title={
              isOpen
                ? 'Закрыть меню'
                : 'Нажмите для добавления. Зажмите или нажмите правой кнопкой для выбора Доход / Расход'
            }
            className={`flex items-center justify-center w-14 h-14 rounded-full text-white shadow-xl hover:shadow-2xl transition-all duration-200 focus:outline-none focus:ring-4 focus:ring-emerald-400/40 cursor-pointer select-none active:scale-95 ${
              isOpen
                ? 'bg-slate-800 hover:bg-slate-700 shadow-slate-900/30'
                : 'bg-emerald-600 hover:bg-emerald-500 hover:scale-105 shadow-emerald-950/30'
            }`}
          >
            <div
              className={`transition-transform duration-200 shrink-0 ${
                isOpen ? 'rotate-45' : 'rotate-0'
              }`}
            >
              {isOpen ? (
                <X size={24} className="stroke-[2.8]" />
              ) : (
                <Plus size={24} className="stroke-[2.8]" />
              )}
            </div>
          </button>
        </div>
      </div>
    </>
  );
};
