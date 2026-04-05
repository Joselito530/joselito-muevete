package com.example.joselitomueveteomuere

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

object ExerciseManager {

    private const val PREFS_NAME = "joselito_prefs"
    private const val KEY_EXERCISES = "exercises"
    private const val KEY_SELECTED = "selected_exercise_index"

    val defaultExercises = listOf(
        "10 sentadillas",
        "10 flexiones",
        "30 seg plancha",
        "20 elevaciones de talon",
        "10 zancadas",
        "Estiramiento cervical 1 min",
        "Rotacion de hombros 30 seg",
        "10 abdominales",
        "Caminar 2 minutos",
        "Respiracion profunda 1 min"
    )

    fun getExercises(context: Context): MutableList<String> {
        val prefs = getPrefs(context)
        val json = prefs.getString(KEY_EXERCISES, null)
        return if (json != null) {
            val arr = JSONArray(json)
            MutableList(arr.length()) { arr.getString(it) }
        } else {
            defaultExercises.toMutableList()
        }
    }

    fun saveExercises(context: Context, exercises: List<String>) {
        val arr = JSONArray(exercises)
        getPrefs(context).edit().putString(KEY_EXERCISES, arr.toString()).apply()
    }

    fun getSelectedIndex(context: Context): Int {
        return getPrefs(context).getInt(KEY_SELECTED, 0)
    }

    fun setSelectedIndex(context: Context, index: Int) {
        getPrefs(context).edit().putInt(KEY_SELECTED, index).apply()
    }

    fun getSelectedExercise(context: Context): String {
        val exercises = getExercises(context)
        val index = getSelectedIndex(context).coerceIn(0, exercises.size - 1)
        return exercises[index]
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
