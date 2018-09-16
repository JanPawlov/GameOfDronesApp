package ch.hackzurich.gameofdrones.main.googlemap

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import ch.hackzurich.gameofdrones.model.AircraftData
import ch.hackzurich.gameofdrones.model.AircraftDataResponse
import ch.hackzurich.gameofdrones.model.AircraftMarkerPosition
import ch.hackzurich.gameofdrones.MainApp
import ch.hackzurich.gameofdrones.model.Drone
import ch.hackzurich.gameofdrones.util.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import okhttp3.*
import okio.ByteString
import java.util.*
import kotlin.collections.ArrayList


class GoogleMapPresenter : GoogleMapBP(), GoogleMapP {

    private var googleMap: GoogleMap? = null

    private val client = OkHttpClient()

    private val aircraftDataPublishSubject: PublishSubject<AircraftData> = PublishSubject.create()

    private val fetchTimer: Timer = Timer()
    private val FETCH_DELAY: Long = 100000

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
                //googleMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(mView?.getContext()!!,R.raw.map_style))
                mView?.onMapViewReady(googleMap!!)
                //start()
                setCameraPosition(LatLng(47.457612, 8.557476))
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
                    it.body()?.data?.shuffled()?.subList(0, it.body()!!.data.size / 2)?.forEach { aircraftDataPublishSubject.onNext(it) }
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
        mView?.startClearingLoop()
        // mView?.startDroneMovement()
    }

    override fun getAircraftDataPublisher(): PublishSubject<AircraftData> {
        return aircraftDataPublishSubject
    }

    override fun setMarker(latLng: LatLng, iconBitmap: Bitmap, data: AircraftData): AircraftMarkerPosition {
        return googleMap!!.setMarker(latLng, iconBitmap, data)
    }

    override fun setOnMarkerDragListener(onMarkerDragListener: GoogleMap.OnMarkerDragListener) {
        googleMap!!.setOnMarkerDragListener(onMarkerDragListener)
    }

    override fun moveDrone(drone: Drone, deltaLat: Double, deltaLong: Double, alt: Int) {
        drone.marker.position = LatLng(drone.marker.position.latitude + deltaLat, drone.marker.position.longitude + deltaLong)
        drone.marker.snippet = "Height: $alt"
        mApi.postDronePosition(AircraftData.setDrone(drone.marker.position, drone.name, mView?.getSimpleDateFormat()!!, alt))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({
                    Log.e("Sending drone position", it.isSuccessful.toString())
                }, {
                    it.printStackTrace()
                    Log.e("Sending drone position", "Error")
                })
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

    override fun start() {
        Log.e("AirWebSocketListener", "Start")
        val request = Request.Builder().url("ws://hackzurich.involi.live/ws").build()
        val listener = AircraftWebSocketListener()
        val ws = client?.newWebSocket(request, listener)

        client?.dispatcher()?.executorService()?.shutdown()
        mView?.startClearingLoop()
        //mView?.startDroneMovement()
    }

    inner class AircraftWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            Log.e("AirWebSocketListener", "OnOpen")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            Log.e("AirWebSocketListener", "Failure")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
            Log.e("AirWebSocketListener", "Closing")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            try {
                val resp = Gson().fromJson<AircraftDataResponse>(text, AircraftDataResponse::class.java)
                resp.data.forEach {
                    aircraftDataPublishSubject.onNext(it)
                }
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            super.onMessage(webSocket, bytes)
            Log.e("AirWebSocketListener", bytes.utf8())
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            Log.e("AirWebSocketListener", "onClosed")
        }
    }
}