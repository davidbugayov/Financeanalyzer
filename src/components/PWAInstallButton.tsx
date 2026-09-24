import React, { useState } from 'react';
import { Download, CheckCircle2, Smartphone, X, Share, PlusSquare } from 'lucide-react';
import { usePWAInstall } from '../hooks/usePWAInstall';

interface PWAInstallButtonProps {
  onNavigateToSettings?: () => void;
  className?: string;
}

export const PWAInstallButton: React.FC<PWAInstallButtonProps> = ({
  onNavigateToSettings,
  className = '',
}) => {
  const { isInstallable, isInstalled, browserInfo, install } = usePWAInstall();
  const [showIOSModal, setShowIOSModal] = useState(false);

  // If already running in standalone mode, hide prompt
  if (isInstalled) {
    return null;
  }

  const handleInstallClick = async () => {
    if (isInstallable) {
      await install();
    } else if (browserInfo.os === 'ios') {
      setShowIOSModal(true);
    } else if (onNavigateToSettings) {
      onNavigateToSettings();
    } else {
      setShowIOSModal(true);
    }
  };

  return (
    <>
      <button
        id="navbar_pwa_install_btn"
        type="button"
        onClick={handleInstallClick}
        className={`flex items-center gap-1.5 px-2.5 py-1.5 rounded-xl text-xs font-bold text-emerald-800 bg-emerald-50 hover:bg-emerald-100 border border-emerald-200 shadow-2xs transition-all cursor-pointer ${className}`}
        title="Установить Finance Analyzer на экран"
      >
        <Download size={14} className="text-emerald-700 stroke-[2.5]" />
        <span className="hidden lg:inline">Установить PWA</span>
      </button>

      {/* Quick iOS install guide popup */}
      {showIOSModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4 animate-in fade-in duration-150">
          <div className="bg-white rounded-3xl p-6 w-full max-w-sm shadow-2xl space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Smartphone size={20} className="text-emerald-600" />
                <h3 className="text-base font-extrabold text-slate-900">
                  Установка на iPhone / iPad
                </h3>
              </div>
              <button
                type="button"
                onClick={() => setShowIOSModal(false)}
                className="p-1 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100"
              >
                <X size={18} />
              </button>
            </div>

            <div className="space-y-3 text-xs text-slate-600">
              <div className="flex items-start gap-2.5 p-2.5 rounded-xl bg-slate-50 border border-slate-200">
                <Share size={18} className="text-blue-500 shrink-0 mt-0.5" />
                <span>
                  1. Нажмите иконку <strong>«Поделиться»</strong> (квадрат со стрелкой вверх) в нижней панели Safari.
                </span>
              </div>
              <div className="flex items-start gap-2.5 p-2.5 rounded-xl bg-slate-50 border border-slate-200">
                <PlusSquare size={18} className="text-emerald-500 shrink-0 mt-0.5" />
                <span>
                  2. Прокрутите меню и выберите <strong>«На экран "Домой"»</strong>.
                </span>
              </div>
              <div className="flex items-start gap-2.5 p-2.5 rounded-xl bg-slate-50 border border-slate-200">
                <CheckCircle2 size={18} className="text-emerald-600 shrink-0 mt-0.5" />
                <span>
                  3. Нажмите <strong>«Добавить»</strong> в правом верхнем углу.
                </span>
              </div>
            </div>

            <button
              type="button"
              onClick={() => setShowIOSModal(false)}
              className="w-full py-2.5 bg-emerald-600 text-white font-bold text-xs rounded-xl hover:bg-emerald-700 transition cursor-pointer"
            >
              Понятно
            </button>
          </div>
        </div>
      )}
    </>
  );
};
