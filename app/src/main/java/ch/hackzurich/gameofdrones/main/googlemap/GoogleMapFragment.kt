package ch.hackzurich.gameofdrones.main.googlemap

import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.hackzurich.gameofdrones.model.AircraftData
import ch.hackzurich.gameofdrones.model.AircraftMarkerPosition
import ch.hackzurich.gameofdrones.R
import ch.hackzurich.gameofdrones.model.Drone
import ch.hackzurich.gameofdrones.util.GoogleMapBF
import ch.hackzurich.gameofdrones.util.GoogleMapP
import ch.hackzurich.gameofdrones.util.GoogleMapV
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.SphericalUtil
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_google_map.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList


class GoogleMapFragment : GoogleMapBF(), GoogleMapV {
    override var mPresenter: GoogleMapP = GoogleMapPresenter()

    private var mOnMapViewReadyListener: OnMapViewReadyListener? = null
    val googleMapPadding: GoogleMapPadding? by lazy { arguments?.getParcelable<GoogleMapPadding>("googleMapPadding") }

    var aircraftDisposable: Disposable? = null

    val aircraftObserver = AircraftDataObserver()

    val aircraftsHashMap = ConcurrentHashMap<String, AircraftMarkerPosition>()

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

    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.getDefault())

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

    val timer: Timer = Timer()
    val clearDelay: Long = 1000

    val droneStartLocation = LatLng(47.451796, 8.532610)
    val droneEndLocation = LatLng(47.465436, 8.596335)

    val droneSpeed = (50 / 3) //m/s
    val droneMovementTime = 400
    val dronePositionRefresh = 2

    val latDelta = (droneEndLocation.latitude - droneStartLocation.latitude) / droneMovementTime
    val lngDelta = (droneEndLocation.longitude - droneStartLocation.longitude) / droneMovementTime

    val droneMoveTimer: Timer = Timer()
    var droneCounter: Long = 0

    var droneMarker: Marker? = null

    override fun getSimpleDateFormat(): SimpleDateFormat = sdf

    override fun startDroneMovement() {
        droneMoveTimer.schedule(object : TimerTask() {
            override fun run() {
                droneMarker?.let {
                    if ((droneCounter % droneMovementTime).toInt() != 0) { //drone has reached its destination
                        if ((droneCounter / droneMovementTime).toInt() % 2 == 1) { //drone is retrning
                            google_map_view.post {
                                //mPresenter.moveDrone(it, -latDelta * dronePositionRefresh, -lngDelta * dronePositionRefresh)
                            }
                        } else {
                            google_map_view.post {
                                //mPresenter.moveDrone(it, latDelta * dronePositionRefresh, lngDelta * dronePositionRefresh)
                            }
                        }
                    }
                } ?: kotlin.run {
                    //initial move
                    google_map_view.post {
                        droneMarker = mPresenter.setMarker(droneStartLocation, droneBmp, AircraftData.setDrone(droneStartLocation, "F2137", sdf)).marker
                    }
                }
                droneCounter += dronePositionRefresh
            }
        }, 0, 1000/15)

    }

    override fun startClearingLoop() {
        timer.schedule(object : TimerTask() {
            override fun run() {
                Log.e(this::class.java.name, "Starting clearing loop")
                val enumerator = aircraftsHashMap.keys()
                while (enumerator.hasMoreElements()) {
                    val key = enumerator.nextElement()
                    val aircraft = aircraftsHashMap[key]
                    aircraft?.data?.last_update?.let {
                        try {
                            val date = sdf.parse(it).time
                            val now = Calendar.getInstance(TimeZone.getDefault()).time.time
                            if (Math.abs(now - date) > 120000) {
                                val pos = aircraftsHashMap[key]
                                google_map_view.post {
                                    pos?.marker?.remove()
                                }
                                aircraftsHashMap.remove(key)
                                Log.e(this::class.java.name, "removing old marker")
                            }
                        } catch (e: ParseException) {
                            e.printStackTrace()
                        }
                    } ?: kotlin.run {
                        val pos = aircraftsHashMap[key]
                        google_map_view.post {
                            pos?.marker?.remove()
                        }
                        aircraftsHashMap.remove(key)
                        Log.e(this::class.java.name, "removing old marker")
                    }
                }
            }
        }, clearDelay, clearDelay * 60)
        setupDrones()
    }

    val firstDroneStartLocation = LatLng(47.453149, 8.557557)
    val firstDroneEndLocation = LatLng(47.462017, 8.558823)

    val secondDroneStartLocation = LatLng(47.458019, 8.551291)
    val secondDroneEndLocation = LatLng(47.456995, 8.564725)

    val thirdDroneStartLocation = LatLng(47.460711, 8.562884)
    val thirdDroneEndLocation = LatLng(47.454572, 8.553242)

    val startLocations = arrayListOf(firstDroneStartLocation, secondDroneStartLocation, thirdDroneStartLocation)
    val endLocations = arrayListOf(firstDroneEndLocation, secondDroneEndLocation, thirdDroneEndLocation)

    val communicator = Communicator()
    lateinit var mDroneController: DroneController

    fun setupDrones() {
        val firstDroneMarker = setMarker(firstDroneStartLocation,
                droneBmp,
                AircraftData.setDrone(firstDroneStartLocation, "F2137/0", sdf)).marker
        val secondDroneMarker = setMarker(secondDroneStartLocation,
                droneBmp,
                AircraftData.setDrone(secondDroneStartLocation, "F2137/1", sdf)).marker
        val thirdDroneMarker = setMarker(thirdDroneStartLocation,
                droneBmp,
                AircraftData.setDrone(thirdDroneStartLocation, "F2137/2", sdf)).marker
        val markers = listOf(firstDroneMarker, secondDroneMarker, thirdDroneMarker)
        val droneNetwork = arrayListOf<Drone>()
        for (i in 0 until markers.size) {
            droneNetwork.add(Drone(name = "F2137/$i", marker = markers[i], communicator = communicator, startLatLng = startLocations[i], endLatLng = endLocations[i]))
        }
        val iterator = droneNetwork.iterator()
        while (iterator.hasNext())
            iterator.next().attachDroneNetwork(droneNetwork)
        mDroneController = DroneController(droneNetwork)
        mDroneController.startDrones()
    }

    inner class Communicator : DroneCommunicator {

        @Volatile
        var firstAcommodated = false
        @Volatile
        var secondAcommodated = false
        @Volatile
        var thirdAcommodated = false

        override fun updateDrone(drone: Drone, latDelta: Double, lngDelta: Double, altitude: Int) {
            google_map_view.post {
                mPresenter.moveDrone(drone, latDelta, lngDelta, altitude)
            }
        }

        @Volatile
        var distance = 0.0

        @Synchronized
        override fun calculateDistance(droneOne: Drone, droneTwo: Drone): Double {
            google_map_view.post {
                distance = SphericalUtil.computeDistanceBetween(droneOne.marker.position, droneTwo.marker.position)
                Log.e("CalculatedDistance", "Calculated distance between drones: %%%%% $distance %%%%%%")
            }
            return distance
        }

        @Synchronized
        override fun collisionCourseEnded() {
            if (firstAcommodated ||
                    secondAcommodated || thirdAcommodated) {
                firstAcommodated = false
                secondAcommodated = false
                thirdAcommodated = false
            }
        }

        @Synchronized
        override fun adjustHeight(): Int {
            return if (!firstAcommodated) {
                firstAcommodated = true
                400
            } else if (!secondAcommodated) {
                secondAcommodated = true
                395
            } else {
                thirdAcommodated = true
                405
            }
        }
    }

    inner class DroneController(private val drones: ArrayList<Drone>) {

        fun startDrones() {
            for (drone in drones)
                google_map_view.post {
                    drone.startDrone()
                }
        }
    }

    interface DroneCommunicator {
        fun updateDrone(drone: Drone, latDelta: Double, lngDelta: Double, altitude: Int = 420)
        fun calculateDistance(droneOne: Drone, droneTwo: Drone): Double
        fun adjustHeight(): Int
        fun collisionCourseEnded()
    }


    fun setMarker(latLng: LatLng, iconBitmap: Bitmap, data: AircraftData): AircraftMarkerPosition {
        return mPresenter.setMarker(latLng, iconBitmap, data)
    }

    fun animateMarker(aircraft: AircraftMarkerPosition, end: LatLng) {
        mPresenter.animateMarker(aircraft, end)
    }


    private fun setCameraPosition(latLng: LatLng) {
        mPresenter.setCameraPosition(latLng)
    }

    fun setCameraPosition(latLngs: ArrayList<LatLng>, animateCamera: Boolean = false, padding: Int = 10) {
        mPresenter.setCameraPosition(latLngs, animateCamera, padding)
    }

    fun getLastGpsPosition(): LatLng? {
        return mPresenter.getLastGpsPosition()
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
        google_map_view?.onDestroy()
        super.onDestroy()
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
            if (!t.icao.contains("F2137"))
                if (aircraftsHashMap.containsKey(t.icao)) {
                    val currentMarker = aircraftsHashMap[t.icao]

                    google_map_view.post {
                        animateMarker(currentMarker!!, LatLng(t.lat, t.lon))
                    }
                    aircraftsHashMap[t.icao]?.data = t
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