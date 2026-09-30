import React, { useState } from 'react';
import {
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
  Tooltip,
  Sector,
} from 'recharts';
import { formatCurrency } from '../utils/financeCalculations';
import { DynamicIcon } from '../utils/iconHelper';

export interface CategoryChartItem {
  name: string;
  categoryId?: string;
  value: number;
  color: string;
  icon?: string;
  percentage: number;
  periodLimit?: number;
  budgetPercent?: number | null;
  isOverBudget?: boolean;
}

interface CategoryDonutChartProps {
  data: CategoryChartItem[];
  totalExpense: number;
  currencySymbol: string;
  periodLabel?: string;
  onSelectCategory?: (categoryId?: string) => void;
  activeCategoryId?: string | null;
}

// Custom Active Shape to highlight hovered sector smoothly
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const renderActiveShape = (props: any) => {
  const {
    cx,
    cy,
    innerRadius,
    outerRadius,
    startAngle,
    endAngle,
    fill,
  } = props;

  return (
    <g>
      <Sector
        cx={cx}
        cy={cy}
        innerRadius={innerRadius - 2}
        outerRadius={outerRadius + 6}
        startAngle={startAngle}
        endAngle={endAngle}
        fill={fill}
        cornerRadius={5}
        className="transition-all duration-300 drop-shadow-md"
      />
    </g>
  );
};

