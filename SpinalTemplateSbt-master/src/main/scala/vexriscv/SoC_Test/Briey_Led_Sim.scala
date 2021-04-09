package vexriscv.SoC_Test

import spinal.core._
import spinal.core.sim._
import spinal.lib.bus.amba3.apb.Apb3Config
import spinal.lib.com.uart.{Apb3UartCtrl, UartCtrlGenerics, UartCtrlInitConfig, UartCtrlMemoryMappedConfig, UartParityType, UartStopType}
import spinal.lib.memory.sdram.sdr.IS42x320D
import vexriscv.ip.{DataCacheConfig, InstructionCacheConfig}
import vexriscv.plugin
import vexriscv.plugin.{BranchPlugin, CsrAccess, CsrPlugin, CsrPluginConfig, DBusCachedPlugin, DecoderSimplePlugin, DivPlugin, FullBarrelShifterPlugin, HazardSimplePlugin, IBusCachedPlugin, IntAluPlugin, MulPlugin, PcManagerSimplePlugin, RegFilePlugin, STATIC, SrcPlugin, StaticMemoryTranslatorPlugin, YamlPlugin}

import scala.collection.mutable.ArrayBuffer

object Briey_Led_Sim {
  def main(args: Array[String]): Unit = {
    val config = BrieyConfig(
      axiFrequency = 50 MHz,
      onChipRamSize  = 4 kB,
      sdramLayout = IS42x320D.layout,
      sdramTimings = IS42x320D.timingGrade7,
      uartCtrlConfig = UartCtrlMemoryMappedConfig(
        uartCtrlConfig = UartCtrlGenerics(
          dataWidthMax      = 8,
          clockDividerWidth = 20,
          preSamplingSize   = 1,
          samplingSize      = 5,
          postSamplingSize  = 2
        ),
        txFifoDepth = 16,
        rxFifoDepth = 16
      ),
      cpuPlugins = ArrayBuffer(
        new PcManagerSimplePlugin(0x80000000l, false),
        //          new IBusSimplePlugin(
        //            interfaceKeepData = false,
        //            catchAccessFault = true
        //          ),
        new IBusCachedPlugin(
          resetVector = 0x80000000l,
          prediction = STATIC,
          config = InstructionCacheConfig(
            cacheSize = 4096,
            bytePerLine =32,
            wayCount = 1,
            addressWidth = 32,
            cpuDataWidth = 32,
            memDataWidth = 32,
            catchIllegalAccess = true,
            catchAccessFault = true,
            asyncTagMemory = false,
            twoCycleRam = true,
            twoCycleCache = true
          )
          //            askMemoryTranslation = true,
          //            memoryTranslatorPortConfig = MemoryTranslatorPortConfig(
          //              portTlbSize = 4
          //            )
        ),
        //                    new DBusSimplePlugin(
        //                      catchAddressMisaligned = true,
        //                      catchAccessFault = true
        //                    ),
        new DBusCachedPlugin(
          config = new DataCacheConfig(
            cacheSize         = 4096,
            bytePerLine       = 32,
            wayCount          = 1,
            addressWidth      = 32,
            cpuDataWidth      = 32,
            memDataWidth      = 32,
            catchAccessError  = true,
            catchIllegal      = true,
            catchUnaligned    = true
          ),
          memoryTranslatorPortConfig = null
          //            memoryTranslatorPortConfig = MemoryTranslatorPortConfig(
          //              portTlbSize = 6
          //            )
        ),
        new StaticMemoryTranslatorPlugin(
          ioRange      = _(31 downto 28) === 0xF
        ),
        new DecoderSimplePlugin(
          catchIllegalInstruction = true
        ),
        new RegFilePlugin(
          regFileReadyKind = plugin.SYNC,
          zeroBoot = false
        ),
        new IntAluPlugin,
        new SrcPlugin(
          separatedAddSub = false,
          executeInsertion = true
        ),
        new FullBarrelShifterPlugin,
        new MulPlugin,
        new DivPlugin,
        new HazardSimplePlugin(
          bypassExecute           = true,
          bypassMemory            = true,
          bypassWriteBack         = true,
          bypassWriteBackBuffer   = true,
          pessimisticUseSrc       = false,
          pessimisticWriteRegFile = false,
          pessimisticAddressMatch = false
        ),
        new BranchPlugin(
          earlyBranch = false,
          catchAddressMisaligned = true
        ),
        new CsrPlugin(
          config = CsrPluginConfig(
            catchIllegalAccess = false,
            mvendorid      = null,
            marchid        = null,
            mimpid         = null,
            mhartid        = null,
            misaExtensionsInit = 66,
            misaAccess     = CsrAccess.NONE,
            mtvecAccess    = CsrAccess.NONE,
            mtvecInit      = 0x80000020l,
            mepcAccess     = CsrAccess.READ_WRITE,
            mscratchGen    = false,
            mcauseAccess   = CsrAccess.READ_ONLY,
            mbadaddrAccess = CsrAccess.READ_ONLY,
            mcycleAccess   = CsrAccess.NONE,
            minstretAccess = CsrAccess.NONE,
            ecallGen       = false,
            wfiGenAsWait         = false,
            ucycleAccess   = CsrAccess.NONE,
            uinstretAccess = CsrAccess.NONE
          )
        ),
        new YamlPlugin("cpu0.yaml")
      )
    )
    val compiled = SimConfig.withWave.compile(new Briey_LED(config))

    compiled.doSim("Signal Identification") { dut =>
      val name = "Signal Identification"
      val start_str = "[" + name + "] "
      val io = dut.io.flatten.map(e => e.getName().replace("io_", "") -> e).toMap
      println(start_str + "The signals are:")
      io foreach { case (key, value) => printf("[" + name + "]   " + key + " : " + value + "\n") }

      ClockDomain(dut.io.axiClk, dut.io.asyncReset).forkStimulus(10)
      ClockDomain(dut.io.axiClk, dut.io.asyncReset).waitRisingEdge(10000)
      simSuccess
    }
  }
}