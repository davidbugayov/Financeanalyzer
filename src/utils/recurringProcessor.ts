import { Transaction, Wallet, RecurrenceInterval } from '../types';

/**
 * Returns human-readable label for recurrence interval in Russian
 */
export function getIntervalLabel(interval: RecurrenceInterval): string {
  switch (interval) {
    case 'daily':
      return 'Ежедневно';
    case 'weekly':
      return 'Еженедельно';
    case 'biweekly':
      return 'Каждые 2 недели';
    case 'monthly':
      return 'Ежемесячно';
    case 'yearly':
      return 'Ежегодно';
    default:
      return 'Регулярно';
  }
}

/**
 * Calculates the next recurrence date following a given date and interval
 * @param fromDateStr YYYY-MM-DD
 * @param interval 'daily' | 'weekly' | 'biweekly' | 'monthly' | 'yearly'
 */
export function calculateNextRecurrenceDate(
  fromDateStr: string,
  interval: RecurrenceInterval
): string {
  const parts = fromDateStr.split('-').map(Number);
  const year = parts[0];
  const monthIndex = parts[1] - 1;
  const day = parts[2];

  const date = new Date(year, monthIndex, day);

  switch (interval) {
    case 'daily':
      date.setDate(date.getDate() + 1);
      break;
    case 'weekly':
      date.setDate(date.getDate() + 7);
      break;
    case 'biweekly':
      date.setDate(date.getDate() + 14);
      break;
    case 'monthly': {
      const originalDay = date.getDate();
      date.setMonth(date.getMonth() + 1);
      // Handle end of month overshoot (e.g. Jan 31 -> Feb 28/29)
      if (date.getDate() !== originalDay) {
        date.setDate(0); // Last day of previous month
      }
      break;
    }
    case 'yearly':
      date.setFullYear(date.getFullYear() + 1);
      break;
  }

  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

/**
 * Gets today's date formatted as YYYY-MM-DD
 */
export function getTodayString(): string {
  const now = new Date();
  const y = now.getFullYear();
  const m = String(now.getMonth() + 1).padStart(2, '0');
  const d = String(now.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

export interface RecurrenceProcessResult {
  updatedTransactions: Transaction[];
  updatedWallets: Wallet[];
  processedCount: number;
  generatedTransactions: Transaction[];
  notifications: string[];
}

/**
 * Background processor that inspects all recurring transactions, generates due
 * entries up to today's date (or asOfDate), and adjusts wallet balances automatically.
 */
export function processScheduledRecurrences(
  transactions: Transaction[],
  wallets: Wallet[],
  asOfDate: string = getTodayString()
): RecurrenceProcessResult {
  let hasChanges = false;
  const newTransactions: Transaction[] = [];
  const walletBalanceDeltas: Record<string, number> = {};
  const notifications: string[] = [];

  // Clone transactions so we can update recurrenceNextDate and lastProcessed on templates
  const updatedTransactionsMap = new Map<string, Transaction>(
    transactions.map((t) => [t.id, { ...t }])
  );

  // Scan all recurring templates
  for (const t of transactions) {
    if (!t.isRecurring || !t.recurrenceInterval) continue;

    let nextDate = t.recurrenceNextDate || calculateNextRecurrenceDate(t.date, t.recurrenceInterval);
    const interval = t.recurrenceInterval;
    const endDate = t.recurrenceEndDate;
    let lastProcessed = t.recurrenceLastProcessed || t.date;
    let occurrencesGenerated = 0;

    // Safety guard: max 24 catch-up instances per template to avoid runaway loops
    const MAX_CATCHUP = 24;

    while (nextDate <= asOfDate && occurrencesGenerated < MAX_CATCHUP) {
      if (endDate && nextDate > endDate) {
        break;
      }

      // Check if we already generated a transaction for this template and date
      const alreadyExists = transactions.some(
        (existing) => existing.parentRecurringId === t.id && existing.date === nextDate
      );

      if (!alreadyExists) {
        // Create spawned recurring transaction instance
        const generatedId = `rec_${t.id}_${nextDate.replace(/-/g, '')}_${Math.random().toString(36).substr(2, 4)}`;
        const timestamp = new Date(nextDate).getTime() || Date.now();

        const generatedTx: Transaction = {
          ...t,
          id: generatedId,
          date: nextDate,
          timestamp,
          isRecurring: false, // Instances are not templates themselves
          parentRecurringId: t.id,
          recurrenceInterval: undefined,
          recurrenceNextDate: undefined,
          recurrenceEndDate: undefined,
          recurrenceLastProcessed: undefined,
          note: t.note ? `${t.note} (Авто: ${getIntervalLabel(interval).toLowerCase()})` : `Регулярный платеж (${getIntervalLabel(interval).toLowerCase()})`,
        };

        newTransactions.push(generatedTx);
        hasChanges = true;

        // Calculate balance delta for wallets
        if (t.type === 'expense') {
          walletBalanceDeltas[t.walletId] = (walletBalanceDeltas[t.walletId] || 0) - t.amount;
        } else if (t.type === 'income') {
          walletBalanceDeltas[t.walletId] = (walletBalanceDeltas[t.walletId] || 0) + t.amount;
        } else if (t.type === 'transfer' && t.targetWalletId) {
          walletBalanceDeltas[t.walletId] = (walletBalanceDeltas[t.walletId] || 0) - t.amount;
          walletBalanceDeltas[t.targetWalletId] = (walletBalanceDeltas[t.targetWalletId] || 0) + t.amount;
        }
      }

      lastProcessed = nextDate;
      nextDate = calculateNextRecurrenceDate(nextDate, interval);
      occurrencesGenerated++;
    }

    // Update master recurring template record
    if (occurrencesGenerated > 0) {
      const template = updatedTransactionsMap.get(t.id);
      if (template) {
        template.recurrenceLastProcessed = lastProcessed;
        template.recurrenceNextDate = nextDate;
        updatedTransactionsMap.set(t.id, template);
      }
      notifications.push(
        `Проведено ${occurrencesGenerated} регулярных операций по шаблону "${t.category}" (${t.amount} ₽)`
      );
    }
  }

  if (!hasChanges && newTransactions.length === 0) {
    return {
      updatedTransactions: transactions,
      updatedWallets: wallets,
      processedCount: 0,
      generatedTransactions: [],
      notifications: [],
    };
  }

  // Update wallet balances with deltas
  const updatedWallets = wallets.map((w) => {
    const delta = walletBalanceDeltas[w.id];
    if (delta !== undefined && delta !== 0) {
      return { ...w, balance: w.balance + delta };
    }
    return w;
  });

  // Recombine all transactions (new spawned at the beginning + updated master templates)
  const combinedTransactions = [
    ...newTransactions,
    ...Array.from(updatedTransactionsMap.values()),
  ].sort((a, b) => {
    // Sort descending by date, then timestamp
    if (a.date !== b.date) {
      return b.date.localeCompare(a.date);
    }
    return b.timestamp - a.timestamp;
  });

  return {
    updatedTransactions: combinedTransactions,
    updatedWallets,
    processedCount: newTransactions.length,
    generatedTransactions: newTransactions,
    notifications,
  };
}
