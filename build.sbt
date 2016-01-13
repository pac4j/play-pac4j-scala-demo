name := "play-pac4j-scala-demo"

version := "2.0.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.pac4j" % "play-pac4j" % "2.1.0-SNAPSHOT",
  "org.pac4j" % "pac4j-http" % "1.8.3",
  "org.pac4j" % "pac4j-cas" % "1.8.3",
  "org.pac4j" % "pac4j-openid" % "1.8.3",
  "org.pac4j" % "pac4j-oauth" % "1.8.3",
  "org.pac4j" % "pac4j-saml" % "1.8.3",
  "org.pac4j" % "pac4j-oidc" % "1.8.3",
  "org.pac4j" % "pac4j-gae" % "1.8.3",
  "org.pac4j" % "pac4j-jwt" % "1.8.3",
  "org.pac4j" % "pac4j-ldap" % "1.8.3",
  "org.pac4j" % "pac4j-sql" % "1.8.3",
  "org.pac4j" % "pac4j-mongo" % "1.8.3",
  "org.pac4j" % "pac4j-stormpath" % "1.8.3",
  "com.typesafe.play" %  "play-cache_2.11"      % "2.4.0"
)

// resolvers := Seq(Resolver.mavenLocal)

resolvers ++= Seq( Resolver.mavenLocal,
                "Sonatype snapshots repository" at "https://oss.sonatype.org/content/repositories/snapshots/",
                "Pablo repo" at "https://raw.github.com/fernandezpablo85/scribe-java/mvn-repo/")

routesGenerator := InjectedRoutesGenerator
