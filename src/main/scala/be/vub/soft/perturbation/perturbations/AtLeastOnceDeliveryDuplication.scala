package be.vub.soft.perturbation.perturbations
import be.vub.soft.parser.ActorConfig
import be.vub.soft.tracer.{Send, TestReport, Traceable}

object AtLeastOnceDeliveryDuplication extends Perturbation {

    override def pre(perturbable: Traceable, report: TestReport): Boolean = perturbable match {
        case Send(spath, _, rpath, _, hash, clazz) if spath.contains("/user/") && rpath.contains("/user/") => true
        case _ => false
    }

    override def inject(perturbable: Traceable, report: TestReport): ActorConfig = {

        // Duplicate after Nth message

        // Before sending it to actor X

        // After message X

        ActorConfig()
    }
}
