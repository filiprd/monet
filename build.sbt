ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := "3.4.2"

ThisBuild / semanticdbEnabled := true

val monixNewtypesVersion = "0.2.3"
val http4sVersion        = "0.23.11"
val circeVersion         = "0.14.9"
val skunkVersion         = "0.6.0"
val redis4CatsVersion    = "1.4.1"
val pureconfigVersion    = "0.17.4"
val weaverVersion        = "0.8.3"

resolvers ++= Resolver.sonatypeOssRepos("snapshots")

lazy val root = (project in file("."))
  .disablePlugins(RevolverPlugin)
  .settings(
    name := "monet"
  )
  .aggregate(core, tests)

lazy val core = (project in file("core"))
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings(
    name := "monet-core",
    scalacOptions ++= Seq("-language:higherKinds", "-Wunused:all", "-no-indent"),
    scalafmtOnCompile := true,
    Defaults.itSettings,
    Docker / packageName := "monet",
    dockerBaseImage      := "openjdk:11-jre-slim-buster",
    Universal / mappings += file("core/application.conf") -> "/application.conf",
    dockerUpdateLatest                                    := true,
    makeBatScripts                                        := Seq(),
    libraryDependencies ++= Seq(
      "io.monix"              %% "newtypes-core"             % monixNewtypesVersion,
      "io.monix"              %% "newtypes-circe-v0-14"      % monixNewtypesVersion,
      "io.monix"              %% "newtypes-pureconfig-v0-17" % monixNewtypesVersion,
      "org.typelevel"         %% "cats-core"                 % "2.9.0",
      "org.typelevel"         %% "kittens"                   % "3.0.0",
      "com.github.cb372"      %% "cats-retry"                % "3.1.0",
      "org.typelevel"         %% "cats-effect"               % "3.5.0",
      "org.http4s"            %% "http4s-dsl"                % http4sVersion,
      "org.http4s"            %% "http4s-ember-server"       % http4sVersion,
      "org.http4s"            %% "http4s-ember-client"       % http4sVersion,
      "org.http4s"            %% "http4s-circe"              % http4sVersion,
      "co.fs2"                %% "fs2-core"                  % "3.6.0",
      "dev.profunktor"        %% "http4s-jwt-auth"           % "1.2.0",
      "io.circe"              %% "circe-core"                % circeVersion,
      "io.circe"              %% "circe-generic"             % circeVersion,
      "io.circe"              %% "circe-parser"              % circeVersion,
      "io.circe"              %% "circe-literal"             % circeVersion,
      "dev.optics"            %% "monocle-core"              % "3.1.0",
      "org.tpolecat"          %% "skunk-core"                % skunkVersion,
      "org.tpolecat"          %% "skunk-circe"               % skunkVersion,
      "dev.profunktor"        %% "redis4cats-effects"        % redis4CatsVersion,
      "dev.profunktor"        %% "redis4cats-streams"        % redis4CatsVersion,
      "dev.profunktor"        %% "redis4cats-log4cats"       % redis4CatsVersion,
      "com.github.pureconfig" %% "pureconfig-core"           % pureconfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect"    % pureconfigVersion,
      "com.github.pureconfig" %% "pureconfig-ip4s"           % pureconfigVersion,
      "org.typelevel"         %% "log4cats-slf4j"            % "2.7.0",
      "ch.qos.logback"         % "logback-classic"           % "1.5.6" % Runtime
    )
  )

lazy val tests = (project in file("tests"))
  .disablePlugins(RevolverPlugin)
  .configs(IntegrationTest)
  .settings(
    name := "monet-tests",
    scalacOptions ++= Seq("-language:higherKinds", "-Wunused:all", "-no-indent"),
    scalafmtOnCompile := true,
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      "com.disneystreaming" %% "weaver-cats"         % weaverVersion,
      "com.disneystreaming" %% "weaver-discipline"   % weaverVersion,
      "com.disneystreaming" %% "weaver-scalacheck"   % weaverVersion,
      "org.typelevel"       %% "log4cats-noop"       % "2.6.0",
      "org.typelevel"       %% "cats-laws"           % "2.9.0",
      "dev.optics"          %% "monocle-law"         % "3.2.0",
      "org.typelevel"       %% "jawn-fs2"            % "2.4.0",
      "org.gnieh"           %% "fs2-data-json-circe" % "1.11.1" % Test
    )
  )
  .dependsOn(core)

addCommandAlias("runLinter", ";scalafixAll --rules OrganizeImports")
