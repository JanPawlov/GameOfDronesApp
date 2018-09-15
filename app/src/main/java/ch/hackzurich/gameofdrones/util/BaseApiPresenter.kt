package ch.hackzurich.gameofdrones.util

import ch.hackzurich.gameofdrones.api.DronesAPI
import pl.applover.kotlinmvp.BaseMvpView
import pl.applover.kotlinmvp.BasePresenter
import javax.inject.Inject

/**
 * Created by janpawlov ( ͡° ͜ʖ ͡°) on 15/09/2018.
 */
abstract class BaseApiPresenter<V : BaseMvpView> : BasePresenter<V>() {

    @Inject
    lateinit var mApi: DronesAPI
}