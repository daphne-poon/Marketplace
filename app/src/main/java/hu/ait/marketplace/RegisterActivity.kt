package hu.ait.marketplace

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.sucho.placepicker.Constants
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.sucho.placepicker.AddressData
import hu.ait.marketplace.ui.data.User
import hu.ait.marketplace.ui.fragments.SellFragment
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_sell.*
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.*


class RegisterActivity : AppCompatActivity(), MyLocationProvider.OnNewLocationAvailable {

    var uploadBitmap : Bitmap? = null
    private lateinit var myLocationProvider: MyLocationProvider
    var city : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btnAttachProfPic.setOnClickListener {
            startActivityForResult(
                Intent(MediaStore.ACTION_IMAGE_CAPTURE), SellFragment.CAMERA_REQUEST_CODE
            )
        }

        requestNeededPermission()

        startLocation()
    }

    fun registerClick(v: View){

        if (!isFormValid()){
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
            etEmail.text.toString(), etPassword.text.toString()
        ).addOnSuccessListener {
            logUserIn()
        }.addOnFailureListener {
            Toast.makeText(
                this@RegisterActivity,
                "Error: ${it.message}",
                Toast.LENGTH_LONG
            ).show()
        }

    }

    private fun logUserIn() {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
            etEmail.text.toString(), etPassword.text.toString()
        ).addOnSuccessListener {
            if (uploadBitmap != null) {
                try {
                    addUserWithImage()
                } catch (e : java.lang.Exception) {
                    e.printStackTrace()
                }
            } else {
                registerUserInFirestore()
            }
        }
    }

    fun registerUserInFirestore(imageUrl: String = "") {

        var usersCollection = FirebaseFirestore.getInstance().collection(
            "users"
        )

        val user = User(
            FirebaseAuth.getInstance().currentUser!!.uid,
            FirebaseAuth.getInstance().currentUser!!.email!!,
            etUsername.text.toString(),
            city,
            imageUrl,
            Calendar.getInstance().time.toString()
        )

        usersCollection.add(user).addOnSuccessListener {
            Toast.makeText(this@RegisterActivity,
                "Registration Successful",
                Toast.LENGTH_LONG).show()
            startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
            finish()
        }.addOnFailureListener {
            Toast.makeText(
                this@RegisterActivity,
                "Error ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun isFormValid(): Boolean {
        return when {
            etEmail.text.isEmpty() -> {
                etEmail.error = "Email can not be empty"
                false
            }
            etPassword.text.isEmpty() -> {
                etPassword.error = "Password can not be empty"
                false
            }
            etUsername.text.isEmpty() -> {
                etUsername.error = "Username can not be empty"
                false
            }
            tvLocation.text.isEmpty() -> {
                tvLocation.error = "Location can not be empty"
                false
            }
            else -> true
        }
    }

    private fun requestNeededPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.CAMERA)) {
                Toast.makeText(this,
                    "I need it for camera", Toast.LENGTH_SHORT).show()
            }

            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.CAMERA),
                SellFragment.PERMISSION_REQUEST_CODE)
        }

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(
                    this,
                    "I need it for location", Toast.LENGTH_SHORT
                ).show()
            }

            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
        }
    }

    @Throws(Exception::class)
    private fun addUserWithImage() {

        val baos = ByteArrayOutputStream()
        uploadBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        // this is what u wanna upload to firebase
        val imageInBytes = baos.toByteArray()

        val storageRef = FirebaseStorage.getInstance().getReference()
        val newImage = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
        val newImagesRef = storageRef.child("images/$newImage")

        newImagesRef.putBytes(imageInBytes)
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }.addOnSuccessListener {
                newImagesRef.downloadUrl.addOnCompleteListener(object: OnCompleteListener<Uri> {
                    override fun onComplete(task: Task<Uri>) {
                        registerUserInFirestore(task.result.toString())
                    }
                })
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SellFragment.CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            uploadBitmap = data!!.extras!!.get("data") as Bitmap
            ivUser.visibility = View.VISIBLE
            ivUser.setImageBitmap(uploadBitmap)
        } else if (requestCode == Constants.PLACE_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                val addressData = data?.getParcelableExtra<AddressData>(Constants.ADDRESS_INTENT)
                Toast.makeText(this, addressData.toString(), Toast.LENGTH_LONG).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            101 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "ACCESS_FINE_LOCATION perm granted", Toast.LENGTH_SHORT)
                        .show()
                    startLocation()
                } else {
                    Toast.makeText(this, "ACCESS_FINE_LOCATION perm NOT granted", Toast.LENGTH_SHORT).show()
                }
            }
            SellFragment.PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "CAMERA perm granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "CAMERA perm NOT granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun startLocation() {
        myLocationProvider = MyLocationProvider(
            this, this
        )
        myLocationProvider.startLocationMonitoring()
    }

    override fun onNewLocation(location: Location) {
        if (location.accuracy < 25) {
            val gc = Geocoder(this, Locale.getDefault())
            var addrs: List<Address> =
                gc.getFromLocation(location!!.latitude, location!!.longitude, 3)
            city = addrs[0].locality
            tvLocation.text = "Current City: $city"
        }
        myLocationProvider.stopLocationMonitoring()
    }

}