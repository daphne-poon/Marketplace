package hu.ait.marketplace.ui.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import hu.ait.marketplace.R
import hu.ait.marketplace.ui.data.Post
import kotlinx.android.synthetic.main.fragment_sell.*
import kotlinx.android.synthetic.main.fragment_sell.view.*
import kotlinx.android.synthetic.main.post_row.*
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.*

class SellFragment : Fragment() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        private const val CAMERA_REQUEST_CODE = 1002
    }

    var uploadBitmap : Bitmap? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_sell, container, false)

        root.btnAttach.setOnClickListener {
            startActivityForResult(
                Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                CAMERA_REQUEST_CODE
            )
        }
        root.btnSend.setOnClickListener {
            sendClick()
        }

        requestNeededPermission()
        return root
    }

    fun sendClick(){

        if (uploadBitmap != null) {
            try {
                uploadPostWithImage()
            } catch (e : java.lang.Exception) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(activity, "Image required", Toast.LENGTH_LONG).show()
        }

    }

    fun checkPost() {
        if (etTitle.toString().isEmpty()) {
            etTitle.error = "Title must not be empty"
        }
        if (etBody.toString().isEmpty()) {
            etTitle.error = "Description must not be empty"
        }
        if (etPrice.toString().isEmpty()) {
            etTitle.error = "Price must not be empty"
        }
    }

    fun uploadPost(imageUrl: String = "") {
        val post = Post(
            FirebaseAuth.getInstance().currentUser!!.uid,
            FirebaseAuth.getInstance().currentUser!!.email!!,
            etTitle.text.toString(),
            etPrice.text.toString(),
            "Hong Kong",
            etBody.text.toString(),
            imageUrl
        )

        var postsCollection = FirebaseFirestore.getInstance().collection(
            "posts"
        )

        postsCollection.add(post).addOnSuccessListener {
            Toast.makeText(
                activity,
                "Post Saved", Toast.LENGTH_LONG).show()
            clearPost()
        }.addOnFailureListener {
            Toast.makeText(
                activity,
                "Error ${it.message}", Toast.LENGTH_LONG).show()
        }

    }

    fun clearPost(){
        etTitle.setText("")
        etPrice.setText("")
        etBody.setText("")
        imgAttach.visibility = View.GONE
    }

    private fun requestNeededPermission() {
        if (ContextCompat.checkSelfPermission(activity!!.applicationContext,
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                    android.Manifest.permission.CAMERA)) {
                Toast.makeText(activity!!.applicationContext,
                    "I need it for camera", Toast.LENGTH_SHORT).show()
            }

            ActivityCompat.requestPermissions(activity!!,
                arrayOf(android.Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CODE)
        } else {
            // we already have permission
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(activity!!.applicationContext, "CAMERA perm granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(activity!!.applicationContext, "CAMERA perm NOT granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            uploadBitmap = data!!.extras!!.get("data") as Bitmap
            imgAttach.setImageBitmap(uploadBitmap)
            imgAttach.visibility = View.VISIBLE
        }
    }

//    fun backHome(){
//        activity!!.
//        supportFragmentManager.
//        beginTransaction().
//        replace(R.id.container, NewFragment.newInstance()).commitNow()
//    }
    @Throws(Exception::class)
    private fun uploadPostWithImage() {

        checkPost()
        val baos = ByteArrayOutputStream()
        uploadBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        val imageInBytes = baos.toByteArray()

        val storageRef = FirebaseStorage.getInstance().getReference()
        val newImage = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
        val newImagesRef = storageRef.child("images/$newImage")

        newImagesRef.putBytes(imageInBytes)
            .addOnFailureListener {
                Toast.makeText(activity!!.applicationContext, it.message, Toast.LENGTH_SHORT).show()
            }.addOnSuccessListener {
                newImagesRef.downloadUrl.addOnCompleteListener(object: OnCompleteListener<Uri> {
                    override fun onComplete(task: Task<Uri>) {
                        uploadPost(task.result.toString())
                    }
                })
            }
    }
}
