package mobi.sevenwinds.app.author

import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import mobi.sevenwinds.app.author.dto.reqests.AuthorCreateRequest
import mobi.sevenwinds.app.author.dto.responses.AuthorResponse

fun NormalOpenAPIRoute.author() {
    route("/author") {
        route("/create").post<Unit, AuthorResponse, AuthorCreateRequest>(info("Создать автора")) { _, request ->
            respond(AuthorService.createAuthor(request))
        }
    }
}