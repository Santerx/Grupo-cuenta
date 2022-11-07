package com.example.myparkea

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.myparkea.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


open class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, NavigationView.OnNavigationItemSelectedListener {
    companion object {
        const val MY_CHANNEL = "myChannel"
    }

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocation: FusedLocationProviderClient
    private var ultimoMarcador : Marker? = null
    private val db = FirebaseFirestore.getInstance()
    private lateinit var ubicacion : LatLng

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*toolbar = findViewById(R.id.toolbar)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        toolbar.overflowIcon = getDrawable(R.drawable.ic_menu)
        setSupportActionBar(toolbar)
        Objects.requireNonNull(supportActionBar)?.title ="Parkea!"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)*/

        drawerLayout = findViewById(R.id.drawerLayout)
        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, /*toolbar,*/ R.string.open, R.string.close)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.white)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()

        navigationView = findViewById(R.id.navView)
        navigationView.setNavigationItemSelectedListener(this)

        val menu : ImageView = findViewById(R.id.iv_menu)
        menu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val headerView : View = navigationView.getHeaderView(0)
        val headerName : TextView = headerView.findViewById(R.id.tv_Username)
        val headerEmail : TextView = headerView.findViewById(R.id.tv_Useremail)
        val headerPhoto : CircleImageView = headerView.findViewById(R.id.tv_Userphoto)

        var provideer = ""

        val user = Firebase.auth.currentUser
        user.let {
            val email = user!!.email.toString()
            db.collection("users").document(email).get().addOnSuccessListener {
                headerName.text = it.get("Nombre") as String
                headerEmail.text = email
                provideer = it.get("Proveedor").toString()

                if(provideer == "google.com") {
                    Picasso.with(this).load(FirebaseAuth.getInstance().currentUser!!.photoUrl.toString()).fit().into(headerPhoto)
                }
            }
        }

        createChannel()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.isAdded
        mapFragment.getMapAsync(this)

        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_account -> openAccount()
            R.id.nav_configuracion -> openConfiguration()
            R.id.nav_logout -> logout()
        }
        return false
    }

    override fun onMapReady(googleMap: GoogleMap) {

        try {
            val success : Boolean = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_standard))
            if (!success) {
                Log.e("MapsActivity", "Style parsing failed.")
            }
        } catch (e : Resources.NotFoundException) {
            Log.e("MapsActivity", "Can't fine style. Error: $e")
        }

        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        mMap.setMinZoomPreference(7f)
        mMap.setMaxZoomPreference(22f)

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isRotateGesturesEnabled = true
        mMap.uiSettings.isScrollGesturesEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = false

        fusedLocation.lastLocation.addOnSuccessListener {
            if(it != null){
                ubicacion = LatLng(it.latitude, it.longitude)
                val cameraPosition : CameraPosition = CameraPosition.Builder().target(ubicacion).zoom(17.5f).build()
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
        }

        db.collection("info_parqueadero").get().addOnSuccessListener {
            for(documento in it){
                val coordenadas = LatLng(documento.get("latitud") as Double,documento.get("longitud") as Double)
                val marker : MarkerOptions = MarkerOptions().position(coordenadas).title(documento.get("nombre") as String)
                marker.snippet(documento.get("direccion") as String)
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
                mMap.addMarker(marker)
                mMap.setOnMarkerClickListener(this)
            }
        }

        /*mMap.setOnMapLongClickListener {

            val markerOptions = MarkerOptions().position(it).flat(false).snippet("mensaje\ndireccion\nabierto hasta las 12")
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

            val nombreUbicacion = obtenerDireccion(it)
            markerOptions.title(nombreUbicacion)

            if(ultimoMarcador != null){
                ultimoMarcador!!.remove()
            }

            ultimoMarcador = mMap.addMarker(markerOptions)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 18f))


            val url : String = getDirectionsUrl(ubicacion,ultimoMarcador!!.position)

            val downloadTask : DownloadTask = DownloadTask()
            downloadTask.execute(url)

        }*/
    }

    private fun obtenerDireccion(latLng: LatLng) : String {
        val geocoder = Geocoder(this)
        val direcciones : List<Address>?
        val prDireccion : Address
        var txtDireccion = ""

        try {
            direcciones = geocoder.getFromLocation(
                latLng.latitude, latLng.longitude, 1
            )

            if((direcciones != null) && direcciones.isNotEmpty()) {
                prDireccion = direcciones[0]

                // Si la dirección tiene varias líneas
                if(prDireccion.maxAddressLineIndex > 0) {
                    for(i in 0 .. prDireccion.maxAddressLineIndex) {
                        txtDireccion += prDireccion.getAddressLine(i) + "\n"
                    }
                }
                // Si hay principal y secundario
                else {
                    txtDireccion += prDireccion.thoroughfare + ", " + prDireccion.subThoroughfare + "\n"
                }
            }
        } catch (e : Exception) {
            txtDireccion = "Dirección no encontrada"
        }

        return txtDireccion
    }

    override fun onMarkerClick(marker: Marker): Boolean {

        val mk = LatLng(marker.position.latitude, marker.position.longitude)
        val cameraPosition : CameraPosition = CameraPosition.Builder().target(mk).zoom(17f).build()
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        val modalBottomSheet = ModalBottomSheet(this)
        modalBottomSheet.show(supportFragmentManager, ModalBottomSheet.TAG)
        modalBottomSheet.direccion(marker,packageManager)

        return false
    }

    private fun createChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MY_CHANNEL,
                "MySuperChannel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificacion de Reserva"
            }

            val notificationManager : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun openAccount() {
        val AccountIntent = Intent(this, micuenta::class.java)
        startActivity(AccountIntent)
    }

    private fun openConfiguration() {

    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()

        val preferences = getSharedPreferences("checkbox", MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("remember", "false")
        editor.apply()

        startActivity(Intent(this, MapsActivity::class.java))
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
            finishAffinity()
        }
    }

