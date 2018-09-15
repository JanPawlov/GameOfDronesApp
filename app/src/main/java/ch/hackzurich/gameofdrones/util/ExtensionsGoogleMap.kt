package ch.hackzurich.gameofdrones.util

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DrawableUtils
import ch.hackzurich.gameofdrones.AircraftData
import ch.hackzurich.gameofdrones.AircraftMarkerPosition
import ch.hackzurich.gameofdrones.MainApp
import ch.hackzurich.gameofdrones.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import pl.applover.kotlinmvp.rotate
import java.lang.Exception

fun GoogleMap.setCameraPosition(latLng: LatLng, animateCamera: Boolean = false) {
    val cameraPosition = CameraPosition.Builder().target(latLng).zoom(12f).build()
    if (animateCamera) {
        animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    } else {
        moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }
}

/**
 * Method needs at least one latLng
 */
fun GoogleMap.setCameraPosition(latLngs: ArrayList<LatLng>, animateCamera: Boolean = false, padding: Int = 10) {
    if (latLngs.isEmpty()) {
        return
    }

    val boundsBuilder = LatLngBounds.Builder()

    latLngs.forEach { latLng ->
        boundsBuilder.include(latLng)
    }

    val bounds = boundsBuilder.build()

    try {
        if (animateCamera) {
            animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
        } else {
            moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
        }
    } catch (e: Exception) {

    }
}

fun GoogleMap.setMarker(latLng: LatLng, iconBitmap: Bitmap, data: AircraftData): AircraftMarkerPosition {
    return AircraftMarkerPosition(data,
            addMarker(MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(iconBitmap))), iconBitmap)
}

fun getPlaneBmp(context: Context, aircraftData: AircraftData): Bitmap {
    val drawable = ContextCompat.getDrawable(context, R.drawable.ic_plane)
    val canvas = Canvas()
    var bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)

    val scale = aircraftData.alt!! / 10000.0.toFloat()
    val width = bitmap.width
    val height = bitmap.height
    if (scale > 0)
        bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(width * scale), Math.round(height * scale), false)

    canvas.setBitmap(bitmap)
    drawable.setBounds(0, 0, bitmap.width, bitmap.height)
    drawable.draw(canvas)
    val matrix = Matrix()
    matrix.setRotate(aircraftData.heading?.toFloat()!!)
    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
    return bitmap
}

fun GoogleMap.changeMarker(aircraft: AircraftMarkerPosition, end: LatLng, context: Context) {
    aircraft.marker.position = end
    aircraft.marker.setIcon(BitmapDescriptorFactory.fromBitmap(getPlaneBmp(context, aircraft.data)))
}

fun GoogleMap.setRangeCircle(circleOptions: CircleOptions): Circle {
    return addCircle(circleOptions)
}

fun GoogleMap.drawPolyline(latLngs: ArrayList<LatLng>, colorRid: Int, width: Float = 5f, context: Context = MainApp.instance): Polyline {
    return addPolyline(PolylineOptions()
            .addAll(latLngs)
            .width(width)
            .color(ContextCompat.getColor(context, colorRid)))
}

fun getLastLocation(): LatLng? {
    val location = MainApp.instance.getLastLocation()
    location?.let {
        return LatLng(location.latitude, location.longitude)
    } ?: run {
        return null
    }
}