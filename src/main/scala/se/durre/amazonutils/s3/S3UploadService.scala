package se.durre.amazonutils.s3

import sun.misc.BASE64Encoder

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

case class S3UploadForm(
  policy: String,
  signature: String,
  accessKey: String
)

class S3UploadService(accessKey: String, secretKey: String) {

  /**
   * Create an upload
   *
   * @param target      The target location
   * @param contentType An optional content type that the upload has to match
   * @param maxSize     A max size for the file
   * @return            An upload form that can be sent to the client
   */
  def createUploadForm(target: S3Resource, contentType: Option[String], maxSize: Int): S3UploadForm = {
    val policy = policyDocument(target, contentType, maxSize)
    val signature = formSignature(policy, secretKey)
    S3UploadForm(policy, signature, accessKey)
  }

  private def policyDocument(target: S3Resource, contentType: Option[String], maxSize: Int): String = {
    val expires = DateTimeFormatter.ISO_INSTANT.format(LocalDateTime.now().plusHours(1))
    new BASE64Encoder().encode(
      s"""
        |{
        |  "expiration": "$expires",
        |  "conditions": [
        |    {"bucket": "${target.bucket.value}"},
        |    {"key": "${target.key.value}"},
        |    {"acl": "private"},
        |    {"success_action_status": "201"},
        |    ["starts-with", "$$Content-Type", "${contentType.getOrElse("")}"],
        |    ["content-length-range", 0, $maxSize]
        |  ]
        |}
      """.stripMargin.getBytes("UTF-8")
    ).replaceAll("\n","").replaceAll("\r","")
  }

  private def formSignature(policy: String, secretKey: String): String = {
    val hmac = Mac.getInstance("HmacSHA1")
    hmac.init(new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA1"))
    new BASE64Encoder().encode(hmac.doFinal(policy.getBytes("UTF-8"))).replaceAll("\n", "")
  }
}
