import { useEffect, useState } from 'react';

export type DetectedBrowser =
  | 'safari_ios'
  | 'safari_mac'
  | 'chrome'
  | 'yandex'
  | 'edge'
  | 'samsung'
  | 'firefox'
  | 'opera'
  | 'other';

export type DetectedOS = 'ios' | 'android' | 'macos' | 'windows' | 'linux' | 'other';

export interface BrowserInfo {
  id: DetectedBrowser;
  name: string;
  os: DetectedOS;
  osName: string;
  isMobile: boolean;
  canPromptDirectly: boolean;
}

interface BeforeInstallPromptEvent extends Event {
  prompt: () => Promise<void>;
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed'; platform: string }>;
}

export function detectBrowserAndOS(): BrowserInfo {
  if (typeof window === 'undefined' || !window.navigator) {
    return {
      id: 'other',
      name: 'Браузер',
      os: 'other',
      osName: 'Устройство',
      isMobile: false,
      canPromptDirectly: false,
    };
  }

  const ua = window.navigator.userAgent.toLowerCase();
  const isMobile = /mobile|iphone|ipad|ipod|android|blackberry|iemobile|opera mini/i.test(ua);

  // Detect OS
  let os: DetectedOS = 'other';
  let osName = 'Неизвестная ОС';
  if (/iphone|ipad|ipod/.test(ua)) {
    os = 'ios';
    osName = 'iOS (iPhone / iPad)';
  } else if (/android/.test(ua)) {
    os = 'android';
    osName = 'Android';
  } else if (/macintosh|mac os x/.test(ua)) {
    os = 'macos';
    osName = 'macOS';
  } else if (/windows nt|win64|win32/.test(ua)) {
    os = 'windows';
    osName = 'Windows';
  } else if (/linux/.test(ua)) {
    os = 'linux';
    osName = 'Linux';
  }

  // Detect Browser (order matters: Yandex, Samsung, Edge, Opera wrap Chrome engine)
  let id: DetectedBrowser = 'other';
  let name = 'Современный браузер';
  let canPromptDirectly = false;

  if (ua.includes('yaapp') || ua.includes('yabrowser') || ua.includes('yasearch')) {
    id = 'yandex';
    name = 'Яндекс.Браузер';
    canPromptDirectly = true;
  } else if (ua.includes('samsungbrowser')) {
    id = 'samsung';
    name = 'Samsung Internet';
    canPromptDirectly = true;
  } else if (ua.includes('edg/') || ua.includes('edge/')) {
    id = 'edge';
    name = 'Microsoft Edge';
    canPromptDirectly = true;
  } else if (ua.includes('opr/') || ua.includes('opera/')) {
    id = 'opera';
    name = 'Opera';
    canPromptDirectly = true;
  } else if (ua.includes('firefox') || ua.includes('fxios')) {
    id = 'firefox';
    name = 'Mozilla Firefox';
    canPromptDirectly = false; // Firefox doesn't support beforeinstallprompt standard
  } else if (os === 'ios' && (ua.includes('safari') || !ua.includes('chrome'))) {
    id = 'safari_ios';
    name = 'Apple Safari (iOS)';
    canPromptDirectly = false; // iOS Safari uses Share sheet
  } else if (os === 'macos' && ua.includes('safari') && !ua.includes('chrome')) {
    id = 'safari_mac';
    name = 'Apple Safari (macOS)';
    canPromptDirectly = false;
  } else if (ua.includes('chrome') || ua.includes('crios') || ua.includes('chromium')) {
    id = 'chrome';
    name = 'Google Chrome';
    canPromptDirectly = true;
  }

  return {
    id,
    name,
    os,
    osName,
    isMobile,
    canPromptDirectly,
  };
}

export function usePWAInstall() {
  const [deferredPrompt, setDeferredPrompt] = useState<BeforeInstallPromptEvent | null>(null);
  const [isInstalled, setIsInstalled] = useState(false);
  const [browserInfo, setBrowserInfo] = useState<BrowserInfo>(detectBrowserAndOS());

  useEffect(() => {
    // Re-detect browser on mount
    setBrowserInfo(detectBrowserAndOS());

    // Detect standalone mode (already installed & running as PWA)
    const checkIsStandalone = () => {
      const isStandaloneMedia = window.matchMedia('(display-mode: standalone)').matches;
      const isNavStandalone = (window.navigator as unknown as { standalone?: boolean }).standalone === true;
      const isReferrerAndroid = document.referrer.startsWith('android-app://');
      return isStandaloneMedia || isNavStandalone || isReferrerAndroid;
    };

    setIsInstalled(checkIsStandalone());

    const mediaQuery = window.matchMedia('(display-mode: standalone)');
    const handleMediaChange = (e: MediaQueryListEvent) => {
      if (e.matches) {
        setIsInstalled(true);
      }
    };
    try {
      mediaQuery.addEventListener('change', handleMediaChange);
    } catch {
      // Older browser support
      mediaQuery.addListener(handleMediaChange);
    }

    const handleBeforeInstallPrompt = (e: Event) => {
      e.preventDefault();
      setDeferredPrompt(e as BeforeInstallPromptEvent);
    };

    const handleAppInstalled = () => {
      setIsInstalled(true);
      setDeferredPrompt(null);
    };

    window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
    window.addEventListener('appinstalled', handleAppInstalled);

    return () => {
      window.removeEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
      window.removeEventListener('appinstalled', handleAppInstalled);
      try {
        mediaQuery.removeEventListener('change', handleMediaChange);
      } catch {
        mediaQuery.removeListener(handleMediaChange);
      }
    };
  }, []);

  const install = async (): Promise<boolean> => {
    if (!deferredPrompt) return false;
    try {
      await deferredPrompt.prompt();
      const choice = await deferredPrompt.userChoice;
      if (choice.outcome === 'accepted') {
        setIsInstalled(true);
        setDeferredPrompt(null);
        return true;
      }
    } catch (err) {
      console.warn('PWA install prompt error:', err);
    }
    return false;
  };

  return {
    isInstallable: !!deferredPrompt,
    isInstalled,
    browserInfo,
    install,
  };
}

export function useOnlineStatus() {
  const [isOnline, setIsOnline] = useState(
    typeof navigator !== 'undefined' ? navigator.onLine : true
  );

  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  return isOnline;
}
