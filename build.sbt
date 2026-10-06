name := "play-pac4j-scala-demo"

version := "14.0.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "3.9.0"

val playPac4jVersion = "14.0.0-SNAPSHOT"
val pac4jVersion = "6.5.9"
val playVersion = "3.0.12"

libraryDependencies ++= Seq(
  guice,
  ehcache, // or cacheApi
  ws,
  filters,
  specs2 % Test,
  "org.pac4j" %% "play-pac4j" % playPac4jVersion,
  "org.pac4j" % "pac4j-http" % pac4jVersion excludeAll(ExclusionRule(organization = "com.fasterxml.jackson.core")),
  "org.pac4j" % "pac4j-cas" % pac4jVersion exclude("com.fasterxml.jackson.core", "jackson-databind"),
  "org.pac4j" % "pac4j-oauth" % pac4jVersion excludeAll(ExclusionRule(organization = "com.fasterxml.jackson.core")),
  "org.pac4j" % "pac4j-saml" % pac4jVersion excludeAll(ExclusionRule(organization = "com.fasterxml.jackson.core")),
  "org.pac4j" % "pac4j-oidc" % pac4jVersion  excludeAll(ExclusionRule("commons-io" , "commons-io"), ExclusionRule(organization = "com.fasterxml.jackson.core")),
  "org.pac4j" % "pac4j-jwt" % pac4jVersion exclude("commons-io" , "commons-io"),
  "org.pac4j" % "pac4j-ldap" % pac4jVersion excludeAll(ExclusionRule(organization = "com.fasterxml.jackson.core")),
  "org.pac4j" % "pac4j-sql" % pac4jVersion exclude("com.fasterxml.jackson.core", "jackson-databind"),
  "org.pac4j" % "pac4j-mongo" % pac4jVersion excludeAll(ExclusionRule(organization = "com.fasterxml.jackson.core")),
  "org.pac4j" % "pac4j-kerberos" % pac4jVersion exclude("org.springframework", "spring-core"),
  "org.apache.shiro" % "shiro-core" % "1.13.0",
  "ch.qos.logback" % "logback-classic" % "1.6.3",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.22.3.1",
  "org.playframework" %% "play-cache" % playVersion,
  "org.projectlombok" % "lombok" % "1.18.48"
)

resolvers ++= Seq(
  Resolver.mavenLocal,
  "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
  "Sonatype snapshots repository" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Shibboleth releases" at "https://build.shibboleth.net/nexus/content/repositories/releases/",
"Central Portal Snapshots" at "https://central.sonatype.com/repository/maven-snapshots/"
)

routesGenerator := InjectedRoutesGenerator

run / fork := true

ThisBuild / evictionErrorLevel := Level.Info
