import sbt.Project.projectToRef

name := "kikimora"

organization := "io.plasmap"

lazy val clients = Seq(client)

lazy val scalaVersionNumber = "2.11.6"

lazy val server = (project in file("server")).settings(
  scalaVersion := scalaVersionNumber,
  scalaJSProjects := clients,
  pipelineStages := Seq(scalaJSProd, gzip),
  resolvers ++= Seq("scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
    "spray repo" at "http://repo.spray.io",
    "spray nightlies repo" at "http://nightlies.spray.io"),
  libraryDependencies ++= Seq(
    "com.vmunier"          %% "play-scalajs-scripts" % "0.3.0"  ,
    "org.scalaz"           %% "scalaz-core"          % "7.1.3"  ,
    "org.scalaz.stream"    %% "scalaz-stream"        % "0.7.2a" ,
    "com.chuusai"          %% "shapeless"            % "2.2.5"  ,
    "org.webjars"           % "jquery"               % "1.11.1" ,
    "com.lihaoyi" %% "upickle" % "0.3.6",
    "io.plasmap" %% "geow" % "0.3.11-SNAPSHOT",
    "net.ruippeixotog" %% "scala-scraper" % "0.1.1",
    "org.scalanlp" % "nak" % "1.2.1",
    "com.github.haifengl" % "smile-core" % "1.0.2",
    "com.github.haifengl" % "smile-plot" % "1.0.2",
    ws,
    specs2 % Test
  )
).enablePlugins(PlayScala)
 .aggregate(clients.map(projectToRef):_*)
 .dependsOn(sharedJvm)

lazy val client = (project in file("client")).settings(
  scalaVersion := scalaVersionNumber,
  persistLauncher := true,
  persistLauncher in Test := false,
  sourceMapsDirectories += sharedJs.base / "..",
  libraryDependencies ++= Seq(
    "org.scala-js"                                    %%% "scalajs-dom"   % "0.8.2",
    "com.github.japgolly.scalajs-react"               %%% "core"          % "0.10.1",
    "com.github.japgolly.scalajs-react"               %%% "ext-scalaz71"  % "0.10.1",
    "com.github.japgolly.scalajs-react"               %%% "ext-monocle"   % "0.10.1",
    "com.github.japgolly.fork.monocle"                %%% "monocle-macro" % "1.1.1",
    "com.github.chandu0101.scalajs-react-components"  %%% "core"          % "0.1.0",
    "com.lihaoyi" %%% "upickle" % "0.3.6",
    "com.hunorkovacs" %% "koauth" % "1.1.0"
  ),
  jsDependencies += "org.webjars.npm" % "react"     % "0.14.1" / "react-with-addons.js" commonJSName "React"    minified "react-with-addons.min.js",
  jsDependencies += "org.webjars.npm" % "react-dom" % "0.14.1" / "react-dom.js"         commonJSName "ReactDOM" minified "react-dom.min.js" dependsOn "react-with-addons.js"
).enablePlugins(ScalaJSPlugin, ScalaJSPlay).
  dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(scalaVersion := scalaVersionNumber).
  jsConfigure(_ enablePlugins ScalaJSPlay).
  jsSettings(sourceMapsBase := baseDirectory.value / "..")

lazy val sharedJvm = shared.jvm

lazy val sharedJs = shared.js

// loads the Play project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value

scalacOptions in Test ++= Seq("-Yrangepos")
