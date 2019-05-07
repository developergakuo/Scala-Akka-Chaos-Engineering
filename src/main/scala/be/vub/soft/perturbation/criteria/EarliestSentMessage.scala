package be.vub.soft.perturbation.criteria

import be.vub.soft.tracer.{ActorStart, Send, TestReport, Traceable}

object EarliestSentMessage extends Criteria {

    override def compute(report: Option[TestReport]): List[Traceable] = report match {
        case Some(r) => {
            val messages = r.trace.collect({
                case a: Send if a.spath.contains("/user/") => a // TODO: filter system actors etc
            })
            //actors.sortBy(a => r.meta.values.find(meta => a.path.equals(meta.actorName)).map(_.timestamp).getOrElse(Long.MaxValue))
            List.empty
        }
        case None => List.empty
    }

    override val abbreviation: String = "EAODM"
}
