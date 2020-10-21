package com.hajarslamah.criminalintent_2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

class CrimeDetailViewModel :ViewModel(){
    private val crimeRepository = CrimeRepository.get()
    private val crimeIdLiveData = MutableLiveData<UUID>()

    var crimeLiveData: LiveData<Crime?> = Transformations.switchMap(crimeIdLiveData) { crimeId ->
        crimeRepository.getCrime(crimeId)        }
    /////////////////////////////////////////////Load
    fun loadCrime(crimeId: UUID) {
        crimeIdLiveData.value = crimeId    }
    ////////////////////////////////////////////SAve
    fun saveCrime(crime: Crime) {
        crimeRepository.updateCrime(crime)
    }
}