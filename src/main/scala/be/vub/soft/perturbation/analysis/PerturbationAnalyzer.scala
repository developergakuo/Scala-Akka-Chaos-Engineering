package be.vub.soft.perturbation.analysis

import java.nio.file.Paths

import be.vub.soft.parser.ActorConfig
import be.vub.soft.tracer.TestReport

case class PerturbationReport(config: ActorConfig, succeeds: Boolean = false)

class PerturbationAnalyzer(suite: String, test: String, output: String) extends Analyzer {

    var results: Map[Int, PerturbationReport] = Map.empty

    def next(): ActorConfig = {
        // Decide next perturbation


        ActorConfig()
    }

    def update(n: Int, config: ActorConfig): Unit = {
        val p = Paths.get(output, s"${test.hashCode}-${suite.hashCode}-$n.bin").toFile

        if(p.exists()) {
            val report: TestReport = be.vub.soft.tracer.Tracer.read(p.getAbsolutePath)

            results = results + (n -> PerturbationReport(config, report.success))

        } else {
            println(s"Something went wrong... $p does not exists.")
        }

    }

    def report(): Unit = {
        results.foreach({
            case (k, v) => println(s"#$k -> ${v.succeeds}:\n${v.config}\n")
        })

    }
}
