package be.vub.soft.perturbation.perturbations

import be.vub.soft.Constants
import be.vub.soft.parser.ActorConfig
import be.vub.soft.tracer.{Send, TestReport, Traceable}

object AtLeastOnceDeliveryDelay extends Perturbation {

    override def pre(perturbable: Traceable, report: TestReport): Boolean = perturbable match {
        case Send(spath, _, rpath, _, hash, clazz) if spath.contains("/user/") => // TODO:  && rpath.contains("/user/") ???
            val senderUsesAtLeastOnceDelivery = report.meta.get(spath).exists(m => m.inheritance.contains(Constants.AtLeastOnceDelivery))

            senderUsesAtLeastOnceDelivery
        case _ => false
    }

    override def inject(perturbable: Traceable, report: TestReport): ActorConfig = {

        // Duplicate after Nth message

        // Before sending it to actor X

        // After message X

        ActorConfig()
    }
}
