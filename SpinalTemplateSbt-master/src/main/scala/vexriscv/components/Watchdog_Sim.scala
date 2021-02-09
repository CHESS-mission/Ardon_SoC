package vexriscv.components


import org.scalatest.FunSuite
import spinal.core._
import spinal.core.sim._
import spinal.lib.bus.amba3.apb.Apb3Config

import scala.util.Random

/*
The apb methods used in this test are not always the same! I used apbRead, apbWrite ... for the first couple of tests.
Later on, I wrote the APBTester class which I used afterwards. As the first part is working fine I saw no reason to change it.
 */


object Watchdog_Sim{
  // APB functions. Source: https://github.com/SpinalHDL/SpinalWorkshop/blob/workshop/src/test/scala/workshop/pwm/ApbPwmTester.scala
  // modified with io and dut as arguments such that they can be placed outside the tests
  def apbRead(address : BigInt, io : scala.collection.immutable.Map[String,spinal.core.BaseType], dut : vexriscv.components.Watchdog) : BigInt = {
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

  def apbReadAssert(address : BigInt, data : BigInt, mask : BigInt, message : String, io : scala.collection.immutable.Map[String,spinal.core.BaseType], dut : vexriscv.components.Watchdog) : Unit =  assert((apbRead(address, io, dut) & mask) == data, message)

  def apbWrite(address : BigInt, data : BigInt, io : scala.collection.immutable.Map[String,spinal.core.BaseType], dut : vexriscv.components.Watchdog) : Unit = {
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

  def set_trigger_value(new_trigger_value : Int, io : scala.collection.immutable.Map[String,spinal.core.BaseType], dut : vexriscv.components.Watchdog) : Unit = {
    apbWrite(2,new_trigger_value, io, dut)
  }

  def reset_counter(io : scala.collection.immutable.Map[String,spinal.core.BaseType], dut : vexriscv.components.Watchdog) : Unit = {
    apbWrite(0,1, io, dut)
  }

  def activate_watchdog(io : scala.collection.immutable.Map[String,spinal.core.BaseType], dut : vexriscv.components.Watchdog) : Unit = {
    apbWrite(1,1, io, dut)
  }

  def deactivate_watchdog(io : scala.collection.immutable.Map[String,spinal.core.BaseType], dut : vexriscv.components.Watchdog) : Unit = {
    apbWrite(1,0, io, dut)
  }


  def main(args: Array[String]): Unit = {
    val apb3Config = Apb3Config(
      addressWidth = 8,
      dataWidth = 32,
      selWidth = 1,
      useSlaveError = false
    )
    val counter_width = 32
    val compiled = SimConfig.withWave.compile(new Watchdog(apb3Config, counter_trigger_value = 10, counter_width = counter_width))

    compiled.doSim("Signal Identification") { dut =>
      // Simulation code here
      val name = "Signal Identification"
      val io = dut.io.flatten.map(e => e.getName().replace("io_", "") -> e).toMap
      println("[" + name + "]   " + "The signals are:")
      io foreach { case (key, value) => printf("[" + name + "]   " + key + " : " + value + "\n") }
      simSuccess
    }

    compiled.doSim("Reset Interrupt Test") { dut =>
      // Simulation code here
      val name = "Reset Interrupt Test"
      val io = dut.io.flatten.map(e => e.getName().replace("io_", "") -> e).toMap
      printf(Console.YELLOW + "[" + name + "] Be aware that signals are randomized between write and read cycles \n" + Console.RESET)

      // initialize
      dut.clockDomain.forkStimulus(period = 10)
      sleep(200)
      // Reset the counter a couple of times
      for (i <- 0 to 100) {
        if (i % 9 == 0) {
          reset_counter(io, dut)
        } else {
          sleep(10)
        }
        assert(!dut.io.interrupt.toBoolean, "Interrupt is 1, expected zero")
      }
      simSuccess
    }

    compiled.doSim("Change counter_trigger_value") { dut =>
      // Simulation code here

      val name = "Change counter_trigger_value"
      val number_of_runs = 20
      val io = dut.io.flatten.map(e => e.getName().replace("io_", "") -> e).toMap
      // initialize
      dut.clockDomain.forkStimulus(period = 10)
      sleep(200)

      var new_trigger_value = 0
      for (j <- 0 to number_of_runs) {
        printf("[" + name + "] run " + j + " of " + number_of_runs + "\n")
        new_trigger_value = scala.util.Random.nextInt(10000) + 5
        set_trigger_value(new_trigger_value, io, dut)
        // Reset the counter
        reset_counter(io, dut)
        dut.clockDomain.waitRisingEdge(new_trigger_value +1)
        assert(dut.io.interrupt.toBoolean, "Expected interrupt = 1, got interrupt = 0")
      }
      simSuccess
    }

    compiled.doSim("Activate and Deactivate Watchdog") { dut =>
      // Simulation code here

      val name = "Activate and Deactivate Watchdog"
      val number_of_runs = 5
      val io = dut.io.flatten.map(e => e.getName().replace("io_", "") -> e).toMap
      // initialize
      dut.clockDomain.forkStimulus(period = 10)
      sleep(200)

      for (j <- 0 to number_of_runs) {
        printf("[" + name + "] run " + j + " of " + number_of_runs + "\n")
        deactivate_watchdog(io, dut)
        for (i <- 0 to 100) {
          dut.clockDomain.waitRisingEdge()
          assert(!dut.io.interrupt.toBoolean, "Interrupt = 1 but the watchdog is deactivated")
        }
        activate_watchdog(io, dut)
        var i = 0
        while (!dut.io.interrupt.toBoolean) {
          dut.clockDomain.waitRisingEdge()
          i += 1
          if (i > 1000) {
            simFailure("Watchdog is activated but there was no interrupt in 1000 clock cycles")
          }
        }
      }
      simSuccess
    }

    /*
    ####################################################################################################################
     ------------------------------ Using the new tester class from here -----------------------------------------------
    ####################################################################################################################
     */
    compiled.doSim("Combinational Test") { dut =>
      val name = "Combinational Test"
      val io = dut.io.flatten.map(e => e.getName().replace("io_", "") -> e).toMap
      val tester = new WatchdogTester(io, dut)
      var trigger_value = BigInt(100)

      def check_for_interrupt(): Unit = {
        tester.reset_counter()
        tester.wait_check(trigger_value, () => !dut.io.interrupt.toBoolean)
        dut.clockDomain.waitRisingEdge()
        assert(dut.io.interrupt.toBoolean, "Interrupt should be one, is zero")
        dut.clockDomain.waitRisingEdge(20)
      }

      // initialize
      dut.clockDomain.forkStimulus(period = 10)
      sleep(200)

      // Test with initial trigger value
      printf("[" + name + "] Test with initial trigger value \n")
      trigger_value = tester.get_trigger_value()
      printf("[" + name + "] Initial trigger_value " + trigger_value + "\n")
      check_for_interrupt

      // Deactivate watchdog
      printf("[" + name + "] Deactivated \n")
      tester.deactivate_watchdog()
      assert(tester.wait_check(100, () => !dut.io.interrupt.toBoolean), "interrupt is 1 but watchdog is deactivated")
      trigger_value = 123
      tester.set_trigger_value(trigger_value)
      assert(tester.wait_check(100, () => !dut.io.interrupt.toBoolean), "interrupt is 1 but watchdog is deactivated")


      //Reactivate watchdog
      printf("[" + name + "] Reactivated \n")
      tester.activate_watchdog()
      assert(tester.wait_check(trigger_value-1, () => !dut.io.interrupt.toBoolean), "reset should be zero after reactivation")
      dut.clockDomain.waitRisingEdge(150)
      check_for_interrupt

      // check interrupt for random triggerValues
      for(i <- 0 to 100){
        trigger_value = scala.util.Random.nextInt(1000) + 5
        printf("[" + name + "] Trigger value " + trigger_value + " in run " + i + " \n")
        tester.set_trigger_value(trigger_value)
        dut.clockDomain.waitRisingEdge(5)
        check_for_interrupt
      }

      // Reading status
      tester.deactivate_watchdog()
      assert(!tester.is_active(), "Watchdog is deactivated but status is active")
      dut.clockDomain.waitRisingEdge()
      tester.activate_watchdog()
      assert(tester.is_active(), "Watchdog is activated but status is inactive")
      dut.clockDomain.waitRisingEdge()

      simSuccess()
    }
  }
}