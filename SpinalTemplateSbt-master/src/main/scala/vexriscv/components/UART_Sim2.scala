package vexriscv.components

import spinal.core._
import spinal.core.sim._
import spinal.lib.bus.amba3.apb.Apb3Config
import spinal.lib.com.uart.{Apb3UartCtrl, UartCtrlGenerics, UartCtrlMemoryMappedConfig}

object UART_Sim2 {
  def main(args: Array[String]): Unit = {
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

    val spinalConfig = SpinalConfig(defaultClockDomainFrequency=FixedFrequency(50 MHz))
    val compiled = SimConfig.withConfig(spinalConfig).withWave.compile(new UART(uartCtrlConfig))

    compiled.doSim("Signal Identification") { dut =>
      val name = "Signal Identification"
      val start_str = "[" + name + "] "
      val io = dut.io.flatten.map(e => e.getName().replace("io_", "") -> e).toMap
      println(start_str + "The signals are:")
      io foreach { case (key, value) => printf("[" + name + "]   " + key + " : " + value + "\n") }

      val tester = new UARTTester(io, dut)
      dut.clockDomain.forkStimulus(period = 20)
      dut.clockDomain.waitRisingEdge(10)
      simSuccess
    }

    compiled.doSim("Test 1") { dut =>
      val name = "Test 1"
      val start_str = "[" + name + "] "
      val io = dut.io.flatten.map(e => e.getName().replace("io_", "") -> e).toMap

      val tester = new UARTTester(io, dut)

      // Initialize clock
      dut.clockDomain.forkStimulus(period = 20)
      dut.clockDomain.waitRisingEdge(10)

      dut.clockDomain.waitRisingEdge(100000)

      /*
      tester.setBaudrate(200)
      print("Reading baudrate: " + (tester read tester.REG_BAUD_RATE))
      dut.clockDomain.waitRisingEdge(10000)

      tester.setBaudrate(500)
      print("Reading baudrate: " + (tester read tester.REG_BAUD_RATE))
      dut.clockDomain.waitRisingEdge(10000)

      tester.setBaudrate(20)
      print("Reading baudrate: " + (tester read tester.REG_BAUD_RATE))
      dut.clockDomain.waitRisingEdge(10000)
*/
/*
      for (b <- 0 to 7){
        println("baud rate:" + (tester read 4))
      }
*/

/*
      for(d <- 0  to 10){
        // send packet
        var packet = new UARTPacket(data = d*2)
        tester.receive_uart_packet(packet)
        dut.clockDomain.waitRisingEdge(100)
        // Read Data
        println( start_str + "Reading from UART:" +  tester.toBinaryList(tester read 4))
        dut.clockDomain.waitRisingEdge(10)
      }
*/

      simSuccess()
    }
  }
}
