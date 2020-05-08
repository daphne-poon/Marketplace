package hu.ait.marketplace.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import hu.ait.marketplace.R
import hu.ait.marketplace.ui.data.Post
import kotlinx.android.synthetic.main.post_row.view.*

class EditablePostsAdapter : RecyclerView.Adapter<EditablePostsAdapter.ViewHolder> {

    lateinit var context: Context
    var postsList = mutableListOf<Post>()
    var postKeys = mutableListOf<String>()

    lateinit var currentUid: String

    constructor(context: Context, uid: String) : super() {
        this.context = context
        this.currentUid = uid
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
        var post = postsList.get(holder.adapterPosition)

        holder.tvTitle.text = post.title
        holder.tvPrice.text = post.price
        holder.tvUser.text = post.author
        holder.tvLocation.text = post.location

        if (post.imgUrl.isNotEmpty()) {
            holder.ivPhoto.visibility = View.VISIBLE
            Glide.with(context).load(post.imgUrl).into(holder.ivPhoto)
        } else {
            holder.ivPhoto.visibility = View.GONE
        }
    }

    fun addPost(post: Post, key: String) {
        postsList.add(post)
        postKeys.add(key)
        notifyDataSetChanged()
    }


    // when i press the delete button
    private fun removePost(index: Int) {
        // postKeys[index] gives u the key for a given post
        FirebaseFirestore.getInstance().collection("posts").document(
            postKeys[index]
        ).delete()

        postsList.removeAt(index)
        postKeys.removeAt(index)

        // notify recycler view
        notifyItemRemoved(index)
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
        var tvLocation = itemView.tvLocation
        var tvUser = itemView.tvUser
        var ivPhoto = itemView.ivPhoto
    }
}