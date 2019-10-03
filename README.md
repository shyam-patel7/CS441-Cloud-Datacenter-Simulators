# HW1: Cloud Datacenter Simulators
### Description: create cloud simulators for evaluating executions of applications in cloud datacenters with different characteristics and deployment models.
This is a homework assignment for CS441 at the University of Illinois at Chicago.
This project is based on [Cloud2Sim](https://sourceforge.net/projects/cloud2sim) and consists of three cloud datacenter simulations, all of which examine performance and cost on the processing of tasks in the form of individual cloudlets.

## Running
To successfully run this project, Java 8 JDK (version 1.8 or higher) and [sbt](https://docs.scala-lang.org/getting-started/sbt-track/getting-started-with-scala-and-sbt-on-the-command-line.html) are required. [IntelliJ IDEA](https://www.jetbrains.com/idea) is highly recommended. The following are two ways to run this project.
1. To run this project on Terminal, or in IntelliJ IDEA's Terminal tool window, enter the following commands:
   1. `cd` into the `homework1` project root directory, and
   1. `sbt clean run` to:
      1. Remove all previously generated files from the target directory,
      2. Compile source code files located in the project's `src/main/scala` directory, and
      3. Run the application.
2. To run this project directly on the sbt shell in IntelliJ IDEA, enter the following commands:
   1. `clean` to remove all previously generated files from the target directory, and
   2. `run` to compile source code files located in the project's `src/main/scala` directory and run the application.

## Testing
This project includes 8 unit tests based on the [ScalaTest](http://www.scalatest.org) testing framework, which are located in the project's `test/scala` directory and include:
1. CloudSim Initialization
2. Loading Configuration
3. Datacenter Creation
4. Host Creation
5. Broker Creation
6. Virtual Machine Creation
7. Cloudlet Creation
8. MapReduce

To run these simulation tests on Terminal, or in IntelliJ IDEA's Terminal tool window, `cd` into the `homework1` project root directory and enter the following command: `sbt test`.