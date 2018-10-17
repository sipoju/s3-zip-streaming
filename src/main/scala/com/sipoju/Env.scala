package com.sipoju

import scala.util.Properties

object Env {
  val PORT = Properties.envOrElse("PORT", "8087").toInt

  val S3_BUCKET = Properties.envOrElse("S3_BUCKET", "")
  val S3_BUCKET_REGION = Properties.envOrElse("S3_BUCKET_REGION", "eu-west-1")

  val MAX_ACTIVE_DOWNLOAD = Properties.envOrElse("MAX_ACTIVE_DOWNLOAD", "3").toInt

  val AWS_ACCESS_KEY_ID = Properties.envOrNone("AWS_ACCESS_KEY_ID")
  val AWS_SECRET_KEY = Properties.envOrNone("AWS_SECRET_KEY")
}
