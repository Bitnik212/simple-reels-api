package moe.bitt.plugin

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.sql.Database


fun configureDatabase(url: String, user: String, password: String): Database {
    val config = HikariConfig().apply {
        jdbcUrl = url
        username = user
        this.password = password
        maximumPoolSize = 10
    }
    val dataSource = HikariDataSource(config)
    return Database.connect(dataSource)
}

fun Application.configureDatabases(config: ApplicationConfig) {
    configureDatabase(
        url = config.property("database.url").getString(),
        user = config.property("database.user").getString(),
        password = config.property("database.password").getString(),
    )
}
