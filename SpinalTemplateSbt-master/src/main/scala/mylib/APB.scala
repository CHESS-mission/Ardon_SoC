package mylib


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