/* CS441 HW1: Cloud Datacenter Simulators
 * Name:   Shyam Patel
 * NetID:  spate54
 * Date:   Oct 4, 2019
 */

import com.hw1.dc.{MyBroker, MyCloudlet, MyDatacenter, MyHost, MyVm}
import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.{Cloudlet, DatacenterBroker, Log}
import org.scalatest.{FlatSpec, Matchers}
import java.util.Calendar
import java.util

// SimulationTests class consisting of 8 unit tests based on the ScalaTest testing framework
class SimulationTests extends FlatSpec with Matchers {
  // configuration
  val conf: Config = ConfigFactory.load("simulation3")

  // TEST 1: initialize CloudSim
  "CloudSim Initialization" should "be successful" in {
    conf.getInt("simulation3.num_user") should be (1)
    conf.getBoolean("simulation3.trace_flag") should be (false)
    CloudSim.init(1, Calendar.getInstance, false)
    Log.disable()
    CloudSim.getCloudInfoServiceEntityId should be (1)
  }

  // TEST 2: load configuration and read from it
  "Datacenter Configuration" should "have 1 user" in {
    ConfigFactory.load("simulation1").getInt("simulation1.num_user") should be (1)
  }
  it should "have trace_flag disabled" in {
    ConfigFactory.load("simulation1").getBoolean("simulation1.trace_flag") should be (false)
  }
  it should "have map_reduce disabled" in {
    ConfigFactory.load("simulation1").getBoolean("simulation1.map_reduce") should be (false)
  }
  it should "have 1 datacenter" in {
    ConfigFactory.load("simulation1").getInt("simulation1.num_datacenter") should be (1)
  }
  it should "have 2 brokers" in {
    ConfigFactory.load("simulation1").getInt("simulation1.num_broker") should be (2)
  }
  it should "have Linux OS" in {
    ConfigFactory.load("simulation1").getString("simulation1.datacenter1.os") should be ("Linux")
  }
  it should "have time UTC-5:00" in {
    ConfigFactory.load("simulation1").getDouble("simulation1.datacenter1.time_zone") should be (-5.0)
  }
  it should "have $0.05 cost per memory" in {
    ConfigFactory.load("simulation1").getDouble("simulation1.datacenter1.costPerMem") should be (0.05)
  }
  it should "have host1 with 2 GB RAM" in {
    ConfigFactory.load("simulation1").getInt("simulation1.datacenter1.host1.ram") should be (2048)
  }
  it should "have host1 with 3000 MIPS" in {
    ConfigFactory.load("simulation1").getInt("simulation1.datacenter1.host1.mips") should be (3000)
  }

  // TEST 3: create datacenter with hosts
  "Datacenter" should "be the only one" in {
    conf.getInt("simulation3.num_datacenter") should be (1)
  }
  it should "be named datacenter1" in {
    List.tabulate(1)(n => MyDatacenter.create(n + 1, 3, "simulation3", conf))(0).getName should be ("datacenter1")
  }
  it should "consist of 2 hosts" in {
    List.tabulate(1)(n => MyDatacenter.create(n + 1, 3, "simulation3", conf))(0).getHostList.size should be (2)
  }

  // TEST 4: create hosts
  "Hosts" should "have 2 in total" in {
    conf.getInt("simulation3.datacenter1.num_host") should be (2)
  }
  it should "have 6000 MIPS in host1" in {
    MyHost.create(2, "datacenter1", "simulation3.datacenter1", conf).get(0).getAvailableMips should be (6000.0)
  }
  it should "have 2 GB RAM in host1" in {
    MyHost.create(2, "datacenter1", "simulation3.datacenter1", conf).get(0).getRam should be (2048.0)
  }
  it should "have 10 GB bandwidth in host2" in {
    MyHost.create(2, "datacenter1", "simulation3.datacenter1", conf).get(1).getBw should be (10000)
  }
  it should "have 1 TB storage in host2" in {
    MyHost.create(2, "datacenter1", "simulation3.datacenter1", conf).get(1).getStorage should be (1000000)
  }

  // TEST 5: create brokers with VMs and cloudlets
  "Brokers" should "be 2 in total" in {
    conf.getInt("simulation3.num_broker") should be (2)
  }
  it should "have the names broker1 and broker2, respectively" in {
    val brokers: List[DatacenterBroker] = List.tabulate(2)(b => MyBroker.create(b + 1, 3, "simulation3", map_reduce = true, conf))
    brokers(0).getName should be ("broker1")
    brokers(1).getName should be ("broker2")
  }
  it should "have 3 VMs and 2 VMs, respectively" in {
    val brokers: List[DatacenterBroker] = List.tabulate(2)(b => MyBroker.create(b + 1, 3, "simulation3", map_reduce = true, conf))
    brokers(0).getVmList.size should be (3)
    brokers(1).getVmList.size should be (2)
  }

  // TEST 6: create virtual machines
  "Virtual Machines" should "have 3 in total" in {
    conf.getInt("simulation3.broker1.num_vm") should be (3)
  }
  it should "have 1 GB bandwidth in host1" in {
    MyVm.create(3, 1, "broker1", "simulation3.broker1", conf).get(0).getBw should be (1000)
  }
  it should "have 1 CPU in VM #1" in {
    MyVm.create(3, 1, "broker1", "simulation3.broker1", conf).get(0).getNumberOfPes should be (1)
  }
  it should "have an image size of 10 GB in VM #2" in {
    MyVm.create(3, 1, "broker1", "simulation3.broker1", conf).get(1).getSize should be (10000)
  }
  it should "have 1000 MIPS in VM #3" in {
    MyVm.create(3, 1, "broker1", "simulation3.broker1", conf).get(2).getMips should be (1000.0)
  }
  it should "have the Xen VM monitor in VM #3" in {
    MyVm.create(3, 1, "broker1", "simulation3.broker1", conf).get(2).getVmm should be ("Xen")
  }

  // TEST 7: create cloudlets
  "Cloudlets" should "be 15 in total" in {
    ConfigFactory.load("simulation2").getInt("simulation2.broker1.num_cloudlet") should be (15)
  }
  it should "have 300 bytes for cloudlet 2" in {
    MyCloudlet.create(15, 0, "broker1", "simulation2.broker1", ConfigFactory.load("simulation2")).get(1).getCloudletFileSize should be (300)
  }
  it should "have ID 12 for cloudlet 12" in {
    MyCloudlet.create(15, 0, "broker1", "simulation2.broker1", ConfigFactory.load("simulation2")).get(11).getCloudletId should be (12)
  }
  it should "need 1 CPU for cloudlet 15" in {
    MyCloudlet.create(15, 0, "broker1", "simulation2.broker1", ConfigFactory.load("simulation2")).get(14).getNumberOfPes should be (1)
  }

  // TEST 8: MapReduce cloudlets and display results
  "MapReduce" should "have datacenter1" in {
    MyDatacenter.create(1, 3, "simulation3", conf).getName should be ("datacenter1")
  }
  it should "have host1 and host2" in {
    MyDatacenter.create(1, 3, "simulation3", conf).getHostList.size should be (2)
  }
  it should "run successfully with broker2" in {
    val broker: DatacenterBroker = MyBroker.create(2, 3, "simulation3", map_reduce = true, conf)
    broker.getName should be ("broker2")
    CloudSim.startSimulation()
    val cloudlets: util.List[_ <: Cloudlet] = broker.getCloudletReceivedList
    cloudlets.size should be > 15
    MyCloudlet.reducer(3, "broker2", cloudlets)
  }
}//end class SimulationTests
