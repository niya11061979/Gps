package com.niya.gps.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.niya.gps.R
import com.niya.gps.presentation.MainActivity
import org.osmdroid.util.GeoPoint


class LocationService : Service() {
    private var distance = 0f
    private lateinit var geoPointList: ArrayList<GeoPoint>
    private var lasLocation: Location? = null
    private lateinit var locProvider: FusedLocationProviderClient
    private lateinit var locRequest: LocationRequest
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isStart = true
        startNotification()
        startLocationUpdates()
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        geoPointList = ArrayList()
        initLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        isStart = false
        locProvider.removeLocationUpdates(locCallBack)
    }

    private val locCallBack = object : LocationCallback() {
        override fun onLocationResult(lResult: LocationResult) {
            super.onLocationResult(lResult)
            val currentLocation = lResult.lastLocation
            if (lasLocation != null && currentLocation != null) {
                distance += currentLocation
                    .let { lasLocation?.distanceTo(it) }!!
                geoPointList.add(GeoPoint(currentLocation.longitude, currentLocation.latitude))
                val locModel = LocationModel(currentLocation.speed, distance, geoPointList)
                sendLocModel(locModel)
            }
            lasLocation = currentLocation
        }
    }

    private fun sendLocModel(locModel: LocationModel) {
        val i = Intent(LOC_MODEL_INTENT)
        i.putExtra(LOC_MODEL_INTENT, locModel)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(i)
    }

    private fun startNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nChannel = NotificationChannel(
                CHANNEL_ID,
                "Сервис местоположения",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val nManager =
                getSystemService((NotificationManager::class.java)) as NotificationManager
            nManager.createNotificationChannel(nChannel)
        }

        val nIntent = Intent(this, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(this, REQUEST_CODE, nIntent, 0)
        val notification = NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Программа GPS работает!")
            .setContentIntent(pIntent)
            .build()
        startForeground(FOREGROUND_ID, notification)
    }

    private fun initLocation() {
        locRequest = LocationRequest.Builder(PRIORITY_HIGH_ACCURACY, 5000).apply {
            setMinUpdateIntervalMillis(5000)
        }.build()

        locProvider = LocationServices.getFusedLocationProviderClient(baseContext)

    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        locProvider.requestLocationUpdates(
            locRequest,
            locCallBack,
            Looper.myLooper()
        )
    }

    companion object {
        const val CHANNEL_ID = "channel_1"
        const val REQUEST_CODE = 12154
        const val FOREGROUND_ID = 33
        var isStart = false
        var startTime = 0L
        const val LOC_MODEL_INTENT = "loc_intent"
    }
}