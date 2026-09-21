export interface ExtendedCurrency {
  code: string;
  name: string;
  symbol: string;
  flag: string;
  country: string;
  // Units per 1 USD (pivot currency)
  ratePerUSD: number;
}

export interface TravelDestination {
  id: string;
  country: string;
  flag: string;
  popularPlaces: string;
  currencyCode: string;
}

// Normalized exchange rates against 1 USD
export const EXTENDED_CURRENCIES: ExtendedCurrency[] = [
  { code: 'RUB', name: 'Российский рубль', symbol: '₽', flag: '🇷🇺', country: 'Россия', ratePerUSD: 92.5 },
  { code: 'USD', name: 'Доллар США', symbol: '$', flag: '🇺🇸', country: 'США', ratePerUSD: 1.0 },
  { code: 'EUR', name: 'Евро', symbol: '€', flag: '🇪🇺', country: 'Еврозона', ratePerUSD: 0.92 },
  { code: 'TRY', name: 'Турецкая лира', symbol: '₺', flag: '🇹🇷', country: 'Турция', ratePerUSD: 34.2 },
  { code: 'THB', name: 'Тайский бат', symbol: '฿', flag: '🇹🇭', country: 'Таиланд', ratePerUSD: 35.8 },
  { code: 'AED', name: 'Дирхам ОАЭ', symbol: 'AED', flag: '🇦🇪', country: 'ОАЭ (Дубай)', ratePerUSD: 3.6725 },
  { code: 'GEL', name: 'Грузинский лари', symbol: '₾', flag: '🇬🇪', country: 'Грузия', ratePerUSD: 2.72 },
  { code: 'KZT', name: 'Казахстанский тенге', symbol: '₸', flag: '🇰🇿', country: 'Казахстан', ratePerUSD: 485.0 },
  { code: 'AMD', name: 'Армянский драм', symbol: '֏', flag: '🇦🇲', country: 'Армения', ratePerUSD: 388.0 },
  { code: 'IDR', name: 'Индонезийская рупия', symbol: 'Rp', flag: '🇮🇩', country: 'Бали / Индонезия', ratePerUSD: 15800.0 },
  { code: 'RSD', name: 'Сербский динар', symbol: 'дин', flag: '🇷🇸', country: 'Сербия', ratePerUSD: 108.0 },
  { code: 'CNY', name: 'Китайский юань', symbol: '¥', flag: '🇨🇳', country: 'Китай', ratePerUSD: 7.15 },
  { code: 'EGP', name: 'Египетский фунт', symbol: 'E£', flag: '🇪🇬', country: 'Египет', ratePerUSD: 48.5 },
  { code: 'VND', name: 'Вьетнамский донг', symbol: '₫', flag: '🇻🇳', country: 'Вьетнам', ratePerUSD: 25200.0 },
  { code: 'UZS', name: 'Узбекский сум', symbol: 'so\'m', flag: '🇺🇿', country: 'Узбекистан', ratePerUSD: 12750.0 },
  { code: 'BYN', name: 'Белорусский рубль', symbol: 'Br', flag: '🇧🇾', country: 'Беларусь', ratePerUSD: 3.28 },
  { code: 'UAH', name: 'Украинская гривна', symbol: '₴', flag: '🇺🇦', country: 'Украина', ratePerUSD: 41.5 },
  { code: 'GBP', name: 'Британский фунт', symbol: '£', flag: '🇬🇧', country: 'Великобритания', ratePerUSD: 0.79 },
  { code: 'JPY', name: 'Японская иена', symbol: '¥', flag: '🇯🇵', country: 'Япония', ratePerUSD: 152.0 },
  { code: 'KRW', name: 'Южнокорейская вона', symbol: '₩', flag: '🇰🇷', country: 'Южная Корея', ratePerUSD: 1380.0 },
  { code: 'CAD', name: 'Канадский доллар', symbol: 'C$', flag: '🇨🇦', country: 'Канада', ratePerUSD: 1.36 },
  { code: 'CHF', name: 'Швейцарский франк', symbol: 'Fr', flag: '🇨🇭', country: 'Швейцария', ratePerUSD: 0.88 },
];

