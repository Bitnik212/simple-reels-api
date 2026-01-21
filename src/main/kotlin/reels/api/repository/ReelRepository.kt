package moe.bitt.reels.api.repository

import io.bitnik212.instagram.reels.api.ShortcodeMedia
import kotlinx.datetime.toKotlinLocalDateTime
import moe.bitt.reels.api.db.entity.ReelEntity
import moe.bitt.reels.api.db.table.ReelsTable
import org.jetbrains.exposed.sql.insertIgnore
import java.time.LocalDateTime


class ReelRepository: CommonRepository<ReelEntity>(ReelEntity) {

    suspend fun insert(reelId: String, metadata: ShortcodeMedia) = suspendTransaction {
        ReelsTable.insertIgnore {
            it[ReelsTable.reel_id] = reelId
            it[ReelsTable.metadata] = metadata
            it[ReelsTable.created_at] = LocalDateTime.now().toKotlinLocalDateTime()
        }
    }

    suspend fun setVideoUrl(reelId: String, videoUrl: String) = suspendTransaction {
        ReelEntity.find { ReelsTable.reel_id eq reelId }.firstOrNull()?.apply {
            this.videoUrl = videoUrl
        }
    }

    suspend fun findByReelId(reelId: String) = suspendTransaction {
        ReelEntity.find { ReelsTable.reel_id eq reelId }.firstOrNull()
    }

}
