package ch.hackzurich.gameofdrones.api

import ch.hackzurich.gameofdrones.model.AircraftData
import ch.hackzurich.gameofdrones.model.AircraftDataResponse
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Created by janpawlov ( ͡° ͜ʖ ͡°) on 15/09/2018.
 */
interface DronesAPI {

    @GET("/full_data")
    fun getAircraftData(): Observable<Response<AircraftDataResponse>>

    @GET("/mirror_recorded")
    fun getMirrorData(): Observable<Response<AircraftDataResponse>>

    @POST("/post_drone_position")
    fun postDronePosition(@Body aircraftData: AircraftData): Observable<Response<ResponseBody>>

}