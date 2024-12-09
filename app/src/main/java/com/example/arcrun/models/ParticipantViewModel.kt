import androidx.lifecycle.ViewModel

class ParticipantViewModel(private val repository: ParticipantRepository) : ViewModel() {

    fun submitForm(userId: String, participant: Participant, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        repository.submitParticipantData(userId, participant, onSuccess, onFailure)
    }
}
