package vexriscv.components

import spinal.core.Component
import spinal.core._
import spinal.core.sim._

class WatchdogTester(io_map : scala.collection.immutable.Map[String,spinal.core.BaseType], obj : vexriscv.components.Watchdog) extends APBTester(io_map, obj){
  def set_trigger_value(new_trigger_value : BigInt) : Unit = {
    write(2,new_trigger_value)
  }

  def reset_counter() : Unit = {
    write(0,1)
  }

  def activate_watchdog() : Unit = {
    write(1,1)
  }

  def deactivate_watchdog() : Unit = {
    write(1,0)
  }

  def get_trigger_value() : BigInt =  {
    read(address = 2)
  }

  def is_active() : Boolean = {
    read(address = 1) == 1
  }
}
