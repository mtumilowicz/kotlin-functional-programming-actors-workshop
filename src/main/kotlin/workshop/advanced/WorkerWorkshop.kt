package workshop.advanced

import common.fibonacci.ComputeFibonacciTask
import workshop.actor.AbstractActorWorkshop

class WorkerWorkshop(id: String) : AbstractActorWorkshop<ComputeFibonacciTask>(id) {

    // hint: override handle
    // run task and send it back to the sender
}

