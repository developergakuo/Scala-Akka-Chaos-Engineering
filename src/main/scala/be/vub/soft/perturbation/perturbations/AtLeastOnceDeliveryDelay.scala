package be.vub.soft.perturbation.perturbations

import be.vub.soft.Constants
import be.vub.soft.parser.{ActorConfig, ActorMessage}
import be.vub.soft.tracer.{Send, TestReport, Traceable}

/*
    We simulate a delay by dropping a message, the ALOD mechanism will resend it after the timeout expired
 */

object AtLeastOnceDeliveryDelay extends Perturbation {

    override def pre(perturbable: Traceable, report: TestReport): Boolean = perturbable match {
        case Send(spath, _, rpath, _, hash, clazz, _) if spath.contains("/user/") => // TODO:  && rpath.contains("/user/") ???
            val senderUsesAtLeastOnceDelivery = report.meta.get(spath).exists(m => m.inheritance.contains(Constants.AtLeastOnceDelivery))

            senderUsesAtLeastOnceDelivery
        case _ => false
    }

    override def inject[Send](perturbable: Send, report: TestReport): ActorConfig = perturbable match {
        case Send(spath, _, rpath, _, hash, clazz, _) =>
            // Delay after Nth message

            // Before sending it to actor X

            // After message X

            // TODO: how to combine these??


            val actorName = rpath // Receiving actor
            val senderName = spath
            val delay = 100
            val message = ActorMessage(clazz.r, 0.5, delay, senderName.r, ".*".r, ".*".r)
            val messages = List(message)

            val config = ActorConfig(actorName.r, ".*".r, ".*".r, 0.0, messages)

            println(s"AtLeastOnceDeliveryDelay: $Send\n$config")

            config
    }
}
