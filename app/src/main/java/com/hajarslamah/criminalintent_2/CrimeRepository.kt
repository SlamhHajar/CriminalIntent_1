package com.hajarslamah.criminalintent_2

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import database.CrimeDatabase
import database.migration1_2
import database.migration2_3
import java.io.File
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context) {
    private val database : CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME    ).addMigrations(migration2_3).build()
    private val crimeDao = database.crimeDao()
    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()
    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)
    private val executor = Executors.newSingleThreadExecutor()
    private val filesDir = context.applicationContext.filesDir


    // fun getCrimes(): List<Crime> = crimeDao.getCrimes()
  //  fun getCrime(id: UUID): Crime? = crimeDao.getCrime(id)

    fun updateCrime(crime: Crime) {
        executor.execute {
            crimeDao.updateCrime(crime)
        }
    }
    fun addCrime(crime: Crime) {
        executor.execute {
            crimeDao.addCrime(crime)
        }

    }
    fun getPhotoFile(crime: Crime): File = File(filesDir, crime.photoFileName)
    companion object {
        private var INSTANCE: CrimeRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(context)
            }
        }
        fun get(): CrimeRepository {
            return INSTANCE ?:
            throw IllegalStateException("CrimeRepository must be initialized")
        }    } }