package mylib

import spinal.core.SpinalVhdl

object Main {
  // Let's go
  def main(args: Array[String]) {
    SpinalVhdl(new Watchdog(
      apbConfig = ApbConfig(
        addressWidth = 8,
        dataWidth = 32,
        selWidth = 4
      ),
      counter_trigger_value = 10,
      counter_width = 8,
      PSEL_Nr = 1
    ))
  }
}