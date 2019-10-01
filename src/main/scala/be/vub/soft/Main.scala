package be.vub.soft

import java.io.{File, FileInputStream, FileWriter, ObjectInputStream, PrintWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.Base64

import be.vub.soft.parser.{ActorConfig, JSONParser}
import be.vub.soft.perturbation.analysis.{PerturbationAnalyzer, StaticAnalyzer}
import be.vub.soft.reporter.DiscoveredTest
import be.vub.soft.tracer.{ActorRegistration, Send}
import org.apache.commons.io.FileUtils

import scala.io.Source
import scala.sys.process.{Process, ProcessLogger}

/*
    Idea: eg trading system/webserver/ecommerce starts many actors, but tests only use a part of them -> optimize by only killing those

    Potential bugs this tool should detect:
        https://doc.akka.io/docs/akka/current/actors.html#initialization-via-message-passing
        https://doc.akka.io/docs/akka/2.5/general/message-delivery-reliability.html
        https://github.com/royrusso/akka-java-examples
        https://github.com/akka/akka/blob/master/akka-persistence/src/main/scala/akka/persistence/JournalProtocol.scala

    TODO:
        done: store the final java command in the index -> speed up because no need for starting sbt and plugin!
        write a setak "whenStable" to avoid tests fail/timeout
            thread.sleep will fail when actors are restarted/messagegs resend w/e (false positive)
            timeouts in general might cause timeouts
        gracefull tests whenDown(actor1) { assert(..) }
 */
/*
load custom exceptions
        // Create a new JavaClassLoader // Create a new JavaClassLoader
        val classLoader = this.getClass.getClassLoader

        // Load the target class using its binary name
        val loadedMyClass = classLoader.loadClass("Shopping.Messages$RestartException")
        val constructor = loadedMyClass.getConstructors
        val myClassObject = constructor.head.newInstance("ok", new Throwable)
        println("lol="+loadedMyClass.getFields.mkString(","))
        System.out.println("Loaded class name: " + loadedMyClass.getName)
 */
object Main {

    val Probability = 1

    def main(args: Array[String]): Unit =
    {
        if(args.isEmpty) {
            println("Missing argument: the path of the system to analyze")
            System.exit(0)
        } else if(args(0).contains(" ")) {
            println("Target path cannot contain spaces")
            System.exit(0)
        } else {
            println(s"Analyzing ${args(0)}")
        }

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



//        val projects = List("CESurvey-jens-results", "CESurvey-thierry-results","CESurvey-bjarno-results", "CESurvey-janwillem-results",
//            "CESurvey-kevin-results", "CESurvey-maarten-results","CESurvey-tim-results", "CESurvey-quentin-results")
//
//        projects.foreach(p => {
//            println(s"Analyzing $p")        }) //End foreach


        /*
            General options
         */
        val short           = true // Debugging, true -> only message type, false -> complete message
        val iterations      = 20
        val skipTrace       = true

        /*
            Modifiable options
         */
        /*
        Modifiable options
     */
        val target          = "/Users/gakuo/Documents/ThesisProject/webshop"
        val config          = Paths.get(target, "perturbation.json").toAbsolutePath.toString
        val testClasses     = Paths.get(target,    "target","scala-2.12","test-classes")
          .toAbsolutePath.toString

/*
    Should not be modified
 */
val index           = Paths.get(target, "tests.index").toFile
val output          = Paths.get(target, s"output-p-${100 * Probability}").toAbsolutePath.toString
val out             = Paths.get(target, s"summary-p-${100 * Probability}").toAbsolutePath
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
println(s"jars found: ${jars.exists()}")
if(!jars.exists()) {
    println("Missing jar files: they should be in the folder 'jars' located in the same folder as the tool.jar")
    System.exit(0)
}
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

val logger = ProcessLogger(line => println(line), line => println(line))
//val logger = ProcessLogger(line => (), line => ())

if(!index.exists()) {
    val process = Process(Seq("sbt", s"discover $testClasses"), pwd)
    process ! logger
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

    def execute(test: String, suite: String, n: Int, cmd: List[String], localConfigPath: String) = {
        //println((cmd++ Seq("-s", suite, "-t", test)).mkString(" "))
        val process = Process(
            cmd ++ Seq("-s", suite, "-t", test),
            None,
            "PERTURBATION_CONFIGURATION" -> localConfigPath,
            "PERTURBATION_ITERATION" -> n.toString,
            "PERTURBATION_OUTPUT" -> output,
            "PERTURBATION_FORMAT" -> short.toString,
            "PERTURBATION_SKIP_TRACE" -> skipTrace.toString)

        process ! logger
    }

    // For every test suite
    for (((suite, tests), suiteID) <- mapping.zipWithIndex) {

        // For every test case
        for((tuple, testID) <- tests.zipWithIndex) {
            val DiscoveredTest(test, succeeded, cmd) = tuple

            // TODO: remove this is for debugging
            //val isTest = test.contains("sum all states correctly")
            //val isTest = true // test.contains("A WebshopActor actor2")
            val isTest = test.contains("send back a messages purchase completed")
            if(succeeded && isTest) {

                // static config:
                //val analyzer = new StaticAnalyzer(config, output)
                val analyzer = new PerturbationAnalyzer(suite, suiteID, test, testID, output)

                println(s"Running initial iteration for '$test'")

                val initial = execute(test, suite, 0, cmd, "")

                if(initial == 0) {
                    println("Initial iteration successful")

                    analyzer.update(0, ActorConfig())

                    if(analyzer.report.nonEmpty) {
                        println(s"Analyzing trace of '$test'")

                        var graph = List.empty[String]
                        def add(p: String, c: String) = graph = graph :+ s"${'"'}$p${'"'} -> ${'"'}$c${'"'}"
                        var messages = Map.empty[(String, String, String), Int]
                        def addMessage(s: String, r: String, c: String) = graph = graph :+ s"${'"'}$s${'"'} -> ${'"'}$r${'"'} [color=${'"'}0.002 0.999 0.999${'"'}, label=${'"'}$c${'"'}];"
                        val all = false
                        var actors = 0
                        var setOfActorTypes = Set.empty[String]
                        analyzer.report.get.trace.foreach({
                            case s: Send => if(all || s.spath.contains("/user/") && s.rpath.contains("/user/") && !s.clazz.contains("akka.")) { //
                                //val k = (s.spath + "#" + s.suid, s.rpath  + "#" + s.ruid, s.clazz)
                                //val k = (s.spath + "#" + s.suid, s.rpath + "#" + s.suid, s.clazz)
                                //println(s"Found message= ${s.clazz}")
                                val k = (s.spath, s.rpath, s.clazz)
                                val v = messages.getOrElse(k, 0) + 1
                                messages = messages + (k -> v)
                            }
                            case c: ActorRegistration => if(all || c.childPath.contains("/user/")) {
                                add(c.parentPath, c.childPath)
                                actors = actors + 1
                                setOfActorTypes = setOfActorTypes + c.actorType
//                                    val p = c.parentPath.indexOf("#")
//                                    val cp = c.childPath.indexOf("#")
//
//                                    add(c.parentPath.substring(0, if(p == -1) p else c.parentPath.length -1),
//                                        c.childPath.substring(0, if(cp == -1) cp else c.childPath.length -1))
                            }
                            case _ => ()
                        })
                        messages.foreach({ case (k,v) => addMessage(k._1, k._2, s"${k._3} ($v)")})
/*
                        val fw = new FileWriter("payment.txt", true)
                        try {
                            val s = s"$test: actors=$actors | types=${setOfActorTypes.size} (${setOfActorTypes.mkString(",")}) \n"
                            println(s)
                            fw.write(s)
                        }
                        finally fw.close()
*/
                        // dot -Tpdf graph.dot -o graph.pdf
                        val gg = graph.mkString("\n")
                        //new PrintWriter("./graph.dot") { write(s"digraph G { $gg }"); close }

                        for (n <- 1 to iterations) {
                            println(s"Iteration #$n: '$test' in $suite")

                            val localConfigOption = analyzer.next(n)

                            if(localConfigOption.isDefined) {
                                val localConfig = localConfigOption.get

                                val localConfigPath = Paths.get(output, s"${test.hashCode}-${suite.hashCode}-$n-config.json").toFile.getAbsolutePath
                                JSONParser.write(localConfigPath, localConfig)

                                val exit = execute(test, suite, n, cmd, localConfigPath)

                                if (exit == 0) {
                                    println(s"Test succeeded... exit=$exit")
                                } else {
                                    println(s"Test failed... exit=$exit")
                                }

                                // Give it a bit of time to write to the file, probably unnecessary
                                Thread.sleep(500)

                                analyzer.update(n, localConfig)
                            } else {
                                println(s"Skipping $n: no perturbations left")
                            }
                        }

                        analyzer.summary(out)
                        /**/
                    } else {
                        println(s"Unable to analyze trace of '$test'")
                    }

                } else {
                    println(s"Cancelling test: '$test' in $suite (initial trace failed: $initial)")
                }

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