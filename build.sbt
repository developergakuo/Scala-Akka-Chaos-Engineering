name := "PerturbationTool"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies += "commons-io" % "commons-io" % "2.6"

unmanagedJars in Compile += file("./jars/PerturbationAspects-assembly-0.1.0-SNAPSHOT.jar")