/*
    @SuppressLint("StaticFieldLeak")
    inner class DownloadTask : AsyncTask<String?, Void?, String>() {
        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            val parseTask = ParserTask()
            parseTask.execute(result)
        }

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg url: String?): String {
            var data = ""
            try {
                data = downloadUrl(url[0].toString())
            }catch (e: java.lang.Exception) {
                Log.d("Background Task ", e.toString())
            }
            return data
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class ParserTask : AsyncTask<String?, Int?, List<List<HashMap<String,String>>>?>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg jsonData: String?): List<List<HashMap<String, String>>>? {
            val jObject: JSONObject
            var routes: List<List<HashMap<String,String>>>? = null

            try {
                jObject = jsonData[0]?.let { JSONObject(it) }!!
                val parser = DataParser()
                routes = parser.parse(jObject)
            }catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return routes
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: List<List<HashMap<String,String>>>?) {
            val points = ArrayList<LatLng?>()
            val lineOptions = PolylineOptions()

            for(i in result!!.indices) {
                val path = result[i]

                for(j in path.indices) {
                    val point = path[j]
                    val lat = point["lat"]!!.toDouble()
                    val lng = point["lng"]!!.toDouble()
                    val position = LatLng(lat,lng)
                    points.add(position)
                }

                lineOptions.addAll(points)
                lineOptions.width(8f)
                lineOptions.color(Color.BLUE)
                lineOptions.geodesic(true)
            }

            if(points.size != 0)
                mMap.addPolyline(lineOptions)
        }

    }

    @Throws(IOException::class)
    private fun downloadUrl(strUrl: String): String {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null

        try {
            val url = URL(strUrl)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connect()
            iStream = urlConnection.inputStream
            val br = BufferedReader(InputStreamReader(iStream))
            val sb = StringBuffer()
            var line: String?

            while (br.readLine().also { line = it } != null) {
                sb.append(line)
            }

            data = sb.toString()
            br.close()

        }catch (e: java.lang.Exception) {
            Log.d("Exception ", e.toString())
        } finally {
            iStream!!.close()
            urlConnection!!.disconnect()
        }

        return data
    }

    private fun getDirectionsUrl(origin: LatLng, dest: LatLng): String {
        val strOrigin = "origin=" + origin.latitude + "," + origin.longitude
        val strDest = "destination=" + dest.latitude + "," + dest.longitude
        val mode = "mode=driving"
        val parameters = "$strOrigin&$strDest$mode"
        val output = "json"

        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters&key=AIzaSyBV7eau74VhalFjWundbrucMiYSaqIQWR0"
    }*/
}

