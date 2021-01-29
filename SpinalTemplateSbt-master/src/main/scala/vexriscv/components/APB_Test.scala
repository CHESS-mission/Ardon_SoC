package vexriscv.components

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.apb.{Apb3, Apb3Config, Apb3Decoder}


class APB_Test(apb3Config: Apb3Config) extends Component{
  val io = new Bundle {
    val apb = slave(Apb3(apb3Config))
    val interrupt = out Bool
    val interrupt2 = out Bool
  }

  // components on the bus
  val watchdog = new Watchdog(
    apb3Config = apb3Config,
    counter_trigger_value = 10,
    counter_width = 8,
    PSEL_Nr = 0
  )
  val watchdog2 = new Watchdog(
    apb3Config = apb3Config,
    counter_trigger_value = 10,
    counter_width = 8,
    PSEL_Nr = 1
  )

  // connect the slave inputs
  watchdog.io.apb.PSEL := io.apb.PSEL
  watchdog.io.apb.PADDR := io.apb.PADDR
  watchdog.io.apb.PENABLE := io.apb.PENABLE
  watchdog.io.apb.PWRITE := io.apb.PWRITE
  watchdog.io.apb.PWDATA := io.apb.PWDATA

  watchdog2.io.apb.PSEL := io.apb.PSEL
  watchdog2.io.apb.PADDR := io.apb.PADDR
  watchdog2.io.apb.PENABLE := io.apb.PENABLE
  watchdog2.io.apb.PWRITE := io.apb.PWRITE
  watchdog2.io.apb.PWDATA := io.apb.PWDATA

  //connect slave outputs
  io.apb.PREADY := False
  io.apb.PRDATA := 0

  when(watchdog.io.apb.PREADY){
    io.apb.PREADY := watchdog.io.apb.PREADY
    io.apb.PRDATA := watchdog.io.apb.PRDATA
  }
  when(watchdog2.io.apb.PREADY){
    io.apb.PREADY := watchdog2.io.apb.PREADY
    io.apb.PRDATA := watchdog2.io.apb.PRDATA
  }

  // connect other output signals
  io.interrupt := watchdog.io.interrupt
  io.interrupt2 := watchdog2.io.interrupt



}