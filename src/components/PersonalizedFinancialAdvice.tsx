import React, { useState, useMemo } from 'react';
import {
  Lightbulb,
  Sparkles,
  TrendingDown,
  TrendingUp,
  AlertTriangle,
  AlertCircle,
  CheckCircle2,
  SlidersHorizontal,
  PiggyBank,
  ShieldCheck,
  ShieldAlert,
  ArrowRight,
  Filter,
  Check,
  EyeOff,
  ChevronDown,
  ChevronUp,
  Flame,
  Zap,
  Target,
  RefreshCw,
  Wallet as WalletIcon,
  Tag,
} from 'lucide-react';
import { useFinance } from '../context/FinanceContext';
import { PeriodType, Transaction, Category, Wallet } from '../types';
import { formatCurrency } from '../utils/financeCalculations';
import { DynamicIcon } from '../utils/iconHelper';

export interface PersonalizedAdviceItem {
  id: string;
  priority: 'critical' | 'warning' | 'opportunity' | 'strategy';
  category: 'budget' | 'saving' | 'pattern' | 'safety';
  title: string;
  description: string;
  metricLabel?: string;
  metricValue?: string;
  potentialSaving?: number; // In base currency
  categoryObj?: Category;
  action?: {
    type: 'open_budget' | 'go_wallets' | 'go_history';
    label: string;
    categoryId?: string;
  };
  impactScore: number; // 1-100 for sorting
  tags: string[];
}

interface PersonalizedFinancialAdviceProps {
  period: PeriodType;
  filteredTxs: Transaction[];
  prevFilteredTxs: Transaction[];
  onOpenBudgetModal: (categoryId?: string) => void;
}

