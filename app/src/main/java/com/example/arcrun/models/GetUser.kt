import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

data class User(
    val name: String,
    val profileImageUrl: String?,
    val email:String?
)

class GetUser {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    fun getCurrentUser(onUser: (User) -> Unit) {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val usersRef = database.getReference("users").child(userId)

            usersRef.get().addOnSuccessListener { dataSnapshot ->
                val name = dataSnapshot.child("name").getValue(String::class.java) ?: "User"
                val profileImageUrl = currentUser.photoUrl?.toString()
                val userEmail = dataSnapshot.child("email").getValue(String::class.java) ?: "User Email"

                val user = User(
                    name = name,
                    profileImageUrl = profileImageUrl,
                    email = userEmail
                )
                onUser(user)
            }.addOnFailureListener {
                val user = User(
                    name = "User",
                    profileImageUrl = currentUser.photoUrl?.toString(),
                    email = "User Email"
                )
                onUser(user)
            }
        } else {
            val user = User(
                name = "Guest",
                profileImageUrl = null,
                email = "User Email"
            )
            onUser(user)
        }
    }
}