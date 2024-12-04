import com.google.firebase.auth.FirebaseAuth

data class User(
    val name: String,
    val profileImageUrl: String?
)

class GetUser {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUser(onUser: (User) -> Unit) {
        val currentUser = auth.currentUser

        val user = if (currentUser != null) {
            User(
                name = currentUser.displayName ?: "User",
                profileImageUrl = currentUser.photoUrl?.toString()
            )
        } else {
            User(
                name = "Guest",
                profileImageUrl = null
            )
        }

        onUser(user)
    }
}