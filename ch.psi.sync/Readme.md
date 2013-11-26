#Overview
The streamer package provides an easy to use way to stream files from one location to an other via ZMQ.
It contains 2 applications, a simple receiver and a stream server. The stream server

# Installation
Prerequisites:
* >= Java 7

Unzip the zip file to any location you like

# Usage

## Receiver
The file receiver can be started via the `receiver` script located in the `bin` folder. The receiver will store the files relatively to 
the directory from which the receiver script is started.

The usage of `receiver` is as follows:

```
Usage: receiver
 -h         Help
 -p <arg>   Source port (default: 8888)
 -s <arg>   Source (default: localhost)
```

## Stream Server
The stream server can be started via the 'streamer' script locatedn in the `bin` folder.

The usage of the `streamer` is as follows:

```
Usage: streamer
 -h         Help
 -p <arg>   Webserver port (default: 8080)
```

# Development

## Build
The project is build via Maven. The installation zip package can be build by executing `mvn clean compile assembly:assembly` . 
After the build the zip file will be inside the `target` directory.

## REST API
Get list of active streams

```
GET stream
200 - [ ]
```

Get version of server

```
GET version
200 - 0.0.0
```

Start new stream

```
PUT stream/{id}

{
    "searchPath" : "/Users/ebner/Desktop/Test",
    "searchPattern":"glob:*",
    "destinationPath":"ttt",

    "numberOfImages":0,
    "destinationPath":"",
    "header":{},

    "port":8888,
    "wipeFile":"true"
    
}

200 Stream created
```
* All keys are optional except searchPath


Terminate stream

```
DELETE stream/{id}

200 - Success
```

### Command Line

```
# Create stream
curl -XPUT --data '{"searchPath":"/Users/ebner/Desktop/Test", "searchPattern":"glob:*","destinationPath":"something"}' --header "Content-Type: application/json" http://<hostname>:<port>/stream/id

# Stop stream
curl -XDELETE http://<hostname>:<port>/stream/id

# Get active streams
curl http://<hostname>:<port>/stream
```