package mobi.sevenwinds.app.budget

import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import mobi.sevenwinds.app.budget.dto.requests.BudgetRecordRequest
import mobi.sevenwinds.app.budget.dto.requests.BudgetYearParam
import mobi.sevenwinds.app.budget.dto.responses.BudgetRecordResponse
import mobi.sevenwinds.app.budget.dto.responses.BudgetYearStatsResponse

fun NormalOpenAPIRoute.budget() {
    route("/budget") {
        route("/add").post<Unit, BudgetRecordResponse, BudgetRecordRequest>(info("Добавить запись")) { param, body ->
            respond(BudgetService.addRecord(body))
        }

        route("/year/{year}/stats") {
            get<BudgetYearParam, BudgetYearStatsResponse>(info("Получить статистику за год")) { param ->
                respond(BudgetService.getYearStats(param))
            }
        }
    }
}