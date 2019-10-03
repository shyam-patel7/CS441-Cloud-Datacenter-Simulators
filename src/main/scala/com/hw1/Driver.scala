/* CS441 HW1: Cloud Datacenter Simulators
 * Name:   Shyam Patel
 * NetID:  spate54
 * Date:   Oct 4, 2019
 */

package com.hw1

import org.slf4j.{Logger, LoggerFactory}

// Driver class that runs all simulations sequentially
object Driver extends App {
  // logger
  val log: Logger = LoggerFactory.getLogger(this.getClass)

  // run simulation #1
  log.info("Beginning simulation #1...")
  Thread.sleep(2000)
  Simulation.run(1)
  log.info("End of simulation #1.")
  Thread.sleep(1000)

  // run simulation #2
  log.info("Beginning simulation #2...")
  Thread.sleep(2000)
  Simulation.run(2)
  log.info("End of simulation #2.")
  Thread.sleep(1000)

  // run simulation #3
  log.info("Beginning simulation #3...")
  Thread.sleep(2000)
  Simulation.run(3)
  log.info("End of simulation #3.")
  Thread.sleep(1000)
}//end object Driver
