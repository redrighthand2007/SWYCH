package com.kush.swych.core.model

import androidx.annotation.DrawableRes
import com.kush.swych.R

/** Section headings on the Home screen, in display order. */
enum class CategoryGroup(val title: String) {
    FOOD("Hungry ?"),
    STUDY("Lock In."),
    CHILL("Wanna' Chill..."),
    CARE("Care"),
    SECOND_HAND("2nd Hand Items")
}

/**
 * Every category in the app. Home, Browse and Post all read this list, so adding a
 * category here (with its group and icon) is the only change needed. The enum name is
 * what gets stored in the database.
 */
enum class Category(
    val displayName: String,
    val group: CategoryGroup,
    @param:DrawableRes val iconRes: Int
) {
    SNACKS("Snacks", CategoryGroup.FOOD, R.drawable.cat_snacks),
    DRINKS("Drinks", CategoryGroup.FOOD, R.drawable.cat_drinks),
    PUFFS("Puffs", CategoryGroup.FOOD, R.drawable.cat_puffs),
    SWEETS("Sweets", CategoryGroup.FOOD, R.drawable.cat_sweets),
    COOKING("Cooking", CategoryGroup.FOOD, R.drawable.cat_cooking),

    BOOKS("Books", CategoryGroup.STUDY, R.drawable.cat_books),
    NOTES("Notes", CategoryGroup.STUDY, R.drawable.cat_notes),
    STATIONERY("Stationery", CategoryGroup.STUDY, R.drawable.cat_stationery),
    COURSES("Courses", CategoryGroup.STUDY, R.drawable.cat_courses),
    EXAMS("Exams", CategoryGroup.STUDY, R.drawable.cat_exams),

    GAMES("Games", CategoryGroup.CHILL, R.drawable.cat_games),
    MOVIES("Movies", CategoryGroup.CHILL, R.drawable.cat_movies),
    MUSIC("Music", CategoryGroup.CHILL, R.drawable.cat_music),
    HUB("Hub", CategoryGroup.CHILL, R.drawable.cat_hub),
    ONLINE_GAMES("Online Games", CategoryGroup.CHILL, R.drawable.cat_online_games),

    MEDS("Meds", CategoryGroup.CARE, R.drawable.cat_meds),
    SKINCARE("Skincare", CategoryGroup.CARE, R.drawable.cat_skincare),
    HYGIENE("Hygiene", CategoryGroup.CARE, R.drawable.cat_hygiene),

    ACCESSORIES("Accessories", CategoryGroup.SECOND_HAND, R.drawable.cat_accessories),
    KETTLES("Kettles", CategoryGroup.SECOND_HAND, R.drawable.cat_kettles),
    EXT_BOARDS("Ext. Boards", CategoryGroup.SECOND_HAND, R.drawable.cat_ext_boards),
    CYCLES("Cycles", CategoryGroup.SECOND_HAND, R.drawable.cat_cycles),
    ONLINE_TO_CASH("Online -> Cash", CategoryGroup.SECOND_HAND, R.drawable.cat_online____cash),
    CASH_TO_ONLINE("Cash -> Online", CategoryGroup.SECOND_HAND, R.drawable.cat_cash____online);

    companion object {
        /** Category for a stored value, or null for an unknown/legacy value. */
        fun fromName(name: String): Category? = entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
}
