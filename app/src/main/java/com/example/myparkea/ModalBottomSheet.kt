package com.example.myparkea

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myparkea.MapsActivity.Companion.MY_CHANNEL
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ModalBottomSheet constructor(Context: Context) : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "ModalBottomSheet"
    }

    private var mContext : Context = Context
    private lateinit var location: Uri
    private lateinit var mapIntent: Intent
    private lateinit var packageManager: PackageManager

    private lateinit var notificationManager: NotificationManager
    lateinit var builder: NotificationCompat.Builder
    private lateinit var time : String
    var hms = "01:00"

    private lateinit var dialogBinding : View
    private lateinit var btnNotificacion : Button
    lateinit var name : String
    private lateinit var address : String

    private var db = FirebaseFirestore.getInstance()

    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        @NonNull inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        val view = inflater.inflate(R.layout.modal_bottom_sheet_content, container, false)

        btnNotificacion = view.findViewById(R.id.btn_card_notificacion)
        val btnClose = view.findViewById<ImageButton>(R.id.btn_card_close)
        val btnDireccion = view.findViewById<ImageButton>(R.id.btn_card_direccion)

        val cardName = view.findViewById<TextView>(R.id.cardName)
        cardName.text = name

        val cardAddress = view.findViewById<TextView>(R.id.cardAddress)
        cardAddress.text = address

        val cardCity = view.findViewById<TextView>(R.id.cardCity)
        val cardStatus = view.findViewById<TextView>(R.id.cardStatus)
        val cardHour1 = view.findViewById<TextView>(R.id.cardHoursMF)
        val cardHour2 = view.findViewById<TextView>(R.id.cardHoursSS)
        val cardPrice = view.findViewById<TextView>(R.id.cardPrice)
        val cardCupo = view.findViewById<TextView>(R.id.cardCupo)

        db.collection("info_parqueadero").document(address).get().addOnSuccessListener {
            cardCity.text = it.get("ciudad").toString().trim()
            cardStatus.text = it.get("estado").toString().trim()
            cardHour1.text = it.get("horario1").toString().trim()
            cardHour2.text = it.get("horario2").toString().trim()
            cardPrice.text = it.get("precio").toString().trim()
            cardCupo.text = it.get("cupo").toString().trim()
            cardPrice.text = getString(R.string.precio,it.get("precio").toString().trim())
        }

        btnNotificacion.setOnClickListener {
            dismiss()
            alert1()
        }

        btnClose.setOnClickListener {
            dismiss()
        }

        btnDireccion.setOnClickListener {
            mapIntent.resolveActivity(packageManager)?.let {
                dismiss()
                startActivity(mapIntent)
            }
        }

        return view
    }

    fun direccion(marker: Marker, packageManager : PackageManager) {
        val latitud = marker.position.latitude
        val longitud = marker.position.longitude
        name = marker.title.toString()
        address = marker.snippet.toString().trim()

        this.packageManager = packageManager

        // Build the intent.
        location = Uri.parse("geo:$latitud,$longitud?q=$latitud,$longitud($name)?z=18")
        mapIntent = Intent(Intent.ACTION_VIEW, location)
    }

    @SuppressLint("InflateParams")
    private fun alert1() {
        dialogBinding = layoutInflater.inflate(R.layout.custom_alert_1,null)
        val myDialog = Dialog(requireContext())

        myDialog.setContentView(dialogBinding)
        myDialog.setCancelable(false)

        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.show()

        val yesBtn : Button = dialogBinding.findViewById(R.id.bnt_alert1_yes)
        val noBtn : Button = dialogBinding.findViewById(R.id.bnt_alert1_no)

        yesBtn.setOnClickListener {
            myDialog.dismiss()
            initializeTimerTask("1")
            createTimerNotification()
            createReminder()
            alert2()
        }

        noBtn.setOnClickListener {
            myDialog.dismiss()
        }
    }

    @SuppressLint("InflateParams")
    private fun alert2() {
        dialogBinding = LayoutInflater.from(mContext).inflate(R.layout.custom_alert_2,null)
        val myDialog = Dialog(mContext)

        myDialog.setContentView(dialogBinding)
        myDialog.setCancelable(false)

        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.show()

        val yes : Button = dialogBinding.findViewById(R.id.bnt_alert2_yes)

        yes.setOnClickListener {
            myDialog.dismiss()
        }
    }

    private fun initializeTimerTask(time: String?) {

        object : CountDownTimer(time!!.toLong() * 1000 * 60, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val f: NumberFormat = DecimalFormat("00")
                val min = millisUntilFinished / 60000 % 60
                val sec = millisUntilFinished / 1000 % 60
                hms = f.format(min).toString() + ":" + f.format(sec)
                raiseNotification(builder, hms)
            }

            override fun onFinish() {
                hms = "00:00"
            }
        }.start()
    }

    @SuppressLint("SimpleDateFormat")
    private fun createTimerNotification() {

        notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(mContext,MapsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val flag = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pendingIntent : PendingIntent = PendingIntent.getActivity(mContext, 0, intent, flag)

        val myTime = (Calendar.getInstance().timeInMillis) + 60000//1800000
        val format = SimpleDateFormat("HH:mm")
        time = format.format(myTime)

        builder = NotificationCompat.Builder(mContext, MY_CHANNEL)
            .setContentTitle("Reserva Parqueadero")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Tiempo restante para finalizar la reserva: $hms \nEl tiempo de su reserva termina a las $time."))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            //.setColor(resources.getColor(android.R.color.holo_blue_dark))
            .setOnlyAlertOnce(true)
            .setTimeoutAfter(60000)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        with(NotificationManagerCompat.from(mContext)) {
            notify(1, builder.build())
        }
    }

    private fun raiseNotification(b: NotificationCompat.Builder, hms: String) {
        b.setStyle(NotificationCompat.BigTextStyle().bigText("Tiempo restante para finalizar la reserva: $hms \nEl tiempo de su reserva termina a las $time."))
        b.setOngoing(true)

        notificationManager.notify(1, b.build())
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun createReminder() {
        Log.i(String(), "Se llego a la funcion del Recordatorio")

        val i =  Intent(mContext,Reminder::class.java)
        val pendingIntent = PendingIntent.getBroadcast(mContext,0,i,0)
        val a = mContext.getSystemService(ALARM_SERVICE) as AlarmManager
        val myTime = (Calendar.getInstance().timeInMillis) + 60000
        a.set(AlarmManager.RTC_WAKEUP,myTime,pendingIntent)

        Log.i(String(), "Recordatorio creado")
    }
}