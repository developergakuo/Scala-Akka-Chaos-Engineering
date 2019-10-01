package be.vub.soft.perturbation.perturbations
import be.vub.soft.Main
import be.vub.soft.parser.{ActorConfig, ActorMessage}
import be.vub.soft.tracer.{Send, TestReport, Traceable}

/*
    Inject a duplicated message to check against message duplication
 */

object AtLeastOnceDeliveryDuplication extends Perturbation {

    override def pre(perturbable: Traceable, report: TestReport): Boolean = perturbable match {
        case Send(spath, _, rpath, _, hash, clazz, _)
            if spath.contains("/user/") && rpath.contains("/user/") && report.atLeastOnceDeliveryMessages.contains(clazz) => true
        case _ => false
    }

    override def inject[Send](perturbable: Send, report: TestReport, messageCandidates: Set[Traceable], actorCandidates: Set[Traceable]): List[ActorConfig] = perturbable match {
        case Send(spath, _, rpath, _, hash, clazz, _) =>

            // Duplicate after Nth message

            // Before sending it to actor X

            // After message X

            // but only duplicate x times (assuming a few triggers will cause a bug to occur)

            val actorName = rpath // Receiving actor
            val senderName = spath
            val message = ActorMessage(clazz.r, 0, Main.Probability, 0, senderName.r, ".*".r, ".*".r)
            val messages = List(message)

            val config = ActorConfig(actorName.r, ".*".r, ".*".r, 0.0, messages)

            println(s"AtLeastOnceDeliveryDuplication: \n\t$perturbable\n\t$config")

            List(config)
    }
}
