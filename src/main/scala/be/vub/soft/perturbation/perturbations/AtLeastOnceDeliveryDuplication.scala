package be.vub.soft.perturbation.perturbations
import be.vub.soft.parser.{ActorConfig, ActorMessage}
import be.vub.soft.tracer.{Send, TestReport, Traceable}

object AtLeastOnceDeliveryDuplication extends Perturbation {

    override def pre(perturbable: Traceable, report: TestReport): Boolean = perturbable match {
        case Send(spath, _, rpath, _, hash, clazz) if spath.contains("/user/") && rpath.contains("/user/") => true
        case _ => false
    }

    override def inject(perturbable: Traceable, report: TestReport): ActorConfig = perturbable match {
        case Send(spath, _, rpath, _, hash, clazz) =>

            // Duplicate after Nth message

            // Before sending it to actor X

            // After message X

            // but only duplicate x times (assuming a few triggers will cause a bug to occur)

            val actorName = rpath // Receiving actor

            val senderName = spath
            val message = ActorMessage(clazz.r, 0, 0, senderName.r, ".*".r, ".*".r)
            val messages = List(message)
            ActorConfig(actorName.r, ".*".r, ".*".r, 0.0, messages)
    }
}
