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
    List.range(1, num_cloudlet + 1).flatMap(x => {
      val num_slave: Int = 2 + (new Random).nextInt(7)                              // 2 to 9 slaves per cloudlet
      List.tabulate(num_slave)(n => ((x.toString + n.toString).toInt, num_slave))
    }).foreach { case (sId, num_slave) =>                                           // create new slave
        val slaveCloudlet: Cloudlet = create(sId, bId, bName, bPath, map_reduce = true, num_slave, conf)
        slaveCloudlet.setVmId((sId.toString.dropRight(1).toInt - 1) % num_vm + 1)   // assign same VM for data locality
        slaveCloudlets.add(slaveCloudlet)
    }

    slaveCloudlets                                                                  // return list of slaves
  }//end def mapper

  // method to calculate and return mean utilization percentage
  def meanUtilization(um: UtilizationModel, time: Double): Double = {
    val utilization: List[Double] = List.tabulate(time.toInt)(n => um.getUtilization(n))
    utilization.sum / utilization.size * 100                                        // return utilization
  }//end def meanUtilization

  // method to reduce and print results for received slave cloudlets
  def reducer(simulationId: Int, brokerName: String, cloudlets: util.List[_ <: Cloudlet]): Unit = {
    // reduce received slave cloudlets into original cloudlets
    val results: List[((String, Int), (String, Int), (String, Int), (String, Double), (String, Double),
      (String, Double), (String, Double), (String, Double))] = List.tabulate(cloudlets.size)(n => {
      val cloudlet: Cloudlet = cloudlets.get(n)
      if (cloudlet.getCloudletStatus == Cloudlet.SUCCESS) {
        (("cloudletId", cloudlet.getCloudletId.toString.dropRight(1).toInt),        // reassign slave cloudlets
          ("datacenterId", cloudlet.getResourceId - 1),                             //   to original cloudlets
          ("vmId", cloudlet.getVmId),
          ("cpuTime", cloudlet.getActualCPUTime),
          ("startTime", cloudlet.getExecStartTime),
          ("finishTime", cloudlet.getFinishTime),
          ("cpuUtil", meanUtilization(cloudlet.getUtilizationModelCpu, cloudlet.getActualCPUTime)),
          ("cost", cloudlet.getCostPerSec * cloudlet.getActualCPUTime))
      } else (("", 0), ("", 0), ("", 0), ("", 0.0), ("", 0.0), ("", 0.0), ("", 0.0), ("", 0.0))
    }).groupBy(_._1).transform((_, v) => {
      val finishTime: Double = v.maxBy(_._6._2)._6._2                               // max finish time,
      val startTime: Double = v.minBy(_._5._2)._5._2                                // min start time
      (("cloudletId", v.map(_._1._2)(0)),    ("datacenterId", v.map(_._2._2)(0)),   //   of slave cloudlets
        ("vmId", v.map(_._3._2)(0)),         ("cpuTime", finishTime - startTime),
        ("startTime", startTime),            ("finishTime", finishTime),
        ("cpuUtil", v.maxBy(_._7._2)._7._2), ("cost", v.map(_._8._2).sum))          // max CPU utilization, sum cost
    }).values.toList.sortBy(_._6._2)                                                //   of slave cloudlets

    Log.printLine(s"    ================================ Simulation #$simulationId Results for $brokerName ================================")
    Log.printLine("    | Cloudlet | Status  | Datacenter | VM # | Time (ms) | Start     | Finish    | CPU    | Cost ($)  |")

    val costs: List[Double] = List.tabulate(results.size)(n => {                    // list of costs
      // tabulate results data of original cloudlets
      Log.printLine(String.format("    |    %-5d | SUCCESS |     %-6d |  %-3d | %9s | %9s | %9s | %-5s%% | %9s |",
        results(n)._1._2, results(n)._2._2, results(n)._3._2,                       // IDs for cloudlet, datacenter, VM
        df.format(results(n)._4._2), df.format(results(n)._5._2),                   // CPU time, start time
        df.format(results(n)._6._2), df.format(results(n)._7._2),                   // finish time, CPU utilization
        df.format(results(n)._8._2)))                                               // cost
      results(n)._8._2
    })
                                                                                    // total CPU time and total cost
    Log.printLine(String.format("    In simulation #%d, %s received %d cloudlets (via %d slaves) in %s ms",
      simulationId, brokerName, results.size, cloudlets.size, df.format(results(results.size - 1)._6._2 - 0.1)))
    Log.printLine(String.format("    The total cost for %s is: $%s\n", brokerName, df.format(costs.sum)))
  }//end def reducer

  // method to print simulation results for received cloudlets
  def results(sId: Int, bName: String, cloudlets: util.List[_ <: Cloudlet]): Unit = {
    Log.printLine(s"    ================================ Simulation #$sId Results for $bName ================================")
    Log.printLine("    | Cloudlet | Status  | Datacenter | VM # | Time (ms) | Start     | Finish    | CPU    | Cost ($)  |")

    val costs: List[Double] = List.tabulate(cloudlets.size)(n => {                  // list of costs
      val cloudlet: Cloudlet = cloudlets.get(n)
      if (cloudlet.getCloudletStatus == Cloudlet.SUCCESS) {
        // tabulate results data of received cloudlets
        Log.printLine(String.format("    |    %-5d | SUCCESS |     %-6d |  %-3d | %9s | %9s | %9s | %-5s%% | %9s |",
          cloudlet.getCloudletId, cloudlet.getResourceId - 1, cloudlet.getVmId,     // IDs for cloudlet, datacenter, VM
          df.format(cloudlet.getActualCPUTime),                                     // CPU time
          df.format(cloudlet.getExecStartTime),                                     // start time
          df.format(cloudlet.getFinishTime),                                        // finish time
          df.format(meanUtilization(cloudlet.getUtilizationModelCpu, cloudlet.getActualCPUTime)), // CPU utilization
          df.format(cloudlet.getCostPerSec * cloudlet.getActualCPUTime)))           // cost

        if (n == cloudlets.size - 1)                                                // total CPU time
          Log.printLine(String.format("    In simulation #%d, %s received %d cloudlets in %s ms",
            sId, bName, cloudlets.size, df.format(cloudlet.getFinishTime - 0.1)))

        cloudlet.getCostPerSec * cloudlet.getActualCPUTime
      } else 0.0
    })
                                                                                    // total cost
    Log.printLine(String.format("    The total cost for %s is: $%s\n", bName, df.format(costs.sum)))
  }//end def results
}//end object MyCloudlet
