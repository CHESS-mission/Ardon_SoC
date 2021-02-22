package vexriscv.components

import spinal.core._
import spinal.core.sim._
import spinal.lib.bus.amba3.apb.Apb3Config
import spinal.lib.com.uart.{Apb3UartCtrl, UartCtrlGenerics, UartCtrlInitConfig, UartCtrlMemoryMappedConfig, UartParityType, UartStopType}

object UART_Sim {
  def main(args: Array[String]): Unit = {
    // The APB3 config is defined inside the Apb3UartCtrl class definition. We might need to change that
    // This is the uart config as used in Briey

    val uartCtrlConfig = UartCtrlMemoryMappedConfig(
      uartCtrlConfig = UartCtrlGenerics(
        dataWidthMax = 8,
        clockDividerWidth = 23, // Value chosen such that no  errrors appear :D
        preSamplingSize = 1,
        samplingSize = 3,
        postSamplingSize = 1
      ),
      initConfig = UartCtrlInitConfig(
        baudrate = 115200, // as needed
        dataLength = 7, //7 => 8 bits
        parity = UartParityType.NONE,
        stop = UartStopType.TWO
      ),
      busCanWriteClockDividerConfig = false,
      busCanWriteFrameConfig = false,
      txFifoDepth = 32, // Value choosen by me, no clue if it's a good choice
      rxFifoDepth = 32
    )
    val spinalConfig = SpinalConfig(defaultClockDomainFrequency=FixedFrequency(50 MHz))

    val compiled = SimConfig.withConfig(spinalConfig).withWave.compile(new Apb3UartCtrl(uartCtrlConfig))

    compiled.doSim("Signal Identification") { dut =>
      val name = "Signal Identification"
      val start_str = "[" + name + "] "
      val io = dut.io.flatten.map(e => e.getName().replace("io_", "") -> e).toMap
      println(start_str + "The signals are:")
      io foreach { case (key, value) => printf("[" + name + "]   " + key + " : " + value + "\n") }

      val tester = new UARTTester(uartCtrlConfig.initConfig.baudrate,io, dut)
      dut.clockDomain.forkStimulus(period = 10)
      dut.clockDomain.waitRisingEdge(10)
      simSuccess
    }

    compiled.doSim("Receive and send waveform") { dut =>
      val name = "Receive / send waveform"
      val start_str = "[" + name + "] "
      val io = dut.io.flatten.map(e => e.getName().replace("io_", "") -> e).toMap
      println(Console.YELLOW + name + "This test is only for the waveform" + Console.RESET)

      val tester = new UARTTester(uartCtrlConfig.initConfig.baudrate, io, dut)
      tester.init()

      // Initialize clock
      dut.clockDomain.forkStimulus(period = 10)
      dut.clockDomain.waitRisingEdge(10)

      dut.clockDomain.waitRisingEdge(10000)

      var packet = new UARTPacket(0)

      for(data<-0 to 255){
        packet = new UARTPacket(data)
        tester.sendPayload(data)
        tester.receive_uart_packet(packet)
        dut.clockDomain.waitRisingEdge(20000)
      }
      simSuccess()
    }

    compiled.doSim("Receive to fifo") { dut =>
      val name = "Receive to fifo"
      val start_str = "[" + name + "] "
      val io = dut.io.flatten.map(e => e.getName().replace("io_", "") -> e).toMap

      val tester = new UARTTester(uartCtrlConfig.initConfig.baudrate, io, dut)
      tester.init()

      // Initialize clock
      dut.clockDomain.forkStimulus(period = 10)
      dut.clockDomain.waitRisingEdge(10)

      dut.clockDomain.waitRisingEdge(10000)

      var packet = new UARTPacket(0)
      var data = 0

      // receive data
      var sent_data =  List[Int]()
      for(i<-0 to 20){
        data = scala.util.Random.nextInt(255)
        sent_data = sent_data :+ data
        packet = new UARTPacket(data)
        tester.receive_uart_packet(packet)
        dut.clockDomain.waitRisingEdge(20000)
      }

      // read data from fifo
      for(expected_data<-sent_data){
        val received = tester.readReceivedPacket()
        print(start_str + "received " + received + " , expected " + expected_data)
        if (received == expected_data) print("ok") else {print("failed"); simFailure()}
        print("\n")
        dut.clockDomain.waitRisingEdge(1000)
      }

      simSuccess()
    }
  }
}
