package ch.hackzurich.gameofdrones.main.googlemap

import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Janusz Hain on 2017-07-25.
 */
class GoogleMapPadding : Parcelable {

    val rectPadding: Rect

    constructor(parcel: Parcel) {
        rectPadding = parcel.readParcelable(Rect::class.java.classLoader)
    }

    constructor(rectPadding: Rect) {
        this.rectPadding = rectPadding
    }

    constructor(rect: Rect, enum: DirectionsEnum, bonusPadding: Int = 0) {
        when (enum) {
            DirectionsEnum.MAP_LEFT_OF -> {
                this.rectPadding = Rect(0, 0, rect.width() + bonusPadding,0)
            }
            DirectionsEnum.MAP_TOP_OF -> {
                this.rectPadding = Rect(0, 0, 0, rect.height() + bonusPadding)
            }
            DirectionsEnum.MAP_RIGHT_OF -> {
                this.rectPadding = Rect(rect.width() + bonusPadding, 0, 0, 0)
            }
            DirectionsEnum.MAP_BOTTOM_OF -> {
                this.rectPadding = Rect(0, rect.height() + bonusPadding, 0, 0)
            }
        }
    }

    enum class DirectionsEnum {
        MAP_LEFT_OF, MAP_TOP_OF, MAP_RIGHT_OF, MAP_BOTTOM_OF
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(rectPadding, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GoogleMapPadding> {
        override fun createFromParcel(parcel: Parcel): GoogleMapPadding {
            return GoogleMapPadding(parcel)
        }

        override fun newArray(size: Int): Array<GoogleMapPadding?> {
            return arrayOfNulls(size)
        }
    }
}