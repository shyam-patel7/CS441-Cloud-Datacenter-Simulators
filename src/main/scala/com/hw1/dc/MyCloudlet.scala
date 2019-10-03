/* CS441 HW1: Cloud Datacenter Simulators
 * Name:   Shyam Patel
 * NetID:  spate54
 * Date:   Oct 4, 2019
 */

package com.hw1.dc

import com.typesafe.config.Config
import org.cloudbus.cloudsim.{Cloudlet, Log, UtilizationModel, UtilizationModelStochastic}
import org.slf4j.{Logger, LoggerFactory}
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

  // method to create and return new cloudlet
  def create(cId: Int, bId: Int, bName: String, bPath: String, map_reduce: Boolean, num_slave: Int,
             conf: Config): Cloudlet = {
    val path:         String = bPath + ".cloudlet"                                  // path of cloudlet
    val pes:          Int    = conf.getInt(path + ".pesNumber")                     // number of CPUs
    val optimization: Double = if (!map_reduce) 0.0                                 // MapReduce optimization factor
                               else             0.7 / num_slave
    val len:          Int    = if (!map_reduce) conf.getInt(path + ".baseLength") + // cloudlet length
                                                (new Random).nextInt(conf.getInt(path + ".excessLength"))
                               else             (optimization * (conf.getInt(path + ".baseLength") +
                                                (new Random).nextInt(conf.getInt(path + ".excessLength")))).toInt
    val fs:           Int    = if (!map_reduce) conf.getInt(path + ".fileSize")     // cloudlet file size
                               else             (optimization * conf.getInt(path + ".fileSize")).toInt
    val out:          Int    = if (!map_reduce) conf.getInt(path + ".outputSize")   // cloudlet output size
                               else             (optimization * conf.getInt(path + ".outputSize")).toInt
    val um: UtilizationModelStochastic = new UtilizationModelStochastic             // utilization model

    if (!map_reduce) log.info(s"$bName: Creating cloudlet $cId...")
    else             log.info(s"$bName: Creating slave cloudlet $cId...")
    log.info(s"[$len mn instructions; $pes CPU; $fs bytes]")
    val cloudlet: Cloudlet = new Cloudlet(cId, len, pes, fs, out, um, um, um)       // create new cloudlet
    cloudlet.setUserId(bId)                                                         // assign cloudlet to broker
    cloudlet                                                                        // return cloudlet
  }//end def create

  // method to create and return list of new mapped slave cloudlets
  def mapper(num_cloudlet: Int, num_vm: Int, bId: Int, bName: String, bPath: String,
             conf: Config): util.ArrayList[Cloudlet] = {
    val slaveCloudlets: util.ArrayList[Cloudlet] =                                  // list of slave cloudlets
      new util.ArrayList[Cloudlet](num_cloudlet * 2)
    (1 until num_cloudlet + 1).flatMap(cId => {
      val num_slave: Int = 2 + (new Random).nextInt(7)                              // 2 to 9 slaves per cloudlet
      List.tabulate(num_slave)(s => ((cId.toString + s.toString).toInt, num_slave))
    }).foreach { case (sId, num_slave) =>                                           // create new slave
        val slaveCloudlet: Cloudlet = create(sId, bId, bName, bPath, map_reduce = true, num_slave, conf)
        slaveCloudlet.setVmId((sId.toString.dropRight(1).toInt - 1) % num_vm + 1)   // assign same VM for data locality
        slaveCloudlets.add(slaveCloudlet)
    }

    slaveCloudlets                                                                  // return list of slaves
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
    }).groupBy(_._1).transform((_, v) =>                                            // group by original cloudlet IDs
      (v.map(_._1)(0),   v.map(_._2)(0),                                            // cloudlet ID, datacenter ID,
       v.map(_._3)(0),   v.maxBy(_._6)._6 - v.minBy(_._5)._5,                       // VM ID, CPU time,
       v.minBy(_._5)._5, v.maxBy(_._6)._6,                                          // start time, finish time,
       v.maxBy(_._7)._7, v.map(_._8).sum)                                           // mean CPU util, and sum cost
    ).values.toList.sortBy(_._6)                                                    // sort by ascending finish time

    Log.printLine(s"    ================================ Simulation #$sId Results for $bName ================================")
    Log.printLine("    | Cloudlet | Status  | Datacenter | VM # | Time (ms) | Start     | Finish    | CPU    | Cost ($)  |")

    val costs: List[Double] = List.tabulate(results.size)(c => {                    // list of costs
      // tabulate results data of original cloudlets
      Log.printLine(String.format("    |    %-5d | SUCCESS |     %-6d |  %-3d | %9s | %9s | %9s | %-5s%% | %9s |",
        results(c)._1, results(c)._2, results(c)._3,                                // IDs for cloudlet, datacenter, VM,
        df.format(results(c)._4), df.format(results(c)._5),                         // CPU time, start time,
        df.format(results(c)._6), df.format(results(c)._7),                         // finish time, mean CPU util,
        df.format(results(c)._8)))                                                  // and cost
      results(c)._8
    })
                                                                                    // total CPU time, total cost
    Log.printLine(String.format("    In simulation #%d, %s received %d cloudlets (via %d slaves) in %s ms",
      sId, bName, results.size, slaves.size, df.format(results(results.size - 1)._6 - 0.1)))
    Log.printLine(String.format("    The total cost for %s is: $%s\n", bName, df.format(costs.sum)))
  }//end def reducer

  // method to print simulation results for received cloudlets
  def results(sId: Int, bName: String, cloudlets: util.List[_ <: Cloudlet]): Unit = {
    Log.printLine(s"    ================================ Simulation #$sId Results for $bName ================================")
    Log.printLine("    | Cloudlet | Status  | Datacenter | VM # | Time (ms) | Start     | Finish    | CPU    | Cost ($)  |")

    val costs: List[Double] = List.tabulate(cloudlets.size)(cloudlet => {           // list of costs
      val c: Cloudlet = cloudlets.get(cloudlet)
      if (c.getCloudletStatus == Cloudlet.SUCCESS) {
        // tabulate results data of received cloudlets
        Log.printLine(String.format("    |    %-5d | SUCCESS |     %-6d |  %-3d | %9s | %9s | %9s | %-5s%% | %9s |",
          c.getCloudletId,               c.getResourceId - 1,                       // cloudlet ID, datacenter ID,
          c.getVmId,                     df.format(c.getActualCPUTime),             // VM ID, CPU time,
          df.format(c.getExecStartTime), df.format(c.getFinishTime),                // start time, finish time,
          df.format(meanUtil(c.getUtilizationModelCpu, c.getActualCPUTime)),        // mean CPU util,
          df.format(c.getCostPerSec * c.getActualCPUTime)))                         // cost

        if (cloudlet == cloudlets.size - 1)                                         // total CPU time
          Log.printLine(String.format("    In simulation #%d, %s received %d cloudlets in %s ms",
            sId, bName, cloudlets.size, df.format(c.getFinishTime - 0.1)))

        c.getCostPerSec * c.getActualCPUTime
      } else 0.0
    })
                                                                                    // total cost
    Log.printLine(String.format("    The total cost for %s is: $%s\n", bName, df.format(costs.sum)))
  }//end def results
}//end object MyCloudlet
