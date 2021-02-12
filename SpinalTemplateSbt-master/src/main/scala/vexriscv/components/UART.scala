package vexriscv.components

import spinal.core._
import spinal.lib.bus.amba3.apb.Apb3
import spinal.lib.com.uart.{Apb3UartCtrl, Uart, UartCtrl, UartCtrlGenerics, UartCtrlMemoryMappedConfig, UartParityType, UartStopType}
import spinal.lib.{CounterFreeRun, Stream, master, slave}



class UART(config : UartCtrlMemoryMappedConfig) extends  Apb3UartCtrl(config){

  val firstrun = Reg(Bool) init(True)

  // allow override
  uartCtrl.io.config.frame.dataLength.allowOverride
  uartCtrl.io.config.frame.parity.allowOverride
  uartCtrl.io.config.frame.stop.allowOverride
  uartCtrl.io.config.allowOverride

  // initial settings, can be overwritten by apb during operation (that's why they are in this if block)
  when(firstrun){
    uartCtrl.io.config.frame.dataLength := 7  //8 bits
    uartCtrl.io.config.frame.parity := UartParityType.EVEN
    uartCtrl.io.config.frame.stop := UartStopType.TWO
    //uartCtrl.io.config.setClockDivider(921.6 kHz)
    uartCtrl.io.config.setClockDivider(921.6 kHz)

    firstrun := False
  }


  val switches = B"8'b11001100"

  //Write the value of switch on the uart each 4000 cycles
  val write = Stream(Bits(8 bits))
  uartCtrl.io.write.allowOverride
  uartCtrl.io.write.valid.allowOverride
  uartCtrl.io.write.payload.allowOverride
  write.valid := CounterFreeRun(2000).willOverflow
  write.payload := switches
  write >-> uartCtrl.io.write

  //Write the 0x55 and then the value of switch on the uart each 4000 cycles
  //  val write = Stream(Fragment(Bits(8 bits)))
  //  write.valid := CounterFreeRun(4000).willOverflow
  //  write.fragment := io.switchs
  //  write.last := True
  //  write.m2sPipe().insertHeader(0x55).toStreamOfFragment >> uartCtrl.io.write
}

