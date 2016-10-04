package se.durre.amazonutils.s3

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions

import scala.concurrent.Await

import scala.concurrent.duration._

import java.nio.charset.Charset
import java.nio.file.{Path, Files}

class S3ServiceSpec extends Specification with Mockito with NoTimeConversions {

  val mockedClient = mock[StorageClient]
  val s3Service = new S3Service(mockedClient)

  def createDummyFile(text: String = "dummy data"): Path = {
    val tmp = Files.createTempFile("testspec", ".tmp")
    tmp.toFile.deleteOnExit()

    val bw = Files.newBufferedWriter(tmp, Charset.defaultCharset())
    bw.write(text)
    bw.close()
    tmp
  }

  "S3Service" should {

    "be able to download files" in {
      val dummy = createDummyFile()
      mockedClient.download("bucket", "key") returns Files.newInputStream(dummy)

      val downloadedFile = Await.result(s3Service.downloadObject(S3Bucket("bucket"), S3Key("key")), 5.seconds)
      downloadedFile.toFile.length mustEqual dummy.toFile.length
    }
  }
}
