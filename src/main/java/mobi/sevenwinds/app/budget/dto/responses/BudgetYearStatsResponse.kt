package mobi.sevenwinds.app.budget.dto.responses

class BudgetYearStatsResponse(
    val total: Int,
    val totalByType: Map<String, Int>,
    val items: List<BudgetRecordResponse>
)