import androidx.lifecycle.ViewModel
import com.example.arcrun.models.Participant

class ParticipantViewModel(private val repository: ParticipantRepository) : ViewModel() {

    fun submitForm(userId: String, participant: Participant, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        repository.submitParticipantData(userId, participant, onSuccess, onFailure)
    }
}
