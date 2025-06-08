package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.category

object TransactionCategoryDetector {

    private val rules = mapOf(
        "Переводы" to listOf(
            "внешний перевод",
            "внутренний перевод",
            "сбп",
            "перевод",
            "перевод сбп"
        ),
        "Пополнения" to listOf(
            "пополнение",
            "перевод от",
            "зачисление",
            "возврат",
            "внесение наличных"
        ),
        "Продукты" to listOf(
            "пятёрочка",
            "pyaterochka",
            "магнит",
            "magnit",
            "вкусвилл",
            "ашан",
            "лента",
            "самбери",
            "близкий",
            "магазин причал",
            "супермаркеты"
        ),
        "Кафе и рестораны" to listOf(
            "кафе",
            "ресторан",
            "додо",
            "dodo",
            "бургер",
            "burger",
            "макдоналдс",
            "mcdonald",
            "кофе",
            "coffee",
            "рестораны",
            "бары",
            "фастфуд"
        ),
        "Транспорт" to listOf(
            "такси",
            "yandex.go",
            "uber",
            "яндекс.такси",
            "метрополитен",
            "metro",
            "автобус",
            "трамвай",
            "транспорт",
            "transport",
            "mos.transport",
            "мострансп",
            "mosgortrans",
            "мосгортранс"
        ),
        "Покупки Ozon" to listOf("ozon", "озон"),
        "Онлайн-покупки" to listOf(
            "яндекс.маркет",
            "yandex market",
            "wildberries",
            "вайлдберриз",
            "wb",
            "aliexpress",
            "али",
            "вайме",
            "vimemc",
            "интернет-магазины"
        ),
        "Автомобиль" to listOf(
            "азс",
            "топливо",
            "бензин",
            "автозаправ",
            "парковк",
            "стоянк",
            "автоуслуги",
            "автосервис"
        ),
        "Аптека" to listOf("аптека", "apteka"),
        "Здоровье" to listOf("здоровье", "клиник", "врач", "доктор", "здоровье и красота"),
        "Электроника" to listOf("связной", "эльдорадо", "мвидео", "mvideo", "ситилинк", "citilink", "dns", "днс", "техника"),
        "ЖКХ" to listOf("жкх", "коммунал", "коммунальные платежи", "связь", "интернет"),
        "Связь" to listOf(
            "связь",
            "мобильный",
            "мтс",
            "билайн",
            "мегафон",
            "tele2",
            "мобильная связь"
        ),
        "Развлечения" to listOf("кино", "cinema", "развлечения", "кинотеатр"),
        "Подписки" to listOf("подписк", "subscription", "spotify", "netflix", "okko", "кинопоиск"),
        "Банковские услуги" to listOf(
            "комиссия",
            "обслуживание",
            "процент",
            "interest",
            "банковские услуги"
        ),
        "Супермаркеты" to listOf(
            "пятёрочка",
            "pyaterochka",
            "магнит",
            "magnit",
            "лента",
            "ашан",
            "вкусвилл"
        ),
        "Прочие расходы" to listOf("прочие операции", "прочие покупки", "разные товары")
    )

    fun detect(description: String): String {
        val lower = description.lowercase()
        for ((category, keywords) in rules) {
            if (keywords.any { lower.contains(it) }) return category
        }
        return "Без категории"
    }
} 
