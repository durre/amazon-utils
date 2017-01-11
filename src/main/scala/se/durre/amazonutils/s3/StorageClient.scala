package se.durre.amazonutils.s3

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client

import java.io.InputStream

import com.amazonaws.services.s3.model.Region


class S3Client(accessKey: String, secretKey: String, region: Option[String] = None) extends StorageClient {

  private val client: AmazonS3Client = {
    val myCredentials = new BasicAWSCredentials(accessKey, secretKey)
    val s3Client = new AmazonS3Client(myCredentials)

    // Set aws region if specified
    region.foreach(id => s3Client.setRegion(Region.fromValue(id).toAWSRegion))

    s3Client
  }

  def download(bucket: String, key: String): InputStream = {
    val s3Object = client.getObject(bucket, key)
    s3Object.getObjectContent
  }
}

trait StorageClient {
  def download(bucket: String, key: String): InputStream
}
