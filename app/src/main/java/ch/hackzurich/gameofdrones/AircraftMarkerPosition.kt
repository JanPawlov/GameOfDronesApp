package ch.hackzurich.gameofdrones

import android.graphics.Bitmap
import com.google.android.gms.maps.model.Marker

/**
 * Created by janpawlov ( ͡° ͜ʖ ͡°) on 15/09/2018.
 */
class AircraftMarkerPosition(val data: AircraftData,
                             var marker: Marker,
                             var iconBitmap: Bitmap) {
}