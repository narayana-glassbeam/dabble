name := "Dabble"

organization := "net.ssanj"

version := "0.0.1"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scalaz"       %% "scalaz-core"  % "7.2.2",
  "com.lihaoyi"      %% "ammonite-ops" % "0.5.7",
  "com.github.scopt" %% "scopt"        % "3.4.0",
  "org.scalatest"    %% "scalatest"    % "2.2.4"  % "test",
  "org.scalacheck"   %% "scalacheck"   % "1.12.5" % "test"
)

scalacOptions ++= Seq(
                      "-unchecked",
                      "-deprecation",
                      "-feature",
                      "-Xfatal-warnings",
                      "-Xlint:_",
                      "-Ywarn-dead-code",
                      "-Ywarn-inaccessible",
                      // "-Ywarn-unused-import",
                      "-Ywarn-infer-any",
                      "-Ywarn-nullary-override",
                      "-Ywarn-nullary-unit"
                     )


def isWindows() = System.getProperty("os.name").toLowerCase.startsWith("windows")

assemblyJarName in assembly := {
  val nameLower = name.value.toLowerCase
  if (isWindows) s"${nameLower}.jar" else s"${nameLower}"
}

mainClass in assembly := Some("net.ssanj.dabble.DabbleApp")

import sbtassembly.AssemblyPlugin.defaultShellScript

assemblyOption in assembly := {
  if (isWindows) (assemblyOption in assembly).value.copy(prependShellScript = None)
  else (assemblyOption in assembly).value.copy(prependShellScript = Some(defaultShellScript))
}

