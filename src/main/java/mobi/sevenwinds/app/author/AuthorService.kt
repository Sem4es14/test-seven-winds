package mobi.sevenwinds.app.author

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.dto.requests.AuthorCreateRequest
import mobi.sevenwinds.app.author.dto.responses.AuthorResponse
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

object AuthorService {

    suspend fun createAuthor(body: AuthorCreateRequest): AuthorResponse = withContext(Dispatchers.IO) {
        transaction {
            val entity = AuthorEntity.new {
                this.name = body.name
                this.createOn = Instant.now()
            }

            return@transaction entity.toResponse()
        }
    }
}