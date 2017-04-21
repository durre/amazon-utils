package com.github.durre.amazonutils.s3

import java.time.LocalDateTime

case class S3Bucket(bucket: String) {
  val value: String = bucket
  override def toString: String = value
}

case class S3Key(key: String) {

  assert(!key.startsWith("/"), "An S3 key must not start with a slash")

  val value: String = key

  def extension: Option[String] = key.lastIndexOf(".") match {
    case index: Int if index > 0 && !key.endsWith(".") => Some(key.substring(index))
    case _ => None
  }

  override def toString: String = value
}

case class S3Resource(bucket: S3Bucket, key: S3Key, region: String) {

  def this(bucket: String, key: String, region: String) {
    this(S3Bucket(bucket), S3Key(key), region)
  }

  def url: String = region match {
    case AwsDefaults.defaultRegion => s"https://$bucket.s3.amazonaws.com/$key"
    case _ => s"https://$bucket.s3-$region.amazonaws.com/$key"
  }

  def signedUrl(expires: LocalDateTime)(implicit s3: S3) =
    s3.signUrl(this, expires)

  def extension: Option[String] = key.extension

  override def toString: String = url
}

object S3Resource {

  def apply(bucket: String, key: String) = new S3Resource(bucket, key, AwsDefaults.defaultRegion)
  def apply(bucket: String, key: String, region: String) = new S3Resource(bucket, key, region)

  // Ex: https://s3-us-west-2.amazonaws.com/bucket/key.jpg
  private val s3PathBasedUrl = """^http[s]?:\/\/s3-?([a-z0-9-]+)?[.]amazonaws[.]com\/([^\/]+)\/(.+)$""".r
  // Ex: https://bucket.s3-us-west-2.amazonaws.com/path/key.jpg
  private val s3VirtualHostUrl = """^http[s]?:\/\/([a-zA-Z0-9-_]+)[.]s3-?([a-z0-9-]+)?[.]amazonaws[.]com\/(.+)$""".r

  def fromUrl(url: String): Option[S3Resource] =
    url match {

      case s3PathBasedUrl(null, bucket, key) => Some(S3Resource(bucket, key))
      case s3PathBasedUrl(region, bucket, key) => Some(S3Resource(bucket, key, region))

      case s3VirtualHostUrl(bucket, null, key) => Some(S3Resource(bucket, key))
      case s3VirtualHostUrl(bucket, region, key) => Some(S3Resource(bucket, key, region))

      case _ => None
    }
}
