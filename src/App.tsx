import React, { useState } from 'react';
import { FinanceProvider, useFinance } from './context/FinanceContext';
import { Navbar } from './components/Navbar';
import { BottomNav } from './components/BottomNav';
import { DashboardView } from './components/DashboardView';
import { HistoryView } from './components/HistoryView';
import { StatisticsView } from './components/StatisticsView';
import { WalletsView } from './components/WalletsView';
import { SettingsView } from './components/SettingsView';
import { TransactionModal, InitialForeignData } from './components/TransactionModal';
import { CurrencyConverterModal } from './components/CurrencyConverterModal';
import { AchievementsModal } from './components/AchievementsModal';
import { ImportExportModal } from './components/ImportExportModal';
import { PinLockScreen } from './components/PinLockScreen';
import { Transaction } from './types';
import { Trophy, X, Repeat } from 'lucide-react';

const AppContent: React.FC = () => {
  const {
    isLocked,
    activeTab,
    unlockedAchievementNotification,
    dismissAchievementNotification,
    recurringNotification,
    dismissRecurringNotification,
  } = useFinance();

  const [isTxModalOpen, setIsTxModalOpen] = useState(false);
  const [editingTx, setEditingTx] = useState<Transaction | null>(null);
  const [foreignDataForTx, setForeignDataForTx] = useState<InitialForeignData | null>(null);
  const [isConverterOpen, setIsConverterOpen] = useState(false);
  const [isAchievementsOpen, setIsAchievementsOpen] = useState(false);
  const [isImportExportOpen, setIsImportExportOpen] = useState(false);

  const handleOpenAddModal = () => {
    setEditingTx(null);
    setForeignDataForTx(null);
    setIsTxModalOpen(true);
  };

  const handleEditTransaction = (tx: Transaction) => {
    setEditingTx(tx);
    setForeignDataForTx(null);
    setIsTxModalOpen(true);
  };

  const handleOpenAddWithForeignData = (data: InitialForeignData) => {
    setEditingTx(null);
    setForeignDataForTx(data);
    setIsTxModalOpen(true);
  };

  if (isLocked) {
    return <PinLockScreen />;
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 flex flex-col antialiased selection:bg-emerald-500 selection:text-white">
      {/* Top Navigation */}
      <Navbar
        onOpenAddModal={handleOpenAddModal}
        onOpenAchievementsModal={() => setIsAchievementsOpen(true)}
        onOpenConverterModal={() => setIsConverterOpen(true)}
      />

      {/* Unlocked Achievement Toast Notification */}
      {unlockedAchievementNotification && (
        <div className="fixed top-20 right-4 z-50 max-w-sm bg-gradient-to-r from-amber-500 to-yellow-500 text-white p-4 rounded-2xl shadow-xl flex items-center gap-3 animate-in slide-in-from-top-5 duration-300">
          <div className="w-10 h-10 rounded-xl bg-white/20 flex items-center justify-center shrink-0">
            <Trophy size={20} className="text-white" />
          </div>
          <div className="min-w-0 flex-1">
            <span className="text-[10px] font-black uppercase tracking-wider text-amber-100 block">
              Достижение разблокировано!
            </span>
            <h4 className="text-xs font-bold truncate">
              {unlockedAchievementNotification.title}
            </h4>
          </div>
          <button
            onClick={dismissAchievementNotification}
            className="p-1 hover:bg-black/10 rounded-lg text-white/80 hover:text-white"
          >
            <X size={16} />
          </button>
        </div>
      )}

      {/* Scheduled Recurring Transactions Toast Notification */}
      {recurringNotification && (
        <div className="fixed top-20 right-4 z-50 max-w-sm bg-gradient-to-r from-teal-600 to-emerald-600 text-white p-4 rounded-2xl shadow-xl flex items-center gap-3 animate-in slide-in-from-top-5 duration-300">
          <div className="w-10 h-10 rounded-xl bg-white/20 flex items-center justify-center shrink-0">
            <Repeat size={20} className="text-white" />
          </div>
          <div className="min-w-0 flex-1">
            <span className="text-[10px] font-black uppercase tracking-wider text-emerald-100 block">
              Автоматическое расписание
            </span>
            <h4 className="text-xs font-bold truncate">
              {recurringNotification}
            </h4>
          </div>
          <button
            onClick={dismissRecurringNotification}
            className="p-1 hover:bg-black/10 rounded-lg text-white/80 hover:text-white"
            title="Закрыть"
          >
            <X size={16} />
          </button>
        </div>
      )}

      {/* Main Container */}
      <main className="flex-1 max-w-4xl w-full mx-auto px-4 sm:px-6 pt-5 pb-16">
        {activeTab === 'home' && (
          <DashboardView
            onOpenAddModal={handleOpenAddModal}
            onEditTransaction={handleEditTransaction}
            onOpenConverterModal={() => setIsConverterOpen(true)}
          />
        )}
        {activeTab === 'history' && (
          <HistoryView
            onEditTransaction={handleEditTransaction}
            onOpenAddModal={handleOpenAddModal}
            onOpenConverterModal={() => setIsConverterOpen(true)}
          />
        )}
        {activeTab === 'stats' && <StatisticsView />}
        {activeTab === 'wallets' && <WalletsView />}
        {activeTab === 'profile' && (
          <SettingsView
            onOpenImportExport={() => setIsImportExportOpen(true)}
            onOpenAchievements={() => setIsAchievementsOpen(true)}
          />
        )}
      </main>

      {/* Bottom Tab Navigation */}
      <BottomNav />

      {/* Modals */}
      <TransactionModal
        isOpen={isTxModalOpen}
        onClose={() => {
          setIsTxModalOpen(false);
          setEditingTx(null);
          setForeignDataForTx(null);
        }}
        initialTransaction={editingTx}
        initialForeignData={foreignDataForTx}
      />

      <CurrencyConverterModal
        isOpen={isConverterOpen}
        onClose={() => setIsConverterOpen(false)}
        onOpenAddTransactionWithData={handleOpenAddWithForeignData}
      />

      <AchievementsModal
        isOpen={isAchievementsOpen}
        onClose={() => setIsAchievementsOpen(false)}
      />

      <ImportExportModal
        isOpen={isImportExportOpen}
        onClose={() => setIsImportExportOpen(false)}
      />
    </div>
  );
};

export function App() {
  return (
    <FinanceProvider>
      <AppContent />
    </FinanceProvider>
  );
}

export default App;
