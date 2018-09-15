package ch.hackzurich.gameofdrones.api

import ch.hackzurich.gameofdrones.main.googlemap.GoogleMapPresenter
import dagger.Component
import javax.inject.Singleton

/**
 * Created by janpawlov ( ͡° ͜ʖ ͡°) on 15/09/2018.
 */
@Singleton
@Component(modules = [(DronesModule::class)])
interface AppComponent {
    fun inject(presenter: GoogleMapPresenter)
}