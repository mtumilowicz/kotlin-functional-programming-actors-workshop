package answers.core.task

data class TaskIndex(val raw: Int) : Comparable<TaskIndex> {

    override fun compareTo(other: TaskIndex): Int = raw.compareTo(other.raw)
}