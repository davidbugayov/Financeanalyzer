import React, { useState } from 'react';
import {
  Download,
  Smartphone,
  Laptop,
  CheckCircle2,
  Share,
  PlusSquare,
  Compass,
  Globe,
  Sparkles,
  Zap,
  WifiOff,
  ShieldCheck,
  ChevronRight,
  ExternalLink,
} from 'lucide-react';
import { usePWAInstall, DetectedBrowser } from '../hooks/usePWAInstall';

interface BrowserGuide {
  id: DetectedBrowser;
  name: string;
  platform: string;
  badge: string;
  steps: {
    title: string;
    description: string;
    icon?: React.ReactNode;
  }[];
  tip?: string;
}

const BROWSER_GUIDES: BrowserGuide[] = [
  {
    id: 'safari_ios',
    name: 'Safari на iPhone / iPad',
    platform: 'iOS (Apple)',
    badge: 'Рекомендуется для iOS',
    steps: [
      {
        title: 'Нажмите кнопку «Поделиться»',
        description: 'В нижней панели Safari найдите и нажмите значок квадрата со стрелкой вверх.',
        icon: <Share size={18} className="text-blue-500" />,
      },
      {
        title: 'Выберите «На экран "Домой"»',
        description: 'Прокрутите меню действий чуть ниже и нажмите на пункт «На экран "Домой"» со значком «+».',
        icon: <PlusSquare size={18} className="text-emerald-500" />,
      },
      {
        title: 'Нажмите «Добавить»',
        description: 'В правом верхнем углу подтвердите добавление. Иконка появится на рабочем столе устройства.',
        icon: <CheckCircle2 size={18} className="text-emerald-600" />,
      },
    ],
    tip: 'В iOS PWA работает как самостоятельное приложение: скрываются панели браузера и поддерживается работа оффлайн.',
  },
  {
    id: 'chrome',
    name: 'Google Chrome',
    platform: 'Android / Windows / macOS',
    badge: 'Поддерживает установку в 1 клик',
    steps: [
      {
        title: 'Установка в 1 клик через кнопку',
        description: 'Нажмите большую кнопку «Установить приложение» выше в этом блоке.',
        icon: <Download size={18} className="text-emerald-600" />,
      },
      {
        title: 'Или через значок в адресной строке',
        description: 'Нажмите на значок монитора со стрелкой в правой части адресной строки Chrome.',
        icon: <Laptop size={18} className="text-blue-500" />,
      },
      {
        title: 'Или через главное меню Chrome (⋮)',
        description: 'Нажмите меню ⋮ (три точки) ➔ выберите «Установить Finance Analyzer» или «Добавить на главный экран».',
        icon: <ChevronRight size={18} className="text-amber-500" />,
      },
    ],
    tip: 'Chrome автоматически зарегистрирует приложение в системе: оно появится в списке установленных программ или на рабочем столе.',
  },
  {
    id: 'yandex',
    name: 'Яндекс.Браузер',
    platform: 'Android / ПК',
    badge: 'Популярно в РФ',
    steps: [
      {
        title: 'Откройте меню браузера',
        description: 'Нажмите на значок трёх точек ⋮ (или три полоски ≡) в правом нижнем или верхнем углу.',
        icon: <Globe size={18} className="text-rose-500" />,
      },
      {
        title: 'Выберите установку приложения',
        description: 'В меню выберите «Добавить на главный экран» (на телефоне) или «Установить приложение» (на ПК).',
        icon: <Download size={18} className="text-emerald-600" />,
      },
      {
        title: 'Подтвердите добавление',
        description: 'Нажмите «Добавить» или «Установить». Приложение запустится в чистом окне.',
        icon: <CheckCircle2 size={18} className="text-emerald-600" />,
      },
    ],
    tip: 'Яндекс.Браузер сохраняет кэш PWA, позволяя вести финансы в самолете и без сети.',
  },
  {
    id: 'safari_mac',
    name: 'Safari на Mac',
    platform: 'macOS Sonoma / Ventura',
    badge: 'Для компьютеров Mac',
    steps: [
      {
        title: 'Откройте меню «Файл»',
        description: 'В строке меню macOS в левом верхнем углу нажмите меню «Файл».',
        icon: <Laptop size={18} className="text-slate-600" />,
      },
      {
        title: 'Выберите «Добавить в Dock...»',
        description: 'В выпадающем меню нажмите «Добавить в Dock...» (Add to Dock). Либо нажмите кнопку «Поделиться» ➔ «Добавить в Dock».',
        icon: <PlusSquare size={18} className="text-blue-500" />,
      },
      {
        title: 'Запускайте прямо из панели Dock',
        description: 'Finance Analyzer появится среди ваших нативных программ и будет открываться в отдельном окне без адресной строки.',
        icon: <CheckCircle2 size={18} className="text-emerald-600" />,
      },
    ],
    tip: 'В macOS Sonoma и новее PWA-приложения из Safari запускаются с полной поддержкой горячих клавиш и изоляции окон.',
  },
  {
    id: 'samsung',
    name: 'Samsung Internet',
    platform: 'Смартфоны Samsung / Android',
    badge: 'Samsung One UI',
    steps: [
      {
        title: 'Откройте меню браузера',
        description: 'Нажмите на значок меню ≡ в правом нижнем углу экрана браузера.',
        icon: <Smartphone size={18} className="text-purple-500" />,
      },
      {
        title: 'Выберите «Добавить страницу в»',
        description: 'В появившемся списке нажмите «Добавить страницу в» ➔ выберите «Главный экран».',
        icon: <Download size={18} className="text-emerald-600" />,
      },
      {
        title: 'Подтвердите установку',
        description: 'Нажмите «Добавить». Приложение мгновенно появится на рабочем столе смартфона.',
        icon: <CheckCircle2 size={18} className="text-emerald-600" />,
      },
    ],
    tip: 'Samsung Internet оптимизирует запуск веб-приложений через встроенный механизм Android WebAPK.',
  },
  {
    id: 'edge',
    name: 'Microsoft Edge',
    platform: 'Windows / Mac / Android',
    badge: 'Для Windows и Edge',
    steps: [
      {
        title: 'Значок приложения в строке URL',
        description: 'В правой части адресной строки Edge нажмите на значок «Установить Finance Analyzer» (три квадрата с плюсом).',
        icon: <Download size={18} className="text-blue-600" />,
      },
      {
        title: 'Или через меню приложений',
        description: 'Нажмите меню «…» вверху ➔ раздел «Приложения» ➔ «Установить этот сайт как приложение».',
        icon: <Laptop size={18} className="text-indigo-500" />,
      },
      {
        title: 'Закрепите на панели задач',
        description: 'После установки вы сможете закрепить иконку в меню «Пуск» или на панели задач Windows.',
        icon: <CheckCircle2 size={18} className="text-emerald-600" />,
      },
    ],
    tip: 'Edge поддерживает глубокую интеграцию с меню «Пуск» и панелью задач Windows.',
  },
  {
    id: 'firefox',
    name: 'Mozilla Firefox',
    platform: 'Android / Desktop',
    badge: 'Firefox Browser',
    steps: [
      {
        title: 'Откройте меню Firefox (⋮)',
        description: 'На смартфоне нажмите на три вертикальные точки рядом со строкой адреса.',
        icon: <Smartphone size={18} className="text-amber-600" />,
      },
      {
        title: 'Выберите «Установить»',
        description: 'В меню выберите «Установить» (или «Добавить на главный экран»).',
        icon: <Download size={18} className="text-emerald-600" />,
      },
      {
        title: 'Подтвердите размещение',
        description: 'Иконка приложения добавится на домашний экран и будет запускаться автономно.',
        icon: <CheckCircle2 size={18} className="text-emerald-600" />,
      },
    ],
    tip: 'На компьютерах в Firefox рекомендуется создать ярлык страницы либо использовать Chrome/Edge для полноэкранного PWA-режима.',
  },
];

