package be.vub.soft.perturbation.perturbations
import be.vub.soft.tracer.{ActorStart, Perturbable, PerturbationKill, TestReport, Traceable}
import be.vub.soft.Constants
import be.vub.soft.parser.ActorConfig

object PersistentActorRestart extends Perturbation {

    override def pre(traceable: Traceable, report: TestReport): Boolean = traceable match {
        case ActorStart(path, _) =>
            report.meta.get(path).exists(m => m.inheritance.contains(Constants.PersistentActor))
        case _ => false
    }

    override def inject(traceable: Traceable, report: TestReport): ActorConfig = {

        // Restart after N messages

        // Restart before last message

        // Restart after message from actor X

        ActorConfig()
    }
}
