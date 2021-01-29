package vexriscv.components

import spinal.core._
import spinal.lib._

// APB definition

case class ApbConfig(addressWidth : Int,
                     dataWidth    : Int,
                     selWidth     : Int)

case class Apb(config: ApbConfig) extends Bundle with IMasterSlave {
  val PSEL       = Bits(config.selWidth bits)
  val PENABLE    = Bool
  val PADDR      = UInt(config.addressWidth bit)
  val PWRITE     = Bool
  val PWDATA     = Bits(config.dataWidth bit)
  val PRDATA     = Bits(config.dataWidth bit)
  val PREADY     = Bool

  override def asMaster(): Unit = {
    out(PADDR,PSEL,PENABLE,PWRITE,PWDATA)
    in(PREADY,PRDATA)
  }
}

class APB_Test(apbConfig: ApbConfig) extends Component{
  val io = new Bundle {
    val apb = slave(Apb(apbConfig))
    val interrupt = out Bool
  }
  val watchdog = new Watchdog(
    apbConfig = apbConfig,
    counter_trigger_value = 10,
    counter_width = 8,
    PSEL_Nr = 1
  )
  // connect the bus
  watchdog.io.apb <> io.apb

  //connect outputs
  io.interrupt := watchdog.io.interrupt
}