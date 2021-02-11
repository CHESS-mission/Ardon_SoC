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
      val io = dut.io.flatten.map(e => e.getName().replace("io_", "") -> e).toMap
      println("[" + name + "]   " + "The signals are:")
      io foreach { case (key, value) => printf("[" + name + "]   " + key + " : " + value + "\n") }
      simSuccess
    }
  }
}
