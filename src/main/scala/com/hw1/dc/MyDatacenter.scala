/* CS441 HW1: Cloud Datacenter Simulators
 * Name:   Shyam Patel
 * NetID:  spate54
 * Date:   Oct 4, 2019
 */

package com.hw1.dc

import com.typesafe.config.Config
import org.cloudbus.cloudsim.{Datacenter, DatacenterCharacteristics, Host, Storage, VmAllocationPolicySimple}
import org.slf4j.{Logger, LoggerFactory}
import java.util

// MyDatacenter class, part of the dc package, used by the Simulation class to create new datacenters
object MyDatacenter {
  val log: Logger = LoggerFactory.getLogger(this.getClass)                          // logger

  // method to create and return new datacenter
  def create(dId: Int, sId: Int, sPath: String, conf: Config): Datacenter = {
    val name:           String = "datacenter" + dId.toString                        // name of datacenter
    val path:           String = sPath + "." + name                                 // path of datacenter
    val arch:           String = conf.getString(path + ".arch")                     // system architecture
    val os:             String = conf.getString(path + ".os")                       // operating system
    val vmm:            String = conf.getString(path + ".vmm")                      // VM monitor
    val time_zone:      Double = conf.getDouble(path + ".time_zone")                // resource time zone
    val cost:           Double = conf.getDouble(path + ".cost")                     // resource CPU cost
    val costPerMem:     Double = conf.getDouble(path + ".costPerMem")               // resource memory cost
    val costPerStorage: Double = conf.getDouble(path + ".costPerStorage")           // resource storage cost
    val costPerBw:      Double = conf.getDouble(path + ".costPerBw")                // resource bandwidth cost
    val num_host:       Int    = conf.getInt(path + ".num_host")                    // number of hosts
    val hosts: util.List[Host] = MyHost.create(num_host, name, path, conf)          // list of hosts
    val dc: DatacenterCharacteristics =                                             // datacenter characteristics
      new DatacenterCharacteristics(arch, os, vmm, hosts, time_zone, cost, costPerMem, costPerStorage, costPerBw)

    log.info(s"Simulation #$sId: Creating $name...")
    log.info(s"[$arch $os; VM monitor: $vmm; UTC$time_zone]")
    log.info(s"[Costs per CPU: $$$cost; Memory: $$$costPerMem; Storage: $$$costPerStorage]")
    new Datacenter(name, dc, new VmAllocationPolicySimple(hosts),                   // return new datacenter
      new util.LinkedList[Storage], 0)
  }//end def create
}//end object MyDatacenter
