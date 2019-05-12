package be.vub.soft.perturbation.criteria

import be.vub.soft.tracer.{TestReport, Traceable}

object Criteria {

    private val criteria: Criteria = EarliestSentMessage // EarliestCreatedActor

    def check(report: Option[TestReport]): List[Traceable] = criteria.compute(report)

}

/*
    Criteria:
        ECA
        ESM

        We can combine those eg earliest messages of a given actor: ie custom calling ECA and then mappingg over it to get messagges

 */

trait Criteria {

    def compute(report: Option[TestReport]): List[Traceable]
    val abbreviation: String

}
