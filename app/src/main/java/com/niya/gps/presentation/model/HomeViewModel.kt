package com.niya.gps.presentation.model

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.niya.gps.data.LocationModel
import com.niya.gps.data.LocationService
import com.niya.gps.data.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class HomeViewModel : ViewModel() {
    var startTime = 0L
    var timer: Timer? = null
    val locationUpdatesMD = MutableLiveData<LocationModel>()
    val timeDataMD = MutableLiveData<String>()
    val locationUpdatesLD: LiveData<LocationModel>
        get() = locationUpdatesMD
    val timeDataLD: LiveData<String>
        get() = timeDataMD


    fun startTimer(activity: FragmentActivity?) {
        timer?.cancel()
        timer = Timer()
        startTime = LocationService.startTime
        timer?.schedule(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread { timeDataMD.value = getCurrentTime() }
            }

        }, 1000, 1000)
    }

    private fun getCurrentTime() = TimeUtils.getTime(System.currentTimeMillis() - startTime)

}