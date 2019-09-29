/* CS441 HW1: Cloud Datacenter Simulators
 * Name:   Shyam Patel
 * NetID:  spate54
 * Date:   Oct 4, 2019
 */

package com.hw1.dc

import com.typesafe.config.Config
import org.cloudbus.cloudsim.{Cloudlet, Log, UtilizationModel, UtilizationModelStochastic}
import org.slf4j.{Logger, LoggerFactory}
import scala.math.{ceil, max, min}
import scala.math.Ordering.Double
import scala.util.Random
import java.text.DecimalFormat
import java.util


object MyCloudlet {
  // logger
  val log: Logger = LoggerFactory.getLogger(this.getClass)

  // decimal formatter (2 decimal places with 3-digit grouping)
  val df: DecimalFormat = new DecimalFormat("#0.00")
  df.setGroupingUsed(true)
  df.setGroupingSize(3)

  // use total ordering for doubles
  implicit val order: Double.TotalOrdering.type = Ordering.Double.TotalOrdering

  // utility method to create and return new cloudlet
  def create(cloudletId: Int, brokerId: Int, brokerName: String, brokerPath: String, conf: Config): Cloudlet = {
    // path of cloudlet
    val cloudletPath: String = brokerPath + ".cloudlet"
    // generate length
    val baseLength: Int = conf.getInt(cloudletPath + ".baseLength")
    val excessLength: Int = conf.getInt(cloudletPath + ".excessLength")
    val random: Random = new Random
    val length: Int = baseLength + random.nextInt(excessLength)
    // number of CPUs
    val pesNumber: Int = conf.getInt(cloudletPath + ".pesNumber")
    // cloudlet size
    val fileSize: Int = conf.getInt(cloudletPath + ".fileSize")
    log.info(s"$brokerName: Creating cloudlet $cloudletId...")
    log.info(s"[$length mn instructions; $pesNumber CPU; $fileSize bytes]")
    // cloudlet output size
    val outputSize: Int = conf.getInt(cloudletPath + ".outputSize")
    // utilization model
    val um: UtilizationModelStochastic = new UtilizationModelStochastic

    // return new cloudlet
    val cloudlet: Cloudlet = new Cloudlet(cloudletId, length, pesNumber, fileSize, outputSize, um, um, um)
    cloudlet.setUserId(brokerId)
    cloudlet
  }//end def create

  // utility method to calculate and return mean utilization
  def meanUtilization(um: UtilizationModel, time: Double): Double = {
    val utilization: List[Double] = List.tabulate(time.toInt)(n => um.getUtilization(n))
    utilization.sum / utilization.size * 100
  }//end def meanUtilization

  // utility method to print simulation results for received cloudlets
  def results(simulationId: Int, brokerName: String, cloudlets: util.List[_ <: Cloudlet]): Unit = {
    // results table header
    Log.printLine(s"    ================================ Simulation #$simulationId Results for $brokerName ================================")
    Log.printLine("    | Cloudlet | Status  | Datacenter | VM # | Time (ms) | Start     | Finish    | CPU    | Cost ($)  |")

    // list of costs
    val costs: List[Double] = List.tabulate(cloudlets.size)(n => {
      val cloudlet: Cloudlet = cloudlets.get(n)
      if (cloudlet.getCloudletStatus == Cloudlet.SUCCESS) {
        // results table data for each cloudlet
        Log.printLine(String.format("    |    %-5d | SUCCESS |     %-6d |  %-3d | %9s | %9s | %9s | %-5s%% | %9s |",
          cloudlet.getCloudletId, cloudlet.getResourceId - 1, cloudlet.getVmId,   // ID, datacenter ID, VM ID
          df.format(cloudlet.getActualCPUTime),                                   // CPU time
          df.format(cloudlet.getExecStartTime),                                   // start time
          df.format(cloudlet.getFinishTime),                                      // finish time
          df.format(meanUtilization(cloudlet.getUtilizationModelCpu, cloudlet.getActualCPUTime)),
          df.format(cloudlet.getCostPerSec * cloudlet.getActualCPUTime)))         // cost

        if (n == cloudlets.size - 1)                                              // total CPU time
          Log.printLine(String.format("    In simulation #%d, %s received %d cloudlets in %s ms",
            simulationId, brokerName, cloudlets.size, df.format(cloudlet.getFinishTime - 0.1)))

        cloudlet.getCostPerSec * cloudlet.getActualCPUTime
      } else 0.0
    })
    // total cost
    Log.printLine(String.format("    The total cost for %s is: $%s\n", brokerName, df.format(costs.sum)))
  }//end def results

