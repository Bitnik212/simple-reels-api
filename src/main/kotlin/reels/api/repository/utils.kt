package moe.bitt.reels.api.repository

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.BooleanColumnType
import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.stringLiteral
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.postgresql.util.PGobject

class PGEnum<T : Enum<T>>(enumTypeName: String, enumValue: T?) : PGobject() {
    init {
        value = enumValue?.name
        type = enumTypeName
    }
}

suspend fun <T> suspendTransaction(block: suspend Transaction.() -> T): T = newSuspendedTransaction(
    context = Dispatchers.IO,
    statement = block
)

fun jsonbExists(jsonb: Expression<*>, key: String): Expression<Boolean> =
    CustomFunction(
        functionName = "jsonb_exists",
        columnType = BooleanColumnType(),
        jsonb,
        stringLiteral(key)
    )


class JsonbTextExtract(
    private val jsonb: Expression<*>,
    private val key: String
) : Expression<String?>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append(jsonb)
        queryBuilder.append(" ->> ")
        queryBuilder.append(stringLiteral(key))
    }
}

fun Expression<*>.jsonbText(key: String): Expression<String?> = JsonbTextExtract(this, key)

class JsonbHasKey(
    private val jsonb: Expression<*>,
    private val key: String
) : Op<Boolean>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append(jsonb)
        queryBuilder.append(" ? ")
        queryBuilder.append(stringLiteral(key))
    }
}

fun Expression<*>.hasJsonbKey(key: String): Op<Boolean> = JsonbHasKey(this, key)

