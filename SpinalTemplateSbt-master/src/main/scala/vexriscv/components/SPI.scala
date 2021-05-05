package mylib
import spinal.core._
import spinal.lib.fsm._
class SPI extends Component{

  val io = new Bundle{
    val clk = in Bool
    val reset = in Bool
    val cs = in Bool
    val rw = in Bool
    val addr = in UInt(2 bits)
    val data_in = in UInt(8 bits)
    val data_out = out UInt(8 bits)
    val irq = out Bool
    val spi_miso = in Bool
    val spi_mosi = out Bool
    val spi_clk = out Bool
    val spi_cs_n = out UInt(8 bits)
  }
  io.data_out := 255
  io.irq := False

  // Shift register
  val shift_reg = Reg(UInt(16 bits)) init(0)
  // Buffer to hold data to be sent
  val spi_data_buf = Reg(UInt(16 bits)) init(0)
  // Start transmission flag
  val start = Reg(Bool) init(False)
  // Number of bits transfered
  val count = Reg(UInt(4 bits)) init(0)
  // Buffered SPI clock
  val spi_clk_buf = Reg(Bool) init(False)
  // Buffered SPI clock output
  val spi_clk_out = Reg(Bool) init(False)
  // Previous SPI clock state
  val prev_spi_clk = Reg(Bool) init(False)
  // Number of clk cycles-1 in this SPI clock period
  var spi_clk_count = Reg(UInt(3 bits)) init (0)
  // SPI clock divisor
  val spi_clk_divide = Reg(UInt(2 bits)) init(3)
  // SPI transfer length
  val transfer_length = Reg(UInt(2 bits)) init(3)
  // Flag to indicate that the SPI slave should be deselected after the current
  // transfer
  val deselect = Reg(Bool) init(False)
  // Flag to indicate that an IRQ should be generated at the end of a transfer
  val irq_enable = Reg(Bool) init(False)
  // Internal chip select signal, will be demultiplexed through the cs_mux
  val spi_cs = Reg(Bool) init(False)
  // Current SPI device address
  val spi_addr = Reg(UInt(3 bits))


  //State machine
  val isrunning = Reg(Bool) init(False)
  val state_machine = new StateMachine{
    val idle : State = new State with EntryPoint{
      whenIsActive {
        count := 0
        shift_reg := 0
        spi_clk_out := False
        spi_cs := False
        isrunning := False
        prev_spi_clk := spi_clk_buf
        io.irq := False
        when(start){
          count := 0
          shift_reg := spi_data_buf
          spi_cs := True
          goto(run)
        }
        when(deselect){
          spi_cs := False
        }
      }
    }
    val run: State = new State{
      whenIsActive {
        isrunning := True
        prev_spi_clk := spi_clk_buf
        io.irq := False
        when(prev_spi_clk && (!spi_clk_buf)){
          spi_clk_out := False
          count := count + U"0001"
          shift_reg := shift_reg(14 downto 0) @@ io.spi_miso
          when(((count === 3) && (transfer_length === 0)) ||
            ((count === 5) && (transfer_length === 1)) ||
            ((count === 11) && (transfer_length === 2)) ||
            ((count === 15) && (transfer_length === 3))) {
            when(deselect) {
              spi_cs := False
            }
            when(irq_enable) {
              io.irq := True
            }
            goto(idle)
          }
          when((!prev_spi_clk) && spi_clk_buf){
            spi_clk_out := True
          }
        }
      }
    }
  }
  start := False
  when(io.cs & (!io.rw)){
    switch(io.addr){
      is(0){
        spi_data_buf := io.data_in.resized
      }
      is(1){
        spi_data_buf := (io.data_in << 8)
      }
      is(2){
        start := io.data_in(0)
        deselect := io.data_in(1)
        irq_enable := io.data_in(2)
        spi_addr := io.data_in(6 downto 4)
      }
      is(3){
        spi_clk_divide := io.data_in(1 downto 0)
        transfer_length := io.data_in(3 downto 2)
      }
    }
  }

  switch(io.addr){
    is(0){
      io.data_out := shift_reg(7 downto 0)
    }
    is(1){
      io.data_out := shift_reg(15 downto 8)
    }
    is(2){
      when(isrunning){
        io.data_out(0) := True
      }otherwise {
        io.data_out(0) := False
      }
      io.data_out(1) := deselect
    }
  }

  // select SPI slave
  when(spi_cs === True){
    switch(spi_addr){
      is(0){io.spi_cs_n := (B"1111_1110").asUInt}
      is(1){io.spi_cs_n := (B"1111_1101").asUInt}
      is(2){io.spi_cs_n := (B"1111_1011").asUInt}
      is(3){io.spi_cs_n := (B"1111_0111").asUInt}
      is(4){io.spi_cs_n := (B"1110_1111").asUInt}
      is(5){io.spi_cs_n := (B"1101_1111").asUInt}
      is(6){io.spi_cs_n := (B"1011_1111").asUInt}
      is(7){io.spi_cs_n := (B"0111_1111").asUInt}
    }
  }otherwise {
    io.spi_cs_n := (B"1111_1111").asUInt
  }

  //common clock
  when(isrunning){
    when((spi_clk_divide === 0) ||
      ((spi_clk_divide === 1) && (spi_clk_count === 1)) ||
      ((spi_clk_divide === 2) && (spi_clk_count === 3)) ||
      ((spi_clk_divide === 3) && (spi_clk_count === 7))){
        spi_clk_buf := !(spi_clk_buf)
        spi_clk_count := 0
    } otherwise{
      spi_clk_count := spi_clk_count + U"001"
    }
  }otherwise{
    spi_clk_buf := False
  }

  switch(transfer_length){
    is(0){
      io.spi_mosi := shift_reg(3)
    }
    is(1){
      io.spi_mosi := shift_reg(7)
    }
    is(2){
      io.spi_mosi := shift_reg(11)
    }
    is(3){
      io.spi_mosi := shift_reg(15)
    }
  }
  io.spi_clk := spi_clk_out
}


object SPI {
  // Let's go
  def main(args: Array[String]) {
    SpinalVhdl(new SPI)
  }
}


