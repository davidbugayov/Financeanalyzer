import { Category, Transaction } from '../types';

export interface CategorySuggestion {
  category: Category;
  subcategory?: string;
  confidence: 'high' | 'medium';
  matchedKeyword: string;
  source: 'keyword' | 'history';
}

interface KeywordRule {
  categoryId?: string;
  categoryKey?: string;
  categoryName?: string;
  subcategory?: string;
  keywords: string[];
  weight: number;
}

const KEYWORD_RULES: KeywordRule[] = [
  // Продукты
  {
    categoryKey: 'food',
    categoryName: 'Продукты',
    subcategory: 'Супермаркет',
    keywords: [
      'пятерочка',
      'пятерочк',
      'перекресток',
      'магнит',
      'вкусвилл',
      'ашан',
      'лента',
      'метро',
      'дикси',
      'супермаркет',
      'продукты',
      'гастроном',
      'spar',
      'eurospar',
      'мираторг',
      'азбука вкуса',
      'глобус',
      'окей',
      'fix price',
      'чижик',
      'ягод',
      'grocery',
      'market',
    ],
    weight: 10,
  },
  {
    categoryKey: 'food',
    categoryName: 'Продукты',
    subcategory: 'Мясо и рыба',
    keywords: ['мясо', 'рыба', 'стейк', 'колбаса', 'сосиски', 'курица', 'фарш'],
    weight: 9,
  },
  {
    categoryKey: 'food',
    categoryName: 'Продукты',
    subcategory: 'Фрукты и овощи',
    keywords: ['фрукты', 'овощи', 'зелень', 'яблоки', 'бананы', 'помидоры', 'огурцы'],
    weight: 8,
  },
  {
    categoryKey: 'food',
    categoryName: 'Продукты',
    subcategory: 'Сладости',
    keywords: ['торт', 'пирожное', 'конфеты', 'шоколад', 'печенье', 'сладости', 'мороженое'],
    weight: 8,
  },

  // Кафе и рестораны
  {
    categoryKey: 'restaurant',
    categoryName: 'Кафе и рестораны',
    subcategory: 'Кофе с собой',
    keywords: [
      'кофе',
      'кофейня',
      'капучино',
      'латте',
      'флэт уайт',
      'американо',
      'эспрессо',
      'старбакс',
      'starbucks',
      'шоколадница',
      'coffee like',
      'кофемания',
      'буханка',
      'one price coffee',
      'дринкит',
      'круассан',
      'coffee',
    ],
    weight: 10,
  },
  {
    categoryKey: 'restaurant',
    categoryName: 'Кафе и рестораны',
    subcategory: 'Доставка еды',
    keywords: [
      'яндекс еда',
      'delivery club',
      'купер',
      'самокат',
      'доставка еды',
      'додо пицца',
      'додо',
      'папа джонс',
      'пицца',
      'суши',
      'роллы',
      'тануки',
      'якитория',
      'food delivery',
    ],
    weight: 10,
  },
  {
    categoryKey: 'restaurant',
    categoryName: 'Кафе и рестораны',
    subcategory: 'Бизнес-ланч',
    keywords: ['бизнес-ланч', 'ланч', 'обед', 'столовая', 'фастфуд', 'вкусно и точка', 'макдоналдс', 'кфс', 'kfc', 'ростикс', 'бургер кинг', 'burger king', 'шаурма', 'теремок'],
    weight: 9,
  },
  {
    categoryKey: 'restaurant',
    categoryName: 'Кафе и рестораны',
    subcategory: 'Ужин',
    keywords: ['ресторан', 'кафе', 'трактир', 'гастропаб', 'ужин', 'стейкхаус', 'рестик', 'restaurant', 'cafe'],
    weight: 8,
  },
  {
    categoryKey: 'restaurant',
    categoryName: 'Кафе и рестораны',
    subcategory: 'Бар',
    keywords: ['бар', 'паб', 'пиво', 'коктейль', 'вино', 'сидр', 'крафт', 'кальян', 'pub', 'bar'],
    weight: 9,
  },

  // Транспорт
  {
    categoryKey: 'transport',
    categoryName: 'Транспорт',
    subcategory: 'Такси',
    keywords: ['такси', 'яндекс go', 'яндекс такси', 'uber', 'убер', 'ситимобил', 'максим такси', 'taxi'],
    weight: 10,
  },
  {
    categoryKey: 'transport',
    categoryName: 'Транспорт',
    subcategory: 'Метро и автобус',
    keywords: ['метро', 'тройка', 'подорожник', 'проездной', 'автобус', 'трамвай', 'маршрутка', 'электричка', 'аэроэкспресс', 'ржд', 'билет на поезд'],
    weight: 10,
  },
  {
    categoryKey: 'transport',
    categoryName: 'Транспорт',
    subcategory: 'Бензин',
    keywords: ['бензин', 'лукойл', 'газпромнефть', 'роснефть', 'татнефть', 'азс', 'заправка', 'shell', 'дизель', 'газ авто', 'fuel', 'gas station'],
    weight: 10,
  },
  {
    categoryKey: 'transport',
    categoryName: 'Транспорт',
    subcategory: 'Парковка',
    keywords: ['парковка', 'паркинг', 'московский паркинг', 'parking'],
    weight: 9,
  },
  {
    categoryKey: 'transport',
    categoryName: 'Транспорт',
    subcategory: 'Обслуживание авто',
    keywords: ['каршеринг', 'делимобиль', 'ситидрайв', 'автомойка', 'мойка', 'шиномонтаж', 'сто', 'автосервис', 'масло авто', 'запчасти', 'штраф гибдд', 'осаго'],
    weight: 9,
  },

  // Жилье и ЖКХ
  {
    categoryKey: 'housing',
    categoryName: 'Жилье и ЖКХ',
    subcategory: 'Аренда',
    keywords: ['аренда квартиры', 'квартплата', 'аренда жилья', 'найм жилья', 'rent'],
    weight: 10,
  },
  {
    categoryKey: 'housing',
    categoryName: 'Жилье и ЖКХ',
    subcategory: 'Коммунальные услуги',
    keywords: ['жкх', 'коммуналка', 'мосэнергосбыт', 'водоканал', 'электроэнергия', 'отопление', 'тсж', 'еирц', 'мособлеирц', 'квартплата', 'газ'],
    weight: 10,
  },
  {
    categoryKey: 'housing',
    categoryName: 'Жилье и ЖКХ',
    subcategory: 'Интернет и ТВ',
    keywords: ['ростелеком', 'дом ру', 'провайдер', 'домашний интернет', 'мгтс', 'тв приставка'],
    weight: 9,
  },
  {
    categoryKey: 'housing',
    categoryName: 'Жилье и ЖКХ',
    subcategory: 'Ремонт',
    keywords: ['леруа мерлен', 'леруа', 'leroy', 'петрович', 'оби', 'obi', 'castorama', 'всеинструменты', 'стройматериалы', 'ремонт', 'краска', 'обои', 'сантехник'],
    weight: 9,
  },
  {
    categoryKey: 'housing',
    categoryName: 'Жилье и ЖКХ',
    subcategory: 'Бытовая химия',
    keywords: ['бытовая химия', 'стиральный порошок', 'улыбка радуги', 'магнит косметик', 'подружка', 'hoff', 'икеа', 'ikea', 'посуда'],
    weight: 8,
  },

  // Здоровье
  {
    categoryKey: 'health',
    categoryName: 'Здоровье',
    subcategory: 'Аптека',
    keywords: ['аптека', 'ригла', 'горздрав', 'еаптека', 'планета здоровья', 'вита', 'лекарства', 'таблетки', 'аспирин', 'витамины', 'бад', 'pharmacy'],
    weight: 10,
  },
  {
    categoryKey: 'health',
    categoryName: 'Здоровье',
    subcategory: 'Врачи и клиники',
    keywords: ['клиника', 'врач', 'доктор', 'анализы', 'гемотест', 'инвитро', 'мрт', 'узи', 'медси', 'прием врача', 'поликлиника', 'hospital'],
    weight: 10,
  },
  {
    categoryKey: 'health',
    categoryName: 'Здоровье',
    subcategory: 'Стоматология',
    keywords: ['стоматолог', 'зуб', 'стоматология', 'пломба', 'чистка зубов', 'брекеты', 'dentist'],
    weight: 10,
  },
  {
    categoryKey: 'health',
    categoryName: 'Здоровье',
    subcategory: 'Спорт',
    keywords: ['фитнес', 'спортзал', 'абонемент', 'world class', 'ddx', 'тренировка', 'бассейн', 'йога', 'fitness', 'gym'],
    weight: 9,
  },

  // Одежда и обувь
  {
    categoryKey: 'clothing',
    categoryName: 'Одежда и обувь',
    subcategory: 'Повседневная одежда',
    keywords: ['одежда', 'zara', 'h&m', 'uniqlo', 'befree', 'lime', 'wildberries', 'вайлдберриз', 'lamoda', 'ламода', 'джинсы', 'футболка', 'куртка', 'платье', 'штаны'],
    weight: 9,
  },
  {
    categoryKey: 'clothing',
    categoryName: 'Одежда и обувь',
    subcategory: 'Обувь',
    keywords: ['обувь', 'кроссовки', 'ботинки', 'туфли', 'сапоги', 'rendez-vous', 'street beat', 'superstep', 'shoes'],
    weight: 9,
  },

  // Развлечения
  {
    categoryKey: 'entertainment',
    categoryName: 'Развлечения',
    subcategory: 'Подписки',
    keywords: [
      'яндекс плюс',
      'yandex plus',
      'кинопоиск',
      'иви',
      'okko',
      'netflix',
      'нетфликс',
      'spotify',
      'спотифай',
      'apple music',
      'youtube premium',
      'подписка',
      'vk музыка',
      'телеграм премиум',
      'telegram premium',
      'subscription',
    ],
    weight: 10,
  },
  {
    categoryKey: 'entertainment',
    categoryName: 'Развлечения',
    subcategory: 'Кино и театры',
    keywords: ['кино', 'кинотеатр', 'синема парк', 'формула кино', 'театр', 'билет в театр', 'концерт', 'билеты на концерт', 'кассир ру', 'cinema'],
    weight: 10,
  },
  {
    categoryKey: 'entertainment',
    categoryName: 'Развлечения',
    subcategory: 'Игры',
    keywords: ['steam', 'стим', 'playstation', 'ps store', 'xbox', 'nintendo', 'игры', 'донат', 'blizzard', 'game'],
    weight: 10,
  },
  {
    categoryKey: 'entertainment',
    categoryName: 'Развлечения',
    subcategory: 'Книги',
    keywords: ['книга', 'книги', 'буквоед', 'читай город', 'литрес', 'букмейт', 'books'],
    weight: 9,
  },

  // Связь
  {
    categoryKey: 'communication',
    categoryName: 'Связь',
    subcategory: 'Мобильная связь',
    keywords: ['мтс', 'mts', 'билайн', 'beeline', 'мегафон', 'megafon', 'tele2', 'теле2', 'тинькофф мобайл', 't-mobile', 'yota', 'йота', 'оплата связи', 'телефон'],
    weight: 10,
  },
  {
    categoryKey: 'communication',
    categoryName: 'Связь',
    subcategory: 'Облачные сервисы',
    keywords: ['icloud', 'google one', 'яндекс диск', 'облако', 'cloud storage', 'dropbox'],
    weight: 9,
  },

  // Питомцы
  {
    categoryKey: 'pet',
    categoryName: 'Питомцы',
    subcategory: 'Корм',
    keywords: ['четыре лапы', 'бетховен', 'зоомагазин', 'корм для кошек', 'корм для собак', 'кошачий корм', 'вискас', 'purina', 'royal canin', 'наполнитель', 'питомец'],
    weight: 10,
  },
  {
    categoryKey: 'pet',
    categoryName: 'Питомцы',
    subcategory: 'Ветеринар',
    keywords: ['ветеринар', 'ветклиника', 'прививка коту', 'лечение собаки'],
    weight: 10,
  },

  // Путешествия
  {
    categoryKey: 'travel',
    categoryName: 'Путешествия и поездки',
    subcategory: 'Отели и жилье',
    keywords: ['отель', 'гостиница', 'booking', 'airbnb', 'ostrovok', 'островок', 'хостел', 'hotel'],
    weight: 10,
  },
  {
    categoryKey: 'travel',
    categoryName: 'Путешествия и поездки',
    subcategory: 'Транспорт и аренда',
    keywords: ['авиабилет', 'авиабилеты', 'авиасейлс', 'aviasales', 'аэрофлот', 's7', 'победа', 'turkish airlines', 'аренда авто за границей', 'flight'],
    weight: 10,
  },

  // Кредиты
  {
    categoryKey: 'credit',
    categoryName: 'Кредиты и долги',
    subcategory: 'Кредитная карта',
    keywords: ['кредит', 'ипотека', 'долг', 'заем', 'займ', 'кредитная карта', 'погашение кредита', 'рассрочка', 'сплит', 'долями'],
    weight: 10,
  },

  // Доходы: Зарплата
  {
    categoryKey: 'salary',
    categoryName: 'Зарплата',
    subcategory: 'Основная работа',
    keywords: ['зарплата', 'аванс', 'премия', 'получка', 'зп', 'оклад', 'заработная плата', 'salary', 'payroll'],
    weight: 10,
  },
  // Доходы: Фриланс
  {
    categoryKey: 'freelance',
    categoryName: 'Фриланс',
    subcategory: 'Проекты',
    keywords: ['фриланс', 'гонорар', 'оплата за проект', 'дизайн сайта', 'верстка', 'разработка сайта', 'консультация', 'клиент фриланс', 'freelance'],
    weight: 10,
  },
  // Доходы: Инвестиции
  {
    categoryKey: 'investments',
    categoryName: 'Инвестиции',
    subcategory: 'Дивиденды',
    keywords: ['дивиденды', 'купоны', 'проценты по вкладу', 'доход от акций', 'выплата дивидендов', 'брокерский счет', 'инвестиции'],
    weight: 10,
  },
  // Доходы: Подарки и бонусы
  {
    categoryKey: 'gifts',
    categoryName: 'Подарки и бонусы',
    subcategory: 'Кэшбэк',
    keywords: ['кэшбэк', 'кешбэк', 'возврат налога', 'налоговый вычет', 'бонус тинькофф', 'подарок на др', 'подарили деньгами', 'cashback'],
    weight: 10,
  },
];

