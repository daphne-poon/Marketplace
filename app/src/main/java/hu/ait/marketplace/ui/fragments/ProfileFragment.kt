package hu.ait.marketplace.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import hu.ait.marketplace.R
import hu.ait.marketplace.ui.data.User
import kotlinx.android.synthetic.main.fragment_profile.view.*


class ProfileFragment : Fragment() {

    lateinit var user: User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_profile, container, false)

        getUserInfo(root)
        return root
    }

    private fun addInfoToUI(root: View) {

        if (user.profPicUrl.isNotEmpty()) {
            Glide.with(activity!!.applicationContext).
            load(user.profPicUrl).
            into(root.ivProfPic)
        }

        root.tvUser.text = user.username
        root.tvLocation.text = user.location
    }

    fun getUserInfo(root: View) {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser!!.email!!
        val query = db.collection("users").whereEqualTo("author", uid)

        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user = task.result!!.documents[0].toObject(User::class.java)!!
                addInfoToUI(root)
            } else {
                Toast.makeText(activity!!.applicationContext, "Error: ${task.exception}", Toast.LENGTH_LONG).show()
            }
        }

    }

//    fun initPosts() {
//        val db = FirebaseFirestore.getInstance()
//        val query = db.collection("posts")
//
//        query.addSnapshotListener(
//            object : EventListener<QuerySnapshot> {
//
//                override fun onEvent(querySnapshot: QuerySnapshot?, e: FirebaseFirestoreException?) {
//                    if (e!=null) {
//                        Toast.makeText(activity!!.getApplicationContext(), "Error: ${e.message}",
//                            Toast.LENGTH_LONG).show()
//                        return
//                    }
//
//                    for (docChange in querySnapshot?.getDocumentChanges()!!) {
//                        if (docChange.type == DocumentChange.Type.ADDED) {
//                            val post = docChange.document.toObject(Post::class.java)
//                            EditablePostsAdapter.addPost(post, docChange.document.id)
//                        } else if (docChange.type == DocumentChange.Type.REMOVED) {
//                            EditablePostsAdapter.removePostByKey(docChange.document.id)
//                        } else if (docChange.type == DocumentChange.Type.MODIFIED) {
//
//                        }
//                    }
//
//                }
//            }
//        )
//    }

}
