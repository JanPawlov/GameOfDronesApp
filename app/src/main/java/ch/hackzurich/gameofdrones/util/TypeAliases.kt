package ch.hackzurich.gameofdrones.util

import ch.hackzurich.gameofdrones.main.MainContract
import ch.hackzurich.gameofdrones.main.googlemap.GoogleMapContract
import pl.applover.kotlinmvp.BaseActivity
import pl.applover.kotlinmvp.BaseFragment
import pl.applover.kotlinmvp.BasePresenter

/**
 * Created by janpawlov ( ͡° ͜ʖ ͡°) on 15/09/2018.
 */

typealias MainV= MainContract.View
typealias MainP = MainContract.Presenter
typealias MainBP = BasePresenter<MainV>
typealias MainBA = BaseActivity<MainV, MainP>

typealias GoogleMapV = GoogleMapContract.View
typealias GoogleMapP = GoogleMapContract.Presenter
typealias GoogleMapBF = BaseFragment<GoogleMapV, GoogleMapP>
typealias GoogleMapBP = BaseApiPresenter<GoogleMapV>
