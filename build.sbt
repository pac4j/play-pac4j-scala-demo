name := "play-pac4j-scala-demo"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  cache,
  "org.pac4j" % "play-pac4j_scala2.10" % "1.1.2-SNAPSHOT",
  "org.pac4j" % "pac4j-core" % "1.4.2-SNAPSHOT",
  "org.pac4j" % "pac4j-http" % "1.4.2-SNAPSHOT",
  "org.pac4j" % "pac4j-cas" % "1.4.2-SNAPSHOT",
  "org.pac4j" % "pac4j-openid" % "1.4.2-SNAPSHOT",
  "org.pac4j" % "pac4j-oauth" % "1.4.2-SNAPSHOT"
)

resolvers ++= Seq(
    "Sonatype snapshots repository" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

play.Project.playScalaSettings

