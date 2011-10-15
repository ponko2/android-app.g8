import sbt._
import Keys._
import AndroidKeys._

object BuildSettings {
  val buildOrganization = "$organization$"
  val buildVersion      = "0.1.0"
  val buildScalaVersion = "$scala_version$"
  val buildPlatformName = "android-$api_level$"
  val buildKeyalias     = "$keyalias$"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion,
    shellPrompt  := ShellPrompt.buildShellPrompt,
    platformName in Android := buildPlatformName
  )

  val androidSettings = AndroidProject.androidSettings ++
    TypedResources.settings ++
    AndroidMarketPublish.settings ++
    Seq(keyalias in Android := buildKeyalias)
}

object ShellPrompt {
  object devnull extends ProcessLogger {
    def info  (s: => String) {}
    def error (s: => String) {}
    def buffer[T] (f: => T): T = f
  }

  def currBranch = (
    ("git status -sb" lines_! devnull headOption)
      getOrElse "-" stripPrefix "## "
  )

  val buildShellPrompt = {
    (state: State) => {
      val currProject = Project.extract (state).currentProject.id
      "%s:%s:%s> ".format (
        currProject, currBranch, BuildSettings.buildVersion
      )
    }
  }
}

object Dependencies {
  val scalatest = "org.scalatest" %% "scalatest" % "1.6.1" % "test"
}

object AndroidBuild extends Build {
  import Dependencies._
  import BuildSettings._

  val mainDeps  = Seq(scalatest)
  val testsDeps = Seq(scalatest)

  lazy val main = Project (
    "$name$",
    file("."),
    settings = buildSettings ++ BuildSettings.androidSettings ++
      Seq(libraryDependencies ++= mainDeps)
  ) dependsOn (tests)

  lazy val tests = Project (
    "tests",
    file("tests"),
    settings = buildSettings ++ AndroidTest.androidSettings ++
      Seq(libraryDependencies ++= testsDeps)
  )
}
