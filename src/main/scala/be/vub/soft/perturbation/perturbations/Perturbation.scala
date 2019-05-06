package be.vub.soft.perturbation.perturbations

import be.vub.soft.parser.ActorConfig
import be.vub.soft.tracer.{Perturbable, TestReport, Traceable}

object Perturbation {

    val perturbations: List[Perturbation] = List(PersistentActorRestart, AtLeastOnceDeliveryDuplication, AtLeastOnceDeliveryDelay)

    def check(traceable: Traceable, report: TestReport): List[ActorConfig] = {
        perturbations.map({case p if p.pre(traceable, report) => p.inject(traceable, report) })
    }

}

abstract class Perturbation {

    def pre(perturbable: Traceable, report: TestReport): Boolean
    def inject(perturbable: Traceable, report: TestReport): ActorConfig

}