export const PersonalizedFinancialAdvice: React.FC<PersonalizedFinancialAdviceProps> = ({
  period,
  filteredTxs,
  prevFilteredTxs,
  onOpenBudgetModal,
}) => {
  const { transactions, wallets, categories, categoryBudgets, currency, setActiveTab } = useFinance();

  const [activeFilter, setActiveFilter] = useState<'all' | 'critical' | 'budget' | 'saving' | 'safety'>('all');
  const [completedAdviceIds, setCompletedAdviceIds] = useState<string[]>(() => {
    try {
      const saved = localStorage.getItem('fa_completed_advice_ids');
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });
  const [dismissedAdviceIds, setDismissedAdviceIds] = useState<string[]>(() => {
    try {
      const saved = localStorage.getItem('fa_dismissed_advice_ids');
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });
  const [isExpanded, setIsExpanded] = useState<boolean>(true);
  const [showCompleted, setShowCompleted] = useState<boolean>(false);

  // Period label helper
  const periodLabel = useMemo(() => {
    switch (period) {
      case 'day':
        return 'за сегодня';
      case 'week':
        return 'за неделю';
      case 'month':
        return 'за текущий месяц';
      case 'year':
        return 'за текущий год';
      case 'all':
      default:
        return 'за всё время';
    }
  }, [period]);

  // Deep spending analysis & personalized advice generator
  const adviceList = useMemo(() => {
    const list: PersonalizedAdviceItem[] = [];

    // 1. Calculate general numbers
    const totalIncome = filteredTxs
      .filter((t) => t.type === 'income')
      .reduce((sum, t) => sum + t.amount, 0);

    const totalExpense = filteredTxs
      .filter((t) => t.type === 'expense')
      .reduce((sum, t) => sum + t.amount, 0);

    const prevExpense = prevFilteredTxs
      .filter((t) => t.type === 'expense')
      .reduce((sum, t) => sum + t.amount, 0);

    const categoryExpenseMap: Record<string, { total: number; count: number; name: string }> = {};
    const prevCategoryExpenseMap: Record<string, number> = {};

    filteredTxs.forEach((t) => {
      if (t.type === 'expense') {
        if (!categoryExpenseMap[t.category]) {
          categoryExpenseMap[t.category] = { total: 0, count: 0, name: t.category };
        }
        categoryExpenseMap[t.category].total += t.amount;
        categoryExpenseMap[t.category].count += 1;
      }
    });

    prevFilteredTxs.forEach((t) => {
      if (t.type === 'expense') {
        prevCategoryExpenseMap[t.category] = (prevCategoryExpenseMap[t.category] || 0) + t.amount;
      }
    });

    // Effective budget multipliers for current period
    const getPeriodMultiplier = () => {
      if (period === 'day') return 1 / 30;
      if (period === 'week') return 7 / 30;
      if (period === 'month') return 1;
      if (period === 'year') return 12;
      return 6;
    };
    const periodMultiplier = getPeriodMultiplier();

    // Find category objects for mapping
    const getCatObj = (name: string): Category | undefined =>
      categories.find((c) => c.name.toLowerCase() === name.toLowerCase());

    const getCatBudget = (cat: Category | undefined): number => {
      if (!cat) return 0;
      return categoryBudgets[cat.id] || categoryBudgets[cat.name] || 0;
    };

    // -------------------------------------------------------------------------
    // Rule 1: Budget Overruns (Critical Priority)
    // -------------------------------------------------------------------------
    categories.forEach((cat) => {
      const monthlyBudget = getCatBudget(cat);
      if (monthlyBudget > 0) {
        const periodLimit = Math.round(monthlyBudget * periodMultiplier);
        const spent = categoryExpenseMap[cat.name]?.total || 0;
        if (spent > periodLimit && periodLimit > 0) {
          const overrun = spent - periodLimit;
          const overrunPercent = Math.round((spent / periodLimit) * 100);
          list.push({
            id: `budget_overrun_${cat.id}`,
            priority: 'critical',
            category: 'budget',
            title: `Превышение лимита в «${cat.name}» на ${overrunPercent}%`,
            description: `Вы израсходовали ${formatCurrency(
              spent,
              currency.symbol
            )} при установленном лимите ${formatCurrency(
              periodLimit,
              currency.symbol
            )}. Перерасход составляет ${formatCurrency(overrun, currency.symbol)}.`,
            metricLabel: 'Перерасход',
            metricValue: `+${formatCurrency(overrun, currency.symbol)}`,
            potentialSaving: overrun,
            categoryObj: cat,
            action: {
              type: 'open_budget',
              label: 'Скорректировать лимит',
              categoryId: cat.id,
            },
            impactScore: 95,
            tags: ['Лимит превышен', 'Срочно'],
          });
        } else if (spent >= periodLimit * 0.85 && periodLimit > 0) {
          const remaining = periodLimit - spent;
          const spentPercent = Math.round((spent / periodLimit) * 100);
          list.push({
            id: `budget_near_${cat.id}`,
            priority: 'warning',
            category: 'budget',
            title: `Лимит «${cat.name}» исчерпан на ${spentPercent}%`,
            description: `До конца периода в этой категории осталось всего ${formatCurrency(
              remaining,
              currency.symbol
            )}. Рекомендуем притормозить траты в данной статье, чтобы не выйти в минус.`,
            metricLabel: 'Остаток лимита',
            metricValue: formatCurrency(remaining, currency.symbol),
            categoryObj: cat,
            action: {
              type: 'open_budget',
              label: 'Посмотреть лимит',
              categoryId: cat.id,
            },
            impactScore: 78,
            tags: ['На грани лимита'],
          });
        }
      }
    });

    // -------------------------------------------------------------------------
    // Rule 2: High Expenses in Category Without a Budget Set (Opportunity)
    // -------------------------------------------------------------------------
    if (totalExpense > 0) {
      Object.entries(categoryExpenseMap).forEach(([catName, data]) => {
        const catObj = getCatObj(catName);
        const monthlyBudget = getCatBudget(catObj);
        const share = Math.round((data.total / totalExpense) * 100);

        // If category accounts for > 18% of total expenses and has NO budget set
        if (monthlyBudget === 0 && share >= 18 && data.total > 3000) {
          const suggestedBudget = Math.round((data.total * 0.85) / periodMultiplier);
          const potentialMonthly = Math.round(data.total * 0.15);
          list.push({
            id: `unbudgeted_high_spend_${catName}`,
            priority: 'opportunity',
            category: 'budget',
            title: `Установите лимит на «${catName}» (занимает ${share}% всех трат)`,
            description: `На «${catName}» ушло ${formatCurrency(
              data.total,
              currency.symbol
            )} (${share}% всех трат), но лимит не задан. Установка лимита в ${formatCurrency(
              suggestedBudget,
              currency.symbol
            )}/мес поможет сберегать до ${formatCurrency(potentialMonthly, currency.symbol)} ежемесячно.`,
            metricLabel: 'Потенциал сбережения',
            metricValue: `~${formatCurrency(potentialMonthly, currency.symbol)}/мес`,
            potentialSaving: potentialMonthly,
            categoryObj: catObj,
            action: {
              type: 'open_budget',
              label: 'Задать лимит сейчас',
              categoryId: catObj?.id,
            },
            impactScore: 82,
            tags: ['Крупная статья', 'Рекомендован лимит'],
          });
        }
      });
    }

    // -------------------------------------------------------------------------
    // Rule 3: Significant Growth Compared to Previous Period (Anomaly / Spike)
    // -------------------------------------------------------------------------
    if (prevExpense > 0 && totalExpense > prevExpense * 1.15) {
      const growthAmount = totalExpense - prevExpense;
      const growthPercent = Math.round(((totalExpense - prevExpense) / prevExpense) * 100);

      // Find which category had the highest increase
      let highestGrowthCat = '';
      let maxDiff = 0;
      Object.entries(categoryExpenseMap).forEach(([name, cur]) => {
        const prev = prevCategoryExpenseMap[name] || 0;
        const diff = cur.total - prev;
        if (diff > maxDiff) {
          maxDiff = diff;
          highestGrowthCat = name;
        }
      });

      const catObj = highestGrowthCat ? getCatObj(highestGrowthCat) : undefined;

      list.push({
        id: 'expense_spike_vs_prev',
        priority: 'warning',
        category: 'pattern',
        title: `Рост расходов на +${growthPercent}% к прошлому периоду`,
        description: `Расходы увеличились на ${formatCurrency(
          growthAmount,
          currency.symbol
        )}. Основной драйвер роста — «${highestGrowthCat || 'основные статьи'}» (+${formatCurrency(
          maxDiff,
          currency.symbol
        )} к прошлому периоду). Проанализируйте разовые траты.`,
        metricLabel: 'Прирост трат',
        metricValue: `+${formatCurrency(growthAmount, currency.symbol)}`,
        categoryObj: catObj,
        action: catObj
          ? {
              type: 'open_budget',
              label: `Проверить ${highestGrowthCat}`,
              categoryId: catObj.id,
            }
          : undefined,
        impactScore: 85,
        tags: ['Всплеск расходов', 'Сравнение'],
      });
    }

    // -------------------------------------------------------------------------
    // Rule 4: Financial Balance & Deficit Alert (Critical)
    // -------------------------------------------------------------------------
    if (totalIncome > 0 && totalExpense > totalIncome) {
      const deficit = totalExpense - totalIncome;
      list.push({
        id: 'budget_deficit_alert',
        priority: 'critical',
        category: 'safety',
        title: `Отрицательный баланс: расходы превышают доходы на ${formatCurrency(
          deficit,
          currency.symbol
        )}`,
        description: `В этом периоде вы тратите больше, чем зарабатываете (дефицит ${formatCurrency(
          deficit,
          currency.symbol
        )}). Чтобы не задействовать накопления и кредитные средства, рекомендуем временно сократить дискреционные расходы (кафе, развлечения, покупки).`,
        metricLabel: 'Дефицит бюджета',
        metricValue: `-${formatCurrency(deficit, currency.symbol)}`,
        potentialSaving: deficit,
        action: {
          type: 'open_budget',
          label: 'Оптимизировать лимиты',
        },
        impactScore: 98,
        tags: ['Дефицит', 'Внимание!'],
      });
    }

    // -------------------------------------------------------------------------
    // Rule 5: 50/30/20 Rule Analysis (Needs vs Wants vs Savings)
    // -------------------------------------------------------------------------
    if (totalIncome > 0 && totalExpense > 0) {
      // Classify categories
      const essentialKeywords = [
        'продукт',
        'жиль',
        'коммун',
        'аренд',
        'аптек',
        'здоров',
        'медиц',
        'транспорт',
        'связь',
        'образован',
      ];
      let needsExpense = 0;
      let wantsExpense = 0;

      Object.entries(categoryExpenseMap).forEach(([catName, data]) => {
        const lower = catName.toLowerCase();
        const isEssential = essentialKeywords.some((kw) => lower.includes(kw));
        if (isEssential) {
          needsExpense += data.total;
        } else {
          wantsExpense += data.total;
        }
      });

      const wantsPercent = Math.round((wantsExpense / totalIncome) * 100);
      const savingsRate = Math.round(((totalIncome - totalExpense) / totalIncome) * 100);

      if (wantsPercent > 35) {
        const targetWants = totalIncome * 0.3;
        const excessWants = Math.round(wantsExpense - targetWants);
        list.push({
          id: 'rule_50_30_20_wants_excess',
          priority: 'opportunity',
          category: 'saving',
          title: `Оптимизация желаний: ${wantsPercent}% дохода уходит на развлечения и комфорт`,
          description: `По правилу 50/30/20 на удовольствия и необязательные покупки рекомендуется выделять не более 30% дохода. Снижение трат на ${formatCurrency(
            excessWants,
            currency.symbol
          )} позволит повысить норму сбережений до рекомендуемых 20%.`,
          metricLabel: 'Потенциал к сбережениям',
          metricValue: `+${formatCurrency(excessWants, currency.symbol)}`,
          potentialSaving: excessWants,
          impactScore: 75,
          tags: ['Правило 50/30/20', 'Комфорт vs Накопления'],
        });
      }

      if (savingsRate >= 25) {
        list.push({
          id: 'excellent_savings_discipline',
          priority: 'strategy',
          category: 'saving',
          title: `Отличная дисциплина: норма сбережений ${savingsRate}%`,
          description: `Вы успешно сохраняете более четверти дохода (${formatCurrency(
            totalIncome - totalExpense,
            currency.symbol
          )}). Рекомендуем распределить свободный остаток в накопительные кошельки или инвестиционные цели.`,
          metricLabel: 'Норма сбережений',
          metricValue: `${savingsRate}%`,
          action: {
            type: 'go_wallets',
            label: 'Управление кошельками',
          },
          impactScore: 60,
          tags: ['Высокая норма', 'Инвестиции'],
        });
      }
    }

    // -------------------------------------------------------------------------
    // Rule 6: Safety Cushion (Emergency Fund) Analysis from Wallets
    // -------------------------------------------------------------------------
    const savingsWallets = wallets.filter(
      (w) => w.type === 'savings' || w.type === 'investment' || w.type === 'goal'
    );
    const totalSavingsBalance = savingsWallets.reduce((sum, w) => sum + Math.max(0, w.balance), 0);

    // Estimate monthly burn
    const monthlyBurn =
      totalExpense > 0
        ? Math.round(totalExpense / periodMultiplier)
        : transactions
            .filter((t) => t.type === 'expense')
            .reduce((s, t) => s + t.amount, 0) / 2 || 30000;

    const cushionMonths = monthlyBurn > 0 ? (totalSavingsBalance / monthlyBurn).toFixed(1) : '0';

    if (parseFloat(cushionMonths) < 2.0 && monthlyBurn > 0) {
      const targetCushion = monthlyBurn * 3;
      const deficitCushion = Math.max(0, targetCushion - totalSavingsBalance);
      list.push({
        id: 'safety_cushion_recommendation',
        priority: 'strategy',
        category: 'safety',
        title: `Укрепите подушку безопасности (текущий запас: ${cushionMonths} мес.)`,
        description: `Ваш текущий сберегательный резерв покрывает около ${cushionMonths} мес. базовых трат при норме в 3–6 месяцев (${formatCurrency(
          targetCushion,
          currency.symbol
        )}). Настройте регулярный автоперевод 10% дохода в раздел «Сбережения».`,
        metricLabel: 'Целевая подушка (3 мес.)',
        metricValue: formatCurrency(targetCushion, currency.symbol),
        potentialSaving: deficitCushion > 0 ? Math.round(monthlyBurn * 0.1) : undefined,
        action: {
          type: 'go_wallets',
          label: 'Открыть накопительный счет',
        },
        impactScore: 70,
        tags: ['Подушка безопасности', 'Финансовый щит'],
      });
    }

    // -------------------------------------------------------------------------
    // Rule 7: Recurring Subscriptions & Regular Commitments
    // -------------------------------------------------------------------------
    const recurringTxs = transactions.filter((t) => t.isRecurring && t.type === 'expense');
    if (recurringTxs.length > 0) {
      const monthlyRecurringTotal = recurringTxs.reduce((sum, t) => {
        if (t.recurrenceInterval === 'daily') return sum + t.amount * 30;
        if (t.recurrenceInterval === 'weekly') return sum + t.amount * 4.3;
        if (t.recurrenceInterval === 'yearly') return sum + t.amount / 12;
        return sum + t.amount; // monthly
      }, 0);

      if (monthlyRecurringTotal > 4000) {
        list.push({
          id: 'recurring_audit_advice',
          priority: 'opportunity',
          category: 'saving',
          title: `Регулярные подписки и платежи: ${formatCurrency(
            monthlyRecurringTotal,
            currency.symbol
          )}/мес`,
          description: `У вас настроено ${recurringTxs.length} регулярных списаний (подписки, связь, сервисы). Проведите ревизию неиспользуемых сервисов — отказ всего от 1–2 подписок освободит до ${formatCurrency(
            Math.round(monthlyRecurringTotal * 0.25),
            currency.symbol
          )} в месяц.`,
          metricLabel: 'В год на подписки',
          metricValue: formatCurrency(monthlyRecurringTotal * 12, currency.symbol),
          potentialSaving: Math.round(monthlyRecurringTotal * 0.25),
          action: {
            type: 'go_history',
            label: 'Посмотреть списания',
          },
          impactScore: 68,
          tags: ['Подписки', 'Ревизия сервисов'],
        });
      }
    }

    // -------------------------------------------------------------------------
    // Rule 8: Frequent Micro-Expenses (The "Latte Factor")
    // -------------------------------------------------------------------------
    const microTxs = filteredTxs.filter((t) => t.type === 'expense' && t.amount <= 600 && t.amount > 0);
    if (microTxs.length >= 6) {
      const microSum = microTxs.reduce((sum, t) => sum + t.amount, 0);
      const potentialMonthlyMicro = Math.round((microSum / periodMultiplier) * 0.35);

      list.push({
        id: 'micro_expenses_latte_factor',
        priority: 'opportunity',
        category: 'pattern',
        title: `Эффект «латте»: ${microTxs.length} мелких трат на ${formatCurrency(
          microSum,
          currency.symbol
        )}`,
        description: `Частые мелкие покупки (кофе с собой, снеки, импульсивные мелочи) в сумме составили ${formatCurrency(
          microSum,
          currency.symbol
        )}. Сокращение мелких трат всего на треть принесет вам около ${formatCurrency(
          potentialMonthlyMicro,
          currency.symbol
        )} в месяц чистой экономии.`,
        metricLabel: 'Потенциал оптимизации',
        metricValue: `~${formatCurrency(potentialMonthlyMicro, currency.symbol)}/мес`,
        potentialSaving: potentialMonthlyMicro,
        impactScore: 65,
        tags: ['Импульсивные траты', 'Мелкие чеки'],
      });
    }

    // -------------------------------------------------------------------------
    // Fallback Advice if list is empty or minimal
    // -------------------------------------------------------------------------
    if (list.length === 0) {
      list.push({
        id: 'general_spending_control',
        priority: 'strategy',
        category: 'budget',
        title: 'Установите персональные лимиты на категории',
        description:
          'Финансовый анализ показывает стабильность ваших трат. Чтобы закрепить успех и ускорить достижение финансовых целей, распределите бюджет по ключевым категориям.',
        action: {
          type: 'open_budget',
          label: 'Настроить лимиты бюджетов',
        },
        impactScore: 50,
        tags: ['Базовый совет'],
      });
    }

    // Sort by impact score (highest priority and impact first)
    return list.sort((a, b) => b.impactScore - a.impactScore);
  }, [
    filteredTxs,
    prevFilteredTxs,
    period,
    categories,
    categoryBudgets,
    wallets,
    currency,
    transactions,
  ]);

  // Aggregate stats
  const totalPotentialSavings = useMemo(() => {
    return adviceList.reduce((sum, item) => sum + (item.potentialSaving || 0), 0);
  }, [adviceList]);

  // Filter advice based on active tab and completed/dismissed status
  const visibleAdvice = useMemo(() => {
    return adviceList.filter((item) => {
      if (dismissedAdviceIds.includes(item.id)) return false;
      const isCompleted = completedAdviceIds.includes(item.id);
      if (!showCompleted && isCompleted) return false;

      if (activeFilter === 'all') return true;
      if (activeFilter === 'critical') return item.priority === 'critical';
      if (activeFilter === 'budget') return item.category === 'budget';
      if (activeFilter === 'saving') return item.category === 'saving';
      if (activeFilter === 'safety') return item.category === 'safety';
      return true;
    });
  }, [adviceList, dismissedAdviceIds, completedAdviceIds, showCompleted, activeFilter]);

  const toggleComplete = (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    setCompletedAdviceIds((prev) => {
      const next = prev.includes(id) ? prev.filter((i) => i !== id) : [...prev, id];
      localStorage.setItem('fa_completed_advice_ids', JSON.stringify(next));
      return next;
    });
  };

  const handleDismiss = (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    setDismissedAdviceIds((prev) => {
      const next = [...prev, id];
      localStorage.setItem('fa_dismissed_advice_ids', JSON.stringify(next));
      return next;
    });
  };

  const handleExecuteAction = (item: PersonalizedAdviceItem) => {
    if (!item.action) return;
    if (item.action.type === 'open_budget') {
      onOpenBudgetModal(item.action.categoryId);
    } else if (item.action.type === 'go_wallets') {
      setActiveTab('wallets');
    } else if (item.action.type === 'go_history') {
      setActiveTab('history');
    }
  };

  const getPriorityStyle = (priority: PersonalizedAdviceItem['priority']) => {
    switch (priority) {
      case 'critical':
        return {
          border: 'border-rose-300/90',
          bg: 'bg-rose-50/70 hover:bg-rose-50',
          badgeBg: 'bg-rose-100 text-rose-800 border-rose-200',
          iconBg: 'bg-rose-500 text-white',
          label: 'Высокий приоритет',
          icon: AlertTriangle,
        };
      case 'warning':
        return {
          border: 'border-amber-300/80',
          bg: 'bg-amber-50/50 hover:bg-amber-50/80',
          badgeBg: 'bg-amber-100 text-amber-900 border-amber-200',
          iconBg: 'bg-amber-500 text-white',
          label: 'Внимание',
          icon: AlertCircle,
        };
      case 'opportunity':
        return {
          border: 'border-emerald-200/90',
          bg: 'bg-emerald-50/40 hover:bg-emerald-50/70',
          badgeBg: 'bg-emerald-100 text-emerald-800 border-emerald-200',
          iconBg: 'bg-emerald-600 text-white',
          label: 'Экономия',
          icon: TrendingDown,
        };
      case 'strategy':
      default:
        return {
          border: 'border-blue-200/90',
          bg: 'bg-blue-50/30 hover:bg-blue-50/60',
          badgeBg: 'bg-blue-100 text-blue-800 border-blue-200',
          iconBg: 'bg-blue-600 text-white',
          label: 'Стратегия',
          icon: Target,
        };
    }
  };

  return (
    <div
      id="personalized_financial_advice_block"
      className="bg-white rounded-3xl border border-slate-200 p-5 sm:p-6 shadow-xs relative overflow-hidden transition-all space-y-4"
    >
      {/* Decorative gradient flare */}
      <div className="absolute top-0 right-0 w-72 h-72 bg-gradient-to-br from-amber-400/10 via-emerald-400/5 to-transparent rounded-full blur-3xl pointer-events-none" />

      {/* Header Section */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 relative z-10 border-b border-slate-100 pb-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-amber-500 via-orange-500 to-emerald-600 text-white flex items-center justify-center shadow-sm shrink-0">
            <Lightbulb size={22} className="stroke-[2.2]" />
          </div>
          <div>
            <div className="flex items-center gap-2 flex-wrap">
              <h3 className="text-base sm:text-lg font-black text-slate-900 tracking-tight">
                Персональные финансовые советы
              </h3>
              <span className="inline-flex items-center gap-1 text-[10px] font-black uppercase tracking-wider px-2 py-0.5 rounded-full bg-amber-50 text-amber-800 border border-amber-200">
                <Sparkles size={11} className="text-amber-600" />
                Умный анализ {periodLabel}
              </span>
            </div>
            <p className="text-xs text-slate-500 mt-0.5">
              Индивидуальные рекомендации на основе структуры расходов, лимитов и баланса
            </p>
          </div>
        </div>

        {/* Toggle Collapse */}
        <div className="flex items-center gap-2 self-start sm:self-auto">
          {totalPotentialSavings > 0 && (
            <div className="px-3 py-1 rounded-xl bg-gradient-to-r from-emerald-50 to-teal-50 border border-emerald-200 text-left sm:text-right hidden md:block">
              <span className="text-[10px] font-bold text-emerald-800 block uppercase tracking-wider">
                Потенциал экономии
              </span>
              <span className="text-xs font-black text-emerald-700">
                +{formatCurrency(totalPotentialSavings, currency.symbol)}
              </span>
            </div>
          )}

          <button
            onClick={() => setIsExpanded(!isExpanded)}
            className="p-1.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-600 transition-colors cursor-pointer"
            title={isExpanded ? 'Свернуть' : 'Развернуть'}
          >
            {isExpanded ? <ChevronUp size={18} /> : <ChevronDown size={18} />}
          </button>
        </div>
      </div>

      {isExpanded && (
        <>
          {/* Executive Summary Metrics Banner */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            {/* Metric 1: Total Savings Potential */}
            <div className="p-3.5 rounded-2xl bg-gradient-to-br from-emerald-500/10 via-emerald-50/50 to-white border border-emerald-200/80 flex items-center justify-between">
              <div>
                <span className="text-[10px] font-bold text-emerald-800 uppercase tracking-wider block">
                  Резерв для оптимизации
                </span>
                <span className="text-lg font-black text-emerald-700">
                  +{formatCurrency(totalPotentialSavings, currency.symbol)}
                </span>
                <span className="text-[10px] text-slate-500 block">при соблюдении советов</span>
              </div>
              <div className="w-8 h-8 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center shrink-0 shadow-2xs">
                <PiggyBank size={18} />
              </div>
            </div>

            {/* Metric 2: Active Advice Count */}
            <div className="p-3.5 rounded-2xl bg-gradient-to-br from-amber-500/10 via-amber-50/50 to-white border border-amber-200/80 flex items-center justify-between">
              <div>
                <span className="text-[10px] font-bold text-amber-800 uppercase tracking-wider block">
                  Активных советов
                </span>
                <span className="text-lg font-black text-amber-800">
                  {visibleAdvice.length} из {adviceList.length}
                </span>
                <span className="text-[10px] text-slate-500 block">готовы к внедрению</span>
              </div>
              <div className="w-8 h-8 rounded-xl bg-amber-100 text-amber-800 flex items-center justify-center shrink-0 shadow-2xs">
                <Zap size={18} />
              </div>
            </div>

            {/* Metric 3: Safety Cushion Status */}
            <div className="p-3.5 rounded-2xl bg-gradient-to-br from-blue-500/10 via-blue-50/50 to-white border border-blue-200/80 flex items-center justify-between">
              <div>
                <span className="text-[10px] font-bold text-blue-800 uppercase tracking-wider block">
                  Финансовая устойчивость
                </span>
                <span className="text-lg font-black text-blue-800">
                  {wallets.length} {wallets.length === 1 ? 'счет' : 'счетов'}
                </span>
                <span className="text-[10px] text-slate-500 block">
                  {wallets.filter((w) => w.balance > 0).length} в плюсе
                </span>
              </div>
              <div className="w-8 h-8 rounded-xl bg-blue-100 text-blue-700 flex items-center justify-center shrink-0 shadow-2xs">
                <ShieldCheck size={18} />
              </div>
            </div>
          </div>

          {/* Filter Pills */}
          <div className="flex items-center justify-between gap-2 flex-wrap pt-1">
            <div className="flex items-center gap-1.5 flex-wrap">
              <button
                onClick={() => setActiveFilter('all')}
                className={`px-3 py-1 rounded-xl text-xs font-bold transition-all cursor-pointer ${
                  activeFilter === 'all'
                    ? 'bg-slate-900 text-white shadow-2xs'
                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                }`}
              >
                Все ({adviceList.length})
              </button>
              {adviceList.some((a) => a.priority === 'critical') && (
                <button
                  onClick={() => setActiveFilter('critical')}
                  className={`px-3 py-1 rounded-xl text-xs font-bold transition-all inline-flex items-center gap-1 cursor-pointer ${
                    activeFilter === 'critical'
                      ? 'bg-rose-600 text-white shadow-2xs'
                      : 'bg-rose-50 text-rose-700 hover:bg-rose-100 border border-rose-200'
                  }`}
                >
                  <AlertTriangle size={12} />
                  Критические
                </button>
              )}
              <button
                onClick={() => setActiveFilter('budget')}
                className={`px-3 py-1 rounded-xl text-xs font-bold transition-all cursor-pointer ${
                  activeFilter === 'budget'
                    ? 'bg-slate-900 text-white shadow-2xs'
                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                }`}
              >
                Бюджеты и лимиты
              </button>
              <button
                onClick={() => setActiveFilter('saving')}
                className={`px-3 py-1 rounded-xl text-xs font-bold transition-all cursor-pointer ${
                  activeFilter === 'saving'
                    ? 'bg-slate-900 text-white shadow-2xs'
                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                }`}
              >
                Экономия
              </button>
              <button
                onClick={() => setActiveFilter('safety')}
                className={`px-3 py-1 rounded-xl text-xs font-bold transition-all cursor-pointer ${
                  activeFilter === 'safety'
                    ? 'bg-slate-900 text-white shadow-2xs'
                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                }`}
              >
                Безопасность
              </button>
            </div>

            {/* Completed Toggle */}
            {completedAdviceIds.length > 0 && (
              <button
                onClick={() => setShowCompleted(!showCompleted)}
                className="text-[11px] font-semibold text-slate-500 hover:text-slate-800 transition-colors inline-flex items-center gap-1 cursor-pointer"
              >
                <CheckCircle2 size={12} className={showCompleted ? 'text-emerald-600' : ''} />
                <span>{showCompleted ? 'Скрыть выполненные' : `Выполнено (${completedAdviceIds.length})`}</span>
              </button>
            )}
          </div>

          {/* Advice Cards List */}
          {visibleAdvice.length === 0 ? (
            <div className="text-center py-8 rounded-2xl bg-slate-50 border border-dashed border-slate-200 space-y-2">
              <CheckCircle2 size={28} className="text-emerald-500 mx-auto" />
              <p className="text-xs font-bold text-slate-700">
                Все финансовые советы в данной категории учтены или выполнены!
              </p>
              <p className="text-[11px] text-slate-400">
                Продолжайте вносить операции для формирования свежей персонализированной аналитики.
              </p>
            </div>
          ) : (
            <div className="space-y-3 pt-1">
              {visibleAdvice.map((item) => {
                const style = getPriorityStyle(item.priority);
                const PriorityIcon = style.icon;
                const isCompleted = completedAdviceIds.includes(item.id);

                return (
                  <div
                    key={item.id}
                    className={`rounded-2xl border ${style.border} ${style.bg} p-4 transition-all duration-200 shadow-2xs relative ${
                      isCompleted ? 'opacity-60 grayscale-[40%]' : ''
                    }`}
                  >
                    <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-3">
                      {/* Left: Icon & Core Content */}
                      <div className="flex items-start gap-3 min-w-0">
                        {/* Icon */}
                        {item.categoryObj ? (
                          <div
                            className="w-9 h-9 rounded-xl flex items-center justify-center shrink-0 text-white shadow-2xs mt-0.5"
                            style={{ backgroundColor: item.categoryObj.color }}
                          >
                            <DynamicIcon
                              name={item.categoryObj.icon}
                              size={18}
                              className="stroke-[2.2]"
                            />
                          </div>
                        ) : (
                          <div
                            className={`w-9 h-9 rounded-xl flex items-center justify-center shrink-0 shadow-2xs mt-0.5 ${style.iconBg}`}
                          >
                            <PriorityIcon size={18} className="stroke-[2.2]" />
                          </div>
                        )}

                        {/* Title & Description */}
                        <div className="space-y-1 min-w-0">
                          <div className="flex items-center gap-2 flex-wrap">
                            <h4
                              className={`text-sm font-black text-slate-900 ${
                                isCompleted ? 'line-through text-slate-500' : ''
                              }`}
                            >
                              {item.title}
                            </h4>

                            {/* Badge */}
                            <span
                              className={`text-[10px] font-black px-2 py-0.5 rounded-md border ${style.badgeBg}`}
                            >
                              {style.label}
                            </span>

                            {item.tags.map((tag) => (
                              <span
                                key={tag}
                                className="text-[10px] font-medium px-1.5 py-0.5 rounded-md bg-white/80 text-slate-600 border border-slate-200/60"
                              >
                                {tag}
                              </span>
                            ))}
                          </div>

                          <p className="text-xs text-slate-600 leading-relaxed font-normal">
                            {item.description}
                          </p>

                          {/* Metric Pill if present */}
                          {item.metricLabel && item.metricValue && (
                            <div className="pt-1 flex items-center gap-2 flex-wrap">
                              <span className="text-[11px] font-bold text-slate-500">
                                {item.metricLabel}:
                              </span>
                              <span className="text-xs font-black text-slate-900 bg-white/90 px-2 py-0.5 rounded-md border border-slate-200">
                                {item.metricValue}
                              </span>
                            </div>
                          )}
                        </div>
                      </div>

                      {/* Right: Action Buttons */}
                      <div className="flex sm:flex-col items-center sm:items-end justify-between sm:justify-start gap-2 shrink-0 pt-2 sm:pt-0 border-t sm:border-t-0 border-slate-200/50">
                        {item.action && (
                          <button
                            onClick={() => handleExecuteAction(item)}
                            className="px-3 py-1.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-white font-bold text-xs inline-flex items-center gap-1.5 transition-all shadow-2xs hover:shadow-xs cursor-pointer active:scale-95"
                          >
                            {item.action.type === 'open_budget' && <SlidersHorizontal size={13} />}
                            {item.action.type === 'go_wallets' && <WalletIcon size={13} />}
                            {item.action.type === 'go_history' && <ArrowRight size={13} />}
                            <span>{item.action.label}</span>
                          </button>
                        )}

                        <div className="flex items-center gap-1.5">
                          <button
                            onClick={(e) => toggleComplete(item.id, e)}
                            className={`p-1.5 rounded-lg border text-xs font-medium transition-colors cursor-pointer inline-flex items-center gap-1 ${
                              isCompleted
                                ? 'bg-emerald-100 text-emerald-800 border-emerald-300'
                                : 'bg-white hover:bg-slate-100 text-slate-600 border-slate-200'
                            }`}
                            title={isCompleted ? 'Отменить выполнение' : 'Отметить как выполненный'}
                          >
                            <Check size={13} className={isCompleted ? 'text-emerald-700 stroke-[3]' : ''} />
                            <span className="text-[10px] hidden sm:inline">
                              {isCompleted ? 'Выполнено' : 'Учесть'}
                            </span>
                          </button>

                          <button
                            onClick={(e) => handleDismiss(item.id, e)}
                            className="p-1.5 rounded-lg bg-white hover:bg-slate-100 text-slate-400 hover:text-slate-600 border border-slate-200 transition-colors cursor-pointer"
                            title="Скрыть совет"
                          >
                            <EyeOff size={13} />
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </>
      )}
    </div>
  );
};
