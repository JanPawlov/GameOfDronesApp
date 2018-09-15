package ch.hackzurich.gameofdrones

data class AircraftData(
        var icao: String,
        var lat: Double,
        var lon: Double,
        var alt: Int?,
        var speed: Double?,
        var heading: Double?,
        var last_update: String?
)