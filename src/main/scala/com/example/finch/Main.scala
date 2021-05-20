package com.example.finch

import cats.effect.IO
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await
import io.finch._
import io.finch.catsEffect._
import io.finch.circe._
import io.circe.generic.auto._

import io.circe.Decoder, io.circe.Encoder, io.circe.generic.semiauto._

import java.sql.{Connection,DriverManager}
import scala.io.Source
import java.io._
import java.util.Calendar

object Main extends App {

	case class Message(hello: String)

	def exec(sql:String) : String = {

		// read config
		val conf = Source.fromFile("app.conf").getLines.toArray.map(_.split('='))

		var urlPrefix = "jdbc:firebirdsql:"
		var driver = "org.firebirdsql.jdbc.FBDriver"

		val uEnv = "SAPI_DB_USER"
		val pEnv = "SAPI_DB_PASS"
		val urlEnv = "SAPI_DB_URL"

		var username = sys.env.get(uEnv).getOrElse(null)
		var password = sys.env.get(pEnv).getOrElse(null)
		var url = sys.env.get(urlEnv).getOrElse(null)

		var connection:Option[Connection] = None

		if (username == null || password == null || url == null) {
			for (value <- conf) {
				if (value(0) == "user" && username == null) {
					username = value(1)
				} else if (value(0) == "password" && password == null) {
					password = value(1)
				} else if (value(0) == "url" && url == null) {
					url = value(1)
				}
			}
		}

		url = urlPrefix + url

		var res = true
		var strOut:String = "";
		try {
			Class.forName(driver)
			connection = Some(DriverManager.getConnection(url, username, password))
			val statement = connection.getOrElse(null).createStatement
			var rs = statement.execute(sql)

			while (rs) {
				val cResult = statement.getResultSet
				val md = cResult.getMetaData()
				var cols:Array[String] = Array()

				if (md.getColumnCount() > 0) {
					var cValue = md.getColumnLabel(1)

					strOut = strOut + (if (cValue.contains(",")) "\"" + s"${cValue}" + "\"" else cValue)
					cols = cols :+ cValue 

					for (i <- 2 to md.getColumnCount()) {
						cValue = md.getColumnLabel(i)
						cols = cols :+ cValue 
						strOut = strOut + s",${cValue}"
					}
				}
				

				while (cResult.next()) {
					strOut += "\n"
					if (cols.length > 0) {
						strOut += cResult.getNString(cols.head)
						for (v <- cols.tail) {
							strOut += "," + cResult.getNString(v)
						}
					}
				}

				println(strOut + "\n\n")
				rs = statement.getMoreResults()
			}

		} catch {
			case e: Exception => e.printStackTrace
			res = false
		}
		if (connection.getOrElse(null) != null) connection.getOrElse(null).close

		val now = Calendar.getInstance()
		val fileName = s"${now.get(Calendar.YEAR)}-${now.get(Calendar.MONTH)}-${now.get(Calendar.DATE)}_${now.get(Calendar.HOUR)}:${now.get(Calendar.MINUTE)}:${now.get(Calendar.SECOND)}:${now.get(Calendar.MILLISECOND)}.sql"
		val outFileName = s"${now.get(Calendar.YEAR)}-${now.get(Calendar.MONTH)}-${now.get(Calendar.DATE)}_${now.get(Calendar.HOUR)}:${now.get(Calendar.MINUTE)}:${now.get(Calendar.SECOND)}:${now.get(Calendar.MILLISECOND)}.csv"
		val pw = new PrintWriter(new File((if (res) "success/" else "errors/") + fileName))
		val outPw = new PrintWriter(new File("out/" + outFileName))

		pw.write(sql)
		pw.close

		outPw.write(strOut)
		outPw.close

		return (if (res) strOut else null)
	}

	def healthcheck: Endpoint[IO, String] = get(pathEmpty) {
		Ok(System.getProperty("user.dir"))
	}

	case class ISQL(sql: String)
	implicit val decoder: Decoder[ISQL] = deriveDecoder[ISQL]

	def execSQL: Endpoint[IO, String] = post("sql" :: jsonBody[ISQL]) { inp:ISQL =>
		val res = exec(inp.sql)
		Ok(if (res != null) res else inp.sql)
	}

	def service: Service[Request, Response] = Bootstrap
		.serve[Text.Plain](healthcheck :+: execSQL)
		//.serve[Application.Json](hello)
		.toService

	Await.ready(Http.server.serve(":8080", service))
}
