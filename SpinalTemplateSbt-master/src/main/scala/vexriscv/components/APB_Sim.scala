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

    compiled.doSim("Signal Identification"){ dut =>
      // Simulation code here
      val name = "Signal Identification"
      val io = dut.io.flatten.map(e => e.getName().replace("io_","") -> e).toMap
      println("[" + name + "]   " + "The signals are:")
      io foreach {case (key, value) => printf("[" + name + "]   " + key + " : " + value + "\n") }
      simSuccess
    }

    compiled.doSim("Watchdog Interrupt Test"){ dut =>
      // Simulation code here
      val name = "Watchdog Interrupt Test"
      val io = dut.io.flatten.map(e => e.getName().replace("io_","") -> e).toMap

      // APB functions. Source: https://github.com/SpinalHDL/SpinalWorkshop/blob/workshop/src/test/scala/workshop/pwm/ApbPwmTester.scala
      def apbWrite(address : BigInt, data : BigInt) : Unit = {
        io("apb_PSEL").assignBigInt(1)
        io("apb_PENABLE").assignBigInt(0)
        io("apb_PWRITE").assignBigInt(1)
        io("apb_PADDR").assignBigInt(address)
        io("apb_PWDATA").assignBigInt(data)
        dut.clockDomain.waitSampling()
        io("apb_PENABLE").assignBigInt(1)
        dut.clockDomain.waitSamplingWhere(io("apb_PREADY").toBigInt == 1)
        io("apb_PSEL").assignBigInt(0)
        io("apb_PENABLE").randomize()
        io("apb_PADDR").randomize()
        io("apb_PWDATA").randomize()
        io("apb_PWRITE").randomize()
      }

      def apbRead(address : BigInt) : BigInt = {
        io("apb_PSEL").assignBigInt(1)
        io("apb_PENABLE").assignBigInt(0)
        io("apb_PADDR").assignBigInt(address)
        io("apb_PWRITE").assignBigInt(0)
        dut.clockDomain.waitSampling()
        io("apb_PENABLE").assignBigInt(1)
        dut.clockDomain.waitSamplingWhere(io("apb_PREADY").toBigInt == 1)
        io("apb_PSEL").assignBigInt(0)
        io("apb_PENABLE").randomize()
        io("apb_PADDR").randomize()
        io("apb_PWDATA").randomize()
        io("apb_PWRITE").randomize()
        io("apb_PRDATA").toBigInt
      }
      def apbReadAssert(address : BigInt, data : BigInt, mask : BigInt, message : String) : Unit =  assert((apbRead(address) & mask) == data, message)

      apbReadAssert(0x00,0x00000000,0x00000001,"read assert message")

      simSuccess
    }
  }
}