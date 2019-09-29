/* CS441 HW1: Cloud Datacenter Simulators
 * Name:   Shyam Patel
 * NetID:  spate54
 * Date:   Oct 4, 2019
 */

name := "homework1"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  // Logback Classic Module
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  // Logback Core Module
  "ch.qos.logback" % "logback-core" % "1.2.3",
  // Typesafe Config
  "com.typesafe" % "config" % "1.3.4",
  // Apache Commons Math
  "org.apache.commons" % "commons-math3" % "3.6.1",
  // JUnit Jupiter API
  "org.junit.jupiter" % "junit-jupiter-api" % "5.5.2",
  // SLF4J API Module
  "org.slf4j" % "slf4j-api" % "1.7.28"
)
