package se.durre.amazonutils.s3

import java.io.InputStream
import java.net.URL
import java.time.{LocalDateTime, ZoneId}
import java.util.Date

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}


class S3Client(accessKey: String, secretKey: String, region: String = AwsDefaults.defaultRegion) extends StorageClient {

  private val client: AmazonS3 = {
    val myCredentials = new BasicAWSCredentials(accessKey, secretKey)

    AmazonS3ClientBuilder
      .standard()
      .withCredentials(new AWSStaticCredentialsProvider(myCredentials))
      .withRegion(region)
      .build()
  }

  def download(bucket: String, key: String): InputStream = {
    val s3Object = client.getObject(bucket, key)
    s3Object.getObjectContent
  }

  def signUrl(s3Resource: S3Resource, expires: LocalDateTime): URL = {

    assert(region == s3Resource.region, "AmazonS3Client region and S3Resource region must match")

    client.generatePresignedUrl(
      s3Resource.bucket.value,
      s3Resource.key.value,
      Date.from(expires.atZone(ZoneId.systemDefault()).toInstant)
    )
  }

}

trait StorageClient {
  def download(bucket: String, key: String): InputStream
  def signUrl(s3Resource: S3Resource, expires: LocalDateTime): URL
}
