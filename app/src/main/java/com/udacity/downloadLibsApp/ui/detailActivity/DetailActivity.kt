package com.udacity.downloadLibsApp.ui.detailActivity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.udacity.downloadLibsApp.R
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
    }

}
