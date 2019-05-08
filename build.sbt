import com.github.retronym.SbtOneJar._

import scala.collection.JavaConverters._

name := "donut"

organization := "report.donut"

description := "Donut - Automated Testing Reporting Tool"

version := "1.2.1"

scalaVersion := "2.11.7"

libraryDependencies ++= {
  Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "org.apache.commons" % "commons-lang3" % "3.4",
    "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.15",
    "com.github.nscala-time" %% "nscala-time" % "1.8.0",
    "org.json4s" %% "json4s-native" % "3.3.0",
    "org.json4s" %% "json4s-jackson" % "3.2.11",
    "com.gilt" %% "handlebars-scala" % "2.1.1",
    "io.dropwizard.metrics" % "metrics-core" % "3.1.2",
    "com.github.scopt" %% "scopt" % "3.3.0",
    "org.scalatest" %% "scalatest" % "2.2.4" % "test"
  )
}

oneJarSettings

mainClass in oneJar := Some("report.donut.Boot")

addArtifact(artifact in (Compile, oneJar), oneJar)

com.github.retronym.SbtOneJar.oneJarSettings
artifact in (Compile, oneJar) ~= { art =>
  art.copy(`classifier` = Some("one-jar"))
}

// Enables publishing to maven repo
publishMavenStyle := true

// Do not append Scala versions to the generated artifacts
crossPaths := false

publishArtifact in Test := false

pomIncludeRepository := { x => false }

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <url>https://github.com/DonutReport/donut</url>
    <licenses>
      <license>
        <name>MIT License</name>
        <url>http://www.opensource.org/licenses/mit-license.php</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <developerConnection>scm:git:git@github.com:DonutReport/donut.git</developerConnection>
      <url>https://github.com/DonutReport/donut</url>
      <tag>HEAD</tag>
    </scm>
    <developers>
      <developer>
        <id>chdask</id>
        <name>Christina Daskalaki</name>
        <url>https://www.magentys.io</url>
      </developer>
      <developer>
        <id>davebassan</id>
        <name>Dave Bassan</name>
        <url>https://www.magentys.io</url>
      </developer>
      <developer>
        <id>amitsha</id>
        <name>Amit Sharma</name>
        <url>https://www.mechanicalrock.io</url>
      </developer>
    </developers>
  )
