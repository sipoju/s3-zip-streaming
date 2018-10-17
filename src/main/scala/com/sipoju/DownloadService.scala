package com.sipoju

import java.io.{PipedInputStream, PipedOutputStream}
import java.util.concurrent.Executors
import java.util.zip.{ZipEntry, ZipOutputStream}

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{ContentDispositionTypes, `Content-Disposition`}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.StreamConverters
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.util.IOUtils
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class DownloadService(implicit ec: ExecutionContext) extends StrictLogging {

  val s3Client: AmazonS3 = (Env.AWS_ACCESS_KEY_ID, Env.AWS_SECRET_KEY) match {
    case (Some(awsAccessKeyId), Some(awsSecretKey)) =>
      AmazonS3ClientBuilder.standard()
        .withRegion(Env.S3_BUCKET_REGION)
        .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)))
        .build()
    case _ =>
      AmazonS3ClientBuilder.standard()
        .withRegion(Env.S3_BUCKET_REGION)
        .build()
  }

  type ByteArray = Array[Byte]

  val corsSettings = CorsSettings.defaultSettings

  val route: Route =
    cors(corsSettings) {
      path("download") {
        (get) {
          parameter('keys) { keys =>
            respondWithHeader(`Content-Disposition`(
              ContentDispositionTypes.attachment, Map("filename" -> "download.zip"))) {
              complete {
                downloadRoute(keys)
              }
            }
          }
        }
      }
    }

  def downloadRoute(keys: String): Future[HttpResponse] = {

    val keyList = keys.split(";").toList

    val pipedInputStream = new PipedInputStream
    val pipedOutputStream = new PipedOutputStream(pipedInputStream)
    val zip = new ZipOutputStream(pipedOutputStream)

    val byteSource = StreamConverters.fromInputStream(() => pipedInputStream)

    Future {
      try { download(keyList, zip) } finally {
        zip.close()
        pipedOutputStream.close()
      }
    }

    Future.successful(
      HttpResponse(entity = HttpEntity(ContentTypes.`application/octet-stream`, byteSource))
    )
  }

  def download(keyList: List[String], zip: ZipOutputStream): Unit = {

    def downloadS3Object(key: String): (String, ByteArray) = {
      val startTime = System.currentTimeMillis
      logger.info(s"Start downloading s3 object $key")

      val filenameReg = """[^\\/]+$""".r
      val fileNameKey = filenameReg.findFirstIn(key).get

      val s3ObjectReq = new GetObjectRequest(Env.S3_BUCKET, key)
      val res = (fileNameKey, IOUtils.toByteArray(s3Client.getObject(s3ObjectReq).getObjectContent))

      logger.info(s"Total time for downloading s3 object $key is ${System.currentTimeMillis - startTime} ms")
      res
    }


    def addZipEntry(zipOutputStream: ZipOutputStream, name: String, data: ByteArray): String = {
      logger.info(s"Start adding s3 object to zip entry $name")
      val startTime = System.currentTimeMillis
      zipOutputStream.putNextEntry(new ZipEntry(name))
      zipOutputStream.write(data)
      zipOutputStream.closeEntry()
      logger.info(s"Total time adding s3 object to zip entry $name is ${System.currentTimeMillis - startTime} ms")
      name
    }

    val activeDownloadThreadContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(Env.MAX_ACTIVE_DOWNLOAD))
    val singleZippingThreadContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1))

    val downloadingList = keyList.map { key =>
      logger.info(s"Processing image $key")
      Future(downloadS3Object(key))(activeDownloadThreadContext)
    }

    val zippingProcess = downloadingList.map(downloading =>
      downloading.flatMap(res =>
        Future(addZipEntry(zip, res._1, res._2))(singleZippingThreadContext)
      )
    )

    Await.result(Future.sequence(zippingProcess), zippingProcess.size.minutes)

    singleZippingThreadContext.shutdown()
    activeDownloadThreadContext.shutdown()
    logger.info("Done")
  }
}

