package hu.ait.marketplace

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hu.ait.marketplace.ui.data.User
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun loginClick(v: View) {
        if (!isFormValid()) {
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(
            etEmail.text.toString(), etPassword.text.toString()
        ).addOnSuccessListener {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        }.addOnFailureListener {
            Toast.makeText(
                this@LoginActivity,
                "Login error: ${it.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun registerClick(v: View){
        if (!isFormValid()){
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
            etEmail.text.toString(), etPassword.text.toString()
        ).addOnFailureListener{
            Toast.makeText(this@LoginActivity,
                "Error: ${it.message}",
                Toast.LENGTH_LONG).show()
        }

        registerUserInFirestore()
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
            else -> true
        }
    }

    fun registerUserInFirestore() {

        var usersCollection = FirebaseFirestore.getInstance().collection(
            "users"
        )

        val user = User(
            FirebaseAuth.getInstance().currentUser!!.uid,
            FirebaseAuth.getInstance().currentUser!!.email!!,
            "Hong Kong",
            "https://www.thepeakid.com/wp-content/uploads/2016/03/default-profile-picture.jpg",
            java.util.Calendar.getInstance().toString()
        )

        usersCollection.add(user).addOnSuccessListener {
            Toast.makeText(this@LoginActivity,
                "Registration Successful",
                Toast.LENGTH_LONG).show()
        }.addOnFailureListener {
            Toast.makeText(
                this@LoginActivity,
                "Error ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

}
