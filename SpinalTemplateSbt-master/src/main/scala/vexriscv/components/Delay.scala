package vexriscv.components

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.apb.{Apb3, Apb3Config}
import spinal.lib.fsm._

// This block is used to test the Watchdog on a real board

class Delay(frequency: Int, active_duration: Double) extends Component{
  val io = new Bundle {
    val input = in Bool
      val led = out Bool
    }

  val ticks = (active_duration * frequency).toInt
  val led_tmp = Reg(Bool) init(False)
  io.led := led_tmp
  val fsm = new StateMachine {
    val state_waiting = new State with EntryPoint
    val state_active = new StateDelay(ticks)

    state_waiting
      .whenIsActive {
        when(io.input) {
          goto(state_active)
        } otherwise {
          led_tmp := False
        }
      }
    state_active
      .whenIsActive(
        led_tmp := True
      )
      .whenCompleted(goto(state_waiting))
  }
}

object Main_Delay {
  // Let's go
  def main(args: Array[String]) {
    SpinalVhdl(new Delay(
      frequency = 100000000,
      active_duration = 1
    ))
  }
}