export const PWAInstallSection: React.FC = () => {
  const { isInstallable, isInstalled, browserInfo, install } = usePWAInstall();
  const [activeTab, setActiveTab] = useState<DetectedBrowser>(browserInfo.id);
  const [installSuccess, setInstallSuccess] = useState<boolean>(false);

  // Pick matching guide or fallback to safari_ios or chrome
  const activeGuide =
    BROWSER_GUIDES.find((g) => g.id === activeTab) ||
    BROWSER_GUIDES.find((g) => g.id === (browserInfo.os === 'ios' ? 'safari_ios' : 'chrome')) ||
    BROWSER_GUIDES[0];

  const isCurrentBrowserSelected = activeTab === browserInfo.id;

  const handleInstallClick = async () => {
    const success = await install();
    if (success) {
      setInstallSuccess(true);
    }
  };

  return (
    <div id="pwa_settings_section" className="bg-white rounded-3xl border border-slate-200 p-5 sm:p-6 shadow-xs space-y-6">
      {/* Title & Status Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-slate-100">
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 rounded-2xl bg-gradient-to-tr from-emerald-600 to-teal-500 text-white flex items-center justify-center shadow-md shadow-emerald-600/20 shrink-0">
            <Smartphone size={22} className="stroke-[2.2]" />
          </div>
          <div>
            <div className="flex items-center gap-2 flex-wrap">
              <h3 className="text-base font-extrabold text-slate-900">
                Установка PWA приложения
              </h3>
              <span className="px-2 py-0.5 rounded-full text-[10px] font-black uppercase tracking-wider bg-emerald-100 text-emerald-800">
                PWA Ready
              </span>
            </div>
            <p className="text-xs text-slate-500">
              Работает как нативное приложение на смартфоне и ПК без интернета
            </p>
          </div>
        </div>

        {/* Current State Indicator Badge */}
        <div>
          {isInstalled ? (
            <div className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs font-bold">
              <CheckCircle2 size={14} className="text-emerald-600 stroke-[2.5]" />
              <span>PWA установлено и активно</span>
            </div>
          ) : (
            <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-xl bg-slate-100 border border-slate-200 text-slate-700 text-xs font-semibold">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse shrink-0" />
              <span>Определен браузер: <strong className="text-slate-900">{browserInfo.name}</strong></span>
            </div>
          )}
        </div>
      </div>

      {/* 1-Click Install Action Card (If installable or user wants prompt) */}
      {!isInstalled && (
        <div className="p-4 rounded-2xl bg-gradient-to-r from-emerald-50 via-teal-50/70 to-emerald-50 border border-emerald-200/90 flex flex-col sm:flex-row sm:items-center justify-between gap-4 shadow-2xs">
          <div className="space-y-1">
            <div className="flex items-center gap-2 text-emerald-950 font-extrabold text-sm">
              <Sparkles size={16} className="text-emerald-600" />
              <span>Добавьте Finance Analyzer на главный экран</span>
            </div>
            <p className="text-xs text-emerald-800/90 leading-relaxed max-w-xl">
              Получите мгновенный доступ в 1 клик, полноэкранный интерфейс без рамок браузера, быстрый запуск и сохранение локальной базы данных.
            </p>
          </div>

          <div className="shrink-0 flex items-center gap-2">
            {isInstallable ? (
              <button
                id="pwa_direct_install_btn"
                type="button"
                onClick={handleInstallClick}
                className="w-full sm:w-auto px-5 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs shadow-md shadow-emerald-600/25 flex items-center justify-center gap-2 transition-all cursor-pointer active:scale-95"
              >
                <Download size={15} className="stroke-[2.5]" />
                <span>Установить сейчас</span>
              </button>
            ) : installSuccess ? (
              <div className="px-4 py-2 rounded-xl bg-emerald-600 text-white text-xs font-bold flex items-center gap-1.5">
                <CheckCircle2 size={14} /> Установлено!
              </div>
            ) : (
              <span className="text-[11px] text-slate-500 font-medium">
                {browserInfo.os === 'ios'
                  ? 'Для Safari используйте инструкцию ниже ↓'
                  : 'Следуйте шагам вашего браузера ниже ↓'}
              </span>
            )}
          </div>
        </div>
      )}

      {/* Browser Selector Tabs */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <label className="text-xs font-bold text-slate-500 uppercase tracking-wider">
            Инструкция по браузерам
          </label>
          {isCurrentBrowserSelected && (
            <span className="text-[11px] font-bold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-md border border-emerald-200">
              Ваш текущий браузер
            </span>
          )}
        </div>

        {/* Tab Pills */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 scrollbar-none">
          {BROWSER_GUIDES.map((guide) => {
            const isSelected = activeTab === guide.id;
            const isDetected = browserInfo.id === guide.id;

            return (
              <button
                key={guide.id}
                id={`pwa_tab_${guide.id}`}
                type="button"
                onClick={() => setActiveTab(guide.id)}
                className={`relative px-3 py-2 rounded-xl text-xs font-bold whitespace-nowrap transition-all cursor-pointer shrink-0 flex items-center gap-1.5 border ${
                  isSelected
                    ? 'bg-slate-900 text-white border-slate-900 shadow-sm'
                    : isDetected
                    ? 'bg-emerald-50 text-emerald-900 border-emerald-300 hover:bg-emerald-100'
                    : 'bg-slate-50 text-slate-600 border-slate-200 hover:bg-slate-100'
                }`}
              >
                {isDetected && (
                  <span className={`w-1.5 h-1.5 rounded-full shrink-0 ${isSelected ? 'bg-emerald-400' : 'bg-emerald-600 animate-pulse'}`} />
                )}
                <span>{guide.name}</span>
                {isDetected && (
                  <span className={`text-[9px] px-1 rounded uppercase font-black tracking-tight ${isSelected ? 'bg-white/20 text-white' : 'bg-emerald-200 text-emerald-900'}`}>
                    Вы здесь
                  </span>
                )}
              </button>
            );
          })}
        </div>
      </div>

      {/* Active Browser Guide Display */}
      <div className="p-4 sm:p-5 rounded-2xl bg-slate-50/80 border border-slate-200/90 space-y-4">
        {/* Guide header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-3 border-b border-slate-200/70">
          <div>
            <div className="flex items-center gap-2">
              <span className="font-extrabold text-slate-900 text-sm">{activeGuide.name}</span>
              <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-200/80 text-slate-700">
                {activeGuide.platform}
              </span>
            </div>
            <p className="text-xs text-slate-500 mt-0.5">{activeGuide.badge}</p>
          </div>

          {activeTab === 'safari_ios' && (
            <div className="flex items-center gap-1.5 text-xs text-blue-700 bg-blue-50 border border-blue-200 px-3 py-1.5 rounded-xl font-semibold">
              <Share size={13} className="shrink-0" />
              <span>Требуется нажать значок «Поделиться»</span>
            </div>
          )}
        </div>

        {/* Steps */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          {activeGuide.steps.map((step, idx) => (
            <div
              key={idx}
              className="p-3.5 rounded-xl bg-white border border-slate-200/80 shadow-2xs space-y-2 flex flex-col justify-between"
            >
              <div className="space-y-1.5">
                <div className="flex items-center justify-between">
                  <span className="w-5 h-5 rounded-full bg-slate-100 border border-slate-200 text-slate-700 text-[10px] font-black flex items-center justify-center">
                    {idx + 1}
                  </span>
                  <div className="p-1 rounded-lg bg-slate-50">{step.icon}</div>
                </div>
                <h4 className="text-xs font-bold text-slate-900 leading-snug">{step.title}</h4>
                <p className="text-[11px] text-slate-500 leading-relaxed">{step.description}</p>
              </div>
            </div>
          ))}
        </div>

        {/* Tip Box */}
        {activeGuide.tip && (
          <div className="p-3 rounded-xl bg-emerald-50/70 border border-emerald-200/70 flex items-start gap-2 text-xs text-emerald-900">
            <Sparkles size={14} className="text-emerald-600 shrink-0 mt-0.5" />
            <span className="leading-relaxed font-medium">{activeGuide.tip}</span>
          </div>
        )}
      </div>

      {/* PWA Advantages Grid */}
      <div className="pt-2">
        <label className="text-xs font-bold text-slate-500 uppercase tracking-wider block mb-3">
          Преимущества PWA версии
        </label>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          <div className="p-3 rounded-xl bg-white border border-slate-200 flex items-start gap-3">
            <div className="w-8 h-8 rounded-lg bg-emerald-100 text-emerald-700 flex items-center justify-center shrink-0">
              <Zap size={16} />
            </div>
            <div>
              <h5 className="text-xs font-bold text-slate-900">Запуск без задержек</h5>
              <p className="text-[11px] text-slate-500 leading-snug mt-0.5">
                Мгновенное открытие с экрана телефона без поисковых строк и лишних вкладок.
              </p>
            </div>
          </div>

          <div className="p-3 rounded-xl bg-white border border-slate-200 flex items-start gap-3">
            <div className="w-8 h-8 rounded-lg bg-blue-100 text-blue-700 flex items-center justify-center shrink-0">
              <WifiOff size={16} />
            </div>
            <div>
              <h5 className="text-xs font-bold text-slate-900">Полный оффлайн</h5>
              <p className="text-[11px] text-slate-500 leading-snug mt-0.5">
                Все транзакции, валюты и аналитика кэшируются через Service Worker.
              </p>
            </div>
          </div>

          <div className="p-3 rounded-xl bg-white border border-slate-200 flex items-start gap-3">
            <div className="w-8 h-8 rounded-lg bg-purple-100 text-purple-700 flex items-center justify-center shrink-0">
              <ShieldCheck size={16} />
            </div>
            <div>
              <h5 className="text-xs font-bold text-slate-900">100% Приватность</h5>
              <p className="text-[11px] text-slate-500 leading-snug mt-0.5">
                Никакой передачи в облако — база данных хранится локально на вашем устройстве.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
