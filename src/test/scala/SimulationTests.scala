/* CS441 HW1: Cloud Datacenter Simulators
 * Name:   Shyam Patel
 * NetID:  spate54
 * Date:   Oct 4, 2019
 */

import com.hw1.dc.{MyBroker, MyCloudlet, MyDatacenter, MyHost, MyVm}
import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.{Cloudlet, Datacenter, DatacenterBroker, Host, Log, Vm}
import org.junit.jupiter.api.{BeforeEach, DisplayName, Test}
import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse, assertNotNull, assertTrue}
import org.slf4j.{Logger, LoggerFactory}
import java.util.Calendar
import java.util

// SimulationTests class consisting of 8 unit tests based on the JUnit test framework
@DisplayName("Simulation Tests")
class SimulationTests {
  // logger
  val log: Logger = LoggerFactory.getLogger(this.getClass)
  // configuration
  val conf: Config = ConfigFactory.load("simulation3")

  // TEST: initialize CloudSim
  @BeforeEach @Test @DisplayName("CloudSim Initialization")
  def initialization(): Unit = {
    assertNotNull(log)
    assertNotNull(conf)
    val num_user: Int = conf.getInt("simulation3.num_user")
    val trace_flag: Boolean = conf.getBoolean("simulation3.trace_flag")
    assertNotNull(num_user)
    assertNotNull(trace_flag)
    assertEquals(1, num_user)
    assertFalse(trace_flag)
    CloudSim.init(num_user, Calendar.getInstance, trace_flag)
    Log.disable()
    val entityId: Int = CloudSim.getCloudInfoServiceEntityId
    assertNotNull(entityId)
    assertEquals(1, entityId)
  }//end def initialization

  // TEST: load configuration and read from it
  @Test @DisplayName("Loading Configuration")
  def configuration(): Unit = {
    log.info("*** TEST: Loading Configuration ***")
    val c: Config = ConfigFactory.load("simulation1")
    log.info("Loading configuration...")
    assertNotNull(c)
    val num_user: Int = c.getInt("simulation1.num_user")
    val trace_flag: Boolean = c.getBoolean("simulation1.trace_flag")
    val map_reduce: Boolean = c.getBoolean("simulation1.map_reduce")
    val num_datacenter: Int = c.getInt("simulation1.num_datacenter")
    val num_broker: Int = c.getInt("simulation1.num_broker")
    val os: String = c.getString("simulation1.datacenter1.os")
    val time_zone: Double = c.getDouble("simulation1.datacenter1.time_zone")
    val costPerMem: Double = c.getDouble("simulation1.datacenter1.costPerMem")
    val ram: Int = c.getInt("simulation1.datacenter1.host1.ram")
    val mips: Int = c.getInt("simulation1.datacenter1.host1.mips")
    assertNotNull(num_user)
    assertNotNull(trace_flag)
    assertNotNull(map_reduce)
    assertNotNull(num_datacenter)
    assertNotNull(num_broker)
    assertNotNull(os)
    assertNotNull(time_zone)
    assertNotNull(costPerMem)
    assertNotNull(ram)
    assertNotNull(mips)
    log.info(s"Number of users: $num_user (expected 1)")
    log.info(s"Trace events: $trace_flag (expected false)")
    log.info(s"MapReduce: $map_reduce (expected false)")
    log.info(s"Number of datacenters: $num_datacenter (expected 1)")
    log.info(s"Number of brokers: $num_broker (expected 2)")
    log.info(s"OS of datacenter1: $os (expected Linux)")
    log.info(s"Time zone of datacenter1: UTC$time_zone (expected UTC-5.0)")
    log.info(s"Memory cost of datacenter1: $$$costPerMem (expected $$0.05)")
    log.info(s"Memory of host1: $ram MB (expected 2048 MB)")
    log.info(s"MIPS rating of host1: $mips mn instructions/sec (expected 3000)")
    assertEquals(1, num_user)
    assertFalse(trace_flag)
    assertFalse(map_reduce)
    assertEquals(1, num_datacenter)
    assertEquals(2, num_broker)
    assertEquals("Linux", os)
    assertEquals(-5.0, time_zone)
    assertEquals(0.05, costPerMem)
    assertEquals(2048, ram)
    assertEquals(3000, mips)
    log.info("Test successfully completed.")
  }//end def configuration

