name := "kikimora"

version := "1.0"

scalaVersion := "2.11.7"

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "io.plasmap" %% "geow" % "0.3.6-SNAPSHOT"

libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "0.1.1"

libraryDependencies += "org.scalanlp" % "nak" % "1.2.1"

libraryDependencies += "com.github.haifengl" % "smile-core" % "1.0.2"

libraryDependencies += "com.github.haifengl" % "smile-plot" % "1.0.2"

libraryDependencies ++= Seq("org.specs2" %% "specs2-core" % "3.6.5" % "test")

scalacOptions in Test ++= Seq("-Yrangepos")
    