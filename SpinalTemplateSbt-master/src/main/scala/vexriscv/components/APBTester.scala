package vexriscv.components

import spinal.core._
import spinal.core.sim._
import spinal.lib.bus.amba3.apb.Apb3Config

class APBWritePacket(addr: BigInt, dat : BigInt){
  val address = addr
  val data =  dat
}
class APBTester(io_map : scala.collection.immutable.Map[String,spinal.core.BaseType], obj : Component) {
  val io = io_map
  val dut = obj

  def read(address : BigInt) : BigInt = {
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

  def readAssert(address : BigInt, data : BigInt, mask : BigInt, message : String) : Unit =  assert((read(address) & mask) == data, message)

  def write(address : BigInt, data : BigInt) : Unit = {
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

  def write(packet: APBWritePacket): Unit = write(packet.address, packet.data)
}
