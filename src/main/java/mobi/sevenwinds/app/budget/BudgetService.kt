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

            return@transaction BudgetYearStatsResponse(
                total = getTotalItemsWithParam(param),
                totalByType = getTotalByTypeOfRecordWithParam(param),
                items = getBudgetRecordsWithParam(param)
            )
        }
    }

    private fun getBudgetRecordsWithParam(param: BudgetYearParam): List<BudgetRecordResponse> {
        val itemsWithJoin = BudgetTable
            .join(AuthorTable, joinType = JoinType.LEFT)

        val items = getQueryWithParam(itemsWithJoin, param)
            .orderBy(BudgetTable.month, SortOrder.ASC)
            .orderBy(BudgetTable.amount, SortOrder.DESC)
            .limit(param.limit, param.offset.toLong())

        return BudgetEntity.wrapRows(items).map { it.toResponse() }
    }

    private fun getTotalItemsWithParam(param: BudgetYearParam): Int {
        val totalQuery = BudgetTable
            .join(AuthorTable, joinType = JoinType.LEFT)
            .slice(BudgetTable.id.count())

        return getQueryWithParam(totalQuery, param)
            .map { it[BudgetTable.id.count()] }
            .first().toInt()
    }

    private fun getTotalByTypeOfRecordWithParam(param: BudgetYearParam): Map<String, Int> {
        val sumByTypeQuery = BudgetTable
            .join(AuthorTable, joinType = JoinType.LEFT)
            .slice(BudgetTable.amount.sum(), BudgetTable.type)

        return getQueryWithParam(sumByTypeQuery, param)
            .groupBy(BudgetTable.type)
            .associate { it[BudgetTable.type].name to (it[BudgetTable.amount.sum()] ?: -1) }
    }

    private fun getQueryWithParam(fields: FieldSet, param: BudgetYearParam): Query {
        val query = when {
            param.author != null -> {
                fields
                    .select {
                        BudgetTable.year eq param.year and AuthorTable.name.lowerCase()
                            .like("%${param.author.toLowerCase()}%")
                    }
            }
            else -> {
                fields
                    .select { BudgetTable.year eq param.year }
            }
        }

        return query
    }
}