export const CategoryDonutChart: React.FC<CategoryDonutChartProps> = ({
  data,
  totalExpense,
  currencySymbol,
  periodLabel,
  onSelectCategory,
  activeCategoryId,
}) => {
  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null);

  // Filter only categories with positive spending
  const positiveData = data.filter((c) => c.value > 0);

  // Find index of category matching external activeCategoryId
  const externalActiveIndex = activeCategoryId
    ? positiveData.findIndex((c) => c.categoryId === activeCategoryId || c.name === activeCategoryId)
    : -1;

  const currentActiveIndex = hoveredIndex !== null ? hoveredIndex : externalActiveIndex !== -1 ? externalActiveIndex : null;
  const activeItem = currentActiveIndex !== null && positiveData[currentActiveIndex] ? positiveData[currentActiveIndex] : null;

  return (
    <div className="flex flex-col items-center justify-center w-full">
      {/* Chart container with absolute centered summary */}
      <div className="relative w-full h-72 sm:h-80 flex items-center justify-center">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={positiveData}
              cx="50%"
              cy="50%"
              innerRadius={72}
              outerRadius={105}
              paddingAngle={3}
              dataKey="value"
              activeIndex={currentActiveIndex !== null ? currentActiveIndex : undefined}
              activeShape={renderActiveShape}
              onMouseEnter={(_, index) => setHoveredIndex(index)}
              onMouseLeave={() => setHoveredIndex(null)}
              onClick={(entry) => onSelectCategory && onSelectCategory(entry.categoryId)}
              className="cursor-pointer outline-hidden"
            >
              {positiveData.map((entry, index) => {
                const isSelected = currentActiveIndex === index;
                return (
                  <Cell
                    key={`cell-${index}`}
                    fill={entry.color}
                    stroke={isSelected ? '#ffffff' : entry.color}
                    strokeWidth={isSelected ? 2 : 1}
                    className="transition-all duration-200"
                  />
                );
              })}
            </Pie>

            <Tooltip
              content={({ active, payload }) => {
                if (!active || !payload || !payload.length) return null;
                const item = payload[0].payload as CategoryChartItem;
                return (
                  <div className="bg-slate-900 text-white px-3.5 py-2.5 rounded-2xl shadow-xl border border-slate-700/80 text-xs space-y-1 z-50 pointer-events-none">
                    <div className="flex items-center gap-2">
                      <div
                        className="w-3 h-3 rounded-full shrink-0"
                        style={{ backgroundColor: item.color }}
                      />
                      <span className="font-extrabold text-sm">{item.name}</span>
                    </div>
                    <div className="flex items-baseline justify-between gap-4 pt-0.5">
                      <span className="text-slate-400">Сумма:</span>
                      <span className="font-black text-emerald-400">
                        {formatCurrency(item.value, currencySymbol)}
                      </span>
                    </div>
                    <div className="flex items-baseline justify-between gap-4">
                      <span className="text-slate-400">Доля от расходов:</span>
                      <span className="font-bold text-slate-200">{item.percentage}%</span>
                    </div>
                    {item.periodLimit && item.periodLimit > 0 && (
                      <div className="flex items-baseline justify-between gap-4 pt-1 border-t border-slate-800 text-[11px]">
                        <span className="text-slate-400">Лимит бюджета:</span>
                        <span className={item.isOverBudget ? 'text-rose-400 font-bold' : 'text-slate-300'}>
                          {item.budgetPercent}%
                        </span>
                      </div>
                    )}
                  </div>
                );
              }}
            />
          </PieChart>
        </ResponsiveContainer>

        {/* Center Donut Hole Content */}
        <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none p-4 text-center">
          {activeItem ? (
            <div className="animate-in fade-in zoom-in-95 duration-200 flex flex-col items-center max-w-[130px]">
              {activeItem.icon && (
                <div
                  className="w-6 h-6 rounded-lg flex items-center justify-center mb-1 text-white shadow-2xs"
                  style={{ backgroundColor: activeItem.color }}
                >
                  <DynamicIcon name={activeItem.icon} size={13} />
                </div>
              )}
              <span className="text-[11px] font-extrabold text-slate-500 uppercase tracking-wider truncate max-w-[120px]">
                {activeItem.name}
              </span>
              <span className="text-base sm:text-lg font-black text-slate-900 whitespace-nowrap leading-tight mt-0.5">
                {formatCurrency(activeItem.value, currencySymbol)}
              </span>
              <span
                className="text-[11px] font-bold px-1.5 py-0.2 rounded-md mt-0.5 text-white"
                style={{ backgroundColor: activeItem.color }}
              >
                {activeItem.percentage}%
              </span>
            </div>
          ) : (
            <div className="flex flex-col items-center">
              <span className="text-[10px] sm:text-[11px] font-bold uppercase tracking-wider text-slate-400">
                Всего расходов
              </span>
              <span className="text-base sm:text-xl font-black text-slate-900 whitespace-nowrap leading-tight my-0.5">
                {formatCurrency(totalExpense, currencySymbol)}
              </span>
              <span className="text-[10px] font-semibold text-slate-400">
                {positiveData.length}{' '}
                {positiveData.length === 1
                  ? 'категория'
                  : positiveData.length > 1 && positiveData.length < 5
                  ? 'категории'
                  : 'категорий'}
              </span>
            </div>
          )}
        </div>
      </div>

      {/* Subtitle / Interactive Guide */}
      <div className="flex items-center justify-between w-full px-2 mt-1 text-[11px] text-slate-400">
        <span>{periodLabel || 'Распределение трат по категориям'}</span>
        <span className="font-semibold text-slate-500">
          Наведите на сектор для деталей
        </span>
      </div>

      {/* Interactive Category Chips below Donut */}
      <div className="flex flex-wrap items-center justify-center gap-1.5 pt-3 w-full">
        {positiveData.slice(0, 6).map((item, idx) => {
          const isHovered = currentActiveIndex === idx;
          return (
            <button
              key={item.name}
              onMouseEnter={() => setHoveredIndex(idx)}
              onMouseLeave={() => setHoveredIndex(null)}
              onClick={() => onSelectCategory && onSelectCategory(item.categoryId)}
              className={`px-2.5 py-1 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5 cursor-pointer border ${
                isHovered
                  ? 'bg-slate-900 text-white border-slate-900 shadow-xs scale-105'
                  : 'bg-slate-50 text-slate-700 border-slate-200/80 hover:bg-slate-100'
              }`}
            >
              <span
                className="w-2 h-2 rounded-full shrink-0"
                style={{ backgroundColor: item.color }}
              />
              <span className="truncate max-w-[90px]">{item.name}</span>
              <span className={`text-[10px] ${isHovered ? 'text-slate-300' : 'text-slate-400'}`}>
                {item.percentage}%
              </span>
            </button>
          );
        })}
      </div>
    </div>
  );
};
