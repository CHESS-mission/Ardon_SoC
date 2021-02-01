package vexriscv.components


import org.scalatest.FunSuite
import spinal.core._
import spinal.core.sim._
import spinal.lib.bus.amba3.apb.Apb3Config

import scala.util.Random

object APB_Sim {
  def main(args: Array[String]): Unit = {
    val apb3Config = Apb3Config(
      addressWidth  = 8,
      dataWidth     = 32,
      selWidth      = 4,
      useSlaveError = false
    )
    val compiled = SimConfig.withWave.compile(new APB_Test(apb3Config))

    compiled.doSim("test A"){ dut =>
      // Simulation code here
      simSuccess
    }

    compiled.doSim("test B"){ dut =>
      // Simulation code here
      simSuccess
    }
  }
}