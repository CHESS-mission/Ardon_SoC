package vexriscv.components


import org.scalatest.FunSuite
import spinal.core._
import spinal.core.sim._
import spinal.lib.bus.amba3.apb.Apb3Config

import scala.util.Random

/*
The apb methods used in this test are not always the same! I used apbRead, apbWrite ... for the first couple of tests.
Later on, I wrote the APBTester class which I used afterwards. As the first part is working fine I saw no reason to change it.
 */


object tmp{
  def main(args: Array[String]): Unit = {
    val apb3Config = Apb3Config(
      addressWidth  = 8,
      dataWidth     = 32,
      selWidth      = 1,
      useSlaveError = false
    )
    val counter_width = 8
    val compiled = SimConfig.withWave.compile(new Watchdog(apb3Config, counter_trigger_value=10, counter_width=counter_width))

    compiled.doSim("Signal Identification"){ dut =>
      // Simulation code here
      val name = "Signal Identification"
      val io = dut.io.flatten.map(e => e.getName().replace("io_","") -> e).toMap
      println("[" + name + "]   " + "The signals are:")
      io foreach {case (key, value) => printf("[" + name + "]   " + key + " : " + value + "\n") }
      simSuccess
    }

    compiled.doSim("Test"){ dut =>
      // Simulation code here
      val name = "Test"
      val io = dut.io.flatten.map(e => e.getName().replace("io_","") -> e).toMap
      printf(Console.YELLOW + "[" + name + "] Be aware that signals are randomized between write and read cycles \n" + Console.RESET )
      val tester = new APBTester(io, dut)
      // initialize
      dut.clockDomain.forkStimulus(period = 10)
      sleep(200)
      // Reset the counter a couple of times
      val resetPacket = new APBWritePacket(0, 1)
      for(i <- 0 to 100){
        if(i % 9 == 0){
          tester write resetPacket
        }else{
          sleep(10)
        }
        assert(!dut.io.interrupt.toBoolean, "Interrupt is 1, expected zero")
      }
      simSuccess
    }
  }
}