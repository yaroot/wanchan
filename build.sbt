lazy val root = project
  .in(file("."))
  .aggregate(ethereum)

val CatsCoreVersion     = "2.8.0"
val CirceVersion        = "0.14.2"
val MUnitVersion        = "0.7.29"
val ScodecVersion = "1.1.34"

// val commonWarts = Seq(
//   Wart.AsInstanceOf,
//   Wart.EitherProjectionPartial,
//   Wart.Null,
//   Wart.OptionPartial,
//   Wart.Product,
//   Wart.Return,
//   // Wart.TraversableOps,
//   Wart.TryPartial,
//   Wart.Var
// )

lazy val commonSettings = Seq(
  // organization                      := "",
  scalaVersion                      := "3.1.3",
  run / fork                        := true,
  // addCompilerPlugin("org.typelevel" % "kind-projector"     % "0.13.2" cross CrossVersion.full),
  // addCompilerPlugin("com.olegpy"   %% "better-monadic-for" % "0.3.1" cross CrossVersion.binary),
  // scalacOptions += "-Ymacro-annotations",
  // scalacOptions += "-Xsource:3",
  testFrameworks += new TestFramework("munit.Framework"),
  scalafmtOnCompile                 := true,
  Global / cancelable               := true,
  javaOptions ++= Seq(
    "-XX:+UseG1GC",
    "-Xmx600m",
    "-Xms600m",
    "-XX:SurvivorRatio=8",
    "-Duser.timezone=UTC"
  ),
  // compile / wartremoverErrors       := commonWarts,
  // Compile / wartremoverErrors       := commonWarts,
  // Compile / console / scalacOptions := (console / scalacOptions).value.filterNot(_.contains("wartremover")),
  version ~= (_.replace('+', '-')),
  dynver ~= (_.replace('+', '-'))
)


lazy val ethereum = project
  .in(file("ethereum"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
    "org.scodec" %% "scodec-bits" % ScodecVersion,
      "io.circe"      %% "circe-generic" % CirceVersion,
      "org.typelevel" %% "cats-core"     % CatsCoreVersion,
      "org.scalameta" %% "munit"         % MUnitVersion % Test
    )
  )

