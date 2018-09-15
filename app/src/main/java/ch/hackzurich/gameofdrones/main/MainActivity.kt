package ch.hackzurich.gameofdrones.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import ch.hackzurich.gameofdrones.*
import ch.hackzurich.gameofdrones.main.googlemap.GoogleMapFragment

/**
 * Created by janpawlov ( ͡° ͜ʖ ͡°) on 15/09/2018.
 */
class MainActivity : MainBA(), MainV {

    override var mPresenter: MainP = MainPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        display(GoogleMapFragment.newInstance())
    }

    override fun display(fragment: Fragment, into: Int?, push: Boolean, animIn: Int?, animOut: Int?, tag: String?) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.mainContainer, fragment)
                .addToBackStack(null)
                .commit()
    }

    override fun goBack() {
        //empty
    }

    override fun getContext(): Context? {
        return baseContext
    }

    override fun proceedToActivity(intent: Intent) {
        //empty
    }


}