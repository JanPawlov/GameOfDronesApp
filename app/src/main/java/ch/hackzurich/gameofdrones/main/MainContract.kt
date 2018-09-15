package ch.hackzurich.gameofdrones.main

import pl.applover.kotlinmvp.BaseMvpPresenter
import pl.applover.kotlinmvp.BaseMvpView

/**
 * Created by janpawlov ( ͡° ͜ʖ ͡°) on 15/09/2018.
 */
interface MainContract {
    interface View : BaseMvpView
    interface Presenter : BaseMvpPresenter<View>
}