/**
 * Normalizes input text into cleaned lower-case tokens and words
 */
function cleanText(text: string): string {
  return text
    .toLowerCase()
    .replace(/[^\w\sа-яё]/gi, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}

/**
 * Suggests the best matching category and optional subcategory
 * based on transaction note/description, tags, and previous user habits.
 */
export function suggestCategoryFromDescription(
  description: string,
  categories: Category[],
  recentTransactions?: Transaction[],
  typeFilter?: 'expense' | 'income'
): CategorySuggestion | null {
  if (!description || !description.trim()) {
    return null;
  }

  const normalized = cleanText(description);
  if (normalized.length < 2) {
    return null;
  }

  // 1. Check user transaction history first for exact or fuzzy merchant/note match
  if (recentTransactions && recentTransactions.length > 0) {
    const matchingTx = recentTransactions.find((t) => {
      if (typeFilter && t.type !== typeFilter) return false;
      if (!t.note) return false;
      const noteNorm = cleanText(t.note);
      if (!noteNorm) return false;
      // Exact or strong substring match
      return (
        noteNorm === normalized ||
        (normalized.length >= 4 && noteNorm.includes(normalized)) ||
        (noteNorm.length >= 4 && normalized.includes(noteNorm))
      );
    });

    if (matchingTx) {
      const foundCategory = categories.find(
        (c) =>
          (matchingTx.categoryId && c.id === matchingTx.categoryId) ||
          c.name.toLowerCase() === matchingTx.category.toLowerCase()
      );
      if (foundCategory) {
        return {
          category: foundCategory,
          subcategory: matchingTx.subcategory,
          confidence: 'high',
          matchedKeyword: matchingTx.note || description,
          source: 'history',
        };
      }
    }
  }

  // 2. Keyword-based matching
  let bestMatch: {
    rule: KeywordRule;
    category: Category;
    matchedKeyword: string;
    score: number;
  } | null = null;

  for (const rule of KEYWORD_RULES) {
    // Find category in user's category list
    const category = categories.find(
      (c) =>
        (rule.categoryId && c.id === rule.categoryId) ||
        (rule.categoryKey && c.key === rule.categoryKey) ||
        (rule.categoryName && c.name.toLowerCase() === rule.categoryName.toLowerCase())
    );

    if (!category) continue;

    // Check if category matches current transaction type
    if (typeFilter === 'expense' && !category.isExpense) continue;
    if (typeFilter === 'income' && category.isExpense) continue;

    // Check each keyword in the rule
    for (const kw of rule.keywords) {
      const cleanKw = kw.toLowerCase().trim();
      let score = 0;

      if (normalized === cleanKw) {
        score = rule.weight + 15; // Exact full match
      } else if (normalized.includes(cleanKw)) {
        // Bonus for longer keyword matches
        score = rule.weight + cleanKw.length * 0.5;
        // Extra bonus if keyword starts at word boundary
        const regex = new RegExp(`(^|\\s)${cleanKw}(\\s|$)`, 'i');
        if (regex.test(normalized)) {
          score += 4;
        }
      }

      if (score > 0 && (!bestMatch || score > bestMatch.score)) {
        bestMatch = {
          rule,
          category,
          matchedKeyword: kw,
          score,
        };
      }
    }
  }

  if (bestMatch && bestMatch.score >= 8) {
    return {
      category: bestMatch.category,
      subcategory: bestMatch.rule.subcategory,
      confidence: bestMatch.score >= 12 ? 'high' : 'medium',
      matchedKeyword: bestMatch.matchedKeyword,
      source: 'keyword',
    };
  }

  // 3. Fallback: Direct category name or subcategory name check in description
  for (const cat of categories) {
    if (typeFilter === 'expense' && !cat.isExpense) continue;
    if (typeFilter === 'income' && cat.isExpense) continue;

    const catNameLower = cat.name.toLowerCase();
    if (normalized.includes(catNameLower)) {
      return {
        category: cat,
        confidence: 'medium',
        matchedKeyword: cat.name,
        source: 'keyword',
      };
    }

    if (cat.subcategories) {
      for (const sub of cat.subcategories) {
        const subLower = sub.toLowerCase();
        if (normalized.includes(subLower)) {
          return {
            category: cat,
            subcategory: sub,
            confidence: 'high',
            matchedKeyword: sub,
            source: 'keyword',
          };
        }
      }
    }
  }

  return null;
}
