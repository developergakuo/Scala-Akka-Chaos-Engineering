package be.vub.soft.perturbation.criteria

import be.vub.soft.tracer.{TestReport, Traceable}

object Criteria {

    private val criteria: Criteria = EarliestCreatedActor

    def check(report: Option[TestReport]): List[Traceable] = criteria.compute(report)

}

/*
    Criteria:
        ECA

 */

trait Criteria {

    def compute(report: Option[TestReport]): List[Traceable]
    val abbreviation: String

}
