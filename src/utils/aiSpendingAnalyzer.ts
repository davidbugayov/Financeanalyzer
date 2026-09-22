import { Transaction, Category, Wallet } from '../types';

export interface AiInsight {
  id: string;
  type: 'saving' | 'alert' | 'pattern' | 'quick_win';
  title: string;
  category: string;
  categoryId?: string;
  iconName: string;
  badgeText: string;
  badgeColor: 'emerald' | 'amber' | 'blue' | 'rose' | 'purple';
  description: string;
  evidence: string;
  potentialSavingsMonthly: number;
  confidence: 'high' | 'medium' | 'moderate';
  actionType?: 'budget' | 'stats' | 'wallets';
  actionLabel?: string;
}

export interface AiSpendingAnalysisResult {
  timeWindow: '7d' | '30d' | 'all';
  totalIncome: number;
  totalExpense: number;
  net: number;
  currentSavingsRate: number;
  dailyBurnRate: number;
  projectedMonthEndExpense: number;
  spendingAccelerationPercent: number;
  totalPotentialMonthlySavings: number;
  projectedNewSavingsRate: number;
  annualPotentialSavings: number;
  topSpendingCategory: { name: string; amount: number; percentage: number } | null;
  insights: AiInsight[];
  discretionaryTotal: number;
  discretionaryPercent: number;
  weekendSurgePercent: number | null;
  recurringMonthlyCommitment: number;
  microExpensesMonthlyTotal: number;
}

// Helper to calculate days between two dates
function getDaysDiff(d1: Date, d2: Date): number {
  return Math.max(1, Math.round(Math.abs(d1.getTime() - d2.getTime()) / (1000 * 60 * 60 * 24)));
}

