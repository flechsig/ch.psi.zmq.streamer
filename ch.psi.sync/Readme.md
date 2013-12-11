#Overview
The streamer package provides an easy to use way to stream files from one location to an other via ZMQ.
It contains 2 applications, a simple receiver and a stream server. The stream server



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

## Stream Server - UI
Streamer comes with a web UI. One can use this UI to list and manage current streams.
The web UI is accessible on `http://<host>:8080/static/` . Note: It is important to have the last / inside the url!

![Screenshot](ch.psi.sync/screenshot.png?raw) 


# Development

## Build
The project is build via Maven. The installation zip package can be build by executing `mvn clean compile assembly:assembly` . 
After the build the zip file will be inside the `target` directory.

## REST API

Get version of server

```
GET version
200 - 0.0.0
```

Get list of active streams

```
GET stream
Accept: application/json

200 - [ ]
```

Register for stream changes 

```
GET events
 
200 - Server Send Event (SSE) stream.
```

****

Messages currently supported:

* **stream** - list of current streams

    ```
    [ ]
```

****

Start new stream

```
PUT stream/{id}

{
    "searchPath" : "/Users/ebner/Desktop/Test",
    "searchPattern":"glob:*",
    "destinationPath":"ttt",

    "numberOfImages":0,
    "header":{},

    "port":8888,
    "wipeFile":"false"
    
}

204 Stream created
```
* All keys are optional except searchPath


Terminate stream

```
DELETE stream/{id}

200 - Success
```

Get Stream information/status

```
GET stream/{id}

404 Not found - If stream does not exist (anymore)
200 
{
    "status": {
        "sendCount": 0
    },
    "configuration": {
        "searchPath": "/Users/ebner/Desktop/test",
        "searchPattern": "glob:*",
        "numberOfImages": 0,
        "destinationPath": "",
        "header": null,
        "port": 8888,
        "highWaterMark": 1000,
        "wipeFile": true
    }
}
```

### Command Line

Create stream

```
curl -XPUT --data '{"searchPath":"/Users/ebner/Desktop/Test", "searchPattern":"glob:*","destinationPath":"something"}' --header "Content-Type: application/json" http://<hostname>:<port>/stream/id
```

Stop stream

```
curl -XDELETE http://<hostname>:<port>/stream/id
```

Get stream status

```
curl http://<hostname>:<port>/stream/id
```

Get active streams (monitor)

```
curl -H "Accept: application/json" http://<hostname>:<port>/stream
```

Get active streams (monitor)

```
curl http://<hostname>:<port>/stream
```


# Installation
The **Stream** package required Java 7 or greater.

## Simple Installation
Extract zip file

## Daemon Installation

```
mkdir /opt/streamer
cd /opt/streamer
unzip ch.psi.streamer-<version>-bin.zip
ln -s ch.psi.streamer-<version> latest
```

Register Init Script

```
cp latest/var/streamer /etc/init.d/
chmod 755 /etc/init.d/streamer
chkconfig --add streamer
chkconfig streamer on
```

Start/Stop Escape Service

```
service streamer start
service streamer stop
```