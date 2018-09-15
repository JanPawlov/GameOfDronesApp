package ch.hackzurich.gameofdrones.main.googlemap

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import ch.hackzurich.gameofdrones.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*

class GoogleMapPresenter : GoogleMapBP(), GoogleMapP {

    private var googleMap: GoogleMap? = null

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
            }
        }
    }

    private fun setGooglePadding(googleMapPadding: GoogleMapPadding?) {
        googleMapPadding?.let {
            val rect: Rect = googleMapPadding.rectPadding
            googleMap!!.setPadding(rect.left, rect.top, rect.right, rect.bottom)
        }
    }

    override fun setMarker(latLng: LatLng, iconResId: Int?, snippet: String?, title: String?): Marker {
        return googleMap!!.setMarker(latLng, iconResId, snippet, title)
    }

    override fun setOnMarkerDragListener(onMarkerDragListener: GoogleMap.OnMarkerDragListener) {
        googleMap!!.setOnMarkerDragListener(onMarkerDragListener)
    }

    override fun animateMarker(marker: Marker?, end: LatLng) {
        googleMap!!.animateMarker(marker, end)
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