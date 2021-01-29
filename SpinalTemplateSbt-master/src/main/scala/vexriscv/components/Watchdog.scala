package vexriscv.components


import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.apb.{Apb3, Apb3Config}
import spinal.lib.fsm._

// Watchdog component

class Watchdog(apb3Config: Apb3Config, counter_trigger_value: Int, counter_width: Int, PSEL_Nr: Int) extends Component {
  require(apb3Config.dataWidth == 32)
  // maybe need other stuff

  val io = new Bundle {
    val apb = slave(Apb3(apb3Config))
    val interrupt = out Bool
  }

  val logic = new Area {
    val counter     = Reg(UInt(counter_width bits)) init(0)
    val reset_counter = Bool
    val trigger_value = Reg(UInt(counter_width bits)) init(counter_trigger_value)
    val set_trigger_value = UInt(counter_width bits)
    val status = Reg(Bool) init(True)
    val set_status = Bool

    when(set_trigger_value =/= 0){
      trigger_value := set_trigger_value
    }

    status := set_status

    io.interrupt := False

    val fsm = new StateMachine {
      val state_Running = new State with EntryPoint
      val state_Paused = new State

      state_Running
        .onEntry{
          counter := 0
        }
        .whenIsActive {
          counter := counter + 1
          // reset if commanded to do so
          when(reset_counter){
            counter := 0
          }
          // trigger the interrupt
          when(counter === trigger_value){
            io.interrupt := True
            counter := 0
          }
          when(!status){
            goto(state_Paused)
          }
        }
      state_Paused
        .whenIsActive{
          when(status){
            goto(state_Running)
          }
        }
    }
  }

  val control = new Area {
    // placeholder for PREADY
    io.apb.PREADY := False
    io.apb.PRDATA := 0

    logic.reset_counter := False
    logic.set_trigger_value := 0
    logic.set_status := logic.status

    val is_initiaded = Reg(Bool) init(False)
    when(io.apb.PSEL(PSEL_Nr) && io.apb.PENABLE){
      // We're in the second cycle
      when(is_initiaded === True){
        io.apb.PREADY := True
        is_initiaded := False
        when(io.apb.PWRITE){
          // Master writes to Watchdog
          switch(io.apb.PADDR) {
            is(0) { // Reset the counter
              when (io.apb.PWDATA(0) === True ){
                logic.reset_counter := True
              }
            }
            is(1){ // activate / deactivate
              logic.set_status := io.apb.PWDATA(0)
            }
            is(2) { // Set the trigger value
              logic.set_trigger_value := io.apb.PWDATA.asUInt.resized
            }
          }
        }.otherwise{ // Read operation
          switch(io.apb.PADDR) {
            is(1){ // activate / deactivate
              io.apb.PRDATA := logic.status.asBits(32 bits)
            }
            is(2) { // Read the trigger value
              io.apb.PRDATA := logic.trigger_value.asBits.resize(32 bits)
            }
          }
        }
        // We're in the first cycle
      }.otherwise{
        io.apb.PREADY := True
        is_initiaded := True
      }
    }
  }
}