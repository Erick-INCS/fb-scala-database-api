package com.example.finch

import cats.effect.IO
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await
import io.finch._
import io.finch.catsEffect._
import io.finch.circe._
import io.circe.generic.auto._

import java.sql.{Connection,DriverManager}
import scala.io.Source
import java.io._
import java.util.Calendar

object Main extends App {

  case class Message(hello: String)

  def exec(sql:String) : Boolean = {

    // read config
    val conf = Source.fromFile("app.conf").getLines.toArray.map(_.split('='))

    var url = "jdbc:firebirdsql:"
    var driver = "org.firebirdsql.jdbc.FBDriver"
    var username = ""
    var password = ""
    var connection:Option[Connection] = None

    for (value <- conf) {
        if (value(0) == "user") {
			username = value(1)
		} else if (value(0) == "password") {
			password = value(1)
		} else if (value(0) == "url") {
			url += value(1)
		}
    }

    var res = true
    try {
        Class.forName(driver)
        connection = Some(DriverManager.getConnection(url, username, password))
        val statement = connection.getOrElse(null).createStatement
        val rs = statement.execute(sql)
    } catch {
        case e: Exception => e.printStackTrace
        res = false
    }
    if (connection.getOrElse(null) != null) connection.getOrElse(null).close

	val now = Calendar.getInstance()
	val fileName = s"${now.get(Calendar.YEAR)}-${now.get(Calendar.MONTH)}-${now.get(Calendar.DATE)}_${now.get(Calendar.HOUR)}:${now.get(Calendar.MINUTE)}:${now.get(Calendar.SECOND)}:${now.get(Calendar.MILLISECOND)}.sql"
	val pw = new PrintWriter(new File((if (res) "success/" else "errors/") + fileName))
	pw.write(sql)
	pw.close
    return res
  }

  def healthcheck: Endpoint[IO, String] = get(pathEmpty) {
    Ok(System.getProperty("user.dir"))
  }

  def execSQL: Endpoint[IO, String] = post("sql" :: param("sql")) { sql:String =>
    Ok(if (exec(sql)) "Correct." else sql)
  }

  // def hello: Endpoint[IO, Message] = get("hello" :: path[String]) { s: String =>
  //   Ok(Message(s))
  // }

  def service: Service[Request, Response] = Bootstrap
    .serve[Text.Plain](healthcheck :+: execSQL)
    // .serve[Application.Json](hello)
    .toService

  Await.ready(Http.server.serve(":8080", service))
}
