package be.vub.soft.perturbation.criteria

import be.vub.soft.tracer.{Perturbable, TestReport}

trait Criteria {

    def compute(report: TestReport): Perturbable

}
