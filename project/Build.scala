import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "play-pac4j-scala-demo"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "org.pac4j" % "play-pac4j" % "1.1.0-SNAPSHOT",
      "org.pac4j" % "pac4j-http" % "1.4.0-SNAPSHOT",
      "org.pac4j" % "pac4j-cas" % "1.4.0-SNAPSHOT",
      "org.pac4j" % "pac4j-openid" % "1.4.0-SNAPSHOT",
      "org.pac4j" % "pac4j-oauth" % "1.4.0-SNAPSHOT"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      resolvers += "Sonatype snapshots repository" at "https://oss.sonatype.org/content/repositories/snapshots/"
    )
}
