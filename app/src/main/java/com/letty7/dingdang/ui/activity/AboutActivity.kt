package com.letty7.dingdang.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.letty7.dingdang.R
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(toolbar)

    }

}