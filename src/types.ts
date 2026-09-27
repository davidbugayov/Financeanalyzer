export type TransactionType = 'expense' | 'income' | 'transfer';

export interface Category {
  id: string;
  name: string;
  key?: string;
  icon: string;
  color: string;
  isExpense: boolean;
  subcategories: string[];
}

export type WalletType = 'card' | 'cash' | 'savings' | 'investment' | 'goal' | 'other';

export interface Wallet {
  id: string;
  name: string;
  type: WalletType;
  balance: number;
  limit: number;
  spent?: number;
  color: string;
  icon: string;
  goalAmount?: number;
  goalDate?: string;
  parentWalletId?: string;
  autoSavingsPercent?: number; // % of income automatically routed here
}

export type RecurrenceInterval = 'daily' | 'weekly' | 'biweekly' | 'monthly' | 'yearly';

export interface Transaction {
  id: string;
  amount: number;
  type: TransactionType;
  category: string;
  categoryId?: string;
  subcategory?: string;
  date: string; // YYYY-MM-DD
  timestamp: number;
  walletId: string;
  targetWalletId?: string; // For transfers
  note?: string;
  // Foreign currency / travel expense fields
  isForeignCurrency?: boolean;
  originalAmount?: number;
  originalCurrency?: string; // e.g. 'TRY', 'THB', 'USD', 'EUR', 'AED'
  exchangeRate?: number; // exchange rate used to convert to base currency
  country?: string; // e.g. 'Турция 🇹🇷', 'Таиланд 🇹🇭', etc.
  city?: string; // e.g. 'Стамбул', 'Бангкок'
  tags?: string[]; // Custom tags for projects, trips, or interests (e.g. ['отпуск', 'ремонт', 'проект-х'])
  // Recurring transaction fields
  isRecurring?: boolean;
  recurrenceInterval?: RecurrenceInterval;
  recurrenceNextDate?: string; // YYYY-MM-DD
  recurrenceEndDate?: string; // YYYY-MM-DD
  recurrenceLastProcessed?: string; // YYYY-MM-DD
  parentRecurringId?: string; // links auto-generated instances to the recurring template
}

export interface CategoryBudget {
  categoryId: string;
  limit: number;
}

export type PeriodType = 'day' | 'week' | 'month' | 'year' | 'all';
export type GroupingType = 'date' | 'month' | 'category' | 'wallet' | 'tag';

export type AchievementCategory = 'GENERAL' | 'TRANSACTIONS' | 'ANALYTICS' | 'BUDGET' | 'EXPORT_IMPORT' | 'SECURITY';
export type AchievementRarity = 'COMMON' | 'RARE' | 'EPIC' | 'LEGENDARY';

export interface Achievement {
  id: string;
  title: string;
  description: string;
  currentProgress: number;
  targetProgress: number;
  isUnlocked: boolean;
  dateUnlocked?: string;
  category: AchievementCategory;
  rarity: AchievementRarity;
  icon: string;
}

export interface CurrencyConfig {
  code: string;
  symbol: string;
  name: string;
  rate: number; // relative to RUB
}

export interface FinancialHealthBreakdown {
  score: number;
  savingsRateScore: number;
  expenseControlScore: number;
  stabilityScore: number;
  diversificationScore: number;
  recommendations: string[];
}

export interface SpendingProjectionMonth {
  monthKey: string; // e.g. "2026-10"
  label: string; // e.g. "Окт 2026"
  fullLabel: string; // e.g. "Октябрь 2026"
  projectedExpense: number;
  lowerBound: number; // optimistic estimate
  upperBound: number; // pessimistic estimate
  baselineExpense: number;
  recurringCommitment: number;
  confidenceScore: number; // 0-100%
}

export interface SpendingChartPoint {
  monthKey: string;
  label: string;
  fullLabel: string;
  actualExpense?: number;
  projectedExpense?: number;
  lowerBound?: number;
  upperBound?: number;
  isForecast: boolean;
  isAnchor?: boolean;
}

export interface SpendingProjection {
  projectedMonths: SpendingProjectionMonth[];
  historicalMonths: { monthKey: string; label: string; actualExpense: number }[];
  combinedChartData: SpendingChartPoint[];
  totalThreeMonthProjected: number;
  averageMonthlyProjected: number;
  trendPercentage: number;
  recurringCommitmentsTotal: number;
  methodology: string;
}
