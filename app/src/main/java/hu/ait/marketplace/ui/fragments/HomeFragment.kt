package hu.ait.marketplace.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import hu.ait.marketplace.R
import hu.ait.marketplace.ui.adapter.PostsAdapter
import hu.ait.marketplace.ui.data.Post
import hu.ait.marketplace.ui.data.User
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment() {

    private lateinit var postsAdapter: PostsAdapter
    private var myLocation : String = "Your City"
    private var currentLocation = "Hong Kong"

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        postsAdapter = PostsAdapter(activity!!.getApplicationContext(),
        FirebaseAuth.getInstance().currentUser!!.uid)

        root.recyclerPosts.adapter = postsAdapter
        setMyLocation()
        root.tvCity.text = "Browse Listings In ${myLocation}"
        initPosts()

        return root
    }

    private fun setMyLocation() {
        var uid = FirebaseAuth.getInstance().currentUser!!.uid
        val query = FirebaseFirestore.getInstance()
            .collection("users").whereEqualTo("uid", uid)

        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {
                     myLocation = document.toObject(User::class.java).location
                }
            }
        }
    }

    fun initPosts() {
        val db = FirebaseFirestore.getInstance()
        val query = db.collection("posts")

        query.addSnapshotListener(
            object : EventListener<QuerySnapshot> {

                override fun onEvent(querySnapshot: QuerySnapshot?, e: FirebaseFirestoreException?) {
                    if (e!=null) {
                        Toast.makeText(activity!!.getApplicationContext(), "Error: ${e.message}",
                            Toast.LENGTH_LONG).show()
                        return
                    }

                    for (docChange in querySnapshot?.getDocumentChanges()!!) {
                        val post = docChange.document.toObject(Post::class.java)
                        var user = User()
                        val db = FirebaseFirestore.getInstance()
                        val query = db.collection("users").whereEqualTo("uid", post.authorid)
                        query.get().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                for (document in task.result!!) {
                                    user = document.toObject(User::class.java)
                                    updateCurrentLocation(user)
                                }
                                if (currentLocation == myLocation) {
                                    if (docChange.type == DocumentChange.Type.ADDED) {
                                        postsAdapter.addPost(post, docChange.document.id)
                                    } else if (docChange.type == DocumentChange.Type.REMOVED) {
                                        postsAdapter.removePostByKey(docChange.document.id)
                                    }
                                }
                            }
                        }

                    }

                }
            }
        )
    }

    private fun updateCurrentLocation(user: User) {
        currentLocation = user.location
    }
}
