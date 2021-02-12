package vexriscv.components

import spinal.core.sim._
import spinal.lib.bus.amba3.apb.Apb3Config
import spinal.lib.com.uart.{Apb3UartCtrl, UartCtrlGenerics, UartCtrlMemoryMappedConfig}

object UART_Sim {
  def main(args: Array[String]): Unit = {
    // The APB3 config is defined inside the Apb3UartCtrl class definition. We might need to change that
    // This is the uart config as used in Briey
    val uartCtrlConfig = UartCtrlMemoryMappedConfig(
      uartCtrlConfig = UartCtrlGenerics(
        dataWidthMax = 8,
        clockDividerWidth = 20,
        preSamplingSize = 1,
        samplingSize = 5,
        postSamplingSize = 2
      ),
      txFifoDepth = 16,
      rxFifoDepth = 16
    )
    val compiled = SimConfig.withWave.compile(new Apb3UartCtrl(uartCtrlConfig))

    compiled.doSim("Signal Identification") { dut =>
      val name = "Signal Identification"
      val start_str = "[" + name + "] "
      val io = dut.io.flatten.map(e => e.getName().replace("io_", "") -> e).toMap
      println(start_str + "The signals are:")
      io foreach { case (key, value) => printf("[" + name + "]   " + key + " : " + value + "\n") }

      val tester = new UARTTester(io, dut)
      dut.clockDomain.forkStimulus(period = 10)
      dut.clockDomain.waitRisingEdge(10)
      simSuccess
    }

    compiled.doSim("Receive") { dut =>
      val name = "Receive"
      val start_str = "[" + name + "] "
      val io = dut.io.flatten.map(e => e.getName().replace("io_", "") -> e).toMap

      val tester = new UARTTester(io, dut)
      tester.init()

      // Initialize clock
      dut.clockDomain.forkStimulus(period = 10)
      dut.clockDomain.waitRisingEdge(10)

      // Set Baud Rate
      tester.setBaudrate(2)
      dut.clockDomain.waitRisingEdge(200)

      // send packet
      tester.write(0, BigInt("000000", 2))
      dut.clockDomain.waitRisingEdge(2000)

      tester.write(0, BigInt("111111", 2))
      dut.clockDomain.waitRisingEdge(10000)

      tester.write(0, BigInt("110111", 2))
      dut.clockDomain.waitRisingEdge(10000)

      tester.write(0, BigInt("100101", 2))
      dut.clockDomain.waitRisingEdge(10000)

      tester.write(0, BigInt("101101", 2))
      dut.clockDomain.waitRisingEdge(10000)
      /*
      tester.setBaudrate(2)
      print("Reading baudrate: " + (tester read tester.REG_BAUD_RATE))
      dut.clockDomain.waitRisingEdge(100)

      tester.setBaudrate(100)
      print("Reading baudrate: " + (tester read tester.REG_BAUD_RATE))
      dut.clockDomain.waitRisingEdge(1000)

      tester.setBaudrate(1000)
      print("Reading baudrate: " + (tester read tester.REG_BAUD_RATE))
      dut.clockDomain.waitRisingEdge(1000)
*/

      simSuccess()
    }
  }
}