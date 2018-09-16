package ch.hackzurich.gameofdrones.model

import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.*

data class AircraftData(
        var icao: String,
        var lat: Double,
        var lon: Double,
        var alt: Int?,
        var speed: Double?,
        var heading: Double?,
        var last_update: String?
) {
    companion object {
        fun setDrone(latLng: LatLng, name: String, sdf: SimpleDateFormat, alt: Int = 420): AircraftData {
            return AircraftData(name,
                    latLng.latitude,
                    latLng.longitude,
                    alt,
                    50 / 3.0,
                    0.0, sdf.format(Calendar.getInstance().time))
        }
    }
}