import com.google.firebase.database.FirebaseDatabase

class ParticipantRepository(private val firebaseDatabase: FirebaseDatabase) {

    fun submitParticipantData(userId: String, participant: Participant, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val participantRef = firebaseDatabase.reference.child("peserta").child(userId)

        participantRef.setValue(participant)
            .addOnSuccessListener {
                onSuccess()  // Callback jika berhasil
            }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Unknown error")  // Callback jika gagal
            }
    }
}
