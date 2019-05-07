package be.vub.soft.perturbation.analysis

import java.nio.file.Paths

import be.vub.soft.parser.ActorConfig
import be.vub.soft.perturbation.criteria.{Criteria, EarliestCreatedActor}
import be.vub.soft.perturbation.perturbations.Perturbation
import be.vub.soft.tracer.TestReport

case class PerturbationReport(config: ActorConfig, report: TestReport)

class PerturbationAnalyzer(suite: String, test: String, output: String) extends Analyzer {

    var results: Map[Int, PerturbationReport] = Map.empty
    var report: Option[TestReport] = None
    var perturbations: List[ActorConfig] = List(ActorConfig())

    def next(n: Int): Option[ActorConfig] = {
        val head = perturbations.headOption

        if(head.isDefined) {
            //TODO: strategies run it until a perturbation was indeed applied (eg probability, or after message X but it might not always happpen)
            perturbations = perturbations.tail

            println(s"Next perturbation:\n$head")
        }

        head
    }

    private def compute(): Unit = {
        val p = if(report.nonEmpty) {
            // Get all candidates according to a criteria
            val candidates = Criteria.check(report)

            // Go through each candidate and check if we can apply a perturbation
            val candidatePerturbations = candidates.flatMap(c => Perturbation.check(c, report.get))

            // TODO: figure out all possible timings

            perturbations = candidatePerturbations
        } else {
            println("First iteration, applying no perturbations.")
            None
        }
    }

    def update(n: Int, config: ActorConfig): Unit = {
        val p = Paths.get(output, s"${test.hashCode}-${suite.hashCode}-$n.bin").toFile

        if(p.exists()) {
            if(report.isEmpty) {
                report = Some(be.vub.soft.tracer.Tracer.read(p.getAbsolutePath))
                compute()
            } else {
                results = results + (n -> PerturbationReport(config, report.get))
            }
            //reports = report :: reports

        } else {
            println(s"Something went wrong... $p does not exists.")
        }

    }

    def summary(): Unit = {
        results.foreach({
            case (k, v) => println(s"#$k -> ${v.report.success}:\n${v.config}\n")
        })

    }
}
