import play.sbt.routes.RoutesKeys
import wartremover.contrib.ContribWart
import wartremover.{Wart, Warts => Wartz}
RoutesKeys.routesImport -= "controllers.Assets.Asset" //workaround for routes unused imports


name := "play-skeleton"
version := "0.1"

scalaVersion := "2.13.1"

val circeVersion                = "0.13.0"
val circeDerivationVersion      = "0.13.0-M1"

val enumeratumVersion           = "1.5.15"
val enumeratumCirceVersion      = "1.5.23"

val silencerVersion             = "1.6.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)


libraryDependencies ++= Seq(
  ws,
  "com.beachape"                    %% "enumeratum"           % enumeratumVersion,
  "com.beachape"                    %% "enumeratum-circe"     % enumeratumCirceVersion,
  "com.dripower"                    %% "play-circe"           % "2812.0",
  "com.github.pureconfig"           %% "pureconfig"           % "0.12.2",

  compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
  "com.github.ghik"        % "silencer-lib"                      % silencerVersion % Provided cross CrossVersion.full,

  "io.circe"                        %% "circe-core"           % circeVersion,
  "io.circe"                        %% "circe-derivation"     % circeDerivationVersion,
  "io.circe"                        %% "circe-generic-extras" % circeVersion,
  "io.circe"                        %% "circe-parser"         % circeVersion,

  "org.typelevel"                   %% "cats-core"            % "2.1.1",
  //TEST
  "com.danielasfregola"        %% "random-data-generator"       % "2.8"    % Test,
  "com.47deg"                  %% "scalacheck-toolbox-datetime" % "0.3.1"  % Test,
  "org.scalatestplus.play"     %% "scalatestplus-play"          % "5.0.0"  % Test,
  "org.mockito"                %% "mockito-scala-scalatest"     % "1.11.2" % Test,
  "com.github.alexarchambault" %% "scalacheck-shapeless_1.14"   % "1.2.4"  % Test
)

excludeDependencies ++= Seq(
  "com.typesafe"     %% "npm",
  "com.typesafe.sbt" % "sbt-web",
  "org.webjars"      % "webjars-locator-core"
)

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)

fork in Test := false

parallelExecution in Test := false

testOptions in Test +=
  Tests.Setup(() => sys.props += "logger.resource" -> "logback.xml")


val wartsExcludes = Seq(
  Wart.Any, // circe encoders/ decoders , mapN , traverse : are failing
  Wart.StringPlusAny, // String interpolation should work
  Wart.DefaultArguments,
  Wart.IsInstanceOf, //why isInstanceOf shouldn't be used?\ asInstanceOf shouldn't be used.
  Wart.Null, //gives a false positive in overloaded methods in logger. splease don't use nulls
  Wart.Nothing, // many false positives
  Wart.ImplicitParameter, // I need implicit parameters. ex. executioncontext
  Wart.PublicInference, // many false positives
  Wart.ToString, // case classes do not need to override string
  Wart.Overloading
)

val wartsContribExcludes = Seq( //from wartremover-contrib , need to be added one by one
  ContribWart.Apply, //many false positives in circe encoders/ decoders
  ContribWart.NoNeedForMonad, //https://github.com/wartremover/wartremover-contrib/issues/34
  ContribWart.UnsafeInheritance, // for this I need major refactoring
)

wartremoverErrors ++=  Wartz.allBut(wartsExcludes ++ wartsContribExcludes : _*)

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8", // Specify character encoding used by source files.
  "-explaintypes", // Explain type errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds", // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked",  // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
  "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
  "-Xlint:option-implicit", // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports",   // Warn if an import selector is not referenced.
  // - imports  // play framework limitation , maybe fixed in future version
  "-Ywarn-unused:locals", // Warn if a local definition is unused.
  "-Ywarn-unused:params", // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates", // Warn if a private member is unused.
  //  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
  // not necessary since catched by wartremover and can be more fine-tuned,
  // I am using silencer because of
  // silence all warnings on autogenerated files
  "-P:silencer:pathFilters=target/.*",
  // Make sure you only exclude warnings for the project directories, i.e. make builds reproducible
  s"-P:silencer:sourceRoots=${baseDirectory.value.getCanonicalPath}",
  // https://github.com/playframework/playframework/issues/6302
  //https://github.com/playframework/playframework/issues/7382
)

// since https://github.com/danielnixon/sbt-ignore-play-generated/issues/2
// I need to do it manually
wartremoverExcluded ++= routes.in(Compile).value

sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false

Global / onChangedBuildSource := ReloadOnSourceChanges
