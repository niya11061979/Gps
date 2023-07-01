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

    val locationUpdatesMD = MutableLiveData<LocationModel>()
    val locationUpdatesLD: LiveData<LocationModel>
        get() = locationUpdatesMD
}