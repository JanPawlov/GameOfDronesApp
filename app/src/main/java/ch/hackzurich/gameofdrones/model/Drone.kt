package ch.hackzurich.gameofdrones.model

import android.location.Location
import android.util.Log
import ch.hackzurich.gameofdrones.main.googlemap.GoogleMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import java.util.*

/**
 * Created by janpawlov ( ͡° ͜ʖ ͡°) on 15/09/2018.
 */
data class Drone(val name: String,
                 val marker: Marker,
                 var droneNetwork: ArrayList<Drone>? = null,
                 val communicator: GoogleMapFragment.DroneCommunicator,
                 val startLatLng: LatLng,
                 val endLatLng: LatLng) {

    fun attachDroneNetwork(droneNetwork: ArrayList<Drone>) {
        this.droneNetwork = droneNetwork
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Drone)
            other.name == this.name
        else
            false
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    var counter: Int = 0
    val travelTime = 50

    val droneLatDelta = (startLatLng.latitude - endLatLng.latitude) / travelTime
    val droneLngDelta = (startLatLng.longitude - endLatLng.longitude) / travelTime

    @Volatile
    var arbitraged = false

    var arbitragedHeight = 10000

    val distanceThreshold = 80 //minumum distance after which drones must adjust their height

    var firstTime = true

    @Volatile
    var distances = arrayListOf<Double>()

    fun startDrone() {
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                if (droneNetwork!![0].name != this@Drone.name)
                    distances.add(communicator.calculateDistance(this@Drone, droneNetwork!![0]))
                if (droneNetwork!![1].name != this@Drone.name)
                    distances.add(communicator.calculateDistance(this@Drone, droneNetwork!![1]))
                if (droneNetwork!![2].name != this@Drone.name)
                    distances.add(communicator.calculateDistance(this@Drone, droneNetwork!![2]))

                counter++
                if (!firstTime) {
                    if (distances[0] <= distanceThreshold && distances[1] <= distanceThreshold) { // all 3 drones on collision course
                        if (!arbitraged) {
                            arbitragedHeight = communicator.adjustHeight()
                            arbitraged = true
                            Log.e("Drone", "Drone ${this@Drone.name} arbitraged height: $arbitragedHeight")
                        }
                    } else if (distances[0] <= distanceThreshold) {
                        if (!arbitraged) {
                            arbitragedHeight = communicator.adjustHeight()
                            arbitraged = true
                            Log.e("Drone", "Drone ${this@Drone.name} arbitraged height: $arbitragedHeight")
                        }
                    } else if (distances[1] <= distanceThreshold) {
                        if (!arbitraged) {
                            arbitragedHeight = communicator.adjustHeight()
                            arbitraged = true
                            Log.e("Drone", "Drone ${this@Drone.name} arbitraged height: $arbitragedHeight")
                        }
                    } else { //no collision course
                        Log.e("Drone", "Drone ${this@Drone.name} returning to normal height")
                        arbitraged = false
                        arbitragedHeight = 10000
                        communicator.collisionCourseEnded()
                    }
                }
                firstTime = false
                distances.clear()
                if (counter / travelTime % 2 == 1)
                    communicator.updateDrone(this@Drone, droneLatDelta, droneLngDelta, arbitragedHeight)
                else
                    communicator.updateDrone(this@Drone, -droneLatDelta, -droneLngDelta, arbitragedHeight)
            }
        }, Math.abs(Random().nextLong() % 200), 2000)
    }
}