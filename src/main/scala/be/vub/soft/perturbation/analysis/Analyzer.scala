package be.vub.soft.perturbation.analysis

import be.vub.soft.parser.ActorConfig

abstract class Analyzer {

    def next(): Option[ActorConfig]
    def update(n: Int, config: ActorConfig): Unit
    def summary(): Unit

}
