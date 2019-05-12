package be.vub.soft.perturbation.criteria

import be.vub.soft.tracer.{ActorStart, Send, TestReport, Traceable}

object EarliestSentMessage extends Criteria {

    override def compute(report: Option[TestReport]): List[Traceable] = report match {
        case Some(r) => {
            val result = r.trace.collect({
                case a: Send if a.spath.contains("/user/") => a // TODO: filter system actors etc
            }).sortBy(_.timestamp)

            println(s"EarliestSentMessage:\n${result.mkString("\n")}")

            result
        }
        case None => List.empty
    }

    override val abbreviation: String = "EAODM"
}
