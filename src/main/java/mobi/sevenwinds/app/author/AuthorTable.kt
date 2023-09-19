package mobi.sevenwinds.app.author

import mobi.sevenwinds.app.author.dto.responses.AuthorResponse
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.timestamp

object AuthorTable : IntIdTable() {
    val name = varchar("name", 64)
    val createOn = timestamp("create_on")
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var name by AuthorTable.name
    var createOn by AuthorTable.createOn

    fun toResponse(): AuthorResponse {
        return AuthorResponse(id.value, name, createOn)
    }
}