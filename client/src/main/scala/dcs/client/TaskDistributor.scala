package dcs.client

import actors.Actor

case class NewTask(id: Int)

class TaskDistributor extends Actor {
  def act() {
    loop {
      react {
        case NewTask(id: Int) => // TODO
      }
    }
  }
}