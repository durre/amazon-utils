package se.durre.amazonutils.s3

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client

import java.io.InputStream


class S3Client(accessKey: String, secretKey: String) extends StorageClient {

  private val client: AmazonS3Client = {
    val myCredentials = new BasicAWSCredentials(accessKey, secretKey)
    new AmazonS3Client(myCredentials)
  }

  def download(bucket: String, key: String): InputStream = {
    val s3Object = client.getObject(bucket, key)
    s3Object.getObjectContent
  }
}

trait StorageClient {
  def download(bucket: String, key: String): InputStream
}
