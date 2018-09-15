package ch.hackzurich.gameofdrones.api

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Created by janpawlov ( ͡° ͜ʖ ͡°) on 15/09/2018.
 */
@Module
class DronesModule {
    @Provides
    @Singleton
    fun getDronesAPI(): DronesAPI {

        val baseUrl = "http://godbackend.scapp.io"//"https://hackzurich.involi.live"

        val logging = HttpLoggingInterceptor().also {
            it.level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
                .addNetworkInterceptor(RetrofitInterceptor())
                .addNetworkInterceptor(logging)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

        val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .client(okHttpClient)
                .build()

        return retrofit.create(DronesAPI::class.java)
    }
}