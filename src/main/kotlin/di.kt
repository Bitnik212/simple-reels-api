package moe.bitt

import io.ktor.server.config.ApplicationConfig
import moe.bitt.reels.api.S3Client
import org.koin.dsl.module


val mainModule = module {

    single<S3Client> {
        val config: ApplicationConfig = get()

        S3Client(
            bucket = config.property("s3.bucket").getString(),
            endpointUrl = config.property("s3.endpointUrl").getString(),
            region = config.property("s3.region").getString(),
            accessKey = config.property("s3.accessKey").getString(),
            secretKey = config.property("s3.secretKey").getString()
        )
    }

}