package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.budget.dto.requests.BudgetRecordRequest
import mobi.sevenwinds.app.budget.dto.requests.BudgetYearParam
import mobi.sevenwinds.app.budget.dto.responses.BudgetRecordResponse
import mobi.sevenwinds.app.budget.dto.responses.BudgetYearStatsResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecordRequest): BudgetRecordResponse = withContext(Dispatchers.IO) {
        transaction {
            addLogger(StdOutSqlLogger)
           // val author = findById(body.authorId!!) // todo check

            val entity = BudgetEntity.new {
                year = body.year
                month = body.month
                amount = body.amount
                type = body.type
                this.author = AuthorEntity[1]
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            addLogger(StdOutSqlLogger)

            val query = BudgetTable
                .select { BudgetTable.year eq param.year }
                .orderBy(BudgetTable.month, SortOrder.ASC)
                .orderBy(BudgetTable.amount, SortOrder.DESC)
                .limit( param.offset)

            val total = BudgetTable
                .select { BudgetTable.year eq param.year }
                .count().toInt()

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