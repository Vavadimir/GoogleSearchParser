name := "GooglePageParser"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.16",
  "com.typesafe.akka" %% "akka-http" % "10.0.3",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.3",
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.2.1",
  "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.19",
  "org.jsoup" % "jsoup" % "1.10.2"
)
    