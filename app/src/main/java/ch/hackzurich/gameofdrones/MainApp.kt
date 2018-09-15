package ch.hackzurich.gameofdrones

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import ch.hackzurich.gameofdrones.api.AppComponent
import ch.hackzurich.gameofdrones.api.DaggerAppComponent
import ch.hackzurich.gameofdrones.api.DronesModule
import ch.hackzurich.gameofdrones.util.DelegatesExt

/**
 * Created by janpawlov ( ͡° ͜ʖ ͡°) on 15/09/2018.
 */
class MainApp : Application(), LocationListener {

    private val locationListeners: ArrayList<MyLocationChangeListener> = ArrayList()
    private var isLocationBeingUpdated = false

    override fun onCreate() {
        super.onCreate()
        instance = this
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    companion object {
        var instance: MainApp by DelegatesExt.notNullSingleValue()
        val appComponent: AppComponent = DaggerAppComponent.create()

        private var locationManager: LocationManager by DelegatesExt.notNullSingleValue()
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation(): Location? {
        return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    }

    fun addLocationListener(locationListener: MyLocationChangeListener) {
        if (!isLocationBeingUpdated) {
            startListeningForLocationChanges()
        }
        locationListeners.add(locationListener)
    }

    @SuppressLint("MissingPermission")
    private fun startListeningForLocationChanges() {
        isLocationBeingUpdated = true
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 200f, this)
    }

    fun deleteLocationListener(locationListener: MyLocationChangeListener) {
        locationListeners.remove(locationListener)
        if (locationListeners.size == 0) {
            stopLocationUpdates()
        }
    }

    private fun stopLocationUpdates() {
        isLocationBeingUpdated = false
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(p0: Location?) {

        if (locationListeners.size == 0) {
            stopLocationUpdates()
            return
        }

        locationListeners.forEach {
            it.onLocationChanged(p0)
        }
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

    interface MyLocationChangeListener {
        fun onLocationChanged(location: Location?)
    }

}