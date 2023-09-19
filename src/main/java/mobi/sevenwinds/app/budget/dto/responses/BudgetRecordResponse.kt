package mobi.sevenwinds.app.budget.dto.responses

import mobi.sevenwinds.app.author.dto.responses.AuthorResponse
import mobi.sevenwinds.app.budget.dto.enums.BudgetType

data class BudgetRecordResponse(
    val year: Int,
    val month: Int,
    val amount: Int,
    val type: BudgetType,
    val author: AuthorResponse?,
)