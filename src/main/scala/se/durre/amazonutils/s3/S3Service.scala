package se.durre.amazonutils.s3

import scala.concurrent._

import java.nio.file.{StandardCopyOption, Path, Files}

class S3Service(storageClient: StorageClient)(implicit ec: ExecutionContext) {

  /**
   * Download a s3 object locally to a temporary file
   *
   * @param bucket  The bucket
   * @param key     The s3 key
   * @return        The local file in the future
   */
  def downloadObject(bucket: S3Bucket, key: S3Key): Future[Path] = Future {
    blocking {
      val inputStream = storageClient.download(bucket.toString, key.toString)
      val localPath = Files.createTempFile("s3-download", key.extension.getOrElse(".tmp"))

      localPath.toFile.deleteOnExit()

      Files.copy(inputStream, localPath, StandardCopyOption.REPLACE_EXISTING)
      localPath
    }
  }

}
