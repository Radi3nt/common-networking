# common networking

A networking abstraction layer that encapsulates a varaity of transport protocols

## Features

- Currently abstract the following:
    - TCP
    - Steam
- Engagements:
    - Compression
    - Encryption
- Reliability
    - Support for unreliable and reliable packets
- Multiple connections from the same machine
- Non blocking reading
- Concurrency friendly
- Easy to integrate and setup
## Roadmap

- UDP handling
    - reliability (using NACK or ACK)

## Installation

Install with maven, and if you want to use the **Steam** module, you will need to download [Steamworks4J](https://code-disaster.github.io/steamworks4j/)
## Usage/Examples

### Tcp
You can check the [client](examples/src/main/java/fr/radi3nt/networking/tcp/example/MainTcpClient.java) and [server](examples/src/main/java/fr/radi3nt/networking/tcp/example/MainTcpServer.java) for TCP.


### Steam
There is also a [client](examples/src/main/java/fr/radi3nt/networking/steam/example/MainSteamClient.java) and a [server](examples/src/main/java/fr/radi3nt/networking/steam/example/MainSteamServer.java) for Steam.

You can also use the `SteamJoinConnectionAcceptor` class to let the client connect to any server they want (from their steam friends).
## Support

For support, email pro.radi3nt@gmail.com or send a message on discord to @radi3nt.


## Authors

- [@radi3nt](https://github.com/Radi3nt)

