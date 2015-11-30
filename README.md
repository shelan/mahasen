### How to run Mahasen ###

1) Download WSO2 Governance Registry 3.5.0 from here.
http://wso2.com/more-downloads/governance-registry

2) Extract WSO2 Governance Registry 3.5.0. (Lets call this as GREG_HOME)

3) Navigate to GREG_HOME/repository/components/dropins and add following jar

https://github.com/shelan/mahasen/blob/master/mahasen/libs/org.mahasen.broker-4.0.0.jar (Which is a pre built jar of source code.)

You can build this by running command ``mvn clean install`` at root pom level. Built jar will be available at mahasen/modules/core/target


4) Add following jars to GREG_HOME/repository/components/lib folder

https://github.com/shelan/mahasen/blob/master/mahasen/libs/FreePastry-2.1.jar
https://github.com/shelan/mahasen/blob/master/mahasen/libs/httpclient-4.1.1.jar

5) add following configuration file to  (Change IP address to match your machine's IP address and folder paths etc.)

GREG_HOME/repository/conf/

```
######################################################
##### Mahasen Broker Configuration file  #############
######################################################
 
## Boot IP used in pastry to bootstrap (Change this to your BOOT IP address)
boot-ip-address = 10.8.99.170
 
## Boot Port used in pastry to bootstrap
boot-port = 9003
 
## Local machine's IP address (Change this to your IP address)
local-ip-address = 10.8.99.170
 
## Local machien's port
local-port = 9003
 
## WSO2 Registry's URL (Change this to your IP address)
registry-url = https://10.8.99.170:9443/registry
 
## Uploaded file storing repository's path
repository-path = /home/mahasen/tmp/upload/
 
## Keystore files's path (Change this to you GREG_HOME)
truststore-path = wso2greg-3.5.0/resources/security/client-truststore.jks
 
## Download repository
download-repository =/home/mahasen/tmp/download/

```

## To run the client

for client
* navigate to /mahasen/client (Cloned source folder)

* execute ```mvn package``` to build the client jar with required dependencies.

* run jar in command line to get the UI.

Give the login details to log.
Host IP : IP of the node that you are connecting to
port : registry's port of connecting node (default 9443)


