package hu.ait.marketplace.ui.fragments

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import hu.ait.marketplace.R
import hu.ait.marketplace.ui.data.User
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.*


class ProfileFragment : Fragment() {

    lateinit var db : FirebaseFirestore
    var uploadBitmap : Bitmap? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_profile, container, false)

        return root
    }

    fun registerUserInFirestore(imageUrl: String = "") {

        var usersCollection = FirebaseFirestore.getInstance().collection(
            "users"
        )

        val user = User(
            "224235","daphne",
            "Hong Kong", "ee", "1111111"
        )

        usersCollection.add(user).addOnSuccessListener {
            Toast.makeText(
                activity,
                "Info Updated", Toast.LENGTH_LONG).show()
        }.addOnFailureListener {
            Toast.makeText(
                activity,
                "Error ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

//    fun getPhotoUrl() {
//
//        // Get current username
//        var user = FirebaseAuth.getInstance().currentUser
//
//
//            if (user != null) {
//                db.collection("users")
//                    .document(user.uid).set({
//
//                        email: user.email,
//
//                        someotherproperty: ‘some user preference’
//
//                    })
//
//            }
//        }

//        var storage = firebase.storage();
//// Create a Storage Ref w/ username
//        var storageRef = FirebaseStorage.ref(user + '/profilePicture/' + file.name);
//
//// Upload file
//        var task = storageRef.put(file);

    @Throws(Exception::class)
    private fun uploadPostWithImage() {

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
                        registerUserInFirestore(task.result.toString())
                    }
                })
            }
    }
    }
