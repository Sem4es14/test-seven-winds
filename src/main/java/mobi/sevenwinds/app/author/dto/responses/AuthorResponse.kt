package mobi.sevenwinds.app.author.dto.responses

import java.time.Instant

data class AuthorResponse(
    val authorId : Int,
    val name : String,
    val createOn: Instant,
)