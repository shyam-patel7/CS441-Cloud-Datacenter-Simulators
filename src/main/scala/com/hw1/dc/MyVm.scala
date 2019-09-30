/* CS441 HW1: Cloud Datacenter Simulators
 * Name:   Shyam Patel
 * NetID:  spate54
 * Date:   Oct 4, 2019
 */

package com.hw1.dc

import com.typesafe.config.Config
import org.cloudbus.cloudsim.{CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared, Vm}
import org.slf4j.{Logger, LoggerFactory}


object MyVm {
  // logger
  val log: Logger = LoggerFactory.getLogger(this.getClass)

  // utility method to create and return new VM
  def create(vmId: Int, brokerId: Int, brokerName: String, brokerPath: String, conf: Config): Vm = {
    // path of VM
    val vmPath: String = brokerPath + ".vm" + vmId.toString
    // million instructions/sec (MIPS) rating
    val mips: Int = conf.getInt(vmPath + ".mips")
    // number of CPUs
    val pesNumber: Int = conf.getInt(vmPath + ".pesNumber")
    // VM memory
    val ram: Int = conf.getInt(vmPath + ".ram")
    // VM bandwidth
    val bw: Int = conf.getInt(vmPath + ".bw")
    // VM image size
    val size: Int = conf.getInt(vmPath + ".size")
    // VM monitor
    val vmm: String = conf.getString(vmPath + ".vmm")
    // cloudlet scheduler policy
    val cloudletPolicy: String = conf.getString(vmPath + ".cloudletPolicy")
    log.info(s"$brokerName: Creating VM #$vmId...")
    log.info(s"[$mips MIPS; $pesNumber vCPU; $ram MB RAM; $cloudletPolicy scheduler policy]")

    // return new VM with time-shared cloudlet scheduler policy
    if (cloudletPolicy == "Time-shared")
      new Vm(vmId, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared)
    // return new VM with space-shared cloudlet scheduler policy
    else
      new Vm(vmId, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared)
  }//end def create
}//end object MyVm
