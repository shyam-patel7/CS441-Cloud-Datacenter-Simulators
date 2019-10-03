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

// Simulation class that assists in running any simulation
// all simulation settings are set by the configuration files in the project '/src/main/resources' folder
object Simulation {
  val log: Logger = LoggerFactory.getLogger(this.getClass)                          // logger

  // method used by driver to run simulation with specified simulation ID
  def run(sId: Int): Unit = {
    // (1) load configuration
    val path:           String  = "simulation" + sId.toString                       // path of simulation
    val conf:           Config  = ConfigFactory.load(path)                          // simulation configuration
    log.info(s"Loading configuration for \'$path\'...")

    // (2) initialize CloudSim library
    val num_user:       Int     = conf.getInt(path + ".num_user")                   // number of cloud users
    val trace_flag:     Boolean = conf.getBoolean(path + ".trace_flag")             // flag to trace events
    val map_reduce:     Boolean = conf.getBoolean(path + ".map_reduce")             // flag to MapReduce
    val num_datacenter: Int     = conf.getInt(path + ".num_datacenter")             // number of datacenters
    val num_broker:     Int     = conf.getInt(path + ".num_broker")                 // number of brokers
    log.info(s"[Number of users: $num_user; Trace events: $trace_flag; MapReduce: $map_reduce]")
    log.info(s"[Number of datacenters: $num_datacenter; Number of brokers: $num_broker]")
    CloudSim.init(num_user, Calendar.getInstance, trace_flag)                       // initialize CloudSim

    // (3) create datacenter(s) with host(s)
    (1 until num_datacenter + 1).foreach(dId => MyDatacenter.create(dId, sId, path, conf))

    // (4) create broker(s) with VM(s) and cloudlet(s)
    val brokers: List[DatacenterBroker] =
      List.tabulate(num_broker)(b => MyBroker.create(b + 1, sId, path, map_reduce, conf))

    // (5) run cloud simulation and display results for received cloudlets
    CloudSim.startSimulation()
    log.info("Analyzing received cloudlets...\n")
    if (!map_reduce) brokers.foreach(b => MyCloudlet.results(sId, b.getName, b.getCloudletReceivedList))
    else             brokers.foreach(b => MyCloudlet.reducer(sId, b.getName, b.getCloudletReceivedList))
  }//end def run
}//end object Simulation
