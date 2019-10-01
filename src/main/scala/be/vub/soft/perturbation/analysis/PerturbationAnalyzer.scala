package be.vub.soft.perturbation.analysis

import java.io.File
import java.nio.file.{Path, Paths}

import be.vub.soft.parser.ActorConfig
import be.vub.soft.perturbation.criteria.{Criteria, EarliestCreatedActor}
import be.vub.soft.perturbation.perturbations.Perturbation
import be.vub.soft.tracer.{ActorRegistration, Send, TestReport, Traceable}

case class PerturbationReport(config: ActorConfig, report: TestReport)

class PerturbationAnalyzer(suite: String, suiteID: Int, test: String, testID: Int, output: String) extends Analyzer {

    var results: Map[Int, PerturbationReport] = Map.empty
    var report: Option[TestReport] = None
    var perturbations: List[ActorConfig] = List()

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
            var messageCandidates = Set.empty[Send]
            var actorCandidates = Set.empty[Traceable]

            val candidates = Criteria.check(report)

            candidates.foreach({
                // Filter duplicate messages
                case s@Send(spath, _, rpath, _, _, tpe, _) =>
                    val exists = messageCandidates.exists({
                        case Send(espath, _, erpath, _, _, etpe, _) => {
//                            println(s"$spath == $espath")
//                            println(s"$rpath == $erpath")
//                            println(s"$tpe == $etpe")
                            val r = (spath == espath && rpath == erpath && tpe == etpe)
                            //println(r)
                            r
                        }
                        case _ => false
                    })

                    if(!exists && !tpe.startsWith("akka.")) {
                        messageCandidates = messageCandidates + s
                    }
                case a: ActorRegistration =>
                    actorCandidates = actorCandidates + a
            })

            //val filtered = messageCandidates ++ actorCandidates

            println(s"Found ${candidates.size} candidates")
            //candidates.foreach(println(_))

            // Go through each candidate and check if we can apply a perturbation
            val candidatePerturbations = candidates.flatMap(c => Perturbation.check(c, report.get, messageCandidates, actorCandidates))

            //TODO: REMOVE only for restarts
            perturbations = candidatePerturbations.filter({
                case a: ActorConfig => a.killProbability > 0
            })

            // TODO: figure out all possible timings
            println(s"Found ${perturbations.size} possible perturbations")

            perturbations.foreach(println(_))


        } else {
            println("First iteration, applying no perturbations.")
            None
        }
    }

    def update(n: Int, config: ActorConfig): Unit = {
        val p = Paths.get(output, s"${test.hashCode}-${suite.hashCode}-$n.bin").toFile

        if(p.exists()) {
            val read = be.vub.soft.tracer.Tracer.read(p.getAbsolutePath)

            if(report.isEmpty) {
                // Set initial trace
                report = Some(read)

                // TODO: only compute after initial iteration
                if(n == 0) compute()
            } else {
                results = results + (n -> PerturbationReport(config, read))
            }
            //reports = report :: reports

        } else {
            println(s"Something went wrong... $p does not exists.")
        }

    }

    def summary(out:Path): Unit = {
        if(!out.toFile.exists()) out.toFile.mkdir()

        println(s"Summary for $test in $suite:")
        val initial = s"#0;${report.get.success};${report.get.duration};0;0"

        val content = initial :: results.toList.sortBy(_._1).map({
            case (k, v) =>
                val applied = if(v.report.appliedPerturbations.nonEmpty) "DONE" else "NONE"
                val ex = if(v.report.exception.nonEmpty) s"${v.report.exception.split("\n")(0)}" else ""
                val tpe = if(v.config.killProbability > 0) "RESTART" else if(v.config.messages.head.delay > 0) "REORDER" else "DUPLICATION"
                s"#$k;${v.report.success};${v.report.duration};$applied;$tpe;${v.config};$ex"
        })

        content.foreach(println(_))

        import java.io.PrintWriter
        val op = Paths.get(out.toString, s"t$suiteID-$testID.csv")
        new PrintWriter(op.toFile) { write(content.mkString("\n")); close }
    }
}
