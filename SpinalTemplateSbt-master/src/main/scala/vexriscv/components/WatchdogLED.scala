package vexriscv.components


import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.apb.{Apb3, Apb3Config}
import spinal.lib.fsm._

class WatchdogLED(apb3Config: Apb3Config, counter_trigger_value: BigInt, counter_width: Int, frequency: Int, active_duration: Double) extends Component {
  val io = new Bundle {
    val apb = slave(Apb3(apb3Config))
    val interrupt = out Bool
  }
  // Subcomponents
  val watchdog = new Watchdog(apb3Config, counter_trigger_value, counter_width)
  val delay = new Delay(frequency, active_duration)

  // Connections
  // APB
  watchdog.io.apb <> io.apb
  //interrupt
  delay.io.input := watchdog.io.interrupt
  //output
  io.interrupt := delay.io.led
}

object Main_WatchdogLED {
  def main(args: Array[String]) {
    val apb3Config = Apb3Config(
      addressWidth  = 32,
      dataWidth     = 32,
      selWidth      = 1,
      useSlaveError = false
    )
    SpinalVhdl(new WatchdogLED(
      apb3Config = apb3Config,
      counter_trigger_value = 4000000000L,
      counter_width = 32 ,
      frequency = 100000000,
      active_duration = 1
    ))
  }
}
