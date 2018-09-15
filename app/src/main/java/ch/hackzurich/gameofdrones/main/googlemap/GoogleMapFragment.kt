package ch.hackzurich.gameofdrones.main.googlemap

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.hackzurich.gameofdrones.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_google_map.*

class GoogleMapFragment : GoogleMapBF(), GoogleMapV {
    override var mPresenter: GoogleMapP = GoogleMapPresenter()

    private var mOnMapViewReadyListener: OnMapViewReadyListener? = null
    val googleMapPadding: GoogleMapPadding? by lazy { arguments?.getParcelable<GoogleMapPadding>("googleMapPadding") }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_google_map, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter.initializeMap(savedInstanceState, google_map_view, googleMapPadding)
    }

    fun setCameraAtLastPositionIfNullGivenLatLng(latLng: LatLng) {
        val lastPosition = getLastGpsPosition()
        if (lastPosition == null) {
            setCameraPosition(latLng)
        } else {
            setCameraPosition(lastPosition)
        }
    }

    fun clear() {
        mPresenter.clear()
    }

    fun setMarker(latLng: LatLng, iconResId: Int? = null, title: String? = null, snippet: String? = null): Marker {
        return mPresenter.setMarker(latLng, iconResId, title, snippet)
    }

    fun animateMarker(marker: Marker?, end: LatLng) {
        mPresenter.animateMarker(marker, end)
    }


    fun setCameraPosition(latLng: LatLng) {
        mPresenter.setCameraPosition(latLng)
    }

    fun setCameraPosition(latLngs: ArrayList<LatLng>, animateCamera: Boolean = false, padding: Int = 10) {
        mPresenter.setCameraPosition(latLngs, animateCamera, padding)
    }

    fun getLastGpsPosition(): LatLng? {
        return mPresenter.getLastGpsPosition()
    }

    fun setRangeCircle(circleOptions: CircleOptions): Circle {
        return mPresenter.setRangeCircle(circleOptions)
    }

    fun drawRoute(latLngs: ArrayList<LatLng>, colorRid: Int = android.R.color.holo_orange_light, width: Float = 8f, context: Context = MainApp.instance): Polyline {
        return mPresenter.drawRoute(latLngs, colorRid, width, context)
    }

    fun setCameraListener(listener: GoogleMap.OnCameraMoveListener?) {
        mPresenter.setCameraListener(listener)
    }

    override fun onResume() {
        super.onResume()
        google_map_view.onResume()
    }

    override fun onPause() {
        super.onPause()
        google_map_view.onPause()
    }

    override fun onDestroy() {
        google_map_view.onDestroy()
        super.onDestroy()
    }


    fun setOnMapClickListener(onMapClickListener: GoogleMap.OnMapClickListener) {
        mPresenter.setOnMapClickListener(onMapClickListener)
    }

    fun setOnMapViewReadyListener(onMapViewReadyListener: OnMapViewReadyListener) {
        mOnMapViewReadyListener = onMapViewReadyListener
    }

    fun setOnMarkerDragListener(onMarkerDragListener: GoogleMap.OnMarkerDragListener) {
        mPresenter.setOnMarkerDragListener(onMarkerDragListener)
    }

    override fun onMapViewReady(googleMap: GoogleMap) {
        mOnMapViewReadyListener?.onMapViewReady(googleMap)
    }

    interface OnMapViewReadyListener {
        fun onMapViewReady(googleMap: GoogleMap)
    }

    companion object {
        fun newInstance(googleMapPadding: GoogleMapPadding? = null): GoogleMapFragment {
            val fragment = GoogleMapFragment()
            with(Bundle()) {
                // put args
                googleMapPadding?.let {
                    putParcelable("googleMapPadding", googleMapPadding)
                }
                fragment.arguments = this
            }
            return fragment
        }
    }
}