export const POPULAR_TRAVEL_DESTINATIONS: TravelDestination[] = [
  { id: 'tr', country: 'Турция', flag: '🇹🇷', popularPlaces: 'Стамбул, Анталья', currencyCode: 'TRY' },
  { id: 'th', country: 'Таиланд', flag: '🇹🇭', popularPlaces: 'Бангкок, Пхукет', currencyCode: 'THB' },
  { id: 'ae', country: 'ОАЭ', flag: '🇦🇪', popularPlaces: 'Дубай, Абу-Даби', currencyCode: 'AED' },
  { id: 'ge', country: 'Грузия', flag: '🇬🇪', popularPlaces: 'Тбилиси, Батуми', currencyCode: 'GEL' },
  { id: 'kz', country: 'Казахстан', flag: '🇰🇿', popularPlaces: 'Алматы, Астана', currencyCode: 'KZT' },
  { id: 'id', country: 'Бали (Индонезия)', flag: '🇮🇩', popularPlaces: 'Убуд, Чангу', currencyCode: 'IDR' },
  { id: 'eu', country: 'Европа', flag: '🇪🇺', popularPlaces: 'Италия, Испания, Франция', currencyCode: 'EUR' },
  { id: 'am', country: 'Армения', flag: '🇦🇲', popularPlaces: 'Ереван, Дилижан', currencyCode: 'AMD' },
  { id: 'eg', country: 'Египет', flag: '🇪🇬', popularPlaces: 'Шарм-эш-Шейх, Хургада', currencyCode: 'EGP' },
  { id: 'rs', country: 'Сербия', flag: '🇷🇸', popularPlaces: 'Белград, Нови-Сад', currencyCode: 'RSD' },
  { id: 'cn', country: 'Китай', flag: '🇨🇳', popularPlaces: 'Пекин, Шанхай', currencyCode: 'CNY' },
  { id: 'us', country: 'США', flag: '🇺🇸', popularPlaces: 'Нью-Йорк, Майами', currencyCode: 'USD' },
];

/**
 * Returns currency info by code
 */
export function getCurrencyInfo(code: string): ExtendedCurrency {
  const found = EXTENDED_CURRENCIES.find((c) => c.code.toUpperCase() === code.toUpperCase());
  if (found) return found;

  return {
    code: code.toUpperCase(),
    name: code.toUpperCase(),
    symbol: code.toUpperCase(),
    flag: '🌐',
    country: 'Другая страна',
    ratePerUSD: 1.0,
  };
}

/**
 * Calculates cross-rate: how much of toCode is in 1 unit of fromCode
 * 1 fromCode = X toCode
 */
export function getCrossExchangeRate(fromCode: string, toCode: string): number {
  if (fromCode.toUpperCase() === toCode.toUpperCase()) return 1.0;

  const fromCurr = getCurrencyInfo(fromCode);
  const toCurr = getCurrencyInfo(toCode);

  // ratePerUSD is how many units of that currency equal 1 USD
  // 1 unit of From in USD = 1 / fromCurr.ratePerUSD
  // In toCurrency = (1 / fromCurr.ratePerUSD) * toCurr.ratePerUSD
  // => toCurr.ratePerUSD / fromCurr.ratePerUSD
  const rate = toCurr.ratePerUSD / fromCurr.ratePerUSD;
  return rate;
}

/**
 * Converts amount from one currency to another, with optional manual custom rate
 */
export function convertCurrency(
  amount: number,
  fromCode: string,
  toCode: string,
  customRate?: number
): number {
  if (isNaN(amount) || amount === 0) return 0;
  if (fromCode.toUpperCase() === toCode.toUpperCase()) return amount;

  const rate = customRate && customRate > 0 ? customRate : getCrossExchangeRate(fromCode, toCode);
  const result = amount * rate;
  return Math.round(result * 100) / 100;
}

/**
 * Format foreign currency with symbol and code
 */
export function formatForeignAmount(amount: number, currencyCode: string): string {
  const curr = getCurrencyInfo(currencyCode);
  const formatted = amount.toLocaleString('ru-RU', {
    maximumFractionDigits: 2,
    minimumFractionDigits: amount % 1 === 0 ? 0 : 2,
  });

  return `${formatted} ${curr.symbol}`;
}
