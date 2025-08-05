package com.davidbugayov.financeanalyzer.presentation.categories.model

import android.content.Context
import com.davidbugayov.financeanalyzer.domain.model.Subcategory
import com.davidbugayov.financeanalyzer.ui.R

/**
 * Провайдер для работы с подкатегориями
 */
object SubcategoryProvider {

    /**
     * Получает предустановленные подкатегории для категории "Продукты"
     */
    fun getDefaultFoodSubcategories(context: Context): List<Subcategory> {
        return listOf(
            Subcategory.create(context.getString(R.string.subcategory_pyaterochka), 1L),
            Subcategory.create(context.getString(R.string.subcategory_vkusvill), 1L),
            Subcategory.create(context.getString(R.string.subcategory_magnit), 1L),
            Subcategory.create(context.getString(R.string.subcategory_lenta), 1L),
            Subcategory.create(context.getString(R.string.subcategory_metro), 1L),
            Subcategory.create(context.getString(R.string.subcategory_okey), 1L),
            Subcategory.create(context.getString(R.string.subcategory_dixy), 1L),
            Subcategory.create(context.getString(R.string.subcategory_billa), 1L),
            Subcategory.create(context.getString(R.string.subcategory_auchan), 1L),
            Subcategory.create(context.getString(R.string.subcategory_carrefour), 1L),
        )
    }

    /**
     * Получает предустановленные подкатегории для категории "Рестораны"
     */
    fun getDefaultRestaurantSubcategories(context: Context): List<Subcategory> {
        return listOf(
            Subcategory.create(context.getString(R.string.subcategory_mcdonalds), 4L),
            Subcategory.create(context.getString(R.string.subcategory_kfc), 4L),
            Subcategory.create(context.getString(R.string.subcategory_burger_king), 4L),
            Subcategory.create(context.getString(R.string.subcategory_subway), 4L),
            Subcategory.create(context.getString(R.string.subcategory_starbucks), 4L),
            Subcategory.create(context.getString(R.string.subcategory_dodo_pizza), 4L),
            Subcategory.create(context.getString(R.string.subcategory_dominos), 4L),
            Subcategory.create(context.getString(R.string.subcategory_yandex_eda), 4L),
            Subcategory.create(context.getString(R.string.subcategory_delivery_club), 4L),
        )
    }

    /**
     * Получает предустановленные подкатегории для категории "Транспорт"
     */
    fun getDefaultTransportSubcategories(context: Context): List<Subcategory> {
        return listOf(
            Subcategory.create(context.getString(R.string.subcategory_metro), 2L),
            Subcategory.create(context.getString(R.string.subcategory_bus), 2L),
            Subcategory.create(context.getString(R.string.subcategory_tram), 2L),
            Subcategory.create(context.getString(R.string.subcategory_trolleybus), 2L),
            Subcategory.create(context.getString(R.string.subcategory_taxi), 2L),
            Subcategory.create(context.getString(R.string.subcategory_yandex_taxi), 2L),
            Subcategory.create(context.getString(R.string.subcategory_uber), 2L),
            Subcategory.create(context.getString(R.string.subcategory_gasoline), 2L),
            Subcategory.create(context.getString(R.string.subcategory_parking), 2L),
            Subcategory.create(context.getString(R.string.subcategory_car_service), 2L),
        )
    }

    /**
     * Получает предустановленные подкатегории для категории "Развлечения"
     */
    fun getDefaultEntertainmentSubcategories(context: Context): List<Subcategory> {
        return listOf(
            Subcategory.create(context.getString(R.string.subcategory_cinema), 3L),
            Subcategory.create(context.getString(R.string.subcategory_theater), 3L),
            Subcategory.create(context.getString(R.string.subcategory_concert), 3L),
            Subcategory.create(context.getString(R.string.subcategory_museum), 3L),
            Subcategory.create(context.getString(R.string.subcategory_exhibition), 3L),
            Subcategory.create(context.getString(R.string.subcategory_amusement_park), 3L),
            Subcategory.create(context.getString(R.string.subcategory_bowling), 3L),
            Subcategory.create(context.getString(R.string.subcategory_billiards), 3L),
            Subcategory.create(context.getString(R.string.subcategory_karaoke), 3L),
        )
    }

    /**
     * Получает предустановленные подкатегории для категории "Здоровье"
     */
    fun getDefaultHealthSubcategories(context: Context): List<Subcategory> {
        return listOf(
            Subcategory.create(context.getString(R.string.subcategory_pharmacy), 5L),
            Subcategory.create(context.getString(R.string.subcategory_doctor), 5L),
            Subcategory.create(context.getString(R.string.subcategory_dentist), 5L),
            Subcategory.create(context.getString(R.string.subcategory_hospital), 5L),
            Subcategory.create(context.getString(R.string.subcategory_clinic), 5L),
            Subcategory.create(context.getString(R.string.subcategory_optics), 5L),
            Subcategory.create(context.getString(R.string.subcategory_medical_tests), 5L),
            Subcategory.create(context.getString(R.string.subcategory_physiotherapy), 5L),
            Subcategory.create(context.getString(R.string.subcategory_massage), 5L),
        )
    }

    /**
     * Получает предустановленные подкатегории для категории "Одежда"
     */
    fun getDefaultClothingSubcategories(context: Context): List<Subcategory> {
        return listOf(
            Subcategory.create(context.getString(R.string.subcategory_zara), 6L),
            Subcategory.create(context.getString(R.string.subcategory_h_m), 6L),
            Subcategory.create(context.getString(R.string.subcategory_uniqlo), 6L),
            Subcategory.create(context.getString(R.string.subcategory_reserved), 6L),
            Subcategory.create(context.getString(R.string.subcategory_mango), 6L),
            Subcategory.create(context.getString(R.string.subcategory_massimo_dutti), 6L),
            Subcategory.create(context.getString(R.string.subcategory_bershka), 6L),
            Subcategory.create(context.getString(R.string.subcategory_pull_bear), 6L),
            Subcategory.create(context.getString(R.string.subcategory_stradivarius), 6L),
        )
    }

    /**
     * Получает все предустановленные подкатегории
     */
    fun getAllDefaultSubcategories(context: Context): List<Subcategory> {
        return getDefaultFoodSubcategories(context) +
            getDefaultRestaurantSubcategories(context) +
            getDefaultTransportSubcategories(context) +
            getDefaultEntertainmentSubcategories(context) +
            getDefaultHealthSubcategories(context) +
            getDefaultClothingSubcategories(context)
    }

    /**
     * Получает предустановленные подкатегории для конкретной категории
     */
    fun getDefaultSubcategoriesForCategory(context: Context, categoryId: Long): List<Subcategory> {
        return when (categoryId) {
            1L -> getDefaultFoodSubcategories(context)
            2L -> getDefaultTransportSubcategories(context)
            3L -> getDefaultEntertainmentSubcategories(context)
            4L -> getDefaultRestaurantSubcategories(context)
            5L -> getDefaultHealthSubcategories(context)
            6L -> getDefaultClothingSubcategories(context)
            else -> emptyList()
        }
    }
}
