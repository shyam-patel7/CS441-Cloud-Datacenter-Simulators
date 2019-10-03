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
  // Typesafe Config
  "com.typesafe" % "config" % "1.3.4",
  // ScalaTest
  "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  // SLF4J API Module
  "org.slf4j" % "slf4j-api" % "1.7.28"
)
