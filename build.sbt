// Define versions for libraries:
val VersionCats       = "1.2.0"
val VersionCatsEffect = "1.0.0-RC3"
val VersionCirce      = "0.9.3"
val VersionSTTP       = "1.3.0"


// Configure the root project:
lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    // Top-level Settings:
    name := "barista-client-core",
    organization := "com.vsthost.rnd",
    scalaVersion := "2.12.6",
    version := "0.0.2-SNAPSHOT",

    // Scalac Options:
    scalacOptions += "-deprecation",

    // BuildInfo Settings:
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.vsthost.rnd.barista.client.core",

    // Libraries:
    libraryDependencies ++= Seq(
      "org.typelevel"         %% "cats-core"     % VersionCats,
      "org.typelevel"         %% "cats-effect"   % VersionCatsEffect,
      "io.circe"              %% "circe-core"    % VersionCirce,
      "io.circe"              %% "circe-generic" % VersionCirce,
      "com.softwaremill.sttp" %% "core"          % VersionSTTP,
      "com.softwaremill.sttp" %% "circe"         % VersionSTTP,
    )
  )
