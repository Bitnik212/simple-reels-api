package moe.bitt.reels.api.repository

import moe.bitt.reels.api.dto.Pagination
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass


abstract class CommonRepository<T: Entity<Int>>(
    internal val objects: EntityClass<Int, T>
) {

    suspend fun findAll(page: Int = 0, count: Int = 5): Pagination<T> = suspendTransaction {
        val allObjects = objects.all()
        Pagination<T>(
            data = allObjects.limit(count = count).offset(start = (page * count).toLong()).toList(),
            page = page,
            size = count,
            totalCount = allObjects.count()
        )
    }

    suspend fun findById(objectId: Int): T? = suspendTransaction {
        objects.findById(objectId)
    }

    suspend fun findAllByIds(ids: List<Int>): List<T> = suspendTransaction {
        objects.find { objects.table.id inList ids }.toList()
    }

    suspend fun delete(objectId: Int): T? = suspendTransaction {
        objects.findById(objectId)?.also { record ->
            record.delete()
        }
    }

}
