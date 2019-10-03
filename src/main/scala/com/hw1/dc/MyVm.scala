/* CS441 HW1: Cloud Datacenter Simulators
 * Name:   Shyam Patel
 * NetID:  spate54
 * Date:   Oct 4, 2019
 */

package com.hw1.dc

import com.typesafe.config.Config
import org.cloudbus.cloudsim.{CloudletScheduler, CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared, Vm}
import org.slf4j.{Logger, LoggerFactory}

// MyVm class, part of the dc package, used by the MyBroker class to create new VMs
object MyVm {
  val log: Logger = LoggerFactory.getLogger(this.getClass)                          // logger

  // method to create and return new VM
  def create(vId: Int, bId: Int, bName: String, bPath: String, conf: Config): Vm = {
    val path:   String = bPath + ".vm" + vId.toString                               // path of VM
    val mips:   Int    = conf.getInt(path + ".mips")                                // MIPS rating
    val pes:    Int    = conf.getInt(path + ".pesNumber")                           // number of CPUs
    val ram:    Int    = conf.getInt(path + ".ram")                                 // VM memory
    val bw:     Int    = conf.getInt(path + ".bw")                                  // VM bandwidth
    val size:   Int    = conf.getInt(path + ".size")                                // VM image size
    val vmm:    String = conf.getString(path + ".vmm")                              // VM monitor
    val policy: String = conf.getString(path + ".cloudletPolicy")                   // cloudlet scheduler policy
    val cs: CloudletScheduler = if (policy == "Time-shared") new CloudletSchedulerTimeShared
                                else                         new CloudletSchedulerSpaceShared

    log.info(s"$bName: Creating VM #$vId...")
    log.info(s"[$mips MIPS; $pes vCPU; $ram MB RAM; $policy scheduler policy]")
    new Vm(vId, bId, mips, pes, ram, bw, size, vmm, cs)                             // return new VM
  }//end def create
}//end object MyVm