  // TEST: create datacenter with hosts
  @Test @DisplayName("Datacenter Creation")
  def createDatacenter(): Unit = {
    log.info("*** TEST: Datacenter Creation ***")
    val num_datacenter: Int = conf.getInt("simulation3.num_datacenter")
    assertNotNull(num_datacenter)
    assertEquals(1, num_datacenter)
    log.info("Creating datacenter...")
    log.info("Creating hosts...")
    val datacenters: List[Datacenter] = List.tabulate(num_datacenter)(n =>
      MyDatacenter.create(n + 1, 3, "simulation3", conf))
    assertNotNull(datacenters)
    assertEquals(1, datacenters.size)
    val name: String = datacenters(0).getName
    assertNotNull(name)
    assertEquals("datacenter1", name)
    log.info(s"$name successfully created.")
    val hosts: util.List[_ <: Host] = datacenters(0).getHostList
    assertNotNull(hosts)
    assertEquals(2, hosts.size)
    hosts.forEach(host => log.info(String.format("host%d successfully created.", host.getId)))
    log.info("Test successfully completed.")
  }//end def createDatacenter

  // TEST: create hosts
  @Test @DisplayName("Host Creation")
  def createHost(): Unit = {
    log.info("*** TEST: Host Creation ***")
    val num_host: Int = conf.getInt("simulation3.datacenter1.num_host")
    assertNotNull(num_host)
    assertEquals(2, num_host)
    log.info("Creating hosts...")
    val hosts: util.List[Host] = MyHost.create(num_host, "datacenter1", "simulation3.datacenter1", conf)
    assertNotNull(hosts)
    assertEquals(num_host, hosts.size)
    val host1Mips: Double = hosts.get(0).getAvailableMips
    val host1Ram: Double = hosts.get(0).getRam
    val host2Bw: Long = hosts.get(1).getBw
    val host2Storage: Long = hosts.get(1).getStorage
    assertNotNull(host1Mips)
    assertNotNull(host1Ram)
    assertNotNull(host2Bw)
    assertNotNull(host2Storage)
    log.info(s"host1 available MIPS: $host1Mips (expected 16000.0)")
    log.info(s"host1 memory: $host1Ram (expected 4096.0)")
    log.info(s"host2 bandwidth: $host2Bw (expected 10000)")
    log.info(s"host2 storage: $host2Storage (expected 1000000)")
    assertEquals(16000.0, host1Mips)
    assertEquals(4096.0, host1Ram)
    assertEquals(10000, host2Bw)
    assertEquals(1000000, host2Storage)
    hosts.forEach(host => log.info(String.format("host%d successfully created.", host.getId)))
    log.info("Test successfully completed.")
  }//end def createHost

  // TEST: create brokers with VMs and cloudlets
  @Test @DisplayName("Broker Creation")
  def createBroker(): Unit = {
    log.info("*** TEST: Broker Creation ***")
    log.info("Creating broker...")
    val num_broker: Int = conf.getInt("simulation3.num_broker")
    val map_reduce: Boolean = conf.getBoolean("simulation3.map_reduce")
    assertNotNull(num_broker)
    assertNotNull(map_reduce)
    assertEquals(2, num_broker)
    assertTrue(map_reduce)
    log.info("Creating VMs...")
    log.info("Creating cloudlets...")
    val broker: DatacenterBroker = MyBroker.create(1, 3, "simulation3", map_reduce, conf)
    assertNotNull(broker)
    val brokerName: String = broker.getName
    assertNotNull(brokerName)
    assertEquals("broker1", brokerName)
    log.info(String.format("%s successfully created.", brokerName))
    val vms: util.List[_ <: Vm] = broker.getVmList
    assertNotNull(vms)
    assertEquals(3, vms.size)
    (0 until vms.size).foreach(vm => log.info(String.format("VM #%d successfully created.", vm + 1)))
    val cloudlets: util.List[_ <: Vm] = broker.getCloudletList
    assertNotNull(cloudlets)
    log.info(String.format("%d slave cloudlets successfully created.", cloudlets.size))
    log.info("Test successfully completed.")
  }//end def createBroker

