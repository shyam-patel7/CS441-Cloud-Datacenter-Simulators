/* CS441 HW1: Cloud Datacenter Simulators
 * Name:   Shyam Patel
 * NetID:  spate54
 * Date:   Oct 4, 2019
 */

package com.hw1.dc

import com.typesafe.config.Config
import org.cloudbus.cloudsim.{Cloudlet, UtilizationModel, UtilizationModelStochastic}
import org.slf4j.{Logger, LoggerFactory}
import scala.annotation.tailrec
import scala.jdk.CollectionConverters._
import scala.math.Ordering.Double
import scala.util.Random
import java.text.DecimalFormat
import java.util

// MyCloudlet class, part of the dc package, used by the MyBroker class to create new cloudlets,
// and by the Simulation class to tabulate simulation results for received cloudlets
// also includes the mapper and reducer methods, which are called when the map_reduce flag is set to true
object MyCloudlet {
  implicit val order: Double.TotalOrdering.type = Ordering.Double.TotalOrdering     // total ordering for doubles
  val log: Logger        = LoggerFactory.getLogger(this.getClass)                   // logger
  val df:  DecimalFormat = new DecimalFormat("#0.00")                               // 2-decimal places formatter
  df.setGroupingSize(3)                                                             // 3-digit grouping
  df.setGroupingUsed(true)

  // method to create and return list of new cloudlets
  def create(num_cloudlet: Int, bId: Int, bName: String, bPath: String, conf: Config): util.List[Cloudlet] = {
    @tailrec def add(cId: Int, cloudlets: List[Cloudlet]): List[Cloudlet] = {       // recursive helper to add cloudlet
      if (cId > num_cloudlet) return cloudlets                                      //   return list of cloudlets
      val path: String = bPath + ".cloudlet"                                        //   path of cloudlet
      val pes:  Int    = conf.getInt(path + ".pesNumber")                           //   number of CPUs
      val len:  Int    = conf.getInt(path + ".baseLength") +                        //   cloudlet length
        (new Random).nextInt(conf.getInt(path + ".excessLength"))
      val fs:   Int    = conf.getInt(path + ".fileSize")                            //   cloudlet file size
      val out:  Int    = conf.getInt(path + ".outputSize")                          //   cloudlet output size
      val um: UtilizationModelStochastic = new UtilizationModelStochastic           //   utilization model

      log.info(s"$bName: Creating cloudlet $cId...")
      log.info(s"[$len mn instructions; $fs bytes]")
      val cloudlet: Cloudlet = new Cloudlet(cId, len, pes, fs, out, um, um, um)     //   create new cloudlet
      cloudlet.setUserId(bId)                                                       //   assign cloudlet to broker
      add(cId + 1, cloudlet :: cloudlets)                                           //   prepend cloudlet to cloudlets
    }//end def add
    add(1, List[Cloudlet]()).reverse.asJava                                         // return list of cloudlets
  }//end def create

  // method to create and return list of new mapped slave cloudlets
  def mapper(num_cloudlet: Int, num_vm: Int, bId: Int, bName: String,
             bPath: String, conf: Config): util.List[Cloudlet] = {                  // recursive helper to map cloudlet
    @tailrec def map(cId: Int, cloudlets: List[List[Cloudlet]]): List[List[Cloudlet]] = {
      if (cId > num_cloudlet) return cloudlets                                      //   return list of cloudlets
      val num_slave: Int = 2 + (new Random).nextInt(7)                              //   2 to 9 slaves per cloudlet
      @tailrec def add(s: Int, slaves: List[Cloudlet]): List[Cloudlet] = {          //   recursive helper to add slave
        if (s > num_slave) return slaves                                            //     return list of slaves
        val sId:  Int    = (cId.toString + s.toString).toInt                        //     slave ID
        val path: String = bPath + ".cloudlet"                                      //     path of cloudlet
        val pes:  Int    = conf.getInt(path + ".pesNumber")                         //     number of CPUs
        val opt:  Double = 0.7 / num_slave                                          //     MapReduce optimization
        val len:  Int    = (opt * (conf.getInt(path + ".baseLength") +              //     slave length
          (new Random).nextInt(conf.getInt(path + ".excessLength")))).toInt
        val fs:   Int    = (opt * conf.getInt(path + ".fileSize")).toInt            //     slave file size
        val out:  Int    = (opt * conf.getInt(path + ".outputSize")).toInt          //     slave output size
        val vId:  Int    = (sId.toString.dropRight(1).toInt - 1) % num_vm + 1       //     slave VM assignment
        val um: UtilizationModelStochastic = new UtilizationModelStochastic         //     utilization model
        val slave: Cloudlet = new Cloudlet(sId, len, pes, fs, out, um, um, um)      //     create new slave

        log.info(s"$bName: Creating slave $s for cloudlet $cId...")
        log.info(s"[$len mn instructions; $fs bytes; VM #$vId]")
        slave.setUserId(bId)                                                        //     assign slave to broker
        slave.setVmId(vId)                                                          //     assign VM for data locality
        add(s + 1, slave :: slaves)                                                 //     prepend slave to slaves
      }//end def add
      map(cId + 1, add(1, List[Cloudlet]()) :: cloudlets)                           //   return list of cloudlets
    }//end def map
    map(1, List()).flatten.reverse.asJava                                           // return list of slave cloudlets
  }//end def mapper

