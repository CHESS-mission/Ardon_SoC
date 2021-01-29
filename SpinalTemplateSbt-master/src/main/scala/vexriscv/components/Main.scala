package vexriscv.components

import spinal.core.SpinalVhdl
import spinal.lib.bus.amba3.apb.Apb3Config

object Main {
  // Let's go
  def main(args: Array[String]) {
    val apb3Config = Apb3Config(
      addressWidth  = 8,
      dataWidth     = 32,
      selWidth      = 4,
      useSlaveError = false
    )
    SpinalVhdl(new APB_Test(
      apb3Config = apb3Config
    ))
  }
}