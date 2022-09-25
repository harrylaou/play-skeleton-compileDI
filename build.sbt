name    := "play-skeleton"
version := "0.1"

scalaVersion := "2.13.9"

lazy val root = (project in file(".")).enablePlugins(PlayScala).disablePlugins(PlayLayoutPlugin)

libraryDependencies ++= Seq(
  ws,
  "com.beachape"               %% "enumeratum"           % "1.7.0",
  "com.beachape"               %% "enumeratum-circe"     % "1.7.0",
  "com.dripower"               %% "play-circe"           % "2814.2",
  "com.github.pureconfig"      %% "pureconfig"           % "0.17.1",
  "com.typesafe.scala-logging" %% "scala-logging"        % "3.9.5",
  "dev.zio"                    %% "zio"                  % "2.0.2",
  "io.circe"                   %% "circe-core"           % "0.14.3",
  "io.circe"                   %% "circe-generic-extras" % "0.14.2",
  "io.circe"                   %% "circe-parser"         % "0.14.3",
  "org.typelevel"              %% "cats-core"            % "2.8.0",
  // TEST
  "org.scalatestplus.play"     %% "scalatestplus-play"   % "5.1.0" % Test
)

excludeDependencies ++= Seq(
  "com.typesafe"    %% "npm",
  "com.typesafe.sbt" % "sbt-web",
  "org.webjars"      % "webjars-locator-core"
)

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full)

Test / fork := false

Test / parallelExecution := false

Test / testOptions +=
  Tests.Setup(() => sys.props += "logger.resource" -> "logback.xml")

scalacOptions ++= Seq(
  "-Xsource:3",                    // Enable Scala 3 syntax in Scala 2, option is intended to encourage early migration.
  "-deprecation",                  // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8",                         // Specify character encoding used by source files.
  "-explaintypes",                 // Explain type errors in more detail.
  "-feature",                      // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials",        // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds",         // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked",                    // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit",                   // Wrap field accessors to throw an exception on uninitialized access.
  s"-Wconf:src=${target.value}/.*:s",
  // https://alexn.org/blog/2020/05/26/scala-fatal-warnings.html
  // https://github.com/playframework/playframework/issues/6302
  // https://github.com/playframework/playframework/issues/7382
  "-Xfatal-warnings",              // Fail the compilation if there are any warnings.
  "-Xlint:adapted-args",           // Warn if an argument list is modified to match the receiver.
  "-Xlint:constant",               // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",     // Selecting member of DelayedInit.
  "-Xlint:doc-detached",           // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",           // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",              // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",   // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-unit",           // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",        // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",         // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",            // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",  // A local type parameter shadows a type already in scope.
  "-Ywarn-dead-code",              // Warn when dead code is identified.
  "-Ywarn-extra-implicit",         // Warn when more than one implicit parameter section is defined.
  "-Ywarn-numeric-widen",          // Warn when numerics are widened.
  "-Ywarn-unused:implicits",       // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports",         // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals",          // Warn if a local definition is unused.
  "-Ywarn-unused:params",          // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars",         // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates",        // Warn if a private member is unused.
  "-Ywarn-value-discard"
  //  "-Ylog-classpath",
)

Compile / doc / sources                := Seq.empty
Compile / packageDoc / publishArtifact := false

Global / onChangedBuildSource := ReloadOnSourceChanges
