package workshop.advanced

import common.fibonacci.ComputeFibonacciTask
import common.fibonacci.FibonacciTaskOutput
import workshop.actor.AbstractActorWorkshop
import workshop.actor.ActorWorkshop
import workshop.actor.MessageProcessorWorkshop

class ManagerWorkshop(
    id: String,
    taskInputs: List<Int>,
    workers: Int,
    private val client: ActorWorkshop<List<FibonacciTaskOutput>>
) : AbstractActorWorkshop<ComputeFibonacciTask>(id) {

    private val processing: List<ComputeFibonacciTask>
    private val waiting: List<ComputeFibonacciTask>
    private val results: List<ComputeFibonacciTask>
    private val processTask: (Behaviour) -> (ComputeFibonacciTask) -> Unit

    init {
        // assign ordinal numbers and transform into ComputeFibonacciTask
        // hint: taskInputs, zip, map, ComputeFibonacciTask, TaskIndex, IntTaskInput
        val tasks: List<ComputeFibonacciTask> = listOf()

        // each task will be assigned to one worker
        // hint: task, take
        this.processing = listOf()

        // rest of the tasks moved to waiting list
        // hint: tasks, drop
        this.waiting = listOf()

        // empty
        this.results = listOf()

        // set initial state
        // hint: context, become, waiting, results

        processTask = { behaviour ->
            { result ->
                // add result to results (note that behaviour.results are immutable)
                // if we have all results - sort them and send to client
                // hint: compare sizes with taskInputs
                // else mutate context
                // hint: behaviour.waiting, drop
            }
        }
    }

    fun start() {
        // assign worker for each task in processing
        // hint: processing, forEach, startWorker
    }

    // create worker and enqueue task, hint: WorkerWorkshop, enqueue
    private fun startWorker(task: ComputeFibonacciTask) = null

    // override handle - not used

    internal inner class Behaviour internal constructor(
        internal val waiting: List<ComputeFibonacciTask>,
        internal val results: List<ComputeFibonacciTask>
    ) : MessageProcessorWorkshop<ComputeFibonacciTask> {

        // override process
        // process task, hint: processTask
        // take next from waiting list, hint: waiting, take, forEach, sender, enqueue
    }
}
