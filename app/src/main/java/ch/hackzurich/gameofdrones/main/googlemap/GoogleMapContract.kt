package ch.hackzurich.gameofdrones.main.googlemap

import android.content.Context
import android.os.Bundle
import ch.hackzurich.gameofdrones.MainApp
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import pl.applover.kotlinmvp.BaseMvpPresenter
import pl.applover.kotlinmvp.BaseMvpView

interface GoogleMapContract {
    interface View : BaseMvpView {
        fun onMapViewReady(googleMap: GoogleMap)
    }

    interface Presenter : BaseMvpPresenter<View> {
        fun initializeMap(savedInstanceState: Bundle?, mMapView: MapView, googleMapPadding: GoogleMapPadding?)
        fun setMarker(latLng: LatLng, iconResId: Int? = null, title: String? = null, snippet: String? = null): Marker
        fun animateMarker(marker: Marker?, end: LatLng)
        fun setCameraPosition(latLng: LatLng)
        fun getLastGpsPosition(): LatLng?
        fun setOnMapClickListener(onMapClickListener: GoogleMap.OnMapClickListener)
        fun setCameraPosition(latLngs: ArrayList<LatLng>, animateCamera: Boolean = false, padding: Int = 10)
        fun setCameraListener(listener: GoogleMap.OnCameraMoveListener?)
        fun setOnMarkerDragListener(onMarkerDragListener: GoogleMap.OnMarkerDragListener)
        fun clear()
        fun setRangeCircle(circleOptions: CircleOptions): Circle
        fun drawRoute(latLngs: ArrayList<LatLng>, colorRid: Int, width: Float = 8f, context: Context = MainApp.instance): Polyline
    }
}