package com.example.myparkea

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class micuenta : AppCompatActivity(){

    private val db = FirebaseFirestore.getInstance()
    var fragmentAzul: Fragment? = null
    var fragmentHead: Fragment? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_micuenta)


        fragmentAzul = FragmentAzul()
        fragmentHead = FragmentHead()
        val headerName : TextView = findViewById(R.id.tv_Username)
        val headerEmail : TextView = findViewById(R.id.tv_Useremail)


        val user = Firebase.auth.currentUser
        user.let {
            val email = user!!.email.toString()
            db.collection("users").document(email).get().addOnSuccessListener {
                headerName.text = it.get("Nombre") as String
                headerEmail.text = email
            }
        }
        click_botonEdit()
        click_botonVolver()
        click_botonClose()
        }

    fun click_botonEdit(){
        val ModUser : Button = findViewById(R.id.ModUser)
        ModUser.setOnClickListener {
            val Edit = Intent(this, Editname::class.java)
            startActivity(Edit)
        }
    }
    fun click_botonVolver(){
        val Backbutton : ImageButton = findViewById(R.id.Backbutton)
        Backbutton.setOnClickListener {
            val Back = Intent(this, MapsActivity::class.java)
            startActivity(Back)
        }
    }

    fun click_botonClose(){
        val Closebutton : ImageButton = findViewById(R.id.Closebutton)
        Closebutton.setOnClickListener {
            val Close = Intent(this, MapsActivity::class.java)
            startActivity(Close)
        }
    }

    }