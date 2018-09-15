package ch.hackzurich.gameofdrones.main.googlemap

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import ch.hackzurich.gameofdrones.Aircraft
import ch.hackzurich.gameofdrones.AircraftData
import ch.hackzurich.gameofdrones.AircraftMarkerPosition
import ch.hackzurich.gameofdrones.MainApp
import ch.hackzurich.gameofdrones.util.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*


class GoogleMapPresenter : GoogleMapBP(), GoogleMapP {

    private var googleMap: GoogleMap? = null

    private val aircraftDataPublishSubject: PublishSubject<AircraftData> = PublishSubject.create()

    private val fetchTimer: Timer = Timer()
    private val FETCH_DELAY: Long = 2000

    init {
        MainApp.appComponent.inject(this)
    }

    @SuppressLint("MissingPermission")
    override fun initializeMap(savedInstanceState: Bundle?, mMapView: MapView, googleMapPadding: GoogleMapPadding?) {
        try {
            MapsInitializer.initialize(MainApp.instance)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mMapView.onCreate(savedInstanceState)
        mMapView.onResume() // needed to get the map to display immediately
        mMapView.getMapAsync { mMap ->
            googleMap = mMap
            mView?.let {
                googleMap!!.isMyLocationEnabled = true
                mView?.onMapViewReady(googleMap!!)
                startFetchingAircraftData()
            }
        }
    }

    fun stopFetching() {
        fetchTimer.cancel()
    }

    fun getAircraftData() {
        mApi.getAircraftData().subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                .subscribe({
                    it.body()?.data?.forEach { aircraftDataPublishSubject.onNext(it) }
                }, {
                    it.printStackTrace()
                })
    }

    private fun startFetchingAircraftData() {
        fetchTimer.schedule(object : TimerTask() {
            override fun run() {
                getAircraftData()
            }
        }, 0, FETCH_DELAY)
    }

    override fun getAircraftDataPublisher(): PublishSubject<AircraftData> {
        return aircraftDataPublishSubject
    }

    private fun setGooglePadding(googleMapPadding: GoogleMapPadding?) {
        googleMapPadding?.let {
            val rect: Rect = googleMapPadding.rectPadding
            googleMap!!.setPadding(rect.left, rect.top, rect.right, rect.bottom)
        }
    }

    override fun setMarker(latLng: LatLng, iconBitmap: Bitmap, data: AircraftData): AircraftMarkerPosition {
        return googleMap!!.setMarker(latLng, iconBitmap, data)
    }

    override fun setOnMarkerDragListener(onMarkerDragListener: GoogleMap.OnMarkerDragListener) {
        googleMap!!.setOnMarkerDragListener(onMarkerDragListener)
    }


    override fun animateMarker(aircraft: AircraftMarkerPosition, end: LatLng) {
        googleMap!!.changeMarker(aircraft, end, mView?.getContext()!!)
    }

    override fun setCameraPosition(latLng: LatLng) {
        googleMap!!.setCameraPosition(latLng)
    }

    override fun setCameraPosition(latLngs: ArrayList<LatLng>, animateCamera: Boolean, padding: Int) {
        googleMap!!.setCameraPosition(latLngs, animateCamera, padding)
    }

    override fun setCameraListener(listener: GoogleMap.OnCameraMoveListener?) {
        googleMap!!.setOnCameraMoveListener(listener)
    }

    override fun setRangeCircle(circleOptions: CircleOptions): Circle {
        return googleMap!!.setRangeCircle(circleOptions)
    }

    override fun getLastGpsPosition(): LatLng? {
        return getLastLocation()
    }

    override fun drawRoute(latLngs: ArrayList<LatLng>, colorRid: Int, width: Float, context: Context): Polyline {
        return googleMap!!.drawPolyline(latLngs, colorRid, width, context)
    }

    override fun setOnMapClickListener(onMapClickListener: GoogleMap.OnMapClickListener) {
        googleMap!!.setOnMapClickListener(onMapClickListener)
    }

    override fun clear() {
        googleMap?.clear()
    }


}