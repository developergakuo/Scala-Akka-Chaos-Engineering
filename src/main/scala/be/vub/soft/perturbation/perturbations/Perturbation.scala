package be.vub.soft.perturbation.perturbations

import be.vub.soft.parser.ActorConfig
import be.vub.soft.tracer.{Perturbable, TestReport, Traceable}

object Perturbation {

    val perturbations: List[Perturbation] =  List(AtLeastOnceDeliveryDuplication) // List(PersistentActorRestart, AtLeastOnceDeliveryDuplication, AtLeastOnceDeliveryDelay)

    def check(traceable: Traceable, report: TestReport): List[ActorConfig] = {
        perturbations.collect({case p if p.pre(traceable, report) => p.inject(traceable, report) })
    }

}

abstract class Perturbation {

    def pre(perturbable: Traceable, report: TestReport): Boolean
    def inject[A <: Traceable](perturbable: A, report: TestReport): ActorConfig

}
