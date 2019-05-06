package be.vub.soft.perturbation.analysis
import java.io.File

import be.vub.soft.parser.{ActorConfig, JSONParser}
import be.vub.soft.tracer.{PerturbationDelay, PerturbationDrop, PerturbationKill, TestReport}
import org.apache.commons.io.FileUtils

class StaticAnalyzer(configurationPath: String, output: String) extends Analyzer {
    override def next(): Option[ActorConfig] = {
        val result = JSONParser.load(configurationPath)

        println(result.head)

        Some(result.head)
    }

    override def update(n: Int, config: ActorConfig): Unit = {

    }

    override def summary(): Unit = {
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
    }
}
