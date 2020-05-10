package hu.ait.marketplace.ui.fragments

import android.os.Bundle
import android.util.Log
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        postsAdapter = PostsAdapter(activity!!.getApplicationContext())
        retainInstance = true
        root.recyclerPosts.adapter = postsAdapter
        var uid = FirebaseAuth.getInstance().currentUser!!.uid
        val query = FirebaseFirestore.getInstance()
            .collection(getString(R.string.users)).whereEqualTo(getString(R.string.uid), uid)

        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {
                    var myLocation = document.toObject(User::class.java).location
                    root.tvCity.text = "Browse Listings In ${myLocation}"
                    setMyLocation(myLocation)
                }
            }
        }

        return root
    }

    private fun setMyLocation(myCity: String) {
        Log.i("location in SetMyLoc: ", myCity)
        initPosts(myCity)
    }

    fun initPosts(myCity : String) {
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
                        query.get().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                var currentLocation = ""
                                for (document in task.result!!) {
                                    currentLocation = post.location
                                }
                                if (currentLocation == myCity) {
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
}
