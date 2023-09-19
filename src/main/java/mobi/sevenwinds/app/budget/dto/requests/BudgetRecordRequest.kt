package mobi.sevenwinds.app.budget.dto.requests

import com.papsign.ktor.openapigen.annotations.type.number.integer.max.Max
import com.papsign.ktor.openapigen.annotations.type.number.integer.min.Min
import mobi.sevenwinds.app.budget.dto.enums.BudgetType

data class BudgetRecordRequest(
    @Min(1900) val year: Int,
    @Min(1) @Max(12) val month: Int,
    @Min(1) val amount: Int,
    val type: BudgetType,
    @Min(1) val authorId: Int?
)