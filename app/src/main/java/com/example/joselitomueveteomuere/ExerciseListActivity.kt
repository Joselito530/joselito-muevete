package com.example.joselitomueveteomuere

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ExerciseListActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var btnAdd: Button
    private lateinit var btnBack: Button
    private lateinit var adapter: ArrayAdapter<String>
    private var exercises = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_list)
        listView = findViewById(R.id.listExercises)
        btnAdd = findViewById(R.id.btnAddExercise)
        btnBack = findViewById(R.id.btnBack)
        exercises = ExerciseManager.getExercises(this)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, exercises)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ -> showEditDialog(position) }
        listView.setOnItemLongClickListener { _, _, position, _ ->
            AlertDialog.Builder(this)
                .setTitle("Eliminar ejercicio")
                .setMessage("Eliminar ejercicio?")
                .setPositiveButton("Eliminar") { _, _ ->
                    exercises.removeAt(position)
                    ExerciseManager.saveExercises(this, exercises)
                    adapter.notifyDataSetChanged()
                }
                .setNegativeButton("Cancelar", null).show()
            true
        }
        btnAdd.setOnClickListener { showEditDialog(-1) }
        btnBack.setOnClickListener { finish() }
    }

    private fun showEditDialog(position: Int) {
        val isNew = position == -1
        val editText = EditText(this).apply {
            setText(if (isNew) "" else exercises[position])
            hint = "Nombre del ejercicio"
            setPadding(40, 30, 40, 30)
        }
        AlertDialog.Builder(this)
            .setTitle(if (isNew) "Nuevo ejercicio" else "Editar ejercicio")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val text = editText.text.toString().trim()
                if (text.isNotEmpty()) {
                    if (isNew) exercises.add(text) else exercises[position] = text
                    ExerciseManager.saveExercises(this, exercises)
                    adapter.notifyDataSetChanged()
                }
            }
            .setNegativeButton("Cancelar", null).show()
    }
}
