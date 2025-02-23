package com.example.progetto.model

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.progetto.classes.ImageVersion
import com.example.progetto.classes.ImageVersionInfo
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "settings")


// Create the DB
// It is defined against the "ImageVersion" class in DataClasses
// define an abstract class in which I define the query function (below)
@Database(entities = [ImageVersion::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageVersionDao(): imageVersionDao
}

// Define the DB queries
@Dao
interface imageVersionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImageVersion(imageVersion: ImageVersion)


    @Query("SELECT Mid, Version FROM ImageVersion")
    suspend fun getAllImageVersions(): List<ImageVersionInfo>

    @Query("SELECT Version FROM ImageVersion")
    suspend fun getVersionImage() : List<Int>

    @Query("SELECT Version FROM ImageVersion WHERE mid= :mid")
    suspend fun getMidVersionImage(mid : Int) : Int

    @Query("SELECT Image FROM ImageVersion WHERE mid= :mid")
    suspend fun getImage64(mid : Int) : String
}


class DBController(context: Context) {
    // DataStore
    private val dataStore = context.dataStore

    // Value of the sid in the AsyncStorage
    private val SID_KEY = stringPreferencesKey("sid")

    // Value of the uid in the AsyncStorage
    private val UID_KEY = intPreferencesKey("uid")

    // Value of the oid in the AsyncStorage
    private val OID_KEY = intPreferencesKey("oid")

    // Value of the mid in the AsyncStorage
    private val MID_KEY = intPreferencesKey("mid")

    private val LAT_KEY = doublePreferencesKey("lat")

    private val LNG_KEY = doublePreferencesKey("lng")

    // User registration value in the AsyncStorage
    private val PROFILE_KEY = booleanPreferencesKey("profile")

    // Screen value went into the background
    private val SCREEN_KEY = stringPreferencesKey("Home")

    val database = Room.databaseBuilder(
        context.applicationContext, AppDatabase::class.java, "imgVersion-database"
    ).build()

    // Get the Int value of the sid, if empty is set to empty String
    suspend fun getSid(): String {
        val prefs = dataStore.data.first()
        return prefs[SID_KEY] ?: ""
    }

    // Set the String value of the sid
    suspend fun setSid(sid: String) {
        dataStore.edit { preferences ->
            preferences[SID_KEY] = sid
        }
        //Log.d("DBController", "setSid called")
    }

    // Get the Int value of the uid, if empty is set to -1
    suspend fun getUid() : Int {
        val prefs = dataStore.data.first()
        return prefs[UID_KEY] ?: -1
    }

    // Set the Int value of the uid
    suspend fun setUid(uid : Int) {
        dataStore.edit { preferences ->
            preferences[UID_KEY] = uid
        }
        //Log.d("DBController", "getUid called")
    }

    // Get the Int value of the Oid, if empty is set to -1
    suspend fun getOid() : Int {
        val prefs = dataStore.data.first()
        return prefs[OID_KEY] ?: -1
    }

    // Set the Int value of the oid
    suspend fun setOid(oid : Int) {
        dataStore.edit { preferences ->
            preferences[OID_KEY] = oid
        }
        //Log.d("DBController", "getOid called")
    }

    // Get the Int value of the Mid, if empty is set to -1
    suspend fun getMid() : Int {
        val prefs = dataStore.data.first()
        return prefs[MID_KEY] ?: -1
    }

    // Set the Int value of the mid
    suspend fun setMid(mid : Int) {
        dataStore.edit { preferences ->
            preferences[MID_KEY] = mid
        }
        //Log.d("DBController", "getMid called")
    }

    suspend fun getLat() : Double {
        val prefs = dataStore.data.first()
        return prefs[LAT_KEY] ?: -48.88120089
    }

    suspend fun setLat(lat : Double) {
        dataStore.edit {  preferences ->
            preferences[LAT_KEY] = lat
        }
    }

    suspend fun getLng() : Double {
        val prefs = dataStore.data.first()
        return prefs[LNG_KEY] ?: -123.34616041
    }

    suspend fun setLng(lng : Double) {
        dataStore.edit {  preferences ->
            preferences[LNG_KEY] = lng
        }
    }

    // Get the boolean value of the profile, if empty is set to false
    suspend fun getProfile() : Boolean {
        val prefs = dataStore.data.first()
        return prefs[PROFILE_KEY] ?: false
    }

    // Set the boolean value of the profile
    suspend fun setProfile(profile : Boolean) {
        dataStore.edit { preferences ->
            preferences[PROFILE_KEY] = profile
        }
    }

    // Get which screen went into the background, if empty is set to "Home"
    suspend fun getScreen() : String {
        val prefs = dataStore.data.first()
        return prefs[SCREEN_KEY] ?: "Home"
    }

    // Set which screen goes in the background
    suspend fun setScreen(screen : String) {
        dataStore.edit { preferences ->
            preferences[SCREEN_KEY] = screen
        }
    }


}