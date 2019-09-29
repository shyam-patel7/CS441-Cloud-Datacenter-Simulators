/* CS441 HW1: Cloud Datacenter Simulators
 * Name:   Shyam Patel
 * NetID:  spate54
 * Date:   Oct 4, 2019
 */

package com.hw1.dc

import com.typesafe.config.Config
import org.cloudbus.cloudsim.{Host, Pe, VmSchedulerTimeShared}
import org.cloudbus.cloudsim.provisioners.{BwProvisionerSimple, PeProvisionerSimple, RamProvisionerSimple}
import org.slf4j.{Logger, LoggerFactory}
import java.util


object MyHost {
  // logger
  val log: Logger = LoggerFactory.getLogger(this.getClass)

  // utility method to create and return new host
  def create(hostId: Int, datacenterName: String, datacenterPath: String, conf: Config): Host = {
    // path of host
    val hostPath: String = datacenterPath + ".host" + hostId.toString
    // machine memory
    val ram: Int = conf.getInt(hostPath + ".ram")
    // machine bandwidth
    val bw: Int = conf.getInt(hostPath + ".bw")
    // machine storage
    val storage: Int = conf.getInt(hostPath + ".storage")
    // million instructions/sec (MIPS) rating
    val mips: Int = conf.getInt(hostPath + ".mips")
    // number of cores
    val num_core: Int = conf.getInt(hostPath + ".num_core")
    log.info(s"$datacenterName: Creating host$hostId...")
    log.info(s"[$ram MB RAM; $storage MB storage; $mips MIPS; $num_core cores]")
    // list of processing elements (Pe's)
    val peIds: List[Int] = List.range(1, num_core + 1)
    val pes: util.ArrayList[Pe] = new util.ArrayList[Pe](num_core)
    peIds.foreach(peId => pes.add(new Pe(peId, new PeProvisionerSimple(mips))))

    // return new host
    new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw),
      storage, pes, new VmSchedulerTimeShared(pes))
  }//end def create
}//end object Host
