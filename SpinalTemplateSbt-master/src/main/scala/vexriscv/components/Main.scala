package vexriscv.components

import spinal.core.SpinalVhdl
import spinal.lib.bus.amba3.apb.Apb3Config

object Main {
  // Let's go
  def main(args: Array[String]) {
    val apb3Config = Apb3Config(
      addressWidth  = 32,
      dataWidth     = 32,
      selWidth      = 1,
      useSlaveError = false
    )
    SpinalVhdl(new Watchdog(
      apb3Config = apb3Config,
      counter_trigger_value = 4000000000L,
      counter_width = 32 ,
      PSEL_Nr  = 0
    ))
  }
}