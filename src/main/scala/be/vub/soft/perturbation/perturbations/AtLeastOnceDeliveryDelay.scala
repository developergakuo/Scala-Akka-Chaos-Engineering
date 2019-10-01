package be.vub.soft.perturbation.perturbations

import be.vub.soft.Constants
import be.vub.soft.parser.{ActorConfig, ActorMessage}
import be.vub.soft.tracer.{Send, TestReport, Traceable}

/*
    Inject a delay to replicate out-of-order message
    Delay should be less than akka.at-least-once-delivery.redeliver-interval to avoid duplication as well
 */

object AtLeastOnceDeliveryDelay extends Perturbation {

    override def pre(perturbable: Traceable, report: TestReport): Boolean = perturbable match {
            /*
                Messages that were delivered by an AtLeastOnceDelivery actor
             */
        case Send(spath, _, rpath, _, hash, clazz, _)
            if spath.contains("/user/") && rpath.contains("/user/") &&
                report.atLeastOnceDeliveryMessages.contains(clazz)
            => true

            /*
                Messages that are send from non-AtLeastOnceDelivery actors to AtLeastOnceDelivery actors
             */
        case Send(spath, _, rpath, _, hash, clazz, _)
            if spath.contains("/user/") && rpath.contains("/user/") &&
                !report.meta.get(spath).exists(m => m.inheritance.contains(Constants.AtLeastOnceDelivery)) &&
                report.meta.get(rpath).exists(m => m.inheritance.contains(Constants.AtLeastOnceDelivery))
            => true

        case _ => false
    }

    override def inject[Send](perturbable: Send, report: TestReport, messageCandidates: Set[Traceable], actorCandidates: Set[Traceable]): List[ActorConfig]  = perturbable match {
        case Send(spath, _, rpath, _, hash, clazz, _) =>
            // Delay after Nth message

            // Before sending it to actor X

            // After message X

            val actorName = rpath // Receiving actor
            val senderName = spath
            val delay = Math.random() * 200
            val message = ActorMessage(clazz.r, 0, 0, delay.toInt, senderName.r, ".*".r, ".*".r)
            val messages = List(message)

            val config = ActorConfig(actorName.r, ".*".r, ".*".r, 0.0, messages)

            println(s"AtLeastOnceDeliveryDelay: \n\t$perturbable\n\t$config")

            List(config)
    }
}
