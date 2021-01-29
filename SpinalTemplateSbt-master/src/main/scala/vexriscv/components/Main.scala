package vexriscv.components

import spinal.core.SpinalVhdl

object Main {
  // Let's go
  def main(args: Array[String]) {
    val apbConfig = ApbConfig(
      addressWidth = 8,
      dataWidth = 32,
      selWidth = 4
    )
    SpinalVhdl(new APB_Test(
      apbConfig = apbConfig
    ))
  }
}