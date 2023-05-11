# NetSim
Event based network simulator.

## Description

## Building NetSim
The easiest way to run net sim is from inside an IDE.
The classes in the jaso.netsim.simulation are the runnable simulations.




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



