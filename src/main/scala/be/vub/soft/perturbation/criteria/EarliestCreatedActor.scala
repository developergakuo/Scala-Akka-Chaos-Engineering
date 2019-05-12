package be.vub.soft.perturbation.criteria
import be.vub.soft.tracer.{ActorRegistration, ActorStart, Perturbable, TestReport, Traceable}

object EarliestCreatedActor extends Criteria {

    override def compute(report: Option[TestReport]): List[Traceable] = report match {
        case Some(r) =>
            val result = r.trace.collect({ case a: ActorRegistration if a.path.contains("/user/") => a }).sortBy(_.timestamp)

            println(s"EarliestCreatedActor:\n${result.mkString("\n")}")

            result
        case None =>
            List.empty
    }

    override val abbreviation: String = "ECA"
}
