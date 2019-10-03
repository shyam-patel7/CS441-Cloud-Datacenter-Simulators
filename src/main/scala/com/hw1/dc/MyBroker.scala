/* CS441 HW1: Cloud Datacenter Simulators
 * Name:   Shyam Patel
 * NetID:  spate54
 * Date:   Oct 4, 2019
 */

package com.hw1.dc

import com.typesafe.config.Config
import org.cloudbus.cloudsim.{Cloudlet, DatacenterBroker, Vm}
import org.slf4j.{Logger, LoggerFactory}
import java.util

// MyBroker class, part of the dc package, used by the Simulation class to create new brokers
object MyBroker {
  val log: Logger = LoggerFactory.getLogger(this.getClass)                          // logger

  // method to create and return new broker
  def create(bNumber: Int, sId: Int, sPath: String, map_reduce: Boolean, conf: Config): DatacenterBroker = {
    val name:   String           = "broker" + bNumber.toString                      // name of broker
    val path:   String           = sPath + "." + name                               // path of broker
    log.info(s"Simulation #$sId: Creating $name...")
    val broker: DatacenterBroker = new DatacenterBroker(name)                       // create new broker
    val bId:    Int              = broker.getId                                     // broker ID
    val num_vm: Int              = conf.getInt(path + ".num_vm")                    // number of VMs
    val vms:    util.List[Vm]    = MyVm.create(num_vm, bId, name, path, conf)       // list of VMs
    log.info(s"Sending list of VMs to $name...")
    broker.submitVmList(vms)                                                        // send list of VMs to broker

    val num_cloudlet: Int = conf.getInt(path + ".num_cloudlet")                     // number of cloudlets
    val cloudlets: util.List[Cloudlet] =                                            // list of cloudlets
      if (map_reduce) MyCloudlet.mapper(num_cloudlet, num_vm, bId, name, path, conf)
      else            MyCloudlet.create(num_cloudlet, bId, name, path, conf)
    log.info(s"Sending list of cloudlets to $name...")
    broker.submitCloudletList(cloudlets)                                            // send list of cloudlets to broker
    broker                                                                          // return broker
  }//end def create
}//end object MyBroker
