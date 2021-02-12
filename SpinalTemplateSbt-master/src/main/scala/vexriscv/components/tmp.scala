package vexriscv.components

object tmp {
  def main(args: Array[String]): Unit = {
    def toBinaryList(value: BigInt, length:Int=32): List[Int] = {
      def step(value: BigInt, li: List[Int]): List[Int] = {
        if (value == BigInt(1)) return (li :+ 1)
        if (value == BigInt(0)) return (li :+ 0)
        else step(value/2, li:+(value%2).toInt)
      }
      def addLeadingZerosAndReverse(li: List[Int], len: Int): List[Int] = {
        println(li)
        if (li.length > len) return li.reverse.drop(li.length-len)
        if (li.length == len) li.reverse else addLeadingZerosAndReverse(li :+ 0, len)
      }
      addLeadingZerosAndReverse(step(value, List()), length)
    }
    println(toBinaryList(BigInt(67), 4))
  }
  }
