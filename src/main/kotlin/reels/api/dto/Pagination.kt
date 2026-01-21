package moe.bitt.reels.api.dto

import kotlinx.serialization.Serializable


@Serializable
data class Pagination<T>(
    val data: List<T>,
    val page: Int,
    val size: Int,
    val totalCount: Long,
) {
    companion object {
        fun <T> empty(): Pagination<T> {
            return Pagination<T>(
                data = emptyList(),
                page = 0,
                size = 0,
                totalCount = 0
            )
        }
    }
}