  // TEST: create virtual machines
  @Test @DisplayName("Virtual Machine Creation")
  def createVm(): Unit = {
    log.info("*** TEST: Virtual Machine Creation ***")
    val num_vm: Int = conf.getInt("simulation3.broker1.num_vm")
    assertNotNull(num_vm)
    assertEquals(3, num_vm)
    log.info("Creating VMs...")
    val vms: util.List[Vm] = MyVm.create(num_vm, 1, "broker1", "simulation3.broker1", conf)
    assertNotNull(vms)
    assertEquals(num_vm, vms.size)
    val vm1Bw: Long = vms.get(0).getBw
    val vm1Pes: Int = vms.get(0).getNumberOfPes
    val vm2Id: Int = vms.get(1).getId
    val vm2Size: Long = vms.get(1).getSize
    val vm3Mips: Double = vms.get(2).getMips
    val vm3Vmm: String = vms.get(2).getVmm
    assertNotNull(vm1Bw)
    assertNotNull(vm1Pes)
    assertNotNull(vm2Id)
    assertNotNull(vm2Size)
    assertNotNull(vm3Mips)
    assertNotNull(vm3Vmm)
    log.info(s"VM #1 bandwidth: $vm1Bw (expected 1000)")
    log.info(s"VM #1 number of CPUs: $vm1Pes (expected 1)")
    log.info(s"VM #2 ID: $vm2Id (expected 2)")
    log.info(s"VM #2 image size: $vm2Size (expected 10000)")
    log.info(s"VM #3 MIPS rating: $vm3Mips (expected 1000.0)")
    log.info(s"VM #3 VM monitor: $vm3Vmm (expected Xen)")
    assertEquals(1000, vm1Bw)
    assertEquals(1, vm1Pes)
    assertEquals(2, vm2Id)
    assertEquals(10000, vm2Size)
    assertEquals(1000.0, vm3Mips)
    assertEquals("Xen", vm3Vmm)
    vms.forEach(vm => log.info(String.format("VM #%d successfully created.", vm.getId)))
    log.info("Test successfully completed.")
  }//end def createVm

  // TEST: create cloudlets
  @Test @DisplayName("Cloudlet Creation")
  def createCloudlet(): Unit = {
    log.info("*** TEST: Cloudlet Creation ***")
    val c: Config = ConfigFactory.load("simulation2")
    assertNotNull(c)
    val num_vm: Int = c.getInt("simulation2.broker1.num_vm")
    val num_cloudlet: Int = c.getInt("simulation2.broker1.num_cloudlet")
    assertNotNull(num_vm)
    assertNotNull(num_cloudlet)
    assertEquals(3, num_vm)
    assertEquals(15, num_cloudlet)
    val cloudlets: util.List[Cloudlet] = MyCloudlet.create(num_cloudlet, 0, "broker1", "simulation2.broker1", c)
    assertNotNull(cloudlets)
    assertEquals(15, cloudlets.size)
    val cloudlet1Id: Int = cloudlets.get(14).getCloudletId
    val cloudlet6Id: Int = cloudlets.get(9).getCloudletId
    val cloudlet15Id: Int = cloudlets.get(0).getCloudletId
    assertNotNull(cloudlet1Id)
    assertNotNull(cloudlet6Id)
    assertNotNull(cloudlet15Id)
    log.info(s"Cloudlet 1 ID: $cloudlet1Id (expected 1)")
    log.info(s"Cloudlet 6 ID: $cloudlet6Id (expected 6)")
    log.info(s"Cloudlet 15 ID: $cloudlet15Id (expected 15)")
    assertEquals(1, cloudlet1Id)
    assertEquals(6, cloudlet6Id)
    assertEquals(15, cloudlet15Id)
    log.info(String.format("%d cloudlets successfully created.", cloudlets.size))
    log.info("Test successfully completed.")
  }//end def createCloudlet

  // TEST: MapReduce cloudlets and display results
  @Test @DisplayName("MapReduce")
  def mapReduce(): Unit = {
    log.info("*** TEST: MapReduce ***")
    log.info("Creating datacenter...")
    log.info("Creating hosts...")
    val dc: Datacenter = MyDatacenter.create(1, 3, "simulation3", conf)
    assertNotNull(dc)
    val name: String = dc.getName
    assertNotNull(name)
    assertEquals("datacenter1", name)
    log.info(s"$name successfully created.")
    val hosts: util.List[_ <: Host] = dc.getHostList
    assertNotNull(hosts)
    assertEquals(2, hosts.size)
    (0 until hosts.size).foreach(host => log.info(String.format("host%d successfully created.", host + 1)))
    log.info("Creating broker...")
    log.info("Creating VMs...")
    log.info("Creating cloudlets...")
    val map_reduce: Boolean = conf.getBoolean("simulation3.map_reduce")
    assertNotNull(map_reduce)
    assertTrue(map_reduce)
    val broker: DatacenterBroker = MyBroker.create(2, 3, "simulation3", map_reduce, conf)
    assertNotNull(broker)
    val brokerName: String = broker.getName
    assertNotNull(brokerName)
    assertEquals("broker2", brokerName)
    log.info(String.format("%s successfully created.", brokerName))
    log.info("Beginning simulation...")
    CloudSim.startSimulation()
    log.info("Reducing mapped cloudlets...\n")
    MyCloudlet.reducer(3, brokerName, broker.getCloudletReceivedList)
    log.info("Test successfully completed.")
  }//end def mapReduce
}//end class SimulationTests
