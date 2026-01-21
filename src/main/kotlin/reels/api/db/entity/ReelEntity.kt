package moe.bitt.reels.api.db.entity

import moe.bitt.reels.api.db.table.ReelsTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass


class ReelEntity(id: EntityID<Int>) : IntEntity(id){
    companion object : IntEntityClass<ReelEntity>(ReelsTable)

    var reelId by ReelsTable.reel_id
    var videoUrl by ReelsTable.video_url
    var metaData by ReelsTable.metadata
    var createdAt by ReelsTable.created_at
}
