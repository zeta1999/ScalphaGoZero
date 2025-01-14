organization := "org.deeplearning4j"
name := "ScalphaGoZero"
version := "1.0.1"
description := "An independent implementation of DeepMind's AlphaGoZero in Scala, using Deeplearning4J (DL4J)"
licenses += "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")

scalafmtOnCompile := true
connectInput in run := true

scalaVersion := "2.12.6"

mainClass in assembly := Some("org.deeplearning4j.scalphagozero.app.GtpClient")
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case x => MergeStrategy.first
}

resolvers += Resolver.jcenterRepo

// Use this if no GPU
lazy val dl4j = ((version: String) => Seq(
  "org.nd4j" % "nd4j-native-platform" % version,
  "org.deeplearning4j" % "deeplearning4j-core" % version
))("1.0.0-beta3")  // or beta6?

// use this if you have GPU with CUDA support.
// Check your CUDA version with nvcc --version. Supported versions are 9.0, 9.2, 10.0, 10.1, 10.2
//lazy val dl4j =  ((version: String) => Seq(
//  "org.nd4j" % "nd4j-cuda-10.2-platform" % version,
//  "org.deeplearning4j" % "deeplearning4j-core" % version,
//  "org.bytedeco" % "cuda-platform-redist" % "10.2-7.6-1.5.2"
//))("1.0.0-beta6")

// This is so we can compute the size of objects at runtime
enablePlugins(JavaAgent)
javaAgents += "org.spire-math" % "clouseau_2.12" % "0.2.2" % "compile;runtime"

val main = Some("org.deeplearning4j.scalphagozero.app.ScalphaGoZero")
mainClass in (Compile, run) := main
mainClass in assembly := main

classpathTypes += "maven-plugin"
libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
  "org.li-soft.gonector" % "gonector" % "1.1.1", // for GTP support
  "org.spire-math" % "clouseau_2.12" % "0.2.2", // for ObjectSizer
  "org.scalatest" %% "scalatest" % "3.1.0" % Test
) ++ dl4j

fork in run := true
javaOptions in run ++= Seq(
  "-Xms2G",
  "-Xmx8G",
  "-Dorg.bytedeco.javacpp.maxbytes=2G",
  "-Dorg.bytedeco.javacpp.maxphysicalbytes=11G", // Xmx + maxbytes + eps
  "-XshowSettings:vm",
)

pomExtra :=
  <url>https://github.com/maxpumperla/ScalphaGoZero</url>
    <scm>
      <url>git@github.com:maxpumperla/ScalphaGoZero.git</url>
      <connection>scm:git:git@github.com:maxpumperla/ScalphaGoZero.git</connection>
    </scm>
    <developers>
      <developer>
        <id>maxpumperla</id>
        <name>Max Pumperla</name>
      </developer>
    </developers>

