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
import scala.annotation.tailrec
import scala.jdk.CollectionConverters._
import java.util

// MyHost class, part of the dc package, used by the MyDatacenter class to create new hosts
object MyHost {
  val log: Logger = LoggerFactory.getLogger(this.getClass)                          // logger

  // method to create and return list of new hosts
  def create(num_host: Int, dName: String, dPath: String, conf: Config): util.List[Host] = {
    @tailrec def addHost(hId: Int, hosts: List[Host]): List[Host] = {               // recursive helper to add host
      if (hId > num_host) return hosts                                              //   return list of hosts
      val path:     String = dPath + ".host" + hId.toString                         //   path of host
      val ram:      Int    = conf.getInt(path + ".ram")                             //   machine memory
      val bw:       Int    = conf.getInt(path + ".bw")                              //   machine bandwidth
      val storage:  Int    = conf.getInt(path + ".storage")                         //   machine storage
      val mips:     Int    = conf.getInt(path + ".mips")                            //   MIPS rating
      val num_core: Int    = conf.getInt(path + ".num_core")                        //   number of cores
      @tailrec def addPe(peId: Int, pes: List[Pe]): List[Pe] = {                    //   recursive helper to add Pe
        if (peId > num_core) return pes                                             //     return list of Pe's
        addPe(peId + 1, new Pe(peId, new PeProvisionerSimple(mips)) :: pes)         //     prepend new Pe to Pe's
      }//end def addPe
      val pes: util.List[Pe] = addPe(1, List[Pe]()).asJava                          //   list of Pe's
      log.info(s"$dName: Creating host$hId...")
      log.info(s"[$ram MB RAM; $storage MB storage; $mips MIPS; $num_core cores]")
      addHost(hId + 1, new Host(hId, new RamProvisionerSimple(ram),                 //   prepend new host to hosts
        new BwProvisionerSimple(bw), storage, pes, new VmSchedulerTimeShared(pes)) :: hosts)
    }//end def addHost
    addHost(1, List[Host]()).reverse.asJava                                         // return list of hosts
  }//end def create
}//end object MyHost
