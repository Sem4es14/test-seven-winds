
package mobi.sevenwinds.app.author.dto.responses

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer
import java.time.Instant

data class AuthorResponse(
    val authorId : Int,
    val name : String,
    @JsonSerialize(using = InstantSerializer::class)
    val createOn: Instant,
)