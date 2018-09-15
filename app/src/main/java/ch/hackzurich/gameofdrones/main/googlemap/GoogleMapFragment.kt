package ch.hackzurich.gameofdrones.main.googlemap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.hackzurich.gameofdrones.AircraftData
import ch.hackzurich.gameofdrones.AircraftMarkerPosition
import ch.hackzurich.gameofdrones.MainApp
import ch.hackzurich.gameofdrones.R
import ch.hackzurich.gameofdrones.util.GoogleMapBF
import ch.hackzurich.gameofdrones.util.GoogleMapP
import ch.hackzurich.gameofdrones.util.GoogleMapV
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_google_map.*


class GoogleMapFragment : GoogleMapBF(), GoogleMapV {
    override var mPresenter: GoogleMapP = GoogleMapPresenter()

    private var mOnMapViewReadyListener: OnMapViewReadyListener? = null
    val googleMapPadding: GoogleMapPadding? by lazy { arguments?.getParcelable<GoogleMapPadding>("googleMapPadding") }

    var aircraftDisposable: Disposable? = null

    val aircraftObserver = AircraftDataObserver()

    val aircraftsHashMap = HashMap<String, AircraftMarkerPosition>()

    val planeBmp: Bitmap by lazy {
        val drawable = ContextCompat.getDrawable(context!!, R.drawable.ic_plane)
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)

        bitmap
    }

    val droneBmp: Bitmap by lazy {
        val drawable = ContextCompat.getDrawable(context!!, R.drawable.ic_drone)
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)

        bitmap
    }

    override fun getDroneBitmap() = droneBmp
    override fun getPlaneBitmap() = planeBmp

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_google_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter.initializeMap(savedInstanceState, google_map_view, googleMapPadding)
        mPresenter.getAircraftDataPublisher().subscribeWith(aircraftObserver)

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

    fun setMarker(latLng: LatLng, iconBitmap: Bitmap, data: AircraftData): AircraftMarkerPosition {
        return mPresenter.setMarker(latLng, iconBitmap, data)
    }

    fun animateMarker(aircraft: AircraftMarkerPosition, end: LatLng) {
        mPresenter.animateMarker(aircraft, end)
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
        aircraftDisposable?.dispose()
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


    inner class AircraftDataObserver : Observer<AircraftData> {
        override fun onComplete() {
            //empty
        }

        override fun onSubscribe(d: Disposable) {
            aircraftDisposable = d
        }

        override fun onNext(t: AircraftData) {
            if (aircraftsHashMap.containsKey(t.icao)) {
                val currentMarker = aircraftsHashMap[t.icao]
                google_map_view.post {
                    animateMarker(currentMarker!!, LatLng(t.lat, t.lon))
                }
            } else {
                val bmp = if (t.icao.startsWith("F")) getDroneBitmap() else getPlaneBitmap()
                google_map_view.post {
                    val aircraft = setMarker(LatLng(t.lat, t.lon), bmp, t)
                    aircraftsHashMap[t.icao] = aircraft
                }
            }
        }

        override fun onError(e: Throwable) {
            //empty
        }
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