package dcs.client

import dcs.common.Task
import java.util.concurrent.Future
import actors.Actor
import java.io.Serializable
import java.util.{UUID, TreeMap}

case class Ping(serverID: UUID)
case class Answer(taskID: Int, result: Serializable)

class DistributedComputeService private (taskDistributor: TaskDistributor = new TaskDistributor) extends Actor {
  private[this] val tasks = new TreeMap[Int, SubmittedTask[Serializable]]()
  private[this] var taskCount = 0

  taskDistributor.start()
  start()
  
  def submit[T <: Serializable](task: Task[T]): java.util.concurrent.Future[T] = {
    (this !? task).asInstanceOf[java.util.concurrent.Future[T]]
  }

  def act() {
    loop {
      react {
        // TODO think about eliminating the type erasure here
        case task: Task[Serializable] =>
          val st = new SubmittedTask(task)
          tasks.put(taskCount, st)
          taskDistributor ! NewTask(taskCount)
          taskCount += 1
          reply(st.getResult)
        case Ping(serverID) =>
          // TODO ask the task distributor and reply with either number of seconds or task from map
          reply(3)
        case Answer(taskID, result) =>
          tasks.get(taskID).setResult(result)
          taskDistributor ! TaskDone(taskID)
      }
    }
  }
}

object DistributedComputeService {
  private[this] val dcs = new DistributedComputeService
  (new ServerCommunicator(dcs)).start()

  def submit[T <: Serializable](task: Task[T]): Future[T] = {
    dcs.submit(task)
  }
}