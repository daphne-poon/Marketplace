package hu.ait.marketplace.ui.data

import com.google.firebase.firestore.FirebaseFirestore

class User(var uid: String = "",
           var email: String = "",
           var username: String = "",
           var location: String = "",
           var profPicUrl: String = "",
           var creationDate: String = "")

internal interface UserCallback {
    fun onGetUser(user: User?)
}
fun getUserFromEmail(email: String) : User {
    var user = User()
    val db = FirebaseFirestore.getInstance()
    val query = db.collection("users").whereEqualTo("author", email)
    query.get().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            for (document in task.result!!) {
                user = document.toObject(User::class.java)
            }
        }
    }
    return user
}