  // utility method to create and return new mapped cloudlet
  def mapper(cloudletId: Int, brokerId: Int, brokerName: String, brokerPath: String, conf: Config): Cloudlet = {
    // original cloudlet ID
    val originalCloudletId: Int = ceil(cloudletId / 2.0).toInt
    val mappedCount: Int = (cloudletId - 1) % 2 + 1
    // path of cloudlet
    val cloudletPath: String = brokerPath + ".cloudlet"
    // generate length for MapReduce optimizations
    val baseLength: Int = conf.getInt(cloudletPath + ".baseLength")
    val excessLength: Int = conf.getInt(cloudletPath + ".excessLength")
    val random: Random = new Random
    val length: Int = baseLength + random.nextInt(excessLength)
    // number of CPUs
    val pesNumber: Int = conf.getInt(cloudletPath + ".pesNumber")
    // cloudlet size
    val fileSize: Int = conf.getInt(cloudletPath + ".fileSize") / 2
    log.info(s"$brokerName: Creating mapped cloudlet $mappedCount for cloudlet $originalCloudletId...")
    log.info(s"[$length mn instructions; $pesNumber CPU; $fileSize bytes]")
    // cloudlet output size
    val outputSize: Int = conf.getInt(cloudletPath + ".outputSize") / 2
    // utilization model
    val um: UtilizationModelStochastic = new UtilizationModelStochastic

    // return new cloudlet
    val cloudlet: Cloudlet = new Cloudlet(cloudletId, length, pesNumber, fileSize, outputSize, um, um, um)
    cloudlet.setUserId(brokerId)
    cloudlet
  }//end def mapper

  // utility method to reduce mapped cloudlets and print results for received cloudlets
  def reducer(simulationId: Int, brokerName: String, cloudlets: util.List[_ <: Cloudlet]): Unit = {
    // reduce results of mapped cloudlets into results of original cloudlets
    val results: List[((String, Int), (String, Int), (String, Int), (String, Double), (String, Double),
      (String, Double), (String, Double), (String, Double))] = List.tabulate(cloudlets.size)(n => {
      val cloudlet: Cloudlet = cloudlets.get(n)
      if (cloudlet.getCloudletStatus == Cloudlet.SUCCESS)
      // reassign mapped cloudlets to original cloudlet IDs
        (("cloudletId", ceil(cloudlet.getCloudletId / 2.0).toInt), ("datacenterId", cloudlet.getResourceId - 1),
          ("vmId", cloudlet.getVmId), ("cpuTime", cloudlet.getActualCPUTime),
          ("startTime", cloudlet.getExecStartTime), ("finishTime", cloudlet.getFinishTime),
          ("cpuUtil", meanUtilization(cloudlet.getUtilizationModelCpu, cloudlet.getActualCPUTime)),
          ("cost", cloudlet.getCostPerSec * cloudlet.getActualCPUTime))
      else (("", 0), ("", 0), ("", 0), ("", 0.0), ("", 0.0), ("", 0.0), ("", 0.0), ("", 0.0))
    }).groupBy(_._1).transform((_, v) => {
      // finish time of original cloudlet (maximum of mapped cloudlets)
      val finishTime: Double = max(v.map(_._6).map(_._2)(0), v.map(_._6).map(_._2)(1))
      // start time of original cloudlet (minimum of mapped cloudlets)
      val startTime: Double = min(v.map(_._5).map(_._2)(0), v.map(_._5).map(_._2)(1))
      // cost of original cloudlet (sum of mapped cloudlets)
      (("cloudletId", v.map(_._1).map(_._2)(0)), ("datacenterId", v.map(_._2).map(_._2)(0)),
        ("vmId", v.map(_._3).map(_._2)(0)), ("cpuTime", finishTime - startTime),
        ("startTime", startTime), ("finishTime", finishTime),
        ("cpuUtil", max(v.map(_._7).map(_._2)(0), v.map(_._7).map(_._2)(1))), ("cost", v.map(_._8).map(_._2).sum))
    }).values.toList.sortBy(_._6._2)

    // results table header
    Log.printLine(s"    ================================ Simulation #$simulationId Results for $brokerName ================================")
    Log.printLine("    | Cloudlet | Status  | Datacenter | VM # | Time (ms) | Start     | Finish    | CPU    | Cost ($)  |")

    // list of costs
    val costs: List[Double] = List.tabulate(results.size)(n => {
      // results table data for each original cloudlet
      Log.printLine(String.format("    |    %-5d | SUCCESS |     %-6d |  %-3d | %9s | %9s | %9s | %-5s%% | %9s |",
        results(n)._1._2, results(n)._2._2, results(n)._3._2, df.format(results(n)._4._2),
        df.format(results(n)._5._2), df.format(results(n)._6._2),
        df.format(results(n)._7._2), df.format(results(n)._8._2)))
      results(n)._8._2
    })

    // total CPU time and total cost
    Log.printLine(String.format("    In simulation #%d, %s received %d cloudlets in %s ms",
      simulationId, brokerName, results.size, df.format(results(results.size - 1)._6._2 - 0.1)))
    Log.printLine(String.format("    The total cost for %s is: $%s\n", brokerName, df.format(costs.sum)))
  }//end def reducer
}//end object Cloudlet