export function analyzeSpendingPatterns(
  transactions: Transaction[],
  categories: Category[],
  categoryBudgets: Record<string, number>,
  wallets: Wallet[],
  timeWindow: '7d' | '30d' | 'all' = '30d'
): AiSpendingAnalysisResult {
  const now = new Date();
  const daysInWindow = timeWindow === '7d' ? 7 : timeWindow === '30d' ? 30 : 90;

  const currentWindowStart = new Date(now);
  currentWindowStart.setDate(now.getDate() - daysInWindow);
  currentWindowStart.setHours(0, 0, 0, 0);

  const prevWindowStart = new Date(currentWindowStart);
  prevWindowStart.setDate(currentWindowStart.getDate() - daysInWindow);

  // Filter transactions for current window and previous window
  const currentTxs = transactions.filter((t) => {
    if (timeWindow === 'all') return true;
    const txDate = new Date(t.date);
    return txDate >= currentWindowStart && txDate <= now;
  });

  const prevTxs = transactions.filter((t) => {
    if (timeWindow === 'all') return false;
    const txDate = new Date(t.date);
    return txDate >= prevWindowStart && txDate < currentWindowStart;
  });

  // Calculate totals
  let totalIncome = 0;
  let totalExpense = 0;
  let prevTotalExpense = 0;

  for (const t of currentTxs) {
    if (t.type === 'income') totalIncome += t.amount;
    else if (t.type === 'expense') totalExpense += t.amount;
  }

  for (const t of prevTxs) {
    if (t.type === 'expense') prevTotalExpense += t.amount;
  }

  const net = totalIncome - totalExpense;
  const currentSavingsRate = totalIncome > 0
    ? Math.max(0, Math.min(100, Math.round(((totalIncome - totalExpense) / totalIncome) * 100)))
    : 0;

  // Daily burn rate
  const actualDays = timeWindow === 'all'
    ? Math.max(1, getDaysDiff(new Date(transactions[transactions.length - 1]?.date || now), now))
    : daysInWindow;
  const dailyBurnRate = Math.round(totalExpense / actualDays);
  const projectedMonthEndExpense = dailyBurnRate * 30;

  // Acceleration vs prior period
  let spendingAccelerationPercent = 0;
  if (prevTotalExpense > 0) {
    spendingAccelerationPercent = Math.round(((totalExpense - prevTotalExpense) / prevTotalExpense) * 100);
  }

  // Category breakdowns
  const categorySpendingMap: Record<string, { amount: number; txCount: number; category: Category | undefined }> = {};
  categories.forEach((cat) => {
    categorySpendingMap[cat.id] = { amount: 0, txCount: 0, category: cat };
  });

  const subcategorySpendingMap: Record<string, number> = {};

  // Track micro-expenses (< 600 rub or small discretionary)
  let microExpensesTotal = 0;
  let microExpensesCount = 0;

  // Weekend vs Weekday analysis
  let weekendExpenseTotal = 0;
  let weekendDaysCount = 0;
  let weekdayExpenseTotal = 0;
  let weekdayDaysCount = 0;

  // Discretionary spending tracking (dining, entertainment, shopping, cafes)
  const discretionaryKeywords = [
    'ресторан',
    'кафе',
    'развлечен',
    'шопинг',
    'одежд',
    'подписк',
    'кино',
    'игры',
    'фастфуд',
    'бар',
    'доставка',
  ];

  let discretionaryTotal = 0;

  // Recurring commitment
  let recurringMonthlyCommitment = 0;

  currentTxs.forEach((t) => {
    if (t.type !== 'expense') return;

    // Find category
    const cat = categories.find((c) => (t.categoryId && c.id === t.categoryId) || c.name === t.category);
    const catId = cat?.id || 'other';

    if (!categorySpendingMap[catId]) {
      categorySpendingMap[catId] = { amount: 0, txCount: 0, category: cat };
    }
    categorySpendingMap[catId].amount += t.amount;
    categorySpendingMap[catId].txCount += 1;

    // Subcategories
    if (t.subcategory) {
      subcategorySpendingMap[t.subcategory] = (subcategorySpendingMap[t.subcategory] || 0) + t.amount;
    }

    // Micro expenses
    if (t.amount <= 650 && t.amount > 0) {
      microExpensesTotal += t.amount;
      microExpensesCount += 1;
    }

    // Day of week analysis
    const txDate = new Date(t.date);
    const dayOfWeek = txDate.getDay(); // 0 is Sunday, 6 is Saturday, 5 is Friday
    if (dayOfWeek === 0 || dayOfWeek === 6 || dayOfWeek === 5) {
      weekendExpenseTotal += t.amount;
      weekendDaysCount += 1;
    } else {
      weekdayExpenseTotal += t.amount;
      weekdayDaysCount += 1;
    }

    // Discretionary check
    const catNameLower = (cat?.name || t.category || '').toLowerCase();
    const subNameLower = (t.subcategory || '').toLowerCase();
    const isDiscretionary = discretionaryKeywords.some(
      (kw) => catNameLower.includes(kw) || subNameLower.includes(kw)
    );
    if (isDiscretionary) {
      discretionaryTotal += t.amount;
    }

    // Recurring commitment
    if (t.isRecurring) {
      let monthlyEquiv = t.amount;
      if (t.recurrenceInterval === 'daily') monthlyEquiv = t.amount * 30;
      else if (t.recurrenceInterval === 'weekly') monthlyEquiv = t.amount * 4.3;
      else if (t.recurrenceInterval === 'biweekly') monthlyEquiv = t.amount * 2.15;
      else if (t.recurrenceInterval === 'yearly') monthlyEquiv = t.amount / 12;
      recurringMonthlyCommitment += monthlyEquiv;
    }
  });

  const discretionaryPercent = totalExpense > 0 ? Math.round((discretionaryTotal / totalExpense) * 100) : 0;

  // Weekend surge rate
  let weekendSurgePercent: number | null = null;
  const weekendAvgDaily = weekendDaysCount > 0 ? weekendExpenseTotal / weekendDaysCount : 0;
  const weekdayAvgDaily = weekdayDaysCount > 0 ? weekdayExpenseTotal / weekdayDaysCount : 0;
  if (weekdayAvgDaily > 0 && weekendAvgDaily > weekdayAvgDaily * 1.25) {
    weekendSurgePercent = Math.round(((weekendAvgDaily - weekdayAvgDaily) / weekdayAvgDaily) * 100);
  }

  // Find top spending category
  let topSpendingCategory: { name: string; amount: number; percentage: number } | null = null;
  let maxCatAmount = 0;

  Object.entries(categorySpendingMap).forEach(([catId, data]) => {
    if (data.amount > maxCatAmount) {
      maxCatAmount = data.amount;
      topSpendingCategory = {
        name: data.category?.name || 'Другое',
        amount: data.amount,
        percentage: totalExpense > 0 ? Math.round((data.amount / totalExpense) * 100) : 0,
      };
    }
  });

  // GENERATE ACTIONABLE AI INSIGHTS
  const insights: AiInsight[] = [];

  // 1. Dining Out & Takeout Optimization
  const restaurantCat = categories.find(
    (c) => c.key === 'restaurant' || c.name.toLowerCase().includes('кафе') || c.name.toLowerCase().includes('ресторан')
  );
  const restaurantSpend = restaurantCat ? categorySpendingMap[restaurantCat.id]?.amount || 0 : 0;
  const restaurantPercent = totalExpense > 0 ? Math.round((restaurantSpend / totalExpense) * 100) : 0;

  if (restaurantSpend >= 3000 || restaurantPercent >= 12) {
    const monthlyNormalized = Math.round((restaurantSpend / actualDays) * 30);
    const potentialSaving = Math.round(monthlyNormalized * 0.25); // 25% reduction is a realistic savings target
    insights.push({
      id: 'insight_dining_optimization',
      type: 'saving',
      title: 'Оптимизация кафе и доставки еды',
      category: restaurantCat?.name || 'Кафе и рестораны',
      categoryId: restaurantCat?.id,
      iconName: 'Utensils',
      badgeText: 'Высокая экономия',
      badgeColor: 'emerald',
      description: `На кафе и готовую еду приходится ${restaurantPercent}% всех ваших расходов. Снижение частоты заказов доставки всего на 2-3 раза в месяц позволит сохранить значительную сумму.`,
      evidence: `Потрачено ${restaurantSpend.toLocaleString('ru-RU')} ₽ за период (~${monthlyNormalized.toLocaleString('ru-RU')} ₽/мес)`,
      potentialSavingsMonthly: potentialSaving,
      confidence: 'high',
      actionType: 'budget',
      actionLabel: 'Установить лимит',
    });
  }

  // 2. Weekend Surge Insight
  if (weekendSurgePercent !== null && weekendSurgePercent >= 30 && weekendExpenseTotal > 4000) {
    const potentialWeekendSaving = Math.round(((weekendExpenseTotal - weekdayAvgDaily * weekendDaysCount) * 0.4) / (actualDays / 30));
    if (potentialWeekendSaving > 1000) {
      insights.push({
        id: 'insight_weekend_surge',
        type: 'pattern',
        title: 'Импульсивный всплеск на выходных',
        category: 'Паттерн расходов',
        iconName: 'Sparkles',
        badgeText: `+${weekendSurgePercent}% к будням`,
        badgeColor: 'amber',
        description: `В пятницу, субботу и воскресенье среднесуточные траты на ${weekendSurgePercent}% выше, чем в будни. Введение отдельного «лимита на выходные» предотвратит спонтанные покупки.`,
        evidence: `Средний день выходного: ${Math.round(weekendAvgDaily).toLocaleString('ru-RU')} ₽ против ${Math.round(weekdayAvgDaily).toLocaleString('ru-RU')} ₽ в будни`,
        potentialSavingsMonthly: potentialWeekendSaving,
        confidence: 'high',
        actionType: 'stats',
        actionLabel: 'Смотреть тренд',
      });
    }
  }

  // 3. Category Budget Overrun or Nearing Limit
  categories.forEach((cat) => {
    const budgetLimit = categoryBudgets[cat.id];
    const spent = categorySpendingMap[cat.id]?.amount || 0;
    if (budgetLimit && budgetLimit > 0) {
      const ratio = spent / budgetLimit;
      if (ratio >= 1) {
        const overspend = spent - budgetLimit;
        insights.push({
          id: `insight_overbudget_${cat.id}`,
          type: 'alert',
          title: `Превышение лимита: ${cat.name}`,
          category: cat.name,
          categoryId: cat.id,
          iconName: 'AlertTriangle',
          badgeText: `Перерасход ${(ratio * 100 - 100).toFixed(0)}%`,
          badgeColor: 'rose',
          description: `Расходы в категории «${cat.name}» превысили установленный месячный лимит на ${overspend.toLocaleString('ru-RU')} ₽. Заморозка трат в этой категории до конца месяца вернет баланс.`,
          evidence: `Факт: ${spent.toLocaleString('ru-RU')} ₽ из лимита ${budgetLimit.toLocaleString('ru-RU')} ₽`,
          potentialSavingsMonthly: Math.round(overspend),
          confidence: 'high',
          actionType: 'budget',
          actionLabel: 'Скорректировать лимит',
        });
      } else if (ratio >= 0.85) {
        insights.push({
          id: `insight_nearbudget_${cat.id}`,
          type: 'alert',
          title: `Внимание к лимиту: ${cat.name}`,
          category: cat.name,
          categoryId: cat.id,
          iconName: 'AlertCircle',
          badgeText: `Израсходовано ${Math.round(ratio * 100)}%`,
          badgeColor: 'amber',
          description: `В категории «${cat.name}» осталось всего ${(budgetLimit - spent).toLocaleString('ru-RU')} ₽ до исчерпания лимита. Контролируйте чеки, чтобы не уйти в перерасход.`,
          evidence: `Потрачено ${spent.toLocaleString('ru-RU')} ₽ из ${budgetLimit.toLocaleString('ru-RU')} ₽`,
          potentialSavingsMonthly: Math.round(budgetLimit * 0.1),
          confidence: 'medium',
          actionType: 'budget',
          actionLabel: 'Проверить лимит',
        });
      }
    }
  });

  // 4. Micro-Transactions & "Coffee Factor"
  if (microExpensesCount >= 5 && microExpensesTotal >= 2000) {
    const normalizedMonthlyMicro = Math.round((microExpensesTotal / actualDays) * 30);
    const potentialMicroSaving = Math.round(normalizedMonthlyMicro * 0.35); // 35% reduction of impulse snacks
    insights.push({
      id: 'insight_micro_expenses',
      type: 'quick_win',
      title: '«Фактор кофе»: мелкие незаметные траты',
      category: 'Спонтанные покупки',
      iconName: 'Coffee',
      badgeText: `${microExpensesCount} мелких трат`,
      badgeColor: 'purple',
      description: `Вы совершили ${microExpensesCount} мелких покупок до 650 ₽ (кофе, перекусы, мелкие снеки). В совокупности они образуют скрытую утечку бюджета.`,
      evidence: `Сумма мелких покупок: ${microExpensesTotal.toLocaleString('ru-RU')} ₽ (~${normalizedMonthlyMicro.toLocaleString('ru-RU')} ₽/мес)`,
      potentialSavingsMonthly: potentialMicroSaving,
      confidence: 'medium',
      actionType: 'stats',
      actionLabel: 'Изучить мелкие чеки',
    });
  }

  // 5. Recurring Subscriptions Audit
  if (recurringMonthlyCommitment >= 1500) {
    const potentialSubSaving = Math.round(recurringMonthlyCommitment * 0.25); // Cancelling 1 or 2 unused services
    insights.push({
      id: 'insight_recurring_audit',
      type: 'saving',
      title: 'Аудит регулярных списаний и подписок',
      category: 'Подписки и автоплатежи',
      iconName: 'Repeat',
      badgeText: 'Автоплатежи',
      badgeColor: 'blue',
      description: `У вас настроены активные регулярные списания на сумму около ${Math.round(recurringMonthlyCommitment).toLocaleString('ru-RU')} ₽/мес. Ревизия забытых сервисов и семейных тарифов освободит резерв.`,
      evidence: `Регулярная нагрузка: ~${Math.round(recurringMonthlyCommitment).toLocaleString('ru-RU')} ₽ в месяц (${Math.round(recurringMonthlyCommitment * 12).toLocaleString('ru-RU')} ₽/год)`,
      potentialSavingsMonthly: potentialSubSaving,
      confidence: 'high',
      actionType: 'stats',
      actionLabel: 'Просмотреть автоплатежи',
    });
  }

  // 6. Grocery & Supermarkets Smart Planning
  const foodCat = categories.find((c) => c.key === 'food' || c.name.toLowerCase().includes('продукт'));
  const foodSpend = foodCat ? categorySpendingMap[foodCat.id]?.amount || 0 : 0;
  if (foodSpend > 15000) {
    const monthlyFood = Math.round((foodSpend / actualDays) * 30);
    const potentialFoodSaving = Math.round(monthlyFood * 0.12); // 12% savings with grocery list / smart shopping
    insights.push({
      id: 'insight_food_planning',
      type: 'saving',
      title: 'Умное планирование закупки продуктов',
      category: foodCat?.name || 'Продукты',
      categoryId: foodCat?.id,
      iconName: 'ShoppingCart',
      badgeText: 'Экономия 10-15%',
      badgeColor: 'emerald',
      description: `Траты на продукты составляют значительную часть бюджета. Покупка базовых товаров оптом и по списку без спонтанных позиций сохраняет до 12-15% чека без ущерба качеству.`,
      evidence: `Потрачено на супермаркеты: ${foodSpend.toLocaleString('ru-RU')} ₽ за период`,
      potentialSavingsMonthly: potentialFoodSaving,
      confidence: 'medium',
      actionType: 'budget',
      actionLabel: 'Задать лимит продуктов',
    });
  }

  // 7. Savings Rate Boost Recommendation
  const hasSavingsGoal = wallets.some(
    (w) => (w.type === 'savings' || w.type === 'goal' || w.type === 'investment') && w.balance > 0
  );
  if (!hasSavingsGoal || currentSavingsRate < 15) {
    const targetAutoSavings = Math.round(totalIncome * 0.15);
    if (targetAutoSavings > 0) {
      insights.push({
        id: 'insight_savings_acceleration',
        type: 'quick_win',
        title: 'Правило «Сначала заплати себе»',
        category: 'Сбережения и цели',
        iconName: 'PiggyBank',
        badgeText: 'Рост капитала',
        badgeColor: 'emerald',
        description: `Текущая норма сбережений составляет ${currentSavingsRate}%. Настройте автоперевод 10-15% сразу в день поступления дохода в накопительный кошелек или цель.`,
        evidence: `Рекомендуемый взнос: ${targetAutoSavings.toLocaleString('ru-RU')} ₽ от входящего дохода`,
        potentialSavingsMonthly: targetAutoSavings,
        confidence: 'high',
        actionType: 'wallets',
        actionLabel: 'К кошелькам и целям',
      });
    }
  }

  // Fallback insight if user has very disciplined or low spending
  if (insights.length === 0) {
    insights.push({
      id: 'insight_healthy_baseline',
      type: 'quick_win',
      title: 'Отличная финансовая дисциплина',
      category: 'Общий анализ',
      iconName: 'ShieldCheck',
      badgeText: 'Стабильность',
      badgeColor: 'emerald',
      description: `Ваши текущие траты сбалансированы, а критических перерасходов не зафиксировано. Продолжайте вносить операции для формирования глубоких аналитических прогнозов.`,
      evidence: `Норма сбережений: ${currentSavingsRate}%, суточный расход: ${dailyBurnRate.toLocaleString('ru-RU')} ₽/день`,
      potentialSavingsMonthly: Math.round(totalExpense * 0.08),
      confidence: 'medium',
      actionType: 'wallets',
      actionLabel: 'Открыть цели',
    });
  }

  // Calculate combined potential savings (de-duplicated, avoiding double-counting)
  // Take the sum of top distinct category savings capped realistically at 35% of total expense
  const uniqueCategorySavings = new Map<string, number>();
  insights.forEach((ins) => {
    if (ins.type === 'saving' || ins.type === 'quick_win' || ins.type === 'alert') {
      const key = ins.categoryId || ins.category;
      const existing = uniqueCategorySavings.get(key) || 0;
      if (ins.potentialSavingsMonthly > existing) {
        uniqueCategorySavings.set(key, ins.potentialSavingsMonthly);
      }
    }
  });

  let rawTotalSavings = 0;
  uniqueCategorySavings.forEach((amount) => {
    rawTotalSavings += amount;
  });

  // Cap potential savings realistically at 35% of monthly normalized expense, or at least 1500 if income exists
  const monthlyExpenseNormalized = (totalExpense / actualDays) * 30;
  const maxSensibleSaving = monthlyExpenseNormalized > 0 ? Math.round(monthlyExpenseNormalized * 0.35) : 10000;
  const totalPotentialMonthlySavings = Math.min(rawTotalSavings, maxSensibleSaving > 0 ? maxSensibleSaving : 5000);
  const annualPotentialSavings = totalPotentialMonthlySavings * 12;

  // Projected new savings rate if potential savings are realized
  const projectedNewExpense = Math.max(0, monthlyExpenseNormalized - totalPotentialMonthlySavings);
  const monthlyIncomeNormalized = (totalIncome / actualDays) * 30;
  const projectedNewSavingsRate = monthlyIncomeNormalized > 0
    ? Math.min(100, Math.round(((monthlyIncomeNormalized - projectedNewExpense) / monthlyIncomeNormalized) * 100))
    : Math.min(100, currentSavingsRate + 15);

  return {
    timeWindow,
    totalIncome,
    totalExpense,
    net,
    currentSavingsRate,
    dailyBurnRate,
    projectedMonthEndExpense,
    spendingAccelerationPercent,
    totalPotentialMonthlySavings,
    projectedNewSavingsRate,
    annualPotentialSavings,
    topSpendingCategory,
    insights,
    discretionaryTotal,
    discretionaryPercent,
    weekendSurgePercent,
    recurringMonthlyCommitment,
    microExpensesMonthlyTotal: microExpensesTotal,
  };
}
