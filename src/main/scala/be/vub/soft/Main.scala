package be.vub.soft

import java.io.{File, FileInputStream, FileWriter, ObjectInputStream}
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.Base64

import be.vub.soft.parser.JSONParser
import be.vub.soft.perturbation.analysis.{PerturbationAnalyzer, StaticAnalyzer}
import be.vub.soft.reporter.DiscoveredTest
import be.vub.soft.tracer.{PerturbationDelay, PerturbationDrop, PerturbationKill, TestReport}
import org.apache.commons.io.FileUtils

import scala.io.Source
import scala.sys.process.{Process, ProcessLogger}

/*
    Idea: eg trading system/webserver/ecommerce starts many actors, but tests only use a part of them -> optimize by only killing those

    Potential bugs this tool should detect:
        https://doc.akka.io/docs/akka/current/actors.html#initialization-via-message-passing
        https://doc.akka.io/docs/akka/2.5/general/message-delivery-reliability.html
        https://github.com/royrusso/akka-java-examples


    TODO: store the final java command in the index -> speed up because no need for starting sbt and plugin!
 */
object Main {

    def main(args: Array[String]): Unit =
    {
        /*
        val cl = ProcessLogger(line => println(line), line => println(line))
        val cmd = List("java", "-javaagent:/Users/jonas/.ivy2/cache/org.aspectj/aspectjweaver/jars/aspectjweaver-1.8.10.jar", "-classpath",
            "/Users/jonas/Downloads/webShop/target/scala-2.12/classes:/Users/jonas/Downloads/webShop/lib/sbt-aspectj.jar:/Users/jonas/Downloads/webShop/lib/PerturbationAspects-assembly-0.1.0-SNAPSHOT.jar:/Users/jonas/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.12.8.jar:/Users/jonas/.ivy2/cache/org.aspectj/aspectjrt/jars/aspectjrt-1.8.10.jar:/Users/jonas/.ivy2/cache/com.github.dnvriend/akka-persistence-inmemory_2.12/jars/akka-persistence-inmemory_2.12-2.5.15.1.jar:/Users/jonas/.ivy2/cache/com.typesafe.akka/akka-actor_2.12/jars/akka-actor_2.12-2.5.15.jar:/Users/jonas/.ivy2/cache/com.typesafe/config/bundles/config-1.3.3.jar:/Users/jonas/.ivy2/cache/org.scala-lang.modules/scala-java8-compat_2.12/bundles/scala-java8-compat_2.12-0.8.0.jar:/Users/jonas/.ivy2/cache/com.typesafe.akka/akka-persistence_2.12/jars/akka-persistence_2.12-2.5.15.jar:/Users/jonas/.ivy2/cache/com.typesafe.akka/akka-protobuf_2.12/jars/akka-protobuf_2.12-2.5.15.jar:/Users/jonas/.ivy2/cache/com.typesafe.akka/akka-persistence-query_2.12/jars/akka-persistence-query_2.12-2.5.15.jar:/Users/jonas/.ivy2/cache/com.typesafe.akka/akka-stream_2.12/jars/akka-stream_2.12-2.5.15.jar:/Users/jonas/.ivy2/cache/org.reactivestreams/reactive-streams/jars/reactive-streams-1.0.2.jar:/Users/jonas/.ivy2/cache/com.typesafe/ssl-config-core_2.12/bundles/ssl-config-core_2.12-0.2.4.jar:/Users/jonas/.ivy2/cache/org.scala-lang.modules/scala-parser-combinators_2.12/bundles/scala-parser-combinators_2.12-1.1.1.jar:/Users/jonas/.ivy2/cache/org.scalaz/scalaz-core_2.12/bundles/scalaz-core_2.12-7.2.25.jar:/Users/jonas/.ivy2/cache/org.json4s/json4s-native_2.12/jars/json4s-native_2.12-3.2.11.jar:/Users/jonas/.ivy2/cache/org.json4s/json4s-core_2.12/jars/json4s-core_2.12-3.2.11.jar:/Users/jonas/.ivy2/cache/org.json4s/json4s-ast_2.12/jars/json4s-ast_2.12-3.2.11.jar:/Users/jonas/.ivy2/cache/com.thoughtworks.paranamer/paranamer/jars/paranamer-2.6.jar:/Users/jonas/.ivy2/cache/org.scala-lang/scalap/jars/scalap-2.12.8.jar:/Users/jonas/.ivy2/cache/org.scala-lang/scala-compiler/jars/scala-compiler-2.12.8.jar:/Users/jonas/.ivy2/cache/org.scala-lang/scala-reflect/jars/scala-reflect-2.12.8.jar:/Users/jonas/.ivy2/cache/org.scala-lang.modules/scala-xml_2.12/bundles/scala-xml_2.12-1.0.6.jar:/Users/jonas/.ivy2/cache/org.json4s/json4s-jackson_2.12/jars/json4s-jackson_2.12-3.2.11.jar:/Users/jonas/.ivy2/cache/com.fasterxml.jackson.core/jackson-databind/bundles/jackson-databind-2.3.1.jar:/Users/jonas/.ivy2/cache/com.fasterxml.jackson.core/jackson-annotations/bundles/jackson-annotations-2.3.0.jar:/Users/jonas/.ivy2/cache/com.fasterxml.jackson.core/jackson-core/bundles/jackson-core-2.3.1.jar:/Users/jonas/.ivy2/cache/org.scalatest/scalatest_2.12/bundles/scalatest_2.12-3.0.5.jar:/Users/jonas/.ivy2/cache/org.scalactic/scalactic_2.12/bundles/scalactic_2.12-3.0.5.jar:/Users/jonas/.ivy2/cache/org.scalameta/scalameta_2.12/jars/scalameta_2.12-4.1.0.jar:/Users/jonas/.ivy2/cache/org.scalameta/common_2.12/jars/common_2.12-4.1.0.jar:/Users/jonas/.ivy2/cache/org.scalameta/semanticdb_2.12/jars/semanticdb_2.12-4.1.0.jar:/Users/jonas/.ivy2/cache/com.thesamet.scalapb/scalapb-runtime_2.12/jars/scalapb-runtime_2.12-0.8.0-RC1.jar:/Users/jonas/.ivy2/cache/com.thesamet.scalapb/lenses_2.12/jars/lenses_2.12-0.8.0-RC1.jar:/Users/jonas/.ivy2/cache/com.lihaoyi/fastparse_2.12/jars/fastparse_2.12-1.0.0.jar:/Users/jonas/.ivy2/cache/com.lihaoyi/fastparse-utils_2.12/jars/fastparse-utils_2.12-1.0.0.jar:/Users/jonas/.ivy2/cache/com.lihaoyi/sourcecode_2.12/bundles/sourcecode_2.12-0.1.4.jar:/Users/jonas/.ivy2/cache/com.google.protobuf/protobuf-java/bundles/protobuf-java-3.5.1.jar:/Users/jonas/.ivy2/cache/org.scalameta/dialects_2.12/jars/dialects_2.12-4.1.0.jar:/Users/jonas/.ivy2/cache/org.scalameta/parsers_2.12/jars/parsers_2.12-4.1.0.jar:/Users/jonas/.ivy2/cache/org.scalameta/inputs_2.12/jars/inputs_2.12-4.1.0.jar:/Users/jonas/.ivy2/cache/org.scalameta/io_2.12/jars/io_2.12-4.1.0.jar:/Users/jonas/.ivy2/cache/org.scalameta/tokens_2.12/jars/tokens_2.12-4.1.0.jar:/Users/jonas/.ivy2/cache/org.scalameta/tokenizers_2.12/jars/tokenizers_2.12-4.1.0.jar:/Users/jonas/.ivy2/cache/org.scalameta/fastparse_2.12/jars/fastparse_2.12-1.0.0.jar:/Users/jonas/.ivy2/cache/org.scalameta/fastparse-utils_2.12/jars/fastparse-utils_2.12-1.0.0.jar:/Users/jonas/.ivy2/cache/org.scalameta/trees_2.12/jars/trees_2.12-4.1.0.jar:/Users/jonas/.ivy2/cache/org.scalameta/quasiquotes_2.12/jars/quasiquotes_2.12-4.1.0.jar:/Users/jonas/.ivy2/cache/org.scalameta/transversers_2.12/jars/transversers_2.12-4.1.0.jar:/Users/jonas/.ivy2/cache/org.scalameta/symtab_2.12/jars/symtab_2.12-4.1.0.jar:/Users/jonas/.ivy2/cache/org.scalameta/metacp_2.12/jars/metacp_2.12-4.1.0.jar:/Users/jonas/.ivy2/cache/org.scalameta/cli_2.12/jars/cli_2.12-4.1.0.jar:/Users/jonas/.ivy2/cache/org.scalameta/contrib_2.12/jars/contrib_2.12-4.1.0.jar:/Users/jonas/.ivy2/cache/org.scalameta/interactive_2.12.7/jars/interactive_2.12.7-4.1.0.jar:/Users/jonas/.ivy2/cache/org.scalameta/semanticdb-scalac-core_2.12.7/jars/semanticdb-scalac-core_2.12.7-4.1.0.jar:/Users/jonas/.ivy2/cache/commons-io/commons-io/jars/commons-io-2.6.jar:/Users/jonas/.ivy2/cache/ch.epfl.scala/scalafix-core_2.12/jars/scalafix-core_2.12-0.9.1.jar:/Users/jonas/.ivy2/cache/org.scalameta/metap_2.12/jars/metap_2.12-4.1.0.jar:/Users/jonas/.ivy2/cache/com.googlecode.java-diff-utils/diffutils/jars/diffutils-1.3.0.jar:/Users/jonas/.ivy2/cache/com.geirsson/metaconfig-typesafe-config_2.12/jars/metaconfig-typesafe-config_2.12-0.9.1.jar:/Users/jonas/.ivy2/cache/com.geirsson/metaconfig-core_2.12/jars/metaconfig-core_2.12-0.9.1.jar:/Users/jonas/.ivy2/cache/com.lihaoyi/pprint_2.12/jars/pprint_2.12-0.5.3.jar:/Users/jonas/.ivy2/cache/com.lihaoyi/fansi_2.12/jars/fansi_2.12-0.2.5.jar:/Users/jonas/.ivy2/cache/org.typelevel/paiges-core_2.12/jars/paiges-core_2.12-0.2.0.jar:/Users/jonas/.ivy2/cache/com.github.pathikrit/better-files_2.12/jars/better-files_2.12-3.7.1.jar:/Users/jonas/Downloads/webShop/target/scala-2.12/test-classes:/Users/jonas/.ivy2/cache/com.typesafe.akka/akka-testkit_2.12/jars/akka-testkit_2.12-2.4.19.jar", "org.scalatest.tools.Runner", "-C", "be.vub.soft.reporter.PerturbationReporter", "-s", "mypackageg.WebShopTestSpec", "-t", "A WebshopActor actor must send back a messages purchase completed")

        Process(
            cmd.toSeq,
            None,
            "PERTURBATION_CONFIGURATION" -> "/Users/jonas/phd/PerturbationTool/perturbation.json",
            "PERTURBATION_ITERATION" -> "1",
            "PERTURBATION_OUTPUT" -> "/Users/jonas/Downloads/webShop/output",
            "PERTURBATION_FORMAT" -> "false") ! cl
            */

        /*
            General options
         */
        val short          = true // Debugging, true -> only message type, false -> complete message
        val iterations      = 1

        /*
            Modifiable options
         */
        val target          = "/Users/jonas/Downloads/webShop" // "/Users/jonas/phd/AkkaStreams" //
        val config          = Paths.get(target, "perturbation.json").toAbsolutePath.toString
        val testClasses     = Paths.get(target, "target", "scala-2.12", "test-classes").toAbsolutePath.toString

        /*
            Should not be modified
         */
        val index           = Paths.get(target, "tests.index").toFile
        val output          = Paths.get(target, "output").toAbsolutePath.toString
        val pwd             = Paths.get(target).toFile
        val lib             = Paths.get(target, "lib").toFile
        val plugins         = Paths.get(target, "project", "plugins.sbt").toFile
        val pluginPath      = Paths.get(target, "lib", "sbt-aspectj.jar")
        val plugin          = s"""
                                  |libraryDependencies += "org.aspectj" % "aspectjtools" % "1.8.10"
                                  |addSbtPlugin("com.lightbend.sbt" % "sbt-aspectj" % "0.11.1" from "file://$pluginPath")
                               """.stripMargin

        println(s"Starting perturbation")

        /*
            Copy local jar files
         */

        println(s"Copying local jar files")

        if(!lib.exists()) {
            lib.mkdirs()
        }

        val jars = Paths.get("./jars").toFile
        FileUtils.copyDirectory(jars, lib)

        /*
            Create output folder
         */

        val outputFile = new File(output)
        if(!outputFile.exists()) {
            outputFile.mkdirs()
        }

        /*
            Modify plugins.sbt
         */

        println(s"Creating plugins.sbt")

        if(!plugins.exists()) {
            plugins.createNewFile()
        }

        val file = Source.fromFile(plugins.getAbsolutePath)
        val exists = file.getLines.exists(l => l.contains("sbt-aspectj"))

        if(!exists) {
            val fw = new FileWriter(plugins.getAbsolutePath, true)
            try fw.write(plugin) finally fw.close()
        }

        /*
            Execute perturbations
         */

        val countLogger = ProcessLogger(line => println(line), line => println(line))

        if(!index.exists()) {
            val process = Process(Seq("sbt", s"discover $testClasses"), pwd)
            process ! countLogger
        }

        if(index.exists()) {
            println("Index file detected:")

            val f = new FileInputStream(index)
            val s = new ObjectInputStream(f)
            val mapping: Map[String, List[DiscoveredTest]] = s.readObject().asInstanceOf[Map[String, List[DiscoveredTest]]]
            s.close()

            mapping.foreach({
                case (k, v) =>
                    println(k)
                    v.foreach(t => println(s"    ${t.name} (${t.succeeds})"))
            })

            for ((suite, tests) <- mapping) {
                for(tuple <- tests) {
                    val DiscoveredTest(test, succeeded, cmd) = tuple

                    // TODO: remove this is for debugging
                    val isTest = test.contains("send back a messages purchase completed")

                    if(succeeded && isTest) {

                        // static config:
                        val analyzer = new StaticAnalyzer(config, output)
                        //val analyzer = new PerturbationAnalyzer(suite, test, output)

                        for(n <- 1 to iterations)
                        {
                            println(s"Iteration #$n: '$test' in $suite")

                            val localConfig = analyzer.next()

                            val localConfigPath = Paths.get(output, s"${test.hashCode}-${suite.hashCode}-$n-config.json").toFile.getAbsolutePath
                            JSONParser.write(localConfigPath, localConfig)

                            //val encoded = Base64.getEncoder.encodeToString(test.getBytes(StandardCharsets.UTF_8))
                            //val process = Process(Seq("sbt", s"perturb $testClasses $config $n $output $short $suite $encoded"), pwd)
                            //val process = Process("sbt perturb" +: Seq(testClasses, config, n.toString, output, short.toString, suite, test), pwd)
                            val process = Process(
                                cmd ++ Seq("-s", suite, "-t", test),
                                None,
                                "PERTURBATION_CONFIGURATION" -> localConfigPath,
                                "PERTURBATION_ITERATION" -> n.toString,
                                "PERTURBATION_OUTPUT" -> output,
                                "PERTURBATION_FORMAT" -> short.toString)
                            //println(process.toString)
                            val exit = process ! countLogger

                            if(exit == 0) {
                                analyzer.update(n, localConfig)
                            } else {
                                println(s"Something went wrong... exit=$exit")
                            }
                        }

                        analyzer.report()
                    } else {
                        println(s"Skipping failed test: '$test' in $suite")
                    }
                }
            }

            println("Finished perturbation tests")

        } else {
            println("Unable to discover tests")
        }

    }

}