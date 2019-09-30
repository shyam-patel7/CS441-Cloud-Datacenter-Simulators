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


object MyBroker {
  // logger
  val log: Logger = LoggerFactory.getLogger(this.getClass)

  // utility method to create and return new broker
  def create(brokerNumber: Int, simulationId: Int, simulationPath: String,
             map_reduce: Boolean, conf: Config): DatacenterBroker = {
    // name of broker
    val brokerName: String = "broker" + brokerNumber.toString
    // path of broker
    val brokerPath: String = simulationPath + "." + brokerName
    // create new broker
    log.info(s"Simulation #$simulationId: Creating $brokerName...")
    val broker: DatacenterBroker = new DatacenterBroker(brokerName)
    val brokerId: Int = broker.getId

    // number of VMs
    val num_vm: Int = conf.getInt(brokerPath + ".num_vm")
    // list of VMs
    val vmIds: List[Int] = List.range(1, num_vm + 1)
    val vms: util.ArrayList[Vm] = new util.ArrayList[Vm](num_vm)
    vmIds.foreach(vmId => vms.add(MyVm.create(vmId, brokerId, brokerName, brokerPath, conf)))
    // send list of VMs to broker
    log.info(s"Sending list of VMs to $brokerName...")
    broker.submitVmList(vms)

    // number of cloudlets
    val num_cloudlet: Int = conf.getInt(brokerPath + ".num_cloudlet")
    if (map_reduce) {
      // list of cloudlets
      val mappedVmIds: List[Int] = List.tabulate(num_cloudlet)(n => n % num_vm + 1).flatMap(x => List(x, x))
      val mappedCloudletIds: List[Int] = List.range(1, num_cloudlet * 2 + 1)
      val mappedCloudlets: util.ArrayList[Cloudlet] = new util.ArrayList[Cloudlet](num_cloudlet)
      mappedCloudletIds.foreach(cloudletId => {
        val mappedCloudlet: Cloudlet = MyCloudlet.mapper(cloudletId, brokerId, brokerName, brokerPath, conf)
        // assign mapped cloudlets to same VM to ensure data locality
        mappedCloudlet.setVmId(mappedVmIds(cloudletId - 1))
        mappedCloudlets.add(mappedCloudlet)
      })
      // send list of mapped cloudlets to broker
      log.info(s"Sending list of mapped cloudlets to $brokerName...")
      broker.submitCloudletList(mappedCloudlets)
    } else {
      // list of cloudlets
      val cloudletIds: List[Int] = List.range(1, num_cloudlet + 1)
      val cloudlets: util.ArrayList[Cloudlet] = new util.ArrayList[Cloudlet](num_cloudlet)
      cloudletIds.foreach(cloudletId => {
        cloudlets.add(MyCloudlet.create(cloudletId, brokerId, brokerName, brokerPath, conf))
      })
      // send list of cloudlets to broker
      log.info(s"Sending list of cloudlets to $brokerName...")
      broker.submitCloudletList(cloudlets)
    }

    // return broker
    broker
  }//end def create
}//end object MyBroker
