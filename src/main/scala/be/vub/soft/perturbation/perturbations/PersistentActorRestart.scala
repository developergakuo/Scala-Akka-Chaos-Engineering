package be.vub.soft.perturbation.perturbations
import be.vub.soft.tracer.{ActorRegistration, ActorStart, Perturbable, PerturbationKill, TestReport, Traceable}
import be.vub.soft.Constants
import be.vub.soft.parser.ActorConfig

object PersistentActorRestart extends Perturbation {

    override def pre(traceable: Traceable, report: TestReport): Boolean = traceable match {
        case a: ActorRegistration if a.inheritance.contains(Constants.PersistentActor) => true
        case _ => false
    }

    override def inject[A <: Traceable](traceable: A, report: TestReport): ActorConfig = {

        // Restart after N messages

        // Restart before last message

        // Restart after message from actor X

        ActorConfig()
    }
}
