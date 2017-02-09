package biz.meetmatch.util

import java.io.File
import java.lang.reflect.{Member, Modifier}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import biz.meetmatch.modules.Module
import org.apache.commons.io.FileUtils
import org.reflections.Reflections
import org.reflections.scanners.{MemberUsageScanner, MethodParameterNamesScanner, MethodParameterScanner, SubTypesScanner}
import org.reflections.util.{ClasspathHelper, ConfigurationBuilder}

import scala.collection.JavaConverters._
import scala.collection.mutable

object DataDependencyPrinter {
  val modulesPkg = "biz.meetmatch.modules"
  val reflections = new Reflections(
    new ConfigurationBuilder()
      .setUrls(ClasspathHelper.forPackage(modulesPkg))
      .setScanners(
        new SubTypesScanner(false),
        new MethodParameterScanner(),
        new MethodParameterNamesScanner(),
        new MemberUsageScanner()
      )
  )

  def main(args: Array[String]): Unit = {
    printModuleDependenciesForNeo4j()
  }

  def isAbstractClass(clas: Class[_]): Boolean = Modifier.isAbstract(clas.getModifiers)

  def getModules: mutable.Set[Class[_ <: Module]] = {
    reflections
      .getSubTypesOf(classOf[Module])
      .asScala
      .filter(_.getPackage.getName == modulesPkg)
  }

  def getAllConcreteSubClasses(clas: Class[_]): Array[Class[_]] = {
    reflections.getSubTypesOf(clas)
      .asScala
      .toArray
      .flatMap { subClass =>
        (if (isAbstractClass(subClass)) Array() else Array(subClass)) ++ getAllConcreteSubClasses(subClass)
      }
  }

  def getModuleDependencies: Map[String, Set[String]] = {
    val dependencyMethodNames = Array("loadResultsFromParquet", "getResultsFileLocation")

    getModules
      .flatMap { module =>
        val modules = (if (isAbstractClass(module)) Array() else Array(module)) ++ getAllConcreteSubClasses(module)

        module.getMethods
          .filter(m => dependencyMethodNames.contains(m.getName))
          .map { method =>
            reflections
              .getMethodUsage(method)
              .asScala
              .toSet[Member]
              .map(_.getDeclaringClass)
              .flatMap(dependentModule => (if (isAbstractClass(dependentModule)) Array() else Array(dependentModule)) ++ getAllConcreteSubClasses(dependentModule))
              .flatMap { dependentModule =>
                if (dependentModule.getPackage.getName == modulesPkg)
                  Some(formatModuleName(dependentModule.getName))
                else
                  None
              }
          }
          .flatMap { dependentModules =>
            modules.map { module =>
              val moduleName = formatModuleName(module.getName)

              (moduleName, dependentModules.filter(_ != moduleName))
            }
          }
          .distinct
      }
      .toMap
  }

  def saveModuleDependenciesToJson(): Unit = {
    val nodes = getModules
      .filterNot(isAbstractClass)
      .map { module =>
        val moduleName = formatModuleName(module.getName)
        "{data: {id:'" + moduleName.toLowerCase + "', label: '" + moduleName.replaceAll("(.)([A-Z])", "$1 $2") + "'}}"
      }
      .mkString(",\n")

    val edges = getModuleDependencies.map { case (module, deps) =>
      deps
        .map(dep => "{data: {source:'" + dep.toLowerCase + "', target: '" + module.toLowerCase + "'}}")
        .mkString(",\n")
    }

    val json = s"{nodes: [ $nodes ], edges: [ $edges ]"

    val path = Utils.getConfig("spark.content") + "/text/data-dependencies.json"
    val file = new File(path)

    FileUtils.forceMkdir(file.getParentFile)

    if (file.exists)
      FileUtils.forceDelete(file)

    Files.write(Paths.get(path), json.getBytes(StandardCharsets.UTF_8))
    println("The data dependencies were exported to " + path)
  }

  def printModuleDependenciesForNeo4j(): Unit = {
    getModules
      .filterNot(isAbstractClass)
      .foreach { module =>
        val moduleName = formatModuleName(module.getName)
        println("MERGE (" + moduleName.toLowerCase + ":Module{name: '" + moduleName + "', label: '" + moduleName.replaceAll("(.)([A-Z])", "$1 $2") + "'})")
      }

    getModuleDependencies.foreach { case (module, deps) =>
      deps
        .foreach { dep =>
          println("MERGE (" + dep.toLowerCase + ")-[:USES_RESULTS_FROM]->(" + module.toLowerCase + ")")
        }
    }

    println()
    println("run the following command separately to visualize the nodes and relations:")
    println("MATCH (n) RETURN n")
  }

  private def formatModuleName(moduleName: String): String = moduleName.replace(modulesPkg + ".", "").split("\\$\\$").head.replace("$", "")
}
