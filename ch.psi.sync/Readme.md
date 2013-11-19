#Overview
File sync via ZeroMQ

```
           +---------------------------------------------------------+
           |                                                         |
           |      +--------------+           +--------------+        |
           |      | watch        |           | sender       |        |
       +------+   |              | EventBus  |              |      +-----+
       |MEMORY|   |              |           |              |      |ZMQ  |
       +------+   |              |           |              |      +-----+
           |      +--------------+           +--------------+        |
           |                                                         |
           | streamer                                                |
           +---------------------------------------------------------+
```


# Receiver
The file receiver can be started via the `receiver` startup script located in the `bin` folder. The receiver will store the files relatively to 
the directory from which the receiver script is tarted.

## Usage

```
Usage: receiver
 -h         Help
 -p <arg>   Source port (default: 8080)
 -s <arg>   Source (default: localhost)
```
