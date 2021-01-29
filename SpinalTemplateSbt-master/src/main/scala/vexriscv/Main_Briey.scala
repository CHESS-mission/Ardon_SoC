package vexriscv

import spinal.core.SpinalVhdl
import vexriscv._
import vexriscv.demo.Briey
import vexriscv.demo.BrieyConfig

object Main_Briey {
  // Let's go
  def main(args: Array[String]) {
    SpinalVhdl(new Briey(BrieyConfig.default))
  }
}