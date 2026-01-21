package moe.bitt.reels.api.db.table

import io.bitnik212.instagram.reels.api.ShortcodeMedia
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime


object ReelsTable: IdTable<Int>("reels") {
    override val id: Column<EntityID<Int>> = integer("id").autoIncrement().entityId()

    val reel_id = varchar("reel_id", 255).uniqueIndex()
    val video_url = text("video_url").nullable().default(null)
    val metadata = jsonb<ShortcodeMedia>("metadata", format)

    val created_at = datetime("created_at").defaultExpression(CurrentDateTime)

}