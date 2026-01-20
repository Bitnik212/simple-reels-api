package moe.bitt.reels.api

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client as AwsS3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.Closeable
import java.io.File
import java.net.URI


class S3Client(
    private val bucket: String,
    private val endpointUrl: String,
    region: String,
    accessKey: String,
    secretKey: String
): Closeable {
    private val s3Client: AwsS3Client = AwsS3Client.builder()
        .endpointOverride(URI.create(endpointUrl))
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            )
        )
        .forcePathStyle(true) // Often needed for custom S3 providers like MinIO or DigitalOcean
        .build()

    fun putFile(file: File, key: String, contentType: String) {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .build()

        s3Client.putObject(putObjectRequest, RequestBody.fromFile(file))
    }

    fun putChannel(channel: ByteReadChannel, contentLength: Long, key: String, contentType: String): String {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .contentLength(contentLength)
            .build()

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(channel.toInputStream(), contentLength))
        return "$endpointUrl/$bucket/$key"
    }

    override fun close() {
        s3Client.close()
    }
}
