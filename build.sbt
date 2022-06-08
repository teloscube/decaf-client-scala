// Define versions for libraries:
val VersionCats       = "1.4.0"
val VersionCatsEffect = "1.0.0"
val VersionCirce      = "0.10.0"
val VersionSTTP       = "1.3.5"

// Configure the root project:
lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "decaf-client-scala",
    version := "0.0.3-SNAPSHOT",
    description := "DECAF API Client for Scala.",
    homepage := Some(
      url(
        "https://github.com/teloscube/decaf-client-scala",
      ),
    ),
    licenses := List(
      "Apache 2" -> new URL(
        "http://www.apache.org/licenses/LICENSE-2.0.txt",
      ),
    ),
    organization := "com.decafhub",
    organizationName := "decafhub",
    organizationHomepage := Some(
      url("https://decafhub.com"),
    ),
    scmInfo := Some(
      ScmInfo(
        url(
          "https://github.com/teloscube/decaf-client-scala",
        ),
        "scm:git@github.com:teloscube/decaf-client-scala.git",
      ),
    ),
    developers := List(
      Developer(
        id = "vst",
        name = "Vehbi Sinan Tunalioglu",
        email = "vst@vsthost.com",
        url = url("https://github.com/vst"),
      ),
    ),
    scalaVersion := "2.12.15",
    scalacOptions += "-deprecation",
    buildInfoKeys := Seq[BuildInfoKey](
      name,
      version,
      scalaVersion,
      sbtVersion,
    ),
    buildInfoPackage := "com.decafhub.decaf.client.buildinfo",
    libraryDependencies ++= Seq(
      "org.typelevel"         %% "cats-core"     % VersionCats,
      "org.typelevel"         %% "cats-effect"   % VersionCatsEffect,
      "io.circe"              %% "circe-core"    % VersionCirce,
      "io.circe"              %% "circe-generic" % VersionCirce,
      "com.softwaremill.sttp" %% "core"          % VersionSTTP,
      "com.softwaremill.sttp" %% "circe"         % VersionSTTP,
    ),
    pomIncludeRepository := { _ => false },
    publishTo := {
      val nexus = "https://s01.oss.sonatype.org/"
      if (isSnapshot.value)
        Some(
          "snapshots" at nexus + "content/repositories/snapshots",
        )
      else
        Some(
          "releases" at nexus + "service/local/staging/deploy/maven2",
        )
    },
    publishMavenStyle := true,
  )
