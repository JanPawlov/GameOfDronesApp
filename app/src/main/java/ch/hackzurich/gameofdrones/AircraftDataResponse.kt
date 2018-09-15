package ch.hackzurich.gameofdrones

import com.google.gson.annotations.SerializedName

data class AircraftDataResponse(
        @SerializedName("timestamp") var timestamp: String,
        @SerializedName("data") var data: List<AircraftData>
)