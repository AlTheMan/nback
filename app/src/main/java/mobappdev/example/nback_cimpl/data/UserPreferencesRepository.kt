package mobappdev.example.nback_cimpl.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * This repository provides a way to interact with the DataStore api,
 * with this API you can save key:value pairs
 *
 * Currently this file contains only one thing: getting the highscore as a flow
 * and writing to the highscore preference.
 * (a flow is like a waterpipe; if you put something different in the start,
 * the end automatically updates as long as the pipe is open)
 *
 * Date: 25-08-2023
 * Version: Skeleton code version 1.0
 * Author: Yeetivity
 *
 */

class UserPreferencesRepository (
    private val dataStore: DataStore<Preferences>
){
    private companion object {
        val HIGHSCORE = intPreferencesKey("highscore")
        val GRIDSIZE = intPreferencesKey("gridsize")
        val NBACK = intPreferencesKey("nback")
        val NROFEVENTS = intPreferencesKey("nrofevents")
        val NROFSPOKENLETTERS = intPreferencesKey("nrofspokenletters")
        val EVENTINTERVAL = longPreferencesKey("eventinterval")


        const val TAG = "UserPreferencesRepo"
    }

    val highscore: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            val highscorevalue= preferences[HIGHSCORE] ?: 0
            Log.d("persistence", "load Highscore.")
            highscorevalue
        }
    val gridsize: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            val gridsizevalue= preferences[GRIDSIZE] ?: 3  // Default value for gridSize
            Log.d("persistence", "load gridSize.")
            gridsizevalue
        }
    val nback: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            val nbackvalue = preferences[NBACK] ?: 2  // Default value for nBack
            //TODO: ändra till 2
            Log.d("persistence", "load nBack.")
            nbackvalue
        }

    val nrofevents: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            val nrofeventsvalue= preferences[NROFEVENTS] ?: 10  // Default value for nrOfEvents
            Log.d("persistence", "load nrOfEvents.")
            nrofeventsvalue
        }
    val nrofspokenletters: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            val nrofspokenlettersvalue= preferences[NROFSPOKENLETTERS] ?: 9  // Default value for nrOfSpokenLetters
            Log.d("persistence", "load nrOfSpokenLetters.")
            nrofspokenlettersvalue
        }

    val eventinterval: Flow<Long> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            Log.d("persistence", "load nrOfSpokenLetters.")
            val eventIntervalValue= preferences[EVENTINTERVAL] ?: 2000L  // Default value for eventInterval
            eventIntervalValue
        }








    suspend fun saveHighScore(score: Int) {
        Log.d("persistence", "saveHighScore: $score")
        dataStore.edit { preferences ->
            preferences[HIGHSCORE] = score
        }
    }
    suspend fun saveGridSize(size: Int) {
        Log.d("persistence", "saveGridSize: $size")
        dataStore.edit { preferences ->
            preferences[GRIDSIZE] = size
        }
    }

    suspend fun saveNBack(n: Int) {
        Log.d("persistence", "saveNBack: $n")
        dataStore.edit { preferences ->
            preferences[NBACK] = n
        }
    }

    suspend fun saveNrOfEvents(nr: Int) {
        Log.d("persistence", "saveNrOfEvents: $nr")
        dataStore.edit { preferences ->
            preferences[NROFEVENTS] = nr
        }
    }

    suspend fun saveNrOfSpokenLetters(nr: Int) {
        Log.d("persistence", "saveNrOfSpokenLetters: $nr")
        dataStore.edit { preferences ->
            preferences[NROFSPOKENLETTERS] = nr
        }
    }


    suspend fun saveEventInterval(interval: Long) {
        Log.d("persistence", "saveEventInterval: $interval")
        dataStore.edit { preferences ->
            preferences[EVENTINTERVAL] = interval
        }
    }
}