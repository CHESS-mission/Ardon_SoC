package vexriscv.SoC_Test

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.apb.{Apb3, Apb3Config}
import spinal.lib.fsm._

// Watchdog component

class BlinkingLED() extends Component {

  val io = new Bundle {
    val led5 = out Bool
  }

  val led5_tmp = Reg(Bool) init True
  io.led5 := led5_tmp


  val logic = new Area {
    val fsm = new StateMachine {

      val stateOn = new StateDelay(cyclesCount = 32000000) with EntryPoint
      val stateOff = new StateDelay(cyclesCount = 32000000)

      stateOn
        .onEntry{
          led5_tmp := True
        }
        .whenCompleted {
          goto(stateOff)
        }
      stateOff
        .onEntry{
          led5_tmp := False
        }
        .whenCompleted {
          goto(stateOn)
        }
    }
  }
}