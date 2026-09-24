import { Transaction, Wallet, FinancialHealthBreakdown } from '../types';

export function calculateTotals(transactions: Transaction[]) {
  let income = 0;
  let expense = 0;

  for (const t of transactions) {
    if (t.type === 'income') {
      income += t.amount;
    } else if (t.type === 'expense') {
      expense += t.amount;
    }
  }

  const net = income - expense;
  const savingsRate = income > 0 ? Math.max(0, Math.min(100, Math.round(((income - expense) / income) * 100))) : 0;

  return { income, expense, net, savingsRate };
}

export function calculateFinancialHealth(
  transactions: Transaction[],
  wallets: Wallet[]
): FinancialHealthBreakdown {
  const { income, expense, savingsRate } = calculateTotals(transactions);

  // 1. Savings Rate Score (0 - 35)
  // Target: 20% or higher savings rate gives full score
  const savingsRateScore = Math.min(35, Math.round((savingsRate / 20) * 35));

  // 2. Expense Control Score (0 - 25)
  // Checks if total expenses exceed wallet limits
  const totalLimit = wallets.reduce((acc, w) => acc + (w.limit || 0), 0);
  let expenseControlScore = 25;
  if (totalLimit > 0) {
    const ratio = expense / totalLimit;
    if (ratio > 1) {
      expenseControlScore = Math.max(5, Math.round(25 - (ratio - 1) * 30));
    } else {
      expenseControlScore = 25;
    }
  } else {
    expenseControlScore = expense <= income ? 22 : 10;
  }

  // 3. Stability & Regularity (0 - 20)
  // Based on transaction count and having income
  const stabilityScore = income > 0 ? (transactions.length >= 10 ? 20 : 14) : 8;

  // 4. Diversification Score (0 - 20)
  // Wallets check (having savings or investment in addition to card/cash)
  const hasSavingsOrInvestment = wallets.some(
    (w) => (w.type === 'savings' || w.type === 'investment' || w.type === 'goal') && w.balance > 0
  );
  const diversificationScore = hasSavingsOrInvestment ? 20 : 10;

  const totalScore = Math.min(100, savingsRateScore + expenseControlScore + stabilityScore + diversificationScore);

  const recommendations: string[] = [];
  if (savingsRate < 15) {
    recommendations.push('Попробуйте откладывать минимум 15-20% от любого входящего дохода сразу после получения.');
  }
  if (!hasSavingsOrInvestment) {
    recommendations.push('Откройте накопительный счет или цель для создания подушки безопасности на 3-6 месяцев.');
  }
  if (totalLimit > 0 && expense > totalLimit) {
    recommendations.push('Текущие расходы превышают установленный лимит бюджета. Проанализируйте категории с наибольшими тратами.');
  }
  if (recommendations.length === 0) {
    recommendations.push('Отличная финансовая дисциплина! Продолжайте регулярно инвестировать и контролировать траты.');
  }

  return {
    score: totalScore,
    savingsRateScore,
    expenseControlScore,
    stabilityScore,
    diversificationScore,
    recommendations,
  };
}

export function formatCurrency(amount: number, symbol: string = '₽'): string {
  const formatted = new Intl.NumberFormat('ru-RU', {
    maximumFractionDigits: 0,
  }).format(amount);
  return `${formatted}\u00A0${symbol}`;
}

export function formatTransactionDate(dateStr: string): string {
  if (!dateStr) return '';
  try {
    const parts = dateStr.split('-');
    if (parts.length !== 3) return dateStr;
    const year = Number(parts[0]);
    const month = Number(parts[1]) - 1;
    const day = Number(parts[2]);
    const d = new Date(year, month, day);
    if (isNaN(d.getTime())) return dateStr;

    const now = new Date();
    const isCurrentYear = d.getFullYear() === now.getFullYear();
    if (isCurrentYear) {
      return d.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' });
    }
    return d.toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit', year: '2-digit' });
  } catch {
    return dateStr;
  }
}

export function getSmartTips(transactions: Transaction[]): { title: string; desc: string; icon: string }[] {
  const tips = [
    {
      title: 'Правило 50/30/20',
      desc: 'Распределяйте доход: 50% на базовые нужды (жилье, еда), 30% на желания и комфорт, 20% — в сбережения.',
      icon: 'PieChart',
    },
    {
      title: 'Автоматические накопления',
      desc: 'Включите автосбережение 10% от каждого входящего перевода прямо в раздел Кошельки.',
      icon: 'PiggyBank',
    },
    {
      title: 'Оптимизация мелких трат',
      desc: 'Кофе с собой и частые перекусы могут составлять до 15% месячного бюджета. Отслеживайте категорию «Кафе».',
      icon: 'Coffee',
    },
  ];

  if (transactions.length > 5) {
    const foodExpenses = transactions
      .filter((t) => t.type === 'expense' && (t.category === 'Продукты' || t.category === 'Кафе и рестораны'))
      .reduce((acc, t) => acc + t.amount, 0);

    if (foodExpenses > 25000) {
      tips[2] = {
        title: 'Заметные траты на питание',
        desc: `В этом периоде на продукты и кафе потрачено ${formatCurrency(foodExpenses)}. Составление меню на неделю помогает сэкономить до 20%.`,
        icon: 'Utensils',
      };
    }
  }

  return tips;
}
