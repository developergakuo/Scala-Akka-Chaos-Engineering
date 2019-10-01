package be.vub.soft.perturbation.criteria

import be.vub.soft.tracer.{ActorRegistration, Send, TestReport, Traceable}

/*
    Based on order of trace, which basically boils down to first created actors and first send messages?
 */
object DefaultCriteria extends Criteria {

    override def compute(report: Option[TestReport]): List[Traceable] = report match {
        case Some(r) =>
            val result: List[Traceable] = r.trace.collect({
                case a: Send if a.spath.contains("/user/") && a.rpath.contains("/user/") => a
                case a: ActorRegistration if a.childPath.contains("/user/") => a
            })

            println(s"DefaultCriteria:\n${result.mkString("\n")}")

            result
        case None =>
            List.empty
    }

    override val abbreviation: String = "DC"
}

