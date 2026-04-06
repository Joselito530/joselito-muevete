package com.example.joselitomueveteomuere

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

object ExerciseManager {

    private const val PREFS_NAME = "joselito_prefs"
    private const val KEY_EXERCISES = "exercises"
    private const val KEY_CURRENT_INDEX = "current_exercise_index"

    val defaultExercises = listOf(
        "10 sentadillas", "10 flexiones", "30 seg plancha",
        "20 elevaciones de talon", "10 zancadas",
        "Estiramiento cervical 1 min", "Rotacion de hombros 30 seg",
        "10 abdominales", "Caminar 2 minutos", "Respiracion profunda 1 min"
    )

    fun getExercises(context: Context): MutableList<String> {
        val json = getPrefs(context).getString(KEY_EXERCISES, null)
        return if (json != null) {
            val arr = JSONArray(json); MutableList(arr.length()) { arr.getString(it) }
        } else defaultExercises.toMutableList()
    }

    fun saveExercises(context: Context, exercises: List<String>) {
        getPrefs(context).edit().putString(KEY_EXERCISES, JSONArray(exercises).toString()).apply()
    }

    fun getCurrentIndex(context: Context) = getPrefs(context).getInt(KEY_CURRENT_INDEX, 0)

    fun getCurrentExercise(context: Context): String {
        val exercises = getExercises(context)
        if (exercises.isEmpty()) return "Sin ejercicios"
        return exercises[getCurrentIndex(context).coerceIn(0, exercises.size - 1)]
    }

    fun advanceToNext(context: Context) {
        val exercises = getExercises(context)
        if (exercises.isEmpty()) return
        val next = (getCurrentIndex(context) + 1) % exercises.size
        getPrefs(context).edit().putInt(KEY_CURRENT_INDEX, next).apply()
    }

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
