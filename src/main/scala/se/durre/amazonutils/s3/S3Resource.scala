package se.durre.amazonutils.s3

case class S3Bucket(bucket: String) {
  val value: String = bucket
  override def toString: String = value
}

case class S3Key(key: String) {

  assert(!key.startsWith("/"), "A S3 key must not start with a slash")

  val value: String = key

  def extension: Option[String] = key.lastIndexOf(".") match {
    case index: Int if index > 0 && !key.endsWith(".") => Some(key.substring(index))
    case _ => None
  }

  override def toString: String = value
}

case class S3Resource(bucket: S3Bucket, key: S3Key) {

  def this(bucket: String, key: String) {
    this(S3Bucket(bucket), S3Key(key))
  }

  def url: String = s"https://$bucket.s3.amazonaws.com/$key"

  def extension: Option[String] = key.extension

  override def toString: String = url
}

object S3Resource {

  def apply(bucket: String, key: String) = new S3Resource(bucket, key)

  private val s3UrlRegex1 = """^http[s]?://(.+)\.s3.amazonaws.com/(.+)$""".r
  private val s3UrlRegex2 = """^http[s]?://s3.(.+)\.amazonaws.com/([^/]+)/(.+)$""".r
  private val s3UrlRegex3 = """^http[s]?://s3.amazonaws.com/([^/]+)/(.+)$""".r

  def fromUrl(url: String): Option[S3Resource] =
    url match {
      case s3UrlRegex1(bucketName, targetKey) => Some(S3Resource(S3Bucket(bucketName), S3Key(targetKey)))
      case s3UrlRegex2(_, bucketName, targetKey) => Some(S3Resource(S3Bucket(bucketName), S3Key(targetKey)))
      case s3UrlRegex3(bucketName, targetKey) => Some(S3Resource(S3Bucket(bucketName), S3Key(targetKey)))
      case _ => None
    }
}
