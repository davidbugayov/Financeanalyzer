import React, { useState } from 'react';
import { Lock, Delete, ShieldCheck } from 'lucide-react';
import { useFinance } from '../context/FinanceContext';

export const PinLockScreen: React.FC = () => {
  const { unlockApp } = useFinance();
  const [enteredPin, setEnteredPin] = useState('');
  const [isError, setIsError] = useState(false);

  const handleDigit = (digit: string) => {
    if (enteredPin.length < 4) {
      const next = enteredPin + digit;
      setEnteredPin(next);
      setIsError(false);

      if (next.length === 4) {
        const success = unlockApp(next);
        if (!success) {
          setIsError(true);
          setTimeout(() => {
            setEnteredPin('');
            setIsError(false);
          }, 600);
        }
      }
    }
  };

  const handleDelete = () => {
    setEnteredPin((prev) => prev.slice(0, -1));
    setIsError(false);
  };

  return (
    <div className="fixed inset-0 z-50 bg-slate-900 text-white flex flex-col items-center justify-center p-6 select-none">
      <div className="w-full max-w-xs flex flex-col items-center">
        {/* Lock Icon */}
        <div className="w-16 h-16 rounded-3xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center mb-6 shadow-inner">
          <Lock size={30} />
        </div>

        <h2 className="text-xl font-black mb-1">Finance Analyzer</h2>
        <p className="text-xs text-slate-400 mb-8">Введите 4-значный PIN-код для входа</p>

        {/* 4 Pin Dots */}
        <div className={`flex gap-4 mb-10 ${isError ? 'animate-bounce' : ''}`}>
          {[0, 1, 2, 3].map((idx) => {
            const isFilled = idx < enteredPin.length;
            return (
              <div
                key={idx}
                className={`w-4 h-4 rounded-full transition-all duration-150 ${
                  isError
                    ? 'bg-red-500 scale-110'
                    : isFilled
                    ? 'bg-emerald-400 scale-125'
                    : 'bg-slate-700'
                }`}
              />
            );
          })}
        </div>

        {isError && (
          <p className="text-xs font-bold text-red-400 mb-4 -mt-6">Неверный PIN-код</p>
        )}

        {/* Keypad */}
        <div className="grid grid-cols-3 gap-4 w-full">
          {['1', '2', '3', '4', '5', '6', '7', '8', '9'].map((d) => (
            <button
              key={d}
              id={`pin_digit_${d}`}
              onClick={() => handleDigit(d)}
              className="w-16 h-16 rounded-2xl bg-slate-800/80 hover:bg-slate-700 active:scale-95 text-xl font-bold flex items-center justify-center mx-auto transition-all shadow-md"
            >
              {d}
            </button>
          ))}

          <div />
          <button
            id="pin_digit_0"
            onClick={() => handleDigit('0')}
            className="w-16 h-16 rounded-2xl bg-slate-800/80 hover:bg-slate-700 active:scale-95 text-xl font-bold flex items-center justify-center mx-auto transition-all shadow-md"
          >
            0
          </button>
          <button
            id="pin_delete_btn"
            onClick={handleDelete}
            className="w-16 h-16 rounded-2xl bg-slate-800/40 hover:bg-slate-700/60 active:scale-95 text-slate-400 hover:text-white flex items-center justify-center mx-auto transition-all"
          >
            <Delete size={22} />
          </button>
        </div>
      </div>
    </div>
  );
};
