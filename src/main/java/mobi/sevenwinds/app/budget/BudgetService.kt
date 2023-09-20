package mobi.sevenwinds.app.budget

import io.ktor.features.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import mobi.sevenwinds.app.budget.dto.requests.BudgetRecordRequest
import mobi.sevenwinds.app.budget.dto.requests.BudgetYearParam
import mobi.sevenwinds.app.budget.dto.responses.BudgetRecordResponse
import mobi.sevenwinds.app.budget.dto.responses.BudgetYearStatsResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecordRequest): BudgetRecordResponse = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                year = body.year
                month = body.month
                amount = body.amount
                type = body.type
                this.author = body.authorId?.let {
                    AuthorEntity.findById(body.authorId)
                        ?: throw NotFoundException("Author with id: {${body.authorId}} is not found")
                }
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = if (param.author != null) {
                BudgetTable
                    .join(AuthorTable, JoinType.LEFT)
                    .select {
                        BudgetTable.year eq param.year and AuthorTable.name.lowerCase()
                            .like("%${param.author.toLowerCase()}%")
                    }
            } else {
                BudgetTable
                    .select {
                        BudgetTable.year eq param.year
                    }
            }
                .orderBy(BudgetTable.month, SortOrder.ASC)
                .orderBy(BudgetTable.amount, SortOrder.DESC)
                .limit(param.limit, param.offset.toLong())

            val total: Int = BudgetTable
                .slice(BudgetTable.id.count())
                .select { BudgetTable.year eq param.year }
                .map { it[BudgetTable.id.count()] }
                .first().toInt()

            val data = BudgetEntity.wrapRows(query).map { it.toResponse() }

            val sumByType = BudgetTable
                .slice(BudgetTable.amount.sum(), BudgetTable.type)
                .select { BudgetTable.year eq param.year }
                .groupBy(BudgetTable.type)
                .associate { it[BudgetTable.type].name to (it[BudgetTable.amount.sum()] ?: -1) }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}