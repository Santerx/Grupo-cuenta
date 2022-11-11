package com.example.myparkea

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.myparkea.databinding.ActivityEditnameBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class Editname: AppCompatActivity() {

    private var fragmentHead2: Fragment? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editname)

        fragmentHead2 = Fragmenthead2()
        click_botonVolver()
        click_botonClose()

        val UpdateBoton : Button = findViewById(R.id.UpdateBoton)




        UpdateBoton.setOnClickListener {
            Update()
            Toast.makeText(this, "Nombre actualizado", Toast.LENGTH_SHORT).show()
        }

    }

    private fun click_botonVolver(){
        val Backbutton2 : ImageButton = findViewById(R.id.Backbutton2)
        Backbutton2.setOnClickListener {
            val Back = Intent(this, micuenta::class.java)
            startActivity(Back)
        }
    }

    private fun click_botonClose(){
        val Closebutton2 : ImageButton = findViewById(R.id.Closebutton2)
        Closebutton2.setOnClickListener {
            val Close = Intent(this, MapsActivity::class.java)
            startActivity(Close)
        }
    }

    private fun Update(){
        val textUpdates : EditText = findViewById(R.id.textUpdates)
        val user = Firebase.auth.currentUser
            val email = user!!.email.toString()
        user.let {
            //Guardar
            db.collection("users").document(email.toString()).set(
                hashMapOf(
                    "Nombre" to textUpdates.toString(),
                    "Email" to email.toString(),
                    "proveedor" to "password",
                    "Dueño de parqueadero" to false
                )
                )
        }
    }

}