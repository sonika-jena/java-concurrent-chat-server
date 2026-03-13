# Java Concurrent Chat Server

A secure concurrent multi-client chat server implemented in Java using TLS encryption, thread pools, and asynchronous message routing.

## Features
- TLS encrypted communication
- Secure password hashing (PBKDF2 + salt)
- Concurrent client handling with thread pools
- Asynchronous broadcast routing using BlockingQueue
- Load testing framework
- Unit tests (JUnit)

## Architecture

Client  
↓  
TLS Socket  
↓  
Thread Pool  
↓  
ClientHandler  
↓  
MessageRouter (BlockingQueue)  
↓  
Broadcast Worker Threads  
↓  
Clients

## Commands

```
REGISTER <username> <password>
LOGIN <username> <password>
MSG <username> <message>
BROADCAST <message>
CLIENTS
QUIT
```
