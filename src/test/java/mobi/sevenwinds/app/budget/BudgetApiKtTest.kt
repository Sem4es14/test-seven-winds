package mobi.sevenwinds.app.budget

import io.restassured.RestAssured
import mobi.sevenwinds.app.author.AuthorTable
import mobi.sevenwinds.app.author.dto.reqests.AuthorCreateRequest
import mobi.sevenwinds.app.author.dto.responses.AuthorResponse
import mobi.sevenwinds.app.budget.dto.enums.BudgetType
import mobi.sevenwinds.app.budget.dto.requests.BudgetRecordRequest
import mobi.sevenwinds.app.budget.dto.responses.BudgetRecordResponse
import mobi.sevenwinds.app.budget.dto.responses.BudgetYearStatsResponse
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.math.abs

class BudgetApiKtTest : ServerTest() {

    @BeforeEach
    internal fun setUp() {
        transaction { BudgetTable.deleteAll() }
        transaction { AuthorTable.deleteAll() }
    }

    @Test
    fun testBudgetPagination() {
        addRecord(BudgetRecordRequest(2020, 5, 10, BudgetType.Приход))
        addRecord(BudgetRecordRequest(2020, 5, 5, BudgetType.Приход))
        addRecord(BudgetRecordRequest(2020, 5, 20, BudgetType.Приход))
        addRecord(BudgetRecordRequest(2020, 5, 30, BudgetType.Приход))
        addRecord(BudgetRecordRequest(2020, 5, 40, BudgetType.Приход))
        addRecord(BudgetRecordRequest(2030, 1, 1, BudgetType.Расход))

        RestAssured.given()
            .queryParam("limit", 3)
            .queryParam("offset", 1)
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType}")

                Assert.assertEquals(5, response.total)
                Assert.assertEquals(3, response.items.size)
                Assert.assertEquals(105, response.totalByType[BudgetType.Приход.name])
            }
    }

    @Test
    fun testBudgetPaginationWithAuthor() {
        createAuthor(AuthorCreateRequest("Robert Marlin"))
        createAuthor(AuthorCreateRequest("Alex Bradley"))
        createAuthor(AuthorCreateRequest("Bob Marley"))
        createAuthor(AuthorCreateRequest("Elon Musk"))
        addRecord(BudgetRecordRequest(2020, 5, 10, BudgetType.Приход))
        addRecord(BudgetRecordRequest(2020, 5, 5, BudgetType.Приход))
        addRecord(BudgetRecordRequest(2020, 5, 20, BudgetType.Приход, 1))
        addRecord(BudgetRecordRequest(2020, 5, 30, BudgetType.Приход, 1))
        addRecord(BudgetRecordRequest(2020, 5, 40, BudgetType.Приход, 4))
        addRecord(BudgetRecordRequest(2020, 5, 30, BudgetType.Приход, 4))
        addRecord(BudgetRecordRequest(2020, 1, 1, BudgetType.Расход, 4))

        RestAssured.given()
            .queryParam("limit", 3)
            .queryParam("offset", 1)
            .queryParam("author", "musk")
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType}")

                Assert.assertEquals(3, response.total)
                Assert.assertEquals(2, response.items.size)
                Assert.assertEquals(70, response.totalByType[BudgetType.Приход.name])
            }
    }


    @Test
    fun testStatsSortOrder() {
        addRecord(BudgetRecordRequest(2020, 5, 100, BudgetType.Приход))
        addRecord(BudgetRecordRequest(2020, 1, 5, BudgetType.Приход))
        addRecord(BudgetRecordRequest(2020, 5, 50, BudgetType.Приход))
        addRecord(BudgetRecordRequest(2020, 1, 30, BudgetType.Приход))
        addRecord(BudgetRecordRequest(2020, 5, 400, BudgetType.Приход))

        // expected sort order - month ascending, amount descending

        RestAssured.given()
            .get("/budget/year/2020/stats?limit=100&offset=0")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println(response.items)

                Assert.assertEquals(30, response.items[0].amount)
                Assert.assertEquals(5, response.items[1].amount)
                Assert.assertEquals(400, response.items[2].amount)
                Assert.assertEquals(100, response.items[3].amount)
                Assert.assertEquals(50, response.items[4].amount)
            }
    }

    @Test
    fun testInvalidMonthValues() {
        RestAssured.given()
            .jsonBody(BudgetRecordRequest(2020, -5, 5, BudgetType.Приход))
            .post("/budget/add")
            .then().statusCode(400)

        RestAssured.given()
            .jsonBody(BudgetRecordRequest(2020, 15, 5, BudgetType.Приход))
            .post("/budget/add")
            .then().statusCode(400)
    }

    private fun addRecord(record: BudgetRecordRequest) {
        RestAssured.given()
            .jsonBody(record)
            .post("/budget/add")
            .toResponse<BudgetRecordResponse>().let { response ->
                Assert.assertEquals(record.year, response.year)
                Assert.assertEquals(record.month, response.month)
                Assert.assertEquals(record.amount, response.amount)
                Assert.assertEquals(record.type, response.type)
                Assert.assertEquals(record.authorId, response.author?.authorId)
            }
    }

    private fun createAuthor(request: AuthorCreateRequest) {
        RestAssured.given()
            .jsonBody(request)
            .post("/author/create")
            .toResponse<AuthorResponse>().let { response ->
                Assert.assertEquals(request.name, response.name)
                Assert.assertTrue(abs( Instant.now().toEpochMilli() - response.createOn.toEpochMilli()) < 1000)
            }
    }
}