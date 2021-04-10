package vexriscv.SoC_Test

import vexriscv.plugin._
import vexriscv._
import vexriscv.ip.{DataCacheConfig, InstructionCacheConfig}
import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.apb._
import spinal.lib.bus.amba4.axi._
import spinal.lib.com.jtag.Jtag
import spinal.lib.com.uart.{Apb3UartCtrl, Uart, UartCtrlGenerics, UartCtrlMemoryMappedConfig}
import spinal.lib.graphic.RgbConfig
import spinal.lib.graphic.vga.{Axi4VgaCtrl, Axi4VgaCtrlGenerics, Vga}
import spinal.lib.io.TriStateArray
import spinal.lib.memory.sdram.SdramGeneration.SDR
import spinal.lib.memory.sdram._
import spinal.lib.memory.sdram.sdr.{Axi4SharedSdramCtrl, IS42x320D, SdramInterface, SdramTimings}
import spinal.lib.misc.HexTools
import spinal.lib.soc.pinsec.{PinsecTimerCtrl, PinsecTimerCtrlExternal}
import spinal.lib.system.debugger.{JtagAxi4SharedDebugger, JtagBridge, SystemDebugger, SystemDebuggerConfig}

import scala.collection.mutable.ArrayBuffer


class toplevel extends Component{
  val io = new Bundle{
    val reset = in Bool
    val clk = in Bool
    val led5 = out Bool
    val jtag = slave(Jtag())
    val uart = master(Uart())
    val timerExternal = in(PinsecTimerCtrlExternal())
    val coreInterrupt = in Bool


  }

  val briey_led = new Briey_LED(BrieyConfig.default)

  //Clocks / reset
  briey_led.io.asyncReset := io.reset
  briey_led.io.axiClk := io.clk
  val vgaClk     = in Bool

  // led5 - from the blinking led component
  io.led5 := briey_led.io.led5

  //Main components IO
  briey_led.io.jtag <> io.jtag
  briey_led.io.uart <> io.uart
  briey_led.io.timerExternal <> io.timerExternal
  briey_led.io.coreInterrupt := io.coreInterrupt

  // Components where the I/O ports are shortened
  briey_led.io.gpioA.read := 0
  //briey_led.io.gpioA.writeEnable := 0
  briey_led.io.gpioB.read := 0
  //briey_led.io.gpioB.writeEnable := 0
  briey_led.io.sdram.DQ.read := 0
  briey_led.io.vgaClk := io.clk

}

object toplevel{
  def main(args: Array[String]) {
    val config = SpinalConfig()
    config.generateVerilog({new toplevel()})
  }
}
