package be.vub.soft.perturbation.criteria
import be.vub.soft.tracer.{ActorRegistration, Send, TestReport, Traceable}

/*
    For each actor: # children, # messages in, # messages out
    For each actor calculate fanIO
    Sort actors based on fanIO
    Go through the trace and sort events based on previous fanIO
 */
object FanInFanOut extends Criteria {

    case class Data(in: Set[(String, String)] = Set.empty, out: Set[(String, String)] = Set.empty, children: Set[String] = Set.empty)

    override def compute(report: Option[TestReport]): List[Traceable] = report match {
        case Some(r) =>
            var messages = Map.empty[(String, String, String), Int]
            var nodes = Map.empty[String, Data]
            val all = false

            def add(p: String, c: String) = {
                val cur = nodes.getOrElse(p, Data())
                val v = cur.copy(children = cur.children + c)
                nodes = nodes + (p -> v)
            }

            // Create fan-in fan-out graph
            r.trace
//                .filter({
//                    case s: Send => s.spath.contains("/user/") && s.rpath.contains("/user/") && !s.clazz.contains("akka.")
//                    case c: ActorRegistration => c.parentPath.contains("/user/") && c.childPath.contains("/user/")
//                    case _ => false
//                })
                .foreach({
                    case s: Send => if(all || s.spath.contains("/user/") && s.rpath.contains("/user/") && !s.clazz.contains("akka.")) {
                        // Add fan in to receiver
                        val cur = nodes.getOrElse(s.rpath, Data())
                        val n: (String, String) = (s.spath, s.clazz)
                        val newSet = cur.in + n
                        val v: Data = cur.copy(in = newSet)
                        nodes = nodes + (s.rpath -> v)

                        // Add fan out to sender
                        val cur2 = nodes.getOrElse(s.spath, Data())
                        val n2: (String, String) = (s.rpath, s.clazz)
                        val newSet2 = cur2.out + n2
                        val v2: Data = cur2.copy(out = newSet2)
                        nodes = nodes + (s.spath -> v2)
                    }
                    case c: ActorRegistration => if(all || c.childPath.contains("/user/")) {
                        println(s"found: ${c.childPath}")
                        add(c.parentPath, c.childPath)
                    }
                    case _ => ()
                })

            val sorted = nodes.toList.sortWith({ case (a,b) => {
                val as = a._2.children.size + a._2.in.size + a._2.out.size
                val bs = b._2.children.size + b._2.in.size + b._2.out.size
                as > bs
            }})

            sorted.foreach(x => println(s"${x._1} => ${x._2.children.size + x._2.in.size + x._2.out.size}; children=${x._2.children.size} in=${x._2.in.size} out=${x._2.out.size}"))

            val result = sorted.flatMap(x => {
                println(s"finding: ${x._1}")
                r.trace.collect({
                    case a: ActorRegistration if a.childPath.equals(x._1) => a
                    case s: Send if s.rpath.equals(x._1) || s.spath.equals(x._1) => s
                })
            })

            println(s"------ sorted: ${result.size} --------")

            //result.foreach(println(_))

            result

            // Rank nodes
            //TODO: try out different rankings: eg only in, only out, combination of both numbers, ratio
            // some better for deep hierarchies, others not
            // eg does killing leave children work better in some apps?

            //List.empty
        case None => List.empty
    }

    override val abbreviation: String = "FanInFanOut"
}