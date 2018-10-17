package com.sipoju

import java.net.InetAddress

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext

object ZipStreamingMain extends App with StrictLogging {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = ExecutionContext.global
  implicit val http = Http()

  val interface = InetAddress.getLocalHost.getHostAddress
  Http().bindAndHandle(new DownloadService().route, interface, Env.PORT)

  logger.info(s"Server online at http://$interface:${Env.PORT}")
}
