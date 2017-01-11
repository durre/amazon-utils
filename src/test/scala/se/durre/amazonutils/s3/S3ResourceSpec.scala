package se.durre.amazonutils

import org.specs2.mutable.Specification
import se.durre.amazonutils.s3.{S3Resource, S3Key}

class S3ResourceSpec extends Specification {

  "S3Key" should {

    "must not start with a slash" in {
      S3Key("/target/key") must throwA[AssertionError]
    }

    "be able to extract file extensions" in {
      S3Key("target/image.jpg").extension must beSome(".jpg")
      S3Key("target/image").extension must beNone
      S3Key("target/image.").extension must beNone
    }
  }

  "S3Resource" should {

    "get url for a s3 resource" in {
      S3Resource("bucket", "mykey").url mustEqual "https://bucket.s3.amazonaws.com/mykey"
    }

    "construct a resource from a https url" in {
      val resource = S3Resource.fromUrl("https://bucket.s3.amazonaws.com/mykey")
      resource must beSome(S3Resource("bucket", "mykey"))
    }

    "construct a resource from a http url" in {
      val resource = S3Resource.fromUrl("http://bucket.s3.amazonaws.com/mykey")
      resource must beSome(S3Resource("bucket", "mykey"))
    }

    "construct a resource from US Standard region url" in {
      val resource = S3Resource.fromUrl("https://s3.amazonaws.com/bucket/mykey")
      resource must beSome(S3Resource("bucket", "mykey"))
    }

    "construct a resource from EU region url" in {
      val resource = S3Resource.fromUrl("https://s3.eu-central-1.amazonaws.com/bucket/mykey")
      resource must beSome(S3Resource("bucket", "mykey"))
    }
  }
}
