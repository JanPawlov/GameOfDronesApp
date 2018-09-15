package ch.hackzurich.gameofdrones

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class Aircraft(
        @SerializedName("icao") var icao: String,
        @SerializedName("position") var position: LatLng
){

    override fun equals(other: Any?): Boolean {
        return if(other is Aircraft)
            other.icao == this.icao
        else false
    }

    override fun hashCode(): Int {
        return icao.hashCode()
    }
}