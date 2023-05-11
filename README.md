# NetSim
Event based network simulator.

## Description

## Building NetSim
The easiest way to run net sim is from inside an IDE.
The classes in the jaso.netsim.simulation are the runnable simulations.

You can also use maven to build the single jar file.
```
mvn clean compile package
```
will create the netsim.jar in the target subdirectory


``` 
java -cp target/netsim.jar jaso.netsim.simulation.SimulatorHostToHost
```



