import React, { useState } from 'react';
import { Trophy, X, Check, Lock, Sparkles, Award } from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { Achievement, AchievementCategory, AchievementRarity } from '../types';
import { DynamicIcon } from '../utils/iconHelper';

interface AchievementsModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const RARITY_STYLES: Record<AchievementRarity, { badge: string; text: string; bg: string }> = {
  COMMON: {
    badge: 'bg-slate-100 text-slate-700 border-slate-200',
    text: 'Обычное',
    bg: 'bg-slate-500',
  },
  RARE: {
    badge: 'bg-blue-50 text-blue-700 border-blue-200',
    text: 'Редкое',
    bg: 'bg-blue-500',
  },
  EPIC: {
    badge: 'bg-purple-50 text-purple-700 border-purple-200',
    text: 'Эпическое',
    bg: 'bg-purple-600',
  },
  LEGENDARY: {
    badge: 'bg-amber-50 text-amber-700 border-amber-200',
    text: 'Легендарное',
    bg: 'bg-gradient-to-r from-amber-500 to-yellow-400',
  },
};

export const AchievementsModal: React.FC<AchievementsModalProps> = ({ isOpen, onClose }) => {
  const { achievements } = useFinance();
  const [filterCat, setFilterCat] = useState<AchievementCategory | 'ALL'>('ALL');

  if (!isOpen) return null;

  const unlockedCount = achievements.filter((a) => a.isUnlocked).length;
  const progressPercent = Math.round((unlockedCount / achievements.length) * 100);

  const filtered = achievements.filter((a) => (filterCat === 'ALL' ? true : a.category === filterCat));

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4 overflow-y-auto">
      <div className="bg-white rounded-3xl w-full max-w-xl shadow-2xl overflow-hidden my-6 animate-in fade-in zoom-in-95 duration-200">
        {/* Header with progress summary */}
        <div className="p-6 bg-gradient-to-r from-amber-500 via-amber-600 to-yellow-500 text-white relative">
          <button
            onClick={onClose}
            className="absolute top-4 right-4 text-white/80 hover:text-white p-1.5 rounded-full hover:bg-black/10 transition-colors"
          >
            <X size={20} />
          </button>

          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-2xl bg-white/20 backdrop-blur-md flex items-center justify-center shadow-inner">
              <Trophy size={28} className="text-white" />
            </div>
            <div>
              <h2 className="text-xl font-black">Финансовые достижения</h2>
              <p className="text-xs text-amber-100 mt-0.5">
                Открыто {unlockedCount} из {achievements.length} наград ({progressPercent}%)
              </p>
            </div>
          </div>

          <div className="mt-4 h-2 w-full bg-black/20 rounded-full overflow-hidden">
            <div
              className="h-full bg-white rounded-full transition-all duration-500"
              style={{ width: `${progressPercent}%` }}
            />
          </div>
        </div>

        {/* Category Filters */}
        <div className="flex gap-1.5 p-3 bg-slate-50 border-b border-slate-100 overflow-x-auto text-xs font-bold text-slate-600">
          {(
            [
              { id: 'ALL', label: 'Все' },
              { id: 'GENERAL', label: 'Общие' },
              { id: 'TRANSACTIONS', label: 'Транзакции' },
              { id: 'BUDGET', label: 'Бюджет' },
              { id: 'ANALYTICS', label: 'Аналитика' },
              { id: 'SECURITY', label: 'Безопасность' },
            ] as const
          ).map((c) => (
            <button
              key={c.id}
              onClick={() => setFilterCat(c.id as any)}
              className={`px-3 py-1.5 rounded-xl whitespace-nowrap transition-colors ${
                filterCat === c.id
                  ? 'bg-amber-600 text-white shadow-xs'
                  : 'bg-white hover:bg-slate-200/70 border border-slate-200'
              }`}
            >
              {c.label}
            </button>
          ))}
        </div>

        {/* List of achievements */}
        <div className="p-5 max-h-[60vh] overflow-y-auto divide-y divide-slate-100 space-y-1">
          {filtered.map((ach) => {
            const rarity = RARITY_STYLES[ach.rarity];
            const isDone = ach.isUnlocked;

            return (
              <div
                key={ach.id}
                className={`py-3.5 flex items-start gap-4 transition-colors ${
                  isDone ? 'opacity-100' : 'opacity-60'
                }`}
              >
                <div
                  className={`w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 shadow-xs ${
                    isDone ? rarity.bg + ' text-white' : 'bg-slate-100 text-slate-400'
                  }`}
                >
                  {isDone ? (
                    <DynamicIcon name={ach.icon || 'Trophy'} size={22} />
                  ) : (
                    <Lock size={20} />
                  )}
                </div>

                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <h4 className="text-xs sm:text-sm font-extrabold text-slate-900 truncate">
                      {ach.title}
                    </h4>
                    <span
                      className={`text-[10px] font-bold px-2 py-0.5 rounded-md border ${rarity.badge}`}
                    >
                      {rarity.text}
                    </span>
                    {isDone && (
                      <span className="text-[10px] font-bold text-emerald-600 flex items-center gap-0.5 ml-auto">
                        <Check size={12} /> Открыто
                      </span>
                    )}
                  </div>
                  <p className="text-xs text-slate-500 mt-1 leading-relaxed">
                    {ach.description}
                  </p>

                  {!isDone && ach.targetProgress > 1 && (
                    <div className="mt-2 space-y-1">
                      <div className="flex justify-between text-[11px] font-bold text-slate-400">
                        <span>Прогресс</span>
                        <span>
                          {ach.currentProgress} / {ach.targetProgress}
                        </span>
                      </div>
                      <div className="h-1.5 w-full bg-slate-100 rounded-full overflow-hidden">
                        <div
                          className="h-full bg-amber-500 rounded-full"
                          style={{
                            width: `${Math.min(
                              100,
                              Math.round((ach.currentProgress / ach.targetProgress) * 100)
                            )}%`,
                          }}
                        />
                      </div>
                    </div>
                  )}

                  {isDone && ach.dateUnlocked && (
                    <span className="text-[10px] text-slate-400 mt-1 block">
                      Получено: {ach.dateUnlocked}
                    </span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
