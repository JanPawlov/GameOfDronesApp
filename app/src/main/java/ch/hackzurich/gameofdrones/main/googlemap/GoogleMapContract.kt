package ch.hackzurich.gameofdrones.main.googlemap

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import ch.hackzurich.gameofdrones.AircraftData
import ch.hackzurich.gameofdrones.AircraftMarkerPosition
import ch.hackzurich.gameofdrones.MainApp
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import io.reactivex.subjects.PublishSubject
import pl.applover.kotlinmvp.BaseMvpPresenter
import pl.applover.kotlinmvp.BaseMvpView
import java.text.SimpleDateFormat

interface GoogleMapContract {
    interface View : BaseMvpView {
        fun onMapViewReady(googleMap: GoogleMap)
        fun getPlaneBitmap(): Bitmap
        fun getDroneBitmap(): Bitmap
        fun startClearingLoop()
        fun startDroneMovement()
        fun getSimpleDateFormat(): SimpleDateFormat
    }

    interface Presenter : BaseMvpPresenter<View> {
        fun initializeMap(savedInstanceState: Bundle?, mMapView: MapView, googleMapPadding: GoogleMapPadding?)
        fun setMarker(latLng: LatLng, iconResId: Bitmap, data: AircraftData): AircraftMarkerPosition
        fun animateMarker(aircraft: AircraftMarkerPosition, end: LatLng)
        fun setCameraPosition(latLng: LatLng)
        fun getLastGpsPosition(): LatLng?
        fun setOnMapClickListener(onMapClickListener: GoogleMap.OnMapClickListener)
        fun setCameraPosition(latLngs: ArrayList<LatLng>, animateCamera: Boolean = false, padding: Int = 10)
        fun setCameraListener(listener: GoogleMap.OnCameraMoveListener?)
        fun setOnMarkerDragListener(onMarkerDragListener: GoogleMap.OnMarkerDragListener)
        fun clear()
        fun moveDrone(marker: Marker, deltaLat: Double, deltaLong: Double)
        fun start()
        fun setRangeCircle(circleOptions: CircleOptions): Circle
        fun drawRoute(latLngs: ArrayList<LatLng>, colorRid: Int, width: Float = 8f, context: Context = MainApp.instance): Polyline
        fun getAircraftDataPublisher(): PublishSubject<AircraftData>
    }
}