  // method to calculate and return mean utilization percentage
  def meanUtil(um: UtilizationModel, time: Double): Double = {
    val utilization: List[Double] = List.tabulate(time.toInt)(t => um.getUtilization(t))
    utilization.sum / utilization.size * 100                                        // return mean utilization
  }//end def meanUtil

  // method to reduce and print results for received slave cloudlets
  def reducer(sId: Int, bName: String, slaves: util.List[_ <: Cloudlet]): Unit = {
    // reduce received slave cloudlets into original cloudlets
    val results: List[(Int, Int, Int, Double, Double, Double, Double, Double)] = List.tabulate(slaves.size)(slave => {
      val s: Cloudlet = slaves.get(slave)
      if (s.getCloudletStatus == Cloudlet.SUCCESS)                                  // reassign slave cloudlet IDs
        (s.getCloudletId.toString.dropRight(1).toInt,                               //   to original cloudlet IDs,
          s.getResourceId - 1, s.getVmId, s.getActualCPUTime,                       // data center ID, VM ID, CPU time
          s.getExecStartTime, s.getFinishTime,                                      // start time, finish time,
          meanUtil(s.getUtilizationModelCpu, s.getActualCPUTime),                   // mean CPU util,
          s.getCostPerSec * s.getActualCPUTime)                                     // and cost
      else (0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0)
    }).groupBy(_._1).transform((_, v) =>                                            // group by original cloudlet IDs:
      (v.map(_._1)(0),   v.map(_._2)(0),                                            // (cloudlet ID, datacenter ID,
       v.map(_._3)(0),   v.maxBy(_._6)._6 - v.minBy(_._5)._5,                       //  VM ID, CPU time (max - min),
       v.minBy(_._5)._5, v.maxBy(_._6)._6,                                          //  min start time, max finish time,
       v.maxBy(_._7)._7, v.map(_._8).sum)).values.toList.sortBy(_._6)               //  max mean CPU util, and sum cost)
                                                                                    // sort by ascending finish time
    println(s"    ================================ Simulation #$sId Results for $bName ================================")
    println("    | Cloudlet | Status  | Datacenter | VM # | Time (ms) | Start     | Finish    | CPU    | Cost ($)  |")

    val costs: List[Double] = List.tabulate(results.size)(c => {                    // list of costs
      // tabulate results data of original cloudlets
      println(String.format("    |    %-5d | SUCCESS |     %-6d |  %-3d | %9s | %9s | %9s | %-5s%% | %9s |",
        results(c)._1, results(c)._2, results(c)._3,                                // IDs for cloudlet, datacenter, VM,
        df.format(results(c)._4), df.format(results(c)._5),                         // CPU time, start time,
        df.format(results(c)._6), df.format(results(c)._7),                         // finish time, mean CPU util,
        df.format(results(c)._8)))                                                  // and cost
      results(c)._8
    })
                                                                                    // total CPU time, total cost
    println(String.format("    In simulation #%d, %s received %d cloudlets (via %d slaves) in %s ms",
      sId, bName, results.size, slaves.size, df.format(results(results.size - 1)._6 - 0.1)))
    println(String.format("    The total cost for %s is: $%s\n", bName, df.format(costs.sum)))
  }//end def reducer

  // method to print simulation results for received cloudlets
  def results(sId: Int, bName: String, cloudlets: util.List[_ <: Cloudlet]): Unit = {
    println(s"    ================================ Simulation #$sId Results for $bName ================================")
    println("    | Cloudlet | Status  | Datacenter | VM # | Time (ms) | Start     | Finish    | CPU    | Cost ($)  |")

    val costs: List[Double] = List.tabulate(cloudlets.size)(cloudlet => {           // list of costs
      val c: Cloudlet = cloudlets.get(cloudlet)
      if (c.getCloudletStatus == Cloudlet.SUCCESS) {
        // tabulate results data of received cloudlets
        println(String.format("    |    %-5d | SUCCESS |     %-6d |  %-3d | %9s | %9s | %9s | %-5s%% | %9s |",
          c.getCloudletId,               c.getResourceId - 1,                       // cloudlet ID, datacenter ID,
          c.getVmId,                     df.format(c.getActualCPUTime),             // VM ID, CPU time,
          df.format(c.getExecStartTime), df.format(c.getFinishTime),                // start time, finish time,
          df.format(meanUtil(c.getUtilizationModelCpu, c.getActualCPUTime)),        // mean CPU util,
          df.format(c.getCostPerSec * c.getActualCPUTime)))                         // cost

        if (cloudlet == cloudlets.size - 1)                                         // total CPU time
          println(String.format("    In simulation #%d, %s received %d cloudlets in %s ms",
            sId, bName, cloudlets.size, df.format(c.getFinishTime - 0.1)))

        c.getCostPerSec * c.getActualCPUTime
      } else 0.0
    })
                                                                                    // total cost
    println(String.format("    The total cost for %s is: $%s\n", bName, df.format(costs.sum)))
  }//end def results
}//end object MyCloudlet
