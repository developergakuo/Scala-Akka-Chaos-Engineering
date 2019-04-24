package be.vub.soft

import java.io.{File, FileInputStream, FileWriter, ObjectInputStream}
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.Base64

import be.vub.soft.tracer.{PerturbationDelay, PerturbationDrop, PerturbationKill, TestReport}
import org.apache.commons.io.FileUtils

import scala.io.Source
import scala.sys.process.{Process, ProcessLogger}

/*

    Potential bugs this tool should detect:
        https://doc.akka.io/docs/akka/current/actors.html#initialization-via-message-passing
 */
object Main {

    def main(args: Array[String]): Unit =
    {
        /*
            General options
         */
        val short           = false // Debugging, true -> only message type, false -> complete message
        val iterations      = 3

        /*
            Modifiable options
         */
        val target          = "/Users/jonas/Downloads/webShop"
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
            Copy configuration file
         */

        println(s"Copying configuration file")

        //FileUtils.copyFileToDirectory(configuration, pwd)

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
            val mapping: Map[String, List[(String, Boolean)]] = s.readObject().asInstanceOf[Map[String, List[(String, Boolean)]]]
            s.close()

            mapping.foreach({
                case (k, v) =>
                    println(k)
                    v.foreach(t => println(s"    $t"))
            })

            for ((suite, tests) <- mapping) {
                for(tuple <- tests) {
                    val test = tuple._1
                    val succeeded = tuple._2

                    if(succeeded) {
                        for(n <- 1 to iterations)
                        {
                            println(s"Iteration #$n: '$test' in $suite")

                            val encoded = Base64.getEncoder.encodeToString(test.getBytes(StandardCharsets.UTF_8))
                            val process = Process(Seq("sbt", s"perturb $testClasses $config $n $output $short $suite $encoded"), pwd)
                            //val process = Process("sbt perturb" +: Seq(testClasses, config, n.toString, output, short.toString, suite, test), pwd)
                            println(process.toString)
                            process ! countLogger
                        }
                    } else {
                        println(s"Skipping failed test: '$test' in $suite")
                    }
                }
            }

            println("Finished perturbation tests")

            /*
                Write output
             */

            val report = new File(output)

            if(report.exists()) {
                val files = FileUtils.listFiles(report, Array("txt"), false)

                println(s"Generated ${files.size} files")
            } else {
                println(s"No files generated. Probably, something went wrong.")
            }

            import scala.collection.JavaConverters._
            val reportFiles = FileUtils.listFiles(report, Array("bin"), false).asScala.toList
            val reports = reportFiles.map(r => be.vub.soft.tracer.Tracer.read(r.getAbsolutePath))

            val groupedByTests: Map[String, Map[String, List[TestReport]]] = reports.groupBy(r => r.suite).map({ case (ste, l) => ste -> l.groupBy(t => t.test)})

            def toTrueOrFalse(b: Boolean): String = if(b) "T" else "F"

            groupedByTests.foreach({
                case (suite, reps) =>
                    println(suite)
                    reps.foreach({
                        case (test, rprts) =>
                            val iterations = rprts.zipWithIndex.map({ case (_, i) => i}).mkString("\t")
                            val results = rprts.map(r => {
                                val killed = toTrueOrFalse(r.trace.exists({
                                    case _: PerturbationKill => true
                                    case _ => false
                                }))

                                val dropped = toTrueOrFalse(r.trace.exists({
                                    case _: PerturbationDrop => true
                                    case _ => false
                                }))

                                val delayed = toTrueOrFalse(r.trace.exists({
                                    case _: PerturbationDelay => true
                                    case _ => false
                                }))

                                s"${r.success} ($delayed, $dropped, $killed)"
                            }).mkString("\t")

                            println(s"\t$test")
                            println(s"\t$iterations")
                            println(s"\t$results")
                    })
            })

        } else {
            println("Unable to discover tests")
        }

    }

}