import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "play-pac4j-scala-demo"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
//      for play 2.0 :
      "org.pac4j" % "play-pac4j_scala2.9" % "1.1.3",
      "org.pac4j" % "pac4j-http" % "1.5.1",
      "org.pac4j" % "pac4j-cas" % "1.5.1",
      "org.pac4j" % "pac4j-openid" % "1.5.1",
      "org.pac4j" % "pac4j-oauth" % "1.5.1",
      "org.pac4j" % "pac4j-saml" % "1.5.1"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      resolvers ++= Seq("Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
                "Sonatype snapshots repository" at "https://oss.sonatype.org/content/repositories/snapshots/")
    )
}
