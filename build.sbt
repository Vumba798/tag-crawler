lazy val tapirVersion = "1.11.10"
lazy val circeVersion = "0.14.10"

lazy val rootProject = (project in file(".")).settings(
  Seq(
    name := "tag-crawler",
    version := "0.1.0-SNAPSHOT",
    organization := "com.softwaremill",
    scalaVersion := "3.5.2",
    scalacOptions += "-Ypartial-unification",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % "0.23.30",
      "com.softwaremill.sttp.client3" %% "core" % "3.10.1",
      "com.softwaremill.sttp.client3" %% "cats" % "3.10.1",
      "com.softwaremill.sttp.tapir" %% "tapir-core"              % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"     % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe"        % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "io.circe" %% "circe-core"    % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser"  % circeVersion,
      "org.slf4j" % "slf4j-simple" % "2.0.16",
      "org.typelevel" %% "cats-core"      % "2.12.0",
      "org.typelevel" %% "cats-effect"    % "3.5.7",
      "org.typelevel" %% "log4cats-slf4j" % "2.7.0",
      "org.jsoup" % "jsoup" % "1.18.3",
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
     // "com.softwaremill.sttp.client3" %% "circe" % "3.10.1" % Test
    )
  )
)
