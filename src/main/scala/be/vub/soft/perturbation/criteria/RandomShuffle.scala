package be.vub.soft.perturbation.criteria

import be.vub.soft.tracer.{ActorRegistration, TestReport, Traceable}

import scala.util.Random

object RandomShuffle extends Criteria {

    override def compute(report: Option[TestReport]): List[Traceable] = report match {
        case Some(r) =>
            val result = Random.shuffle(r.trace.collect({
                case a: ActorRegistration if a.childPath.contains("/user/") => a
            }))

            println(s"RandomShuffle:\n${result.mkString("\n")}")

           result
        case None =>
            List.empty
    }

    override val abbreviation: String = "RS"
}
