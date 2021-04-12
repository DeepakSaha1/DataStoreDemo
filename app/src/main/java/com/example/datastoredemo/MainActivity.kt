package com.example.datastoredemo

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.asLiveData
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

class MainActivity : AppCompatActivity() {
    var value = 0
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "counter_data")
    private val COUNTER = intPreferencesKey("int_counter")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkForCounterData()
        increment_btn.setOnClickListener {
            incrementBy()
        }

        save_btn.setOnClickListener {
            GlobalScope.launch {
                saveData(value)
            }
            Toast.makeText(this, "data is saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkForCounterData() {
        getCounterData().asLiveData().observe(this) { data ->
            if (data > 0) {
                value = data
                counter_tv.text = data.toString()
            }
            else
                counter_tv.text = "no data found!"
        }
    }

    private fun incrementBy() {
        value++
        counter_tv.text = value.toString()
    }

    private suspend fun saveData(data: Int) {
        dataStore.edit { preferences ->
            preferences[COUNTER] = data
        }
    }

    private fun getCounterData(): Flow<Int> =
            dataStore.data
                    .catch { exception ->
                        if (exception is IOException) {
                            emit(emptyPreferences())
                        } else {
                            throw exception
                        }
                    }
                    .map { preferences ->
                        preferences[COUNTER] ?: -1
                    }
}