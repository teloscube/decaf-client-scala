// Define versions for libraries:
val VersionCats       = "1.4.0"
val VersionCatsEffect = "1.0.0"
val VersionCirce      = "0.10.0"
val VersionSTTP       = "1.3.5"

// Configure the root project:
lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    // Top-level Settings:
    name := "decaf-client-scala-core",
    organization := "com.decafhub",
    scalaVersion := "2.12.13",
    version := "0.0.2-SNAPSHOT",

    // Scalac Options:
    scalacOptions += "-deprecation",

    // BuildInfo Settings:
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.decafhub.decaf.client.buildinfo",

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
