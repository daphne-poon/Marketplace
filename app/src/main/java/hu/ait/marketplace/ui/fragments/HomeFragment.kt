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
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment() {

    private lateinit var postsAdapter: PostsAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        postsAdapter = PostsAdapter(activity!!.getApplicationContext(),
        FirebaseAuth.getInstance().currentUser!!.uid)

        root.recyclerPosts.adapter = postsAdapter

        initPosts()

        return root
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
                        if (docChange.type == DocumentChange.Type.ADDED) {
                            val post = docChange.document.toObject(Post::class.java)
                            postsAdapter.addPost(post, docChange.document.id)
                        } else if (docChange.type == DocumentChange.Type.REMOVED) {
                            postsAdapter.removePostByKey(docChange.document.id)
                        } else if (docChange.type == DocumentChange.Type.MODIFIED) {

                        }
                    }

                }
            }
        )
    }
}
