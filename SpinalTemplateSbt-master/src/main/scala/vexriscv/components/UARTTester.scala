package vexriscv.components

import spinal.core._
import spinal.core.sim._

class BinaryOperations {
  def toBinaryList(value: BigInt, length:Int=32): List[Int] = {
    def step(value: BigInt, li: List[Int]): List[Int] = {
      if (value == BigInt(1)) return (li :+ 1)
      if (value == BigInt(0)) return (li :+ 0)
      else step(value/2, li:+(value%2).toInt)
    }
    def addLeadingZerosAndReverse(li: List[Int], len: Int): List[Int] = {
      if (li.length > len) return li.reverse.drop(li.length-len)
      if (li.length == len) li.reverse else addLeadingZerosAndReverse(li :+ 0, len)
    }
    addLeadingZerosAndReverse(step(value, List()), length)
  }

  def binaryListToInt(li: List[Int]): Int = {
    if(li.length >=1) (scala.math.pow(2, li.length-1) * li.head + binaryListToInt(li.tail)).toInt else 0
  }
}

class UARTPacket(data: Int) extends BinaryOperations {
  var binary = List[Int]()
  val payload = data
  def add_start_bit() = {binary = binary :+ 0}
  def add_payload() = {binary = List.concat(binary, toBinaryList(data, 8).reverse)}
  def add_stop_bit() = {binary = List.concat(binary, List(1)) }
  def add_parity_bit() = {
    // 0: even, 1 odd
    var parity = 0
    for(bit <- toBinaryList(data, 8)){
      if (bit == 1) parity += 1
    }
    binary = binary :+ (parity%2)
  }
  def createPacket() = {
    add_start_bit()
    add_payload()
    //add_parity_bit()
    add_stop_bit()
  }
}


class UARTTester(baudrate: Int, io_map : scala.collection.immutable.Map[String,spinal.core.BaseType], obj : Component) extends APBTester(io_map, obj) {
  val REG_BAUD_RATE = 8
  val REG_RECEIVED_DATA = 0
  val RECEIVED_PACKET_VALID_BIT = 15 // from front === 32-offset-1

  var baud_rate = BigInt(0)
  val ns_per_bit = (1000000000.0/(115200*2)).toInt // factpr 1/2 needed to have the desired frequency, no clue why

  def init() = {
    io("uart_rxd").assignBigInt(1) // Default HIGH
  }
  def receive_uart_packet(packet: UARTPacket): Unit ={
    packet.createPacket()
    for(bit <- packet.binary){
      io("uart_rxd").assignBigInt(bit)
      sleep(ns_per_bit)
    }
    obj.clockDomain.waitRisingEdge()
  }

  def readReceivedPacket(): Int = {
    val data = read(REG_RECEIVED_DATA)
    val binary = toBinaryList(data)
    if(binary(RECEIVED_PACKET_VALID_BIT) == 1){
      binaryListToInt(binary.drop(24))
    } else {
      println("received non valid packet")
      0
    }
  }

  def sendPayload(payload: Int): Unit = write(0, payload)

}
