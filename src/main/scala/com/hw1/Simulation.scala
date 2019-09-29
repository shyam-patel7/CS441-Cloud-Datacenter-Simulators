/* CS441 HW1: Cloud Datacenter Simulators
 * Name:   Shyam Patel
 * NetID:  spate54
 * Date:   Oct 4, 2019
 */

package com.hw1

import com.hw1.dc.{MyBroker, MyCloudlet, MyDatacenter}
import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.DatacenterBroker
import org.cloudbus.cloudsim.core.CloudSim
import org.slf4j.{Logger, LoggerFactory}
import java.util.Calendar


object Simulation {
  // logger
  val log: Logger = LoggerFactory.getLogger(this.getClass)

  // method used by driver to run simulation
  def run(simulationId: Int): Unit = {
    // path of simulation
    val simulationPath: String = "simulation" + simulationId.toString
    // simulation configuration
    val conf: Config = ConfigFactory.load(simulationPath)
    log.info(s"Loading configuration for \'$simulationPath\'...")

    // (1) initialize CloudSim library
    //   number of cloud users
    val num_user: Int = conf.getInt(simulationPath + ".num_user")
    //   flag to trace events
    val trace_flag: Boolean = conf.getBoolean(simulationPath + ".trace_flag")
    //   flag to MapReduce
    val map_reduce: Boolean = conf.getBoolean(simulationPath + ".map_reduce")
    //   number of datacenters
    val num_datacenter: Int = conf.getInt(simulationPath + ".num_datacenter")
    //   number of brokers
    val num_broker: Int = conf.getInt(simulationPath + ".num_broker")
    log.info(s"[Number of users: $num_user; Trace events: $trace_flag; MapReduce: $map_reduce]")
    log.info(s"[Number of datacenters: $num_datacenter; Number of brokers: $num_broker]")
    CloudSim.init(num_user, Calendar.getInstance, trace_flag)

    // (2) create datacenter(s) with host(s)
    (0 until num_datacenter).foreach(n => MyDatacenter.create(n + 1, simulationId, simulationPath, conf))

    // (3) create broker(s) with VM(s) and cloudlet(s)
    val brokers: List[DatacenterBroker] = List.tabulate(num_broker)(n => {
      MyBroker.create(n + 1, simulationId, simulationPath, map_reduce, conf)
    })

    // (4) run cloud simulation and display results for received cloudlets
    CloudSim.startSimulation()
    println
    if (map_reduce)
      brokers.foreach(broker => MyCloudlet.reducer(simulationId, broker.getName, broker.getCloudletReceivedList))
    else
      brokers.foreach(broker => MyCloudlet.results(simulationId, broker.getName, broker.getCloudletReceivedList))
    CloudSim.stopSimulation()
  }//end def run
}
