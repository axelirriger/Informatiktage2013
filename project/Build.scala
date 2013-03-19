import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "informatiktage2013"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
	javaCore, javaJdbc, jdbc, javaEbean,
	"org.mongodb" % "mongo-java-driver" % "2.10.1"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
    )

}
