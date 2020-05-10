package hu.ait.marketplace

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import hu.ait.marketplace.ui.data.User
import hu.ait.marketplace.ui.fragments.ProfileFragment
import hu.ait.marketplace.ui.fragments.SellFragment
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.etEmail
import kotlinx.android.synthetic.main.activity_register.etPassword
import kotlinx.android.synthetic.main.fragment_sell.*
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.*

class RegisterActivity : AppCompatActivity() {

    var uploadBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btnAttachProfPic.setOnClickListener {
            startActivityForResult(
                Intent(MediaStore.ACTION_IMAGE_CAPTURE), SellFragment.CAMERA_REQUEST_CODE
            )
        }

    }

    fun registerClick(v: View){

        if (!isFormValid()){
            return
        }

        registerUserInAuth()

        loginIn()

    }

    private fun loginIn() {
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

    private fun registerUserInAuth() {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
            etEmail.text.toString(), etPassword.text.toString()
        ).addOnFailureListener {
            Toast.makeText(
                this@RegisterActivity,
                "Error: ${it.message}",
                Toast.LENGTH_LONG
            ).show()
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
            tvLocation.text.toString(),
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
        } else {
            // we already have permission
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            SellFragment.PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "CAMERA perm granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "CAMERA perm NOT granted", Toast.LENGTH_SHORT).show()
                }
            }
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
            imgAttach.setImageBitmap(uploadBitmap)
            imgAttach.visibility = View.VISIBLE
        }
    }

}