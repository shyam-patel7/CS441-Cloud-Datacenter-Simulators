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


object MyDatacenter {
  // logger
  val log: Logger = LoggerFactory.getLogger(this.getClass)

  // utility method to create and return new datacenter
  def create(datacenterId: Int, simulationId: Int, simulationPath: String, conf: Config): Datacenter = {
    // name of datacenter
    val datacenterName: String = "datacenter" + datacenterId.toString
    // path of datacenter
    val datacenterPath: String = simulationPath + "." + datacenterName
    // system architecture
    val arch: String = conf.getString(datacenterPath + ".arch")
    // operating system
    val os: String = conf.getString(datacenterPath + ".os")
    // VM monitor
    val vmm: String = conf.getString(datacenterPath + ".vmm")
    // resource time zone
    val time_zone: Double = conf.getDouble(datacenterPath + ".time_zone")
    // resource CPU cost
    val cost: Double = conf.getDouble(datacenterPath + ".cost")
    // resource memory cost
    val costPerMem: Double = conf.getDouble(datacenterPath + ".costPerMem")
    // resource storage cost
    val costPerStorage: Double = conf.getDouble(datacenterPath + ".costPerStorage")
    log.info(s"Simulation #$simulationId: Creating $datacenterName...")
    log.info(s"[$arch $os; VM monitor: $vmm; UTC$time_zone]")
    log.info(s"[Costs per CPU: $$$cost; Memory: $$$costPerMem; Storage: $$$costPerStorage]")
    // resource bandwidth cost
    val costPerBw: Double = conf.getDouble(datacenterPath + ".costPerBw")
    // number of hosts
    val num_host: Int = conf.getInt(datacenterPath + ".num_host")
    // list of hosts
    val hostIds: List[Int] = List.range(1, num_host + 1)
    val hosts: util.ArrayList[Host] = new util.ArrayList[Host](num_host)
    hostIds.foreach(hostId => hosts.add(MyHost.create(hostId, datacenterName, datacenterPath, conf)))
    // characteristics instance
    val characteristics: DatacenterCharacteristics =
      new DatacenterCharacteristics(arch, os, vmm, hosts, time_zone, cost, costPerMem, costPerStorage, costPerBw)

    // return new datacenter
    new Datacenter(datacenterName, characteristics, new VmAllocationPolicySimple(hosts),
      new util.LinkedList[Storage], 0)
  }//end def create
}//end object MyDatacenter
