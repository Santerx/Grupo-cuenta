package com.example.myparkea

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.fragment.app.Fragment

class Editname : AppCompatActivity() {

    var fragmentHead2: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editname)

        fragmentHead2 = Fragmenthead2()
        click_botonVolver()
        click_botonClose()
    }
    fun click_botonVolver(){
        val Backbutton2 : ImageButton = findViewById(R.id.Backbutton2)
        Backbutton2.setOnClickListener {
            val Back = Intent(this, micuenta::class.java)
            startActivity(Back)
        }
    }

    fun click_botonClose(){
        val Closebutton2 : ImageButton = findViewById(R.id.Closebutton2)
        Closebutton2.setOnClickListener {
            val Close = Intent(this, MapsActivity::class.java)
            startActivity(Close)
        }
    }
}