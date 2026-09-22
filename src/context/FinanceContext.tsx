import React, { createContext, useContext, useState, useEffect, useRef, useCallback } from 'react';
import confetti from 'canvas-confetti';
import {
  Transaction,
  Wallet,
  Category,
  Achievement,
  CurrencyConfig,
  TransactionType,
} from '../types';
import {
  DEFAULT_CATEGORIES,
  DEFAULT_WALLETS,
  DEFAULT_ACHIEVEMENTS,
  INITIAL_TRANSACTIONS,
  CURRENCIES,
  DEFAULT_CATEGORY_BUDGETS,
} from '../data/initialData';
import {
  processScheduledRecurrences,
  calculateNextRecurrenceDate,
} from '../utils/recurringProcessor';

interface FinanceContextType {
  transactions: Transaction[];
  wallets: Wallet[];
  categories: Category[];
  categoryBudgets: Record<string, number>;
  achievements: Achievement[];
  currency: CurrencyConfig;
  language: 'ru' | 'en';
  isLocked: boolean;
  pinCode: string | null;
  activeTab: 'home' | 'history' | 'stats' | 'wallets' | 'profile';
  setActiveTab: (tab: 'home' | 'history' | 'stats' | 'wallets' | 'profile') => void;
  setCurrency: (code: string) => void;
  setLanguage: (lang: 'ru' | 'en') => void;
  setPinCode: (pin: string | null) => void;
  unlockApp: (pin: string) => boolean;
  lockApp: () => void;
  addTransaction: (tx: Omit<Transaction, 'id' | 'timestamp'>) => void;
  updateTransaction: (tx: Transaction) => void;
  deleteTransaction: (id: string) => void;
  addWallet: (wallet: Omit<Wallet, 'id'>) => void;
  updateWallet: (wallet: Wallet) => void;
  deleteWallet: (id: string) => void;
  transferBetweenWallets: (fromId: string, toId: string, amount: number, note?: string) => void;
  addCategory: (category: Omit<Category, 'id'>) => void;
  addSubcategory: (categoryId: string, subcategory: string) => void;
  setCategoryBudget: (categoryId: string, limit: number) => void;
  removeCategoryBudget: (categoryId: string) => void;
  processRecurring: () => void;
  recurringNotification: string | null;
  dismissRecurringNotification: () => void;
  importTransactions: (newTxs: Transaction[]) => void;
  resetAllData: () => void;
  loadDemoData: () => void;
  unlockedAchievementNotification: Achievement | null;
  dismissAchievementNotification: () => void;
}

const FinanceContext = createContext<FinanceContextType | undefined>(undefined);

