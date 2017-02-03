package se.durre.amazonutils.s3

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import java.io.InputStream
import java.net.URL
import java.time.{LocalDateTime, ZoneId}
import java.util.Date

import com.amazonaws.services.s3.model.Region


class S3Client(accessKey: String, secretKey: String, region: String = AwsDefaults.defaultRegion) extends StorageClient {

  private val client: AmazonS3Client = {
    val myCredentials = new BasicAWSCredentials(accessKey, secretKey)
    val s3Client = new AmazonS3Client(myCredentials)

    // Set aws region if not default one
    if (region != AwsDefaults.defaultRegion) {
      s3Client.setRegion(Region.fromValue(region).toAWSRegion)
    }

    s3Client
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
