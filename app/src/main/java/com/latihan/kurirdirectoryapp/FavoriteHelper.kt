package com.latihan.kurirdirectoryapp

import android.content.Context
import android.content.SharedPreferences

class FavoriteHelper(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "kurir_directory_prefs"
        private const val KEY_FAVORITES = "key_favorite_user_ids"
    }

    fun isFavorite(userId: Int): Boolean {
        val favs = prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
        return favs.contains(userId.toString())
    }

    fun setFavorite(userId: Int, isFavorite: Boolean) {
        val favs = HashSet(prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet())
        if (isFavorite) {
            favs.add(userId.toString())
        } else {
            favs.remove(userId.toString())
        }
        prefs.edit().putStringSet(KEY_FAVORITES, favs).apply()
    }

    fun toggleFavorite(userId: Int): Boolean {
        val newState = !isFavorite(userId)
        setFavorite(userId, newState)
        return newState
    }

    fun getFavoriteIds(): Set<Int> {
        val favs = prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
        return favs.mapNotNull { it.toIntOrNull() }.toSet()
    }
}
