package be.vub.soft.perturbation.criteria
import be.vub.soft.tracer.{ActorStart, Perturbable, TestReport, Traceable}

object EarliestCreatedActor extends Criteria {

    override def compute(report: Option[TestReport]): List[Traceable] = report match {
        case Some(r) => {
            val actors = r.trace.collect({
                case a: ActorStart if a.path.contains("/user/") => a // TODO: filter system actors etc
            })
            actors.sortBy(a => r.meta.values.find(meta => a.path.equals(meta.actorName)).map(_.timestamp).getOrElse(Long.MaxValue))
        }
        case None => List.empty
    }

    override val abbreviation: String = "ECA"
}
