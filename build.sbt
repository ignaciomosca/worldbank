val Http4sVersion  = "0.23.13"
val CirceVersion   = "0.14.2"
val LogbackVersion = "1.2.3"
val DoobieVersion = "1.0.0-RC1"

enablePlugins(JavaAppPackaging)

dockerBaseImage := "xqdocker/ubuntu-openjdk:jdk-8"

lazy val root = (project in file("."))
  .settings(
    organization := "com.worldbank",
    name := "worldbank",
    version := "1.0",
    assembly / mainClass := Some("com.worldbank.Main"),
    assembly / assemblyJarName := "worldbank.jar",
    scalaVersion := "2.13.1",
    libraryDependencies ++= Seq(
      "org.http4s"            %% "http4s-ember-server" % Http4sVersion,
      "org.http4s"            %% "http4s-ember-client" % Http4sVersion,
      "org.http4s"            %% "http4s-circe"        % Http4sVersion,
      "org.http4s"            %% "http4s-dsl"          % Http4sVersion,
      "io.circe"              %% "circe-generic"       % CirceVersion,
      "io.circe"              %% "circe-literal"       % CirceVersion,
      "org.tpolecat"          %% "doobie-core"         % DoobieVersion,
      "org.tpolecat"          %% "doobie-hikari"       % DoobieVersion,
      "ch.qos.logback"        % "logback-classic"      % LogbackVersion,
      "org.xerial"            % "sqlite-jdbc"          % "3.23.1",
      "org.typelevel"         %% "log4cats-core"       % "2.4.0",
      "org.typelevel"         %% "log4cats-slf4j"      % "2.4.0",
      "com.github.pureconfig" %% "pureconfig" % "0.17.1"
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3"),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.0")
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Xfatal-warnings"
)
