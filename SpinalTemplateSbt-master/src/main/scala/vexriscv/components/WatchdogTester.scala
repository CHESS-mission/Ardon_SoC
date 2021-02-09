package vexriscv.components

import spinal.core.Component

class WatchdogTester(io_map : scala.collection.immutable.Map[String,spinal.core.BaseType], obj : vexriscv.components.Watchdog) extends APBTester(io_map, obj){
  def set_trigger_value(new_trigger_value : Int, io : scala.collection.immutable.Map[String,spinal.core.BaseType], dut : vexriscv.components.Watchdog) : Unit = {
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
}
