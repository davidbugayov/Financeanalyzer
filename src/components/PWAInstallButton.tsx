import React, { useState } from 'react';
import {
  Download,
  CheckCircle2,
  Smartphone,
  X,
  Share,
  PlusSquare,
  ExternalLink,
  Copy,
  Check,
  MoreVertical,
  Laptop,
  Zap,
  WifiOff,
  ShieldCheck,
} from 'lucide-react';
import { usePWAInstall } from '../hooks/usePWAInstall';

interface PWAInstallButtonProps {
  onNavigateToSettings?: () => void;
  className?: string;
}

export const PWAInstallButton: React.FC<PWAInstallButtonProps> = ({
  className = '',
}) => {
  const { isInstallable, isInstalled, browserInfo, install } = usePWAInstall();
  const [showModal, setShowModal] = useState(false);
  const [copied, setCopied] = useState(false);
  const [isInstalling, setIsInstalling] = useState(false);

  // If already running in standalone mode, show subtle checkmark or hide
  if (isInstalled) {
    return (
      <div
        className="flex items-center gap-1 px-2 py-1 rounded-xl text-[11px] font-bold text-emerald-800 bg-emerald-50/80 border border-emerald-200"
        title="Приложение уже установлено и работает как PWA"
      >
        <CheckCircle2 size={13} className="text-emerald-600 shrink-0" />
        <span className="hidden xl:inline">PWA</span>
      </div>
    );
  }

  const isInsideIframe =
    typeof window !== 'undefined' &&
    (window.self !== window.top ||
      window.location.hostname.includes('google.com') ||
      document.referrer.includes('aistudio'));

  const handleInstallClick = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    // If native prompt is primed and we are not stuck in an iframe, attempt direct install
    if (isInstallable && !isInsideIframe) {
      setIsInstalling(true);
      try {
        const success = await install();
        if (!success) {
          setShowModal(true);
        }
      } catch {
        setShowModal(true);
      } finally {
        setIsInstalling(false);
      }
    } else {
      // Always show the install guide modal with exact instructions for their platform!
      setShowModal(true);
    }
  };

  const handleDirectInstallPrompt = async () => {
    if (isInstallable) {
      setIsInstalling(true);
      try {
        const success = await install();
        if (success) {
          setShowModal(false);
        }
      } finally {
        setIsInstalling(false);
      }
    }
  };

  const handleCopyLink = () => {
    if (typeof window !== 'undefined') {
      navigator.clipboard.writeText(window.location.href);
      setCopied(true);
      setTimeout(() => setCopied(false), 2500);
    }
  };

  const handleOpenStandalone = () => {
    if (typeof window !== 'undefined') {
      window.open(window.location.href, '_blank', 'noopener,noreferrer');
    }
  };

  return (
    <>
      <button
        id="navbar_pwa_install_btn"
        type="button"
        onClick={handleInstallClick}
        disabled={isInstalling}
        className={`flex items-center gap-1.5 px-2.5 py-1.5 rounded-xl text-xs font-bold text-emerald-800 bg-emerald-50 hover:bg-emerald-100 active:scale-95 border border-emerald-300/80 shadow-xs hover:shadow-md transition-all cursor-pointer min-w-[36px] min-h-[36px] justify-center ${className}`}
        title="Установить Finance Analyzer на рабочий стол"
        aria-label="Установить приложение"
      >
        <Download size={15} className="text-emerald-700 stroke-[2.5] shrink-0" />
        <span className="hidden md:inline font-bold">Установить</span>
      </button>

      {/* Universal PWA Install Modal */}
      {showModal && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4 animate-in fade-in duration-200"
          onClick={(e) => {
            if (e.target === e.currentTarget) setShowModal(false);
          }}
        >
          <div className="bg-white rounded-3xl p-5 sm:p-6 w-full max-w-md shadow-2xl space-y-4 max-h-[92vh] overflow-y-auto">
            {/* Modal Header */}
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <div className="flex items-center gap-2.5">
                <div className="w-10 h-10 rounded-2xl bg-emerald-100 text-emerald-700 flex items-center justify-center shrink-0">
                  <Download size={20} className="stroke-[2.5]" />
                </div>
                <div>
                  <h3 className="text-base font-extrabold text-slate-900 leading-snug">
                    Установка на экран
                  </h3>
                  <div className="flex items-center gap-1.5 mt-0.5">
                    <span className="text-[11px] font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-md border border-emerald-200">
                      {browserInfo.osName} • {browserInfo.name}
                    </span>
                  </div>
                </div>
              </div>
              <button
                type="button"
                id="close_pwa_modal_btn"
                onClick={() => setShowModal(false)}
                className="p-1.5 rounded-xl text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors cursor-pointer"
              >
                <X size={20} />
              </button>
            </div>

            {/* If direct browser prompt is primed, give 1-click install button */}
            {isInstallable && (
              <div className="p-3 bg-gradient-to-r from-emerald-500 to-teal-600 rounded-2xl text-white shadow-md">
                <div className="flex items-center justify-between gap-3">
                  <div className="text-xs">
                    <p className="font-extrabold text-sm">Браузер готов к установке</p>
                    <p className="text-emerald-100 text-[11px]">Нажмите для мгновенной установки</p>
                  </div>
                  <button
                    type="button"
                    onClick={handleDirectInstallPrompt}
                    disabled={isInstalling}
                    className="px-4 py-2 bg-white text-emerald-800 rounded-xl font-black text-xs hover:bg-emerald-50 shadow-sm cursor-pointer whitespace-nowrap"
                  >
                    {isInstalling ? 'Установка...' : 'Установить'}
                  </button>
                </div>
              </div>
            )}

            {/* If running inside iframe / preview mode */}
            {isInsideIframe && (
              <div className="p-3.5 bg-amber-50/90 border border-amber-200 rounded-2xl space-y-2 text-xs text-amber-900">
                <div className="flex items-start gap-2">
                  <ExternalLink size={16} className="text-amber-600 shrink-0 mt-0.5" />
                  <p className="font-medium text-[11px] leading-relaxed">
                    Вы открыли приложение во фрейме Google AI Studio. Чтобы браузер Chrome позволил установить значок на рабочий стол, откройте приложение в отдельной вкладке:
                  </p>
                </div>
                <div className="flex gap-2 pt-1">
                  <button
                    type="button"
                    onClick={handleOpenStandalone}
                    className="flex-1 flex items-center justify-center gap-1.5 py-2 px-3 bg-amber-600 hover:bg-amber-700 text-white font-bold text-xs rounded-xl shadow-xs cursor-pointer transition-colors"
                  >
                    <ExternalLink size={13} />
                    <span>Открыть во вкладке Chrome</span>
                  </button>
                  <button
                    type="button"
                    onClick={handleCopyLink}
                    className="py-2 px-3 bg-white hover:bg-amber-100 text-amber-800 border border-amber-300 font-bold text-xs rounded-xl cursor-pointer transition-colors flex items-center gap-1"
                    title="Скопировать ссылку"
                  >
                    {copied ? <Check size={14} className="text-emerald-600" /> : <Copy size={14} />}
                    <span>{copied ? 'Скопировано!' : 'Копия'}</span>
                  </button>
                </div>
              </div>
            )}

            {/* Platform-specific instructions */}
            <div className="space-y-2.5">
              <h4 className="text-xs font-bold uppercase tracking-wider text-slate-500">
                Пошаговая инструкция для вашего устройства:
              </h4>

              {browserInfo.os === 'android' ? (
                /* Android Chrome / Samsung Instructions */
                <div className="space-y-2 text-xs text-slate-700">
                  <div className="flex items-start gap-3 p-3 rounded-2xl bg-slate-50 border border-slate-200/90">
                    <div className="w-7 h-7 rounded-xl bg-blue-100 text-blue-700 flex items-center justify-center shrink-0 font-black text-xs">
                      <MoreVertical size={16} />
                    </div>
                    <div>
                      <strong className="block text-slate-900 text-[13px]">1. Меню Chrome</strong>
                      <span className="text-slate-600 text-[11px] leading-relaxed">
                        В правом верхнем углу экрана браузера нажмите на значок <strong>«три точки» (⋮)</strong>.
                      </span>
                    </div>
                  </div>

                  <div className="flex items-start gap-3 p-3 rounded-2xl bg-slate-50 border border-slate-200/90">
                    <div className="w-7 h-7 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center shrink-0 font-black text-xs">
                      <Download size={16} />
                    </div>
                    <div>
                      <strong className="block text-slate-900 text-[13px]">
                        2. «Установить приложение»
                      </strong>
                      <span className="text-slate-600 text-[11px] leading-relaxed">
                        Выберите в списке пункт <strong>«Установить приложение»</strong> или <strong>«Добавить на главный экран»</strong>.
                      </span>
                    </div>
                  </div>

                  <div className="flex items-start gap-3 p-3 rounded-2xl bg-slate-50 border border-slate-200/90">
                    <div className="w-7 h-7 rounded-xl bg-purple-100 text-purple-700 flex items-center justify-center shrink-0 font-black text-xs">
                      <CheckCircle2 size={16} />
                    </div>
                    <div>
                      <strong className="block text-slate-900 text-[13px]">3. Подтвердите установку</strong>
                      <span className="text-slate-600 text-[11px] leading-relaxed">
                        Нажмите <strong>«Установить»</strong> в диалоговом окне. Иконка появится на рабочем столе и в списке приложений.
                      </span>
                    </div>
                  </div>
                </div>
              ) : browserInfo.os === 'ios' ? (
                /* iOS Safari Instructions */
                <div className="space-y-2 text-xs text-slate-700">
                  <div className="flex items-start gap-3 p-3 rounded-2xl bg-slate-50 border border-slate-200/90">
                    <div className="w-7 h-7 rounded-xl bg-blue-100 text-blue-700 flex items-center justify-center shrink-0">
                      <Share size={15} />
                    </div>
                    <div>
                      <strong className="block text-slate-900 text-[13px]">1. Кнопка «Поделиться»</strong>
                      <span className="text-slate-600 text-[11px]">
                        В нижней панели Safari нажмите значок квадрата со стрелкой вверх.
                      </span>
                    </div>
                  </div>

                  <div className="flex items-start gap-3 p-3 rounded-2xl bg-slate-50 border border-slate-200/90">
                    <div className="w-7 h-7 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center shrink-0">
                      <PlusSquare size={15} />
                    </div>
                    <div>
                      <strong className="block text-slate-900 text-[13px]">2. «На экран "Домой"»</strong>
                      <span className="text-slate-600 text-[11px]">
                        Прокрутите меню вниз и выберите <strong>«На экран "Домой"»</strong> (+).
                      </span>
                    </div>
                  </div>

                  <div className="flex items-start gap-3 p-3 rounded-2xl bg-slate-50 border border-slate-200/90">
                    <div className="w-7 h-7 rounded-xl bg-purple-100 text-purple-700 flex items-center justify-center shrink-0">
                      <CheckCircle2 size={15} />
                    </div>
                    <div>
                      <strong className="block text-slate-900 text-[13px]">3. Нажмите «Добавить»</strong>
                      <span className="text-slate-600 text-[11px]">
                        В правом верхнем углу нажмите <strong>«Добавить»</strong>.
                      </span>
                    </div>
                  </div>
                </div>
              ) : (
                /* Desktop Chrome / Edge Instructions */
                <div className="space-y-2 text-xs text-slate-700">
                  <div className="flex items-start gap-3 p-3 rounded-2xl bg-slate-50 border border-slate-200/90">
                    <div className="w-7 h-7 rounded-xl bg-blue-100 text-blue-700 flex items-center justify-center shrink-0">
                      <Laptop size={15} />
                    </div>
                    <div>
                      <strong className="block text-slate-900 text-[13px]">
                        Значок в адресной строке
                      </strong>
                      <span className="text-slate-600 text-[11px]">
                        В правой части адресной строки браузера нажмите на значок компьютера со стрелкой (или меню ⋮ ➔ «Установить Finance Analyzer»).
                      </span>
                    </div>
                  </div>
                </div>
              )}
            </div>

            {/* PWA Advantages */}
            <div className="grid grid-cols-3 gap-2 pt-2 border-t border-slate-100">
              <div className="flex flex-col items-center text-center p-2 rounded-xl bg-slate-50">
                <Zap size={16} className="text-amber-500 mb-1" />
                <span className="text-[10px] font-bold text-slate-700">Быстрый запуск</span>
              </div>
              <div className="flex flex-col items-center text-center p-2 rounded-xl bg-slate-50">
                <WifiOff size={16} className="text-blue-500 mb-1" />
                <span className="text-[10px] font-bold text-slate-700">Работает офлайн</span>
              </div>
              <div className="flex flex-col items-center text-center p-2 rounded-xl bg-slate-50">
                <ShieldCheck size={16} className="text-emerald-500 mb-1" />
                <span className="text-[10px] font-bold text-slate-700">Все данные в памяти</span>
              </div>
            </div>

            {/* Close Button */}
            <button
              type="button"
              id="confirm_pwa_modal_btn"
              onClick={() => setShowModal(false)}
              className="w-full py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-xl shadow-xs transition-colors cursor-pointer"
            >
              Понятно
            </button>
          </div>
        </div>
      )}
    </>
  );
};
