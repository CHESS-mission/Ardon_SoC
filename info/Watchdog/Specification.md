# Watchdog Specification

## Interface

### Inputs

- APB (see specification in the coresponding section)

### Outputs

- Interrupt (a simple boolean at the moment)

## Register Mapping

| Register | Read / Write    | Values                                 | Description                                                  |
| -------- | --------------- | -------------------------------------- | ------------------------------------------------------------ |
| 0        | Write only      | 0: default - do nothing<br />1 : reset | Reset the internal counter. Must be done every X clock cycles. |
| 1        | read  and write | 0 : deactivated<br />1 : activated     | Deactivate / activate the watchdog. Useful during development |
| 2        | read and write  | 32 bit unsigned int                    | Set the number of clock cycles after which the watchdog sends an interrupt if no reset occured. |
| 3        | -               | -                                      | not used                                                     |

## APB Specification

This are the default specifications used by Briey SoC at the moment

| Name        | Value |
| ----------- | ----- |
| adressWidth | 20    |
| dataWidth   | 32    |
| idWidth     | 4     |

## Component Settings

| Setting               | Default Value   | Note                                                         |
| --------------------- | --------------- | ------------------------------------------------------------ |
| counter_trigger_value | 10 clock cycles | Can be changed during operation                              |
| counter width         | 8 bit           | this implies the max value of counter_trigger_value          |
| PSEL_NR               | 1               | Value of the line (0,1,2,3,...) and not the bit pattern (0,1,2,4,...) |

