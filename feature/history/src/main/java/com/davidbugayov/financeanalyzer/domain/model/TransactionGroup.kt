import java.util.Date
import com.davidbugayov.financeanalyzer.domain.model.Transaction

data class TransactionGroup(
    val date: Date,
    val transactions: List<Transaction>,
    val balance: Double,
    val displayPeriod: String,
)
