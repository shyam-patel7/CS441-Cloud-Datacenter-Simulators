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

// MyHost class, part of the dc package, used by the MyDatacenter class to create new hosts
object MyHost {
  val log: Logger = LoggerFactory.getLogger(this.getClass)                          // logger

  // method to create and return new host
  def create(hId: Int, dName: String, dPath: String, conf: Config): Host = {
    val path:     String = dPath + ".host" + hId.toString                           // path of host
    val ram:      Int    = conf.getInt(path + ".ram")                               // machine memory
    val bw:       Int    = conf.getInt(path + ".bw")                                // machine bandwidth
    val storage:  Int    = conf.getInt(path + ".storage")                           // machine storage
    val mips:     Int    = conf.getInt(path + ".mips")                              // MIPS rating
    val num_core: Int    = conf.getInt(path + ".num_core")                          // number of cores
    val pes: util.ArrayList[Pe] = new util.ArrayList[Pe](num_core)                  // list of processing elements
    List.range(1, num_core + 1).foreach(peId => pes.add(new Pe(peId, new PeProvisionerSimple(mips))))

    log.info(s"$dName: Creating host$hId...")
    log.info(s"[$ram MB RAM; $storage MB storage; $mips MIPS; $num_core cores]")
    new Host(hId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw),    // return new host
      storage, pes, new VmSchedulerTimeShared(pes))
  }//end def create
}//end object MyHost
