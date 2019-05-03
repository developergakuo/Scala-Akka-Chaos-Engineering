package be.vub.soft.perturbation.criteria
import be.vub.soft.tracer.{Perturbable, TestReport}

class EarliestCreatedActor extends Criteria {

    override def compute(report: TestReport): Perturbable = {
        report.meta.values.toList.sortBy(m => m.timestamp)
        null
    }

}
