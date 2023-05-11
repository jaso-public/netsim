# NetSim
Event based network simulator.

## Description

## Building NetSim
The easiest way to run and modify NetSim is from inside an IDE.
The classes in the jaso.netsim.simulation are the runnable simulations.


##Using Maven
The fastest way to get going is to use maven (if you have it installed)

```
git clone https://github.com/jaso-public/netsim.git
cd netsim
mvn clean compile package
```
will create the netsim.jar in the target subdirectory


To run the simulation do:

``` 
java -cp target/netsim.jar jaso.netsim.simulation.SimulatorHostToHost
```

## Bootstrapping an EC2 ubuntu instance
Note: if you want to run this on a brand new ubuntu machine in EC2, do the following first.

```
sudo apt-get update -y
sudo apt install -y openjdk-19-jre-headless
sudo apt install -y maven
```
