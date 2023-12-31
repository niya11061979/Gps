package com.niya.gps.presentation.fragment

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.LocationManager
import android.os.Build

import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.niya.gps.R
import com.niya.gps.data.LocationModel
import com.niya.gps.data.LocationService
import com.niya.gps.data.LocationService.Companion.LOC_MODEL_INTENT
import com.niya.gps.databinding.FragmentHomeBinding
import com.niya.gps.presentation.model.HomeViewModel
import com.niya.gps.utils.*
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*

class HomeFragment : ViewBindingFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    private var isServiceRunning = false
    private var pl: Polyline? = null
    private var isFirstStar = true
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsOsm()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClicks()
        registerPermissions()
        checkServiceState()
        updateTime()
        registerLocReceiver()
        bindViewModel()
    }

    override fun onDetach() {
        super.onDetach()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
    }

    override fun onResume() {
        super.onResume()
        checkLocPermissions()
    }

    private fun bindViewModel() = with(binding) {
        viewModel.locationUpdatesLD.observe(viewLifecycleOwner) {
            val distance = resources.getString(R.string.distance_text) +
                    "${String.format("%.1f", it.distance)}м"
            val speed = resources.getString(R.string.speed_text) +
                    "${String.format("%.1f", 3.6 * it.speed)} км/ч"
            val aSpeed = resources.getString(R.string.average_speed_text) +
                    "${averageSpeed(it.distance)} км/ч"
            speedTV.text = speed
            distanceTV.text = distance
            averageSpeedTV.text = aSpeed
            updatePolyline(it.geoPointList)
        }
    }

    private fun updateTime() {
        viewModel.timeDataLD.observe(viewLifecycleOwner) {
            binding.timeTV.text = "${resources.getString(R.string.time_text)}: $it"
        }
    }


    private fun checkServiceState() {
        isServiceRunning = LocationService.isStart
        if (isServiceRunning) {
            binding.startStopFA.setImageResource(R.drawable.ic_stop)
            viewModel.startTimer(activity)
        }
    }

    private fun setOnClicks() {
        val listener = onClicks()
        with(binding) {
            centerFA.setOnClickListener(listener)
            startStopFA.setOnClickListener(listener)
            zoomInButton.setOnClickListener(listener)
            zoomOutButton.setOnClickListener(listener)
        }
    }


    private fun onClicks(): OnClickListener {
        return OnClickListener {
            when (it.id) {
                R.id.startStopFA -> startStopService()
                R.id.zoomInButton -> binding.mapView.controller.zoomIn()
                R.id.zoomOutButton -> binding.mapView.controller.zoomOut()
                R.id.centerFA -> {
                }
            }
        }
    }

    private fun startStopService() {
        if (isServiceRunning) {
            activity?.stopService(Intent(activity, LocationService::class.java))
            binding.startStopFA.setImageResource(R.drawable.ic_play)
            viewModel.timer?.cancel()
        } else {
            startLocService()
            binding.startStopFA.setImageResource(R.drawable.ic_stop)
        }
        isServiceRunning = !isServiceRunning
    }

    private fun startLocService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(Intent(activity, LocationService::class.java))
        } else {
            activity?.startService(Intent(activity, LocationService::class.java))
        }
        LocationService.startTime = System.currentTimeMillis()
        viewModel.startTimer(activity)
    }


    private fun settingsOsm() {
        Configuration.getInstance().load(
            activity as AppCompatActivity,
            activity?.getSharedPreferences(OSM_PREF, Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    private fun initOsm() = with(binding) {
        pl = Polyline()
        pl?.outlinePaint?.color = Color.BLACK
        mapView.controller.setZoom(15.0)
        val mLocProvider = GpsMyLocationProvider(activity)
        val mLocOverlay = MyLocationNewOverlay(mLocProvider, mapView)
        mLocOverlay.enableMyLocation()
        mLocOverlay.enableFollowLocation()
        mLocOverlay.runOnFirstFix {
            mapView.overlays.clear()
            mapView.overlays.add(mLocOverlay)
            mapView.overlays.add(pl)
        }

    }

    private fun registerPermissions() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                initOsm()
                checkLocationEnabled()
            }
        }
    }

    private fun checkLocPermissions() {
        if (haveQ()) checkPermissionAfter10() else checkPermissionBefor10()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissionAfter10() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            && checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            initOsm()
            checkLocationEnabled()
        } else {
            pLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        }
    }


    private fun checkPermissionBefor10() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            initOsm()
            checkLocationEnabled()
        } else {
            pLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            )
        }
    }


    private fun checkLocationEnabled() {
        val lManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isEnabled) {
            DialogManager.showDialog(requireContext(), object : DialogManager.GeoEnableListener {
                override fun onClick() {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LOC_MODEL_INTENT) {
                val locModel = intent.getSerializableExtra(LOC_MODEL_INTENT) as LocationModel
                viewModel.locationUpdatesMD.value = locModel
            }
        }
    }

    private fun registerLocReceiver() {
        val locFilter = IntentFilter(LOC_MODEL_INTENT)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, locFilter)
    }

    private fun addPoint(list: List<GeoPoint>) {
        pl?.addPoint(list[list.size - 1])

    }

    private fun fillPolyline(list: List<GeoPoint>) {
        list.forEach { pl?.addPoint(it) }
    }

    private fun updatePolyline(list: List<GeoPoint>) {
        if (list.size > 1 && isFirstStar) {
            fillPolyline(list)
            isFirstStar = false
        } else {
            addPoint(list)
        }
    }

    private fun averageSpeed(distance: Float): String {
        return String.format(
            "%.1f",
            3.6f * (distance / (System.currentTimeMillis() - viewModel.startTime))
        )
    }


    companion object {
        private const val OSM_PREF = "osm_pref"

    }
}

