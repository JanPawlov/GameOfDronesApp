package ch.hackzurich.gameofdrones.api

import ch.hackzurich.gameofdrones.AircraftDataResponse
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

/**
 * Created by janpawlov ( ͡° ͜ʖ ͡°) on 15/09/2018.
 */
interface DronesAPI {

    @GET("/http")
    fun getAircraftData(): Observable<Response<AircraftDataResponse>>

    @GET("/mirror_recorded")
    fun getMirrorData(): Observable<Response<AircraftDataResponse>>

}