package dcs.client

import dcs.common.Task
import actors.Actor
import java.io.Serializable
import java.util.{UUID, TreeMap}
import java.util.concurrent.{Executors, Future}

// TODO move some of these messages used by more than one actor (not necessarily) to a separate file
case class Ping(serverID: UUID)
case class Answer(taskID: Int, result: Serializable)
case class Sleep(seconds: Int)
case class DoTask(taskID: Int)

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
        case ping: Ping =>
          taskDistributor ! ping
          val server = sender
          receive {
            case Sleep(seconds) =>
              server ! seconds
            case DoTask(taskID) =>
              server ! IdentifiableTask(tasks.get(taskID).task, taskID)
          }
        case Answer(taskID, result) =>
          tasks.get(taskID).setResult(result)
          taskDistributor ! TaskDone(taskID)
      }
    }
  }
}

object DistributedComputeService {
  private[this] val dcs = new DistributedComputeService
  Executors.newSingleThreadExecutor().submit(new ServerCommunicator(dcs))

  def submit[T <: Serializable](task: Task[T]): Future[T] = {
    dcs.submit(task)
  }
}