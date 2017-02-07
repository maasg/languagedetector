name := "languagedetector"

version := "1.0"
val build = "need new build"

scalaVersion := "2.11.8"
val sparkVersion = "2.1.0"

//spark
libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion % "provided"
libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion % "provided"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided"

libraryDependencies += "com.typesafe" % "config" % "1.3.1"

// https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12
libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.22"

libraryDependencies += "org.apache.commons" % "commons-io" % "1.3.2"

// scallop command line arg parsing
libraryDependencies += "org.rogach" %% "scallop" % "2.0.6"

//nlp - stanford
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0"
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0" classifier "models"

// language detection
libraryDependencies += "com.optimaize.languagedetector" % "language-detector" % "0.6"

// https://mvnrepository.com/artifact/com.google.code.gson/gson
libraryDependencies += "com.google.code.gson" % "gson" % "1.7.1"

// http client
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0"

libraryDependencies += "org.reflections" % "reflections" % "0.9.10"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % Test

javaOptions in Test += "-Dconfig.resource=test.conf"

parallelExecution in test := false // parallel execution doesnt work with spark contexts (one per jvm)

//sbt-assembly settings =======================
test in assembly := {} //don't execute tests during assembly

assemblyMergeStrategy in assembly := {
  case m if m.startsWith("META-INF") => MergeStrategy.discard
  case _ => MergeStrategy.first
}

// don't include scala libs
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
//sbt-assembly settings =======================
