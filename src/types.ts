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
}

export type PeriodType = 'day' | 'week' | 'month' | 'year' | 'all';
export type GroupingType = 'date' | 'month' | 'category' | 'wallet';

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
