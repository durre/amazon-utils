package com.github.durre.amazonutils.s3

import java.io.File
import java.net.URL
import java.nio.file.{Files, Path, StandardCopyOption}
import java.time.{LocalDateTime, ZoneId}
import java.util.Date

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.services.s3.model.{CannedAccessControlList, ObjectMetadata, PutObjectRequest}

import scala.concurrent._

class S3Client(accessKey: String, secretKey: String, region: String = AwsDefaults.defaultRegion)(implicit ec: ExecutionContext) extends S3 {

  private val client: AmazonS3 = {
    val myCredentials = new BasicAWSCredentials(accessKey, secretKey)

    AmazonS3ClientBuilder
      .standard()
      .withCredentials(new AWSStaticCredentialsProvider(myCredentials))
      .withRegion(region)
      .build()
  }

  private val utc = ZoneId.of("UTC")

  override def download(bucket: String, key: String): Future[Path] = Future {
    blocking {
      val s3Object = client.getObject(bucket, key)
      val inputStream = s3Object.getObjectContent
      val localPath = Files.createTempFile("s3-download", S3Key(key).extension.getOrElse(".tmp"))

      localPath.toFile.deleteOnExit()

      Files.copy(inputStream, localPath, StandardCopyOption.REPLACE_EXISTING)
      localPath
    }
  }

  override def download(s3Resource: S3Resource): Future[Path] = {
    assert(s3Resource.region == region, "S3Client region and S3Resource region doesn't match")
    download(s3Resource.bucket.value, s3Resource.key.value)
  }

  override def signUrl(s3Resource: S3Resource, expires: LocalDateTime): URL = {
    assert(region == s3Resource.region, "S3Client region and S3Resource region doesn't match")

    client.generatePresignedUrl(
      s3Resource.bucket.value,
      s3Resource.key.value,
      Date.from(expires.atZone(utc).toInstant)
    )
  }

  override def upload(bucket: String, key: String, contentType: Option[String], file: File, cannedAcl: CannedAccessControlList): Future[S3Resource] = Future {
    blocking {

      val metadata = new ObjectMetadata()
      contentType.foreach(metadata.setContentType)
      metadata.setContentLength(file.length())

      val req = new PutObjectRequest(bucket, key, file)
        .withMetadata(metadata)
        .withCannedAcl(cannedAcl)

      client.putObject(req)
      S3Resource(bucket, key, region)
    }
  }

  override def delete(bucket: String, key: String): Future[Unit] = Future {
    blocking {
      client.deleteObject(bucket, key)
    }
  }
}
