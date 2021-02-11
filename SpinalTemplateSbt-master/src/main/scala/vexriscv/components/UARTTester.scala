package vexriscv.components

import spinal.core._

class UARTTester(io_map : scala.collection.immutable.Map[String,spinal.core.BaseType], obj : Component) extends APBTester(io_map, obj) {
  def receive_uart_packet(packet: Int): Unit ={
    print("Sending to UART: " + packet.toBinaryString())
  }
}
