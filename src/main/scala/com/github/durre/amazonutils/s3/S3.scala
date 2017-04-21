package com.github.durre.amazonutils.s3

import java.io.File
import java.net.URL
import java.nio.file.Path
import java.time.LocalDateTime

import com.amazonaws.services.s3.model.CannedAccessControlList

import scala.concurrent.Future

trait S3 {

  /**
    * Download a s3 object locally to a temporary file
    *
    * @param bucket  The bucket
    * @param key     The s3 key
    * @return        The local file in the future
    */
  def download(bucket: String, key: String): Future[Path]

  /**
    * Download a s3 object locally to a temporary file
    *
    * @param s3Resource   The s3 resource
    * @return             The local file in the future
    */
  def download(s3Resource: S3Resource): Future[Path]

  /**
    * Creates a temporary download url that can be used to access otherwise private resources
    *
    * @param s3Resource The resource to download
    * @param expires    When the url expires
    * @return           The url
    */
  def signUrl(s3Resource: S3Resource, expires: LocalDateTime): URL

  /**
    * Uploads a file to s3
    *
    * @param bucket       The bucket
    * @param key          The key
    * @param contentType  Optionally specify content type
    * @param file         The file to upload
    * @param cannedAcl    The permission to the file
    */
  def upload(bucket: String, key: String, contentType: Option[String], file: File, cannedAcl: CannedAccessControlList): Future[S3Resource]

  /**
    * Removes S3 object
    *
    * @param bucket The bucket
    * @param key    The key
    */
  def delete(bucket: String, key: String): Future[Unit]
}
