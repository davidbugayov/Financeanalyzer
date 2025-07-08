import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.util.Date

data class TransactionGroup(
    val date: Date,
    val transactions: List<Transaction>,
    val balance: Double,
    val displayPeriod: String,
)
