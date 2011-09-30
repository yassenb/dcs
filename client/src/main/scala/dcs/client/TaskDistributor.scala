package dcs.client

import actors.Actor
import java.util.{NoSuchElementException, LinkedList, Deque, UUID}
import collection.mutable.{ListBuffer, Buffer, HashSet, HashMap}
import dcs.common.Logging

case class NewTask(id: Int)
case class TaskDone(id: Int)

class TaskDistributor extends Actor with Logging {
  private[this] val tasks: Deque[Int] = new LinkedList[Int]
  private[this] val tasksInProgress = new HashMap[Int, Buffer[UUID]]
  private[this] val workers = new HashSet[UUID]

  def act() {
    loop {
      react {
        case Ping(serverID) =>
          logger.debug("got ping")
          if (workers.contains(serverID) || tasks.isEmpty) {
            // TODO think about changing the value
            logger.debug("replying with sleep")
            reply(Sleep(1))
          } else {
            val task = tasks.removeFirst()
            tasks.addLast(task)

            val buffer = try {
              tasksInProgress(task)
            } catch {
              case _: NoSuchElementException =>
                val buffer = new ListBuffer[UUID]
                tasksInProgress += ((task, buffer))
                buffer
            }
            buffer += serverID

            workers += serverID

            logger.debug("replying with task " + task)
            reply(DoTask(task))
          }
        case NewTask(taskID) =>
          logger.debug("got new task " + taskID)
          tasks.addFirst(taskID)
        case TaskDone(taskID) =>
          logger.debug("was notified that task %d finished".format(taskID))
          tasks.remove(taskID)
          val freeWorkers = tasksInProgress.remove(taskID)
          freeWorkers.foreach(workers --= _)
      }
    }
  }
}