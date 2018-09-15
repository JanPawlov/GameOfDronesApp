package ch.hackzurich.gameofdrones.api

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by janpawlov ( ͡° ͜ʖ ͡°) on 15/09/2018.
 */
class RetrofitInterceptor : okhttp3.Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        builder.header("Accept", "application/json")
        builder.header("Content-Type", "application/json")

        val newRequest = builder!!.build()
        return chain.proceed(newRequest)
    }
}