export const FinanceProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // 1. Hydrate state from localStorage or default
  const [transactions, setTransactions] = useState<Transaction[]>(() => {
    const saved = localStorage.getItem('fa_transactions');
    return saved ? JSON.parse(saved) : INITIAL_TRANSACTIONS;
  });

  const [wallets, setWallets] = useState<Wallet[]>(() => {
    const saved = localStorage.getItem('fa_wallets');
    return saved ? JSON.parse(saved) : DEFAULT_WALLETS;
  });

  const [categories, setCategories] = useState<Category[]>(() => {
    const saved = localStorage.getItem('fa_categories');
    return saved ? JSON.parse(saved) : DEFAULT_CATEGORIES;
  });

  const [categoryBudgets, setCategoryBudgets] = useState<Record<string, number>>(() => {
    const saved = localStorage.getItem('fa_category_budgets');
    return saved ? JSON.parse(saved) : DEFAULT_CATEGORY_BUDGETS;
  });

  const [achievements, setAchievements] = useState<Achievement[]>(() => {
    const saved = localStorage.getItem('fa_achievements');
    return saved ? JSON.parse(saved) : DEFAULT_ACHIEVEMENTS;
  });

  const [currencyCode, setCurrencyCode] = useState<string>(() => {
    return localStorage.getItem('fa_currency') || 'RUB';
  });

  const [language, setLanguageState] = useState<'ru' | 'en'>(() => {
    return (localStorage.getItem('fa_language') as 'ru' | 'en') || 'ru';
  });

  const [pinCode, setPinCodeState] = useState<string | null>(() => {
    return localStorage.getItem('fa_pin') || null;
  });

  const [isLocked, setIsLocked] = useState<boolean>(() => {
    const pin = localStorage.getItem('fa_pin');
    return Boolean(pin);
  });

  const [activeTab, setActiveTab] = useState<'home' | 'history' | 'stats' | 'wallets' | 'profile'>('home');
  const [unlockedAchievementNotification, setUnlockedAchievementNotification] = useState<Achievement | null>(null);
  const [recurringNotification, setRecurringNotification] = useState<string | null>(null);

  // References to avoid stale closures in background recurrence task
  const transactionsRef = useRef(transactions);
  transactionsRef.current = transactions;
  const walletsRef = useRef(wallets);
  walletsRef.current = wallets;

  // Background processor for scheduled recurring transactions
  const processRecurring = useCallback(() => {
    const result = processScheduledRecurrences(transactionsRef.current, walletsRef.current);
    if (result.processedCount > 0) {
      setTransactions(result.updatedTransactions);
      setWallets(result.updatedWallets);
      setRecurringNotification(
        `Автоматически проведено ${result.processedCount} регулярных операций`
      );
    }
  }, []);

  // Background task to automatically check and process due recurring transactions
  useEffect(() => {
    // Initial check after app hydration
    const timer = setTimeout(() => {
      processRecurring();
    }, 1200);

    // Periodic background run every 45 seconds
    const interval = setInterval(() => {
      processRecurring();
    }, 45000);

    return () => {
      clearTimeout(timer);
      clearInterval(interval);
    };
  }, [processRecurring]);

  const dismissRecurringNotification = () => {
    setRecurringNotification(null);
  };

  // Sync to localStorage
  useEffect(() => {
    localStorage.setItem('fa_transactions', JSON.stringify(transactions));
  }, [transactions]);

  useEffect(() => {
    localStorage.setItem('fa_wallets', JSON.stringify(wallets));
  }, [wallets]);

  useEffect(() => {
    localStorage.setItem('fa_categories', JSON.stringify(categories));
  }, [categories]);

  useEffect(() => {
    localStorage.setItem('fa_category_budgets', JSON.stringify(categoryBudgets));
  }, [categoryBudgets]);

  useEffect(() => {
    localStorage.setItem('fa_achievements', JSON.stringify(achievements));
  }, [achievements]);

  useEffect(() => {
    localStorage.setItem('fa_currency', currencyCode);
  }, [currencyCode]);

  useEffect(() => {
    localStorage.setItem('fa_language', language);
  }, [language]);

  const currency = CURRENCIES.find((c) => c.code === currencyCode) || CURRENCIES[0];

  const setCurrency = (code: string) => {
    setCurrencyCode(code);
  };

  const setLanguage = (lang: 'ru' | 'en') => {
    setLanguageState(lang);
  };

  const setPinCode = (pin: string | null) => {
    if (pin) {
      localStorage.setItem('fa_pin', pin);
      setPinCodeState(pin);
      triggerAchievementUnlock('security_sentinel');
    } else {
      localStorage.removeItem('fa_pin');
      setPinCodeState(null);
      setIsLocked(false);
    }
  };

  const unlockApp = (enteredPin: string) => {
    if (enteredPin === pinCode) {
      setIsLocked(false);
      return true;
    }
    return false;
  };

  const lockApp = () => {
    if (pinCode) {
      setIsLocked(true);
    }
  };

  const triggerAchievementUnlock = (achievementId: string) => {
    setAchievements((prev) =>
      prev.map((ach) => {
        if (ach.id === achievementId && !ach.isUnlocked) {
          const unlocked = {
            ...ach,
            isUnlocked: true,
            currentProgress: ach.targetProgress,
            dateUnlocked: new Date().toISOString().split('T')[0],
          };
          setUnlockedAchievementNotification(unlocked);
          try {
            confetti({
              particleCount: 60,
              spread: 70,
              origin: { y: 0.7 },
            });
          } catch {
            // Ignore if confetti fails
          }
          return unlocked;
        }
        return ach;
      })
    );
  };

  const checkAchievementsAfterTx = (updatedTxs: Transaction[]) => {
    if (updatedTxs.length >= 1) {
      triggerAchievementUnlock('first_steps');
    }
    if (updatedTxs.length >= 50) {
      triggerAchievementUnlock('transaction_master');
    }
  };

  // 2. Add Transaction (handles wallet balances & auto-savings allocation!)
  const addTransaction = (txData: Omit<Transaction, 'id' | 'timestamp'>) => {
    const id = `tx_${Date.now()}_${Math.random().toString(36).substr(2, 4)}`;
    const timestamp = new Date(txData.date).getTime() || Date.now();
    const newTx: Transaction = { ...txData, id, timestamp };

    // Set recurring defaults if recurring is enabled
    if (newTx.isRecurring && newTx.recurrenceInterval) {
      if (!newTx.recurrenceNextDate) {
        newTx.recurrenceNextDate = calculateNextRecurrenceDate(newTx.date, newTx.recurrenceInterval);
      }
      if (!newTx.recurrenceLastProcessed) {
        newTx.recurrenceLastProcessed = newTx.date;
      }
    }

    // Update wallet balances
    setWallets((prevWallets) => {
      return prevWallets.map((w) => {
        if (newTx.type === 'expense' && w.id === newTx.walletId) {
          return { ...w, balance: w.balance - newTx.amount };
        }
        if (newTx.type === 'income' && w.id === newTx.walletId) {
          return { ...w, balance: w.balance + newTx.amount };
        }
        if (newTx.type === 'transfer') {
          if (w.id === newTx.walletId) {
            return { ...w, balance: w.balance - newTx.amount };
          }
          if (w.id === newTx.targetWalletId) {
            return { ...w, balance: w.balance + newTx.amount };
          }
        }
        return w;
      });
    });

    const newTransactionsList = [newTx, ...transactions];
    setTransactions(newTransactionsList);
    checkAchievementsAfterTx(newTransactionsList);

    // Auto-savings logic: If this is an income transaction, check if any savings wallet has autoSavingsPercent!
    if (newTx.type === 'income') {
      const savingsWallet = wallets.find((w) => w.type === 'savings' && (w.autoSavingsPercent || 0) > 0);
      if (savingsWallet && savingsWallet.id !== newTx.walletId) {
        const percent = savingsWallet.autoSavingsPercent || 10;
        const autoSavingsAmount = Math.round((newTx.amount * percent) / 100);
        if (autoSavingsAmount > 0) {
          setTimeout(() => {
            transferBetweenWallets(
              newTx.walletId,
              savingsWallet.id,
              autoSavingsAmount,
              `Автосбережение ${percent}% от дохода`
            );
          }, 300);
        }
      }
    }
  };

  const updateTransaction = (updatedTx: Transaction) => {
    const oldTx = transactions.find((t) => t.id === updatedTx.id);
    if (!oldTx) return;

    const txToSave = { ...updatedTx };
    if (txToSave.isRecurring && txToSave.recurrenceInterval && !txToSave.recurrenceNextDate) {
      txToSave.recurrenceNextDate = calculateNextRecurrenceDate(txToSave.date, txToSave.recurrenceInterval);
    }

    // Revert old effect on wallets, apply new
    setWallets((prevWallets) => {
      return prevWallets.map((w) => {
        let bal = w.balance;
        // Revert old
        if (oldTx.type === 'expense' && w.id === oldTx.walletId) bal += oldTx.amount;
        if (oldTx.type === 'income' && w.id === oldTx.walletId) bal -= oldTx.amount;
        if (oldTx.type === 'transfer') {
          if (w.id === oldTx.walletId) bal += oldTx.amount;
          if (w.id === oldTx.targetWalletId) bal -= oldTx.amount;
        }

        // Apply new
        if (txToSave.type === 'expense' && w.id === txToSave.walletId) bal -= txToSave.amount;
        if (txToSave.type === 'income' && w.id === txToSave.walletId) bal += txToSave.amount;
        if (txToSave.type === 'transfer') {
          if (w.id === txToSave.walletId) bal -= txToSave.amount;
          if (w.id === txToSave.targetWalletId) bal += txToSave.amount;
        }

        return { ...w, balance: bal };
      });
    });

    setTransactions((prev) => prev.map((t) => (t.id === txToSave.id ? txToSave : t)));
  };

  const deleteTransaction = (id: string) => {
    const oldTx = transactions.find((t) => t.id === id);
    if (!oldTx) return;

    // Revert wallet balance
    setWallets((prevWallets) => {
      return prevWallets.map((w) => {
        let bal = w.balance;
        if (oldTx.type === 'expense' && w.id === oldTx.walletId) bal += oldTx.amount;
        if (oldTx.type === 'income' && w.id === oldTx.walletId) bal -= oldTx.amount;
        if (oldTx.type === 'transfer') {
          if (w.id === oldTx.walletId) bal += oldTx.amount;
          if (w.id === oldTx.targetWalletId) bal -= oldTx.amount;
        }
        return { ...w, balance: bal };
      });
    });

    setTransactions((prev) => prev.filter((t) => t.id !== id));
  };

  const addWallet = (walletData: Omit<Wallet, 'id'>) => {
    const id = `w_${Date.now()}`;
    setWallets((prev) => [...prev, { ...walletData, id }]);
  };

  const updateWallet = (wallet: Wallet) => {
    setWallets((prev) => prev.map((w) => (w.id === wallet.id ? wallet : w)));
  };

  const deleteWallet = (id: string) => {
    setWallets((prev) => prev.filter((w) => w.id !== id));
  };

  const transferBetweenWallets = (fromId: string, toId: string, amount: number, note?: string) => {
    if (fromId === toId || amount <= 0) return;

    setWallets((prev) =>
      prev.map((w) => {
        if (w.id === fromId) return { ...w, balance: w.balance - amount };
        if (w.id === toId) return { ...w, balance: w.balance + amount };
        return w;
      })
    );

    const newTx: Transaction = {
      id: `tx_${Date.now()}_tr`,
      amount,
      type: 'transfer',
      category: 'Перевод',
      date: new Date().toISOString().split('T')[0],
      timestamp: Date.now(),
      walletId: fromId,
      targetWalletId: toId,
      note: note || 'Перевод между кошельками',
    };

    setTransactions((prev) => [newTx, ...prev]);
  };

  const addCategory = (categoryData: Omit<Category, 'id'>) => {
    const id = `cat_${Date.now()}`;
    setCategories((prev) => [...prev, { ...categoryData, id }]);
  };

  const addSubcategory = (categoryId: string, subcategory: string) => {
    setCategories((prev) =>
      prev.map((c) => {
        if (c.id === categoryId && !c.subcategories.includes(subcategory)) {
          return { ...c, subcategories: [...c.subcategories, subcategory] };
        }
        return c;
      })
    );
  };

  const setCategoryBudget = (categoryId: string, limit: number) => {
    setCategoryBudgets((prev) => {
      if (limit <= 0) {
        const next = { ...prev };
        delete next[categoryId];
        return next;
      }
      return { ...prev, [categoryId]: limit };
    });
  };

  const removeCategoryBudget = (categoryId: string) => {
    setCategoryBudgets((prev) => {
      const next = { ...prev };
      delete next[categoryId];
      return next;
    });
  };

  const importTransactions = (newTxs: Transaction[]) => {
    if (newTxs.length === 0) return;
    setTransactions((prev) => [...newTxs, ...prev]);
    triggerAchievementUnlock('bank_importer');
  };

  const resetAllData = () => {
    setTransactions([]);
    setWallets(DEFAULT_WALLETS.map((w) => ({ ...w, balance: 0 })));
    setCategoryBudgets({});
    localStorage.removeItem('fa_transactions');
    localStorage.removeItem('fa_wallets');
    localStorage.removeItem('fa_category_budgets');
  };

  const loadDemoData = () => {
    setTransactions(INITIAL_TRANSACTIONS);
    setWallets(DEFAULT_WALLETS);
    setCategories(DEFAULT_CATEGORIES);
    setCategoryBudgets(DEFAULT_CATEGORY_BUDGETS);
    setAchievements(DEFAULT_ACHIEVEMENTS);
  };

  const dismissAchievementNotification = () => {
    setUnlockedAchievementNotification(null);
  };

  return (
    <FinanceContext.Provider
      value={{
        transactions,
        wallets,
        categories,
        categoryBudgets,
        achievements,
        currency,
        language,
        isLocked,
        pinCode,
        activeTab,
        setActiveTab,
        setCurrency,
        setLanguage,
        setPinCode,
        unlockApp,
        lockApp,
        addTransaction,
        updateTransaction,
        deleteTransaction,
        addWallet,
        updateWallet,
        deleteWallet,
        transferBetweenWallets,
        addCategory,
        addSubcategory,
        setCategoryBudget,
        removeCategoryBudget,
        processRecurring,
        recurringNotification,
        dismissRecurringNotification,
        importTransactions,
        resetAllData,
        loadDemoData,
        unlockedAchievementNotification,
        dismissAchievementNotification,
      }}
    >
      {children}
    </FinanceContext.Provider>
  );
};

export const useFinance = () => {
  const context = useContext(FinanceContext);
  if (!context) {
    throw new Error('useFinance must be used within a FinanceProvider');
  }
  return context;
};
