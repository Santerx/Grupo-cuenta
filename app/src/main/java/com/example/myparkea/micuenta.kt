package com.example.myparkea

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction


class micuenta : AppCompatActivity() {
    var transaction: FragmentTransaction? = null
    var fragmentAzul: Fragment? = null
    var fragmentHead: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_micuenta)

        fragmentAzul = FragmentAzul()
        fragmentHead = FragmentHead()
    }
}