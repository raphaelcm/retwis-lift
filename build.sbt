
organization := "com.retwis"

name := "retwis-lift"

version := "1.0"

scalaVersion := "2.9.0-1"

seq(webSettings: _*)

fork := true

libraryDependencies ++= {
  val liftVersion = "2.4-M1"
  Seq(
  "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
  "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default",
  "net.liftweb" %% "lift-wizard" % liftVersion % "compile->default"
  )
}
 
 
libraryDependencies ++= Seq(
  "junit" % "junit" % "4.5" % "test->default",
  "org.eclipse.jetty" % "jetty-webapp" % "7.4.4.v20110707" % "jetty",
  //"javax.servlet" % "servlet-api" % "2.5" % "provided->default",
  //"com.h2database" % "h2" % "1.2.138",
  "ch.qos.logback" % "logback-classic" % "0.9.26" % "compile->default",
  "redis.clients" % "jedis" % "2.0.0",
  "net.debasishg" % "redisclient_2.9.0" % "2.3.1"
)
