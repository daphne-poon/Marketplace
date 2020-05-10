package hu.ait.marketplace.ui.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import hu.ait.marketplace.R
import hu.ait.marketplace.ui.data.Post
import hu.ait.marketplace.ui.data.User
import kotlinx.android.synthetic.main.post_row.view.*

class PostsAdapter : RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    lateinit var context: Context
    var postsList = mutableListOf<Post>()
    var postKeys = mutableListOf<String>()

    constructor(context: Context) : super() {
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.post_row, parent, false
        )

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return postsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var post = postsList[holder.adapterPosition]
        var user = User()
        val query = FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("uid", post.authorid)

        user = onBindViewHolderUsername(query, user, holder)

        holder.tvDescription.text = post.body
        holder.tvTitle.text = post.title
        holder.tvPrice.text = "$${post.price}"

        onBindViewHolderImage(post, holder)

        onBindViewHolderEmail(holder, query, user, post)
    }

    private fun onBindViewHolderUsername(
        query: Query,
        user: User,
        holder: ViewHolder
    ): User {
        var user1 = user
        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {
                    user1 = document.toObject(User::class.java)
                }
                holder.tvUser.text = user1.username
            }
        }
        return user1
    }

    private fun onBindViewHolderImage(
        post: Post,
        holder: ViewHolder
    ) {
        if (post.imgUrl.isNotEmpty()) {
            holder.ivPhoto.visibility = View.VISIBLE
            Glide.with(context).load(post.imgUrl).into(holder.ivPhoto)
        } else {
            holder.ivPhoto.visibility = View.GONE
        }
    }

    private fun onBindViewHolderEmail(
        holder: ViewHolder,
        query: Query,
        user: User,
        post: Post
    ) {
        var user1 = user
        holder.view.setOnClickListener {
            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        user1 = document.toObject(User::class.java)
                    }
                    sendEmail(user1.email, post)
                }
            }
        }
    }

    fun sendEmail(address: String, post: Post) {
        val subject = "Interested in buying ${post.title}"
        val body = """
            Hi,
            
            I saw your listing of '${post.title}' on the Marketplace app. 
            
            If that's still available, I'd like to arrange a meetup to buy it from you.
            
            Thanks!
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    fun addPost(post: Post, key: String) {
        postsList.add(post)
        postKeys.add(key)
        notifyDataSetChanged()
    }

    fun removePostByKey(key: String) {
        var index = postKeys.indexOf(key)
        // -1 is not found index
        if (index!=-1) {
            postsList.removeAt(index)
            postKeys.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var tvTitle = itemView.tvTitle
        var tvPrice = itemView.tvPrice
        var tvUser = itemView.tvUser
        var ivPhoto = itemView.ivPhoto
        var tvDescription = itemView.tvDescription
        var view = itemView
    }

}