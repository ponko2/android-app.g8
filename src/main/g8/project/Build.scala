import sbt._
import Keys._
import AndroidKeys._

object BuildSettings {
  val buildName         = "$name$"
  val buildOrganization = "$organization$"
  val buildVersion      = "0.1.0"
  val buildScalaVersion = "$scala_version$"
  val buildPlatformName = "android-$api_level$"
  val buildKeyalias     = "$keyalias$"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    name         := buildName,
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion,
    shellPrompt  := ShellPrompt.buildShellPrompt,
    platformName in Android := buildPlatformName
  )

  val proguardSettings = Seq (
    useProguard in Android := $use_proguard$
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
  val scalatestVersion = "$scalatest_version$"

  val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion % "test"
}

object AndroidBuild extends Build {
  import Dependencies._
  import BuildSettings._

  val mainDeps  = Seq(scalatest)
  val testsDeps = Seq(scalatest)

  lazy val main = Project (
    buildName.replace(" ","-").toLowerCase,
    file("."),
    settings = buildSettings ++
      androidSettings ++
      Seq(libraryDependencies ++= mainDeps)
  )

  lazy val tests = Project (
    "tests",
    file("tests"),
    settings = buildSettings ++
      proguardSettings ++
      AndroidTest.androidSettings ++
      Seq(name := buildName + " Tests", libraryDependencies ++= testsDeps)
  ) dependsOn (main)
}
