# firebase-rsocket-server

Provides non-blocking interface to firebase database over network using [rsocket](https://github.com/rsocket/rsocket)  protocol   
Based on [reactivesocket-java](https://github.com/ReactiveSocket/reactivesocket-java) and [firebase-data-rxjava](https://github.com/mostroverkhov/firebase-data-rxjava)    

#### Motivations
* Firebase database maintains data for queried paths in memory, this may cause unpredictable memory usage by service host process
* Every database client becomes aware of firebase authentication
* Native APIs are callback based, with threading controlled by firebase, this makes composing results of different queries hard 

#### Supported transports   
tcp, websockets, udp. `ClientTransport` and `ServerTransport` implementations for each transport are available on `transport-` submodules    

#### Standalone binaries
`mvn package` on `server-binary-tcp` will produce runnable uber jar supporting tcp transport. Authorization credentials and port must be provided with `--config`, `--port` arguments.   

Server can be started as    
```
java -jar firebase-rsocket-server-binary-tcp-<VERSION>.jar --port <PORT> --config <CREDENTIALS_FILE>
```    

For example
```
java -jar firebase-rsocket-server-binary-tcp-0.1.1-SNAPSHOT.jar --port 8090 --config creds.properties
```
will start server on 8090 port. It assumes credentials file `creds.properties` is in same directory as `jar`. It's convenient to have firebase service account file in same directory aswell. Authorization file format is described in `Authorization` block below    

#### Usage
###### Setup
Server
```
     InetSocketAddress socketAddress = new InetSocketAddress(8090);
          Server server = new ServerBuilder(
                 new ServerTransportTcp(socketAddress))
                .cacheReads()
                .credentialsAuth("creds.properties")
                .build();
```

This server caches reads by leveraging firebase database in-memory caching capabilities (`DatabaseReference.keepSynced`).
Builtin implementation provides simple caching of every data window query for
5 seconds, and is enabled by default. To disable, use `ServerBuilder.noCacheReads()`, to customize - 
`ServerBuilder.cacheReads(NativeCache nativeCache,CacheDuration cacheDuration)`

Client
```
        ClientFactory clientFactory = new ClientBuilder(
                new ClientTransportTcp(socketAddress))
                .build();
        Client client = clientFactory.client();
```
###### Read
 ```
              ReadRequest readRequest =
                 Requests
                 .readRequest("test", "read")
                 .asc()
                 .windowWithSize(2)
                 .orderByKey()
                 .build();
                 
                 Flowable<ReadResponse> dataWindowFlow = client
                                     .dataWindow(readRequest);
 ```
 Reads data windows of size `2` on path `\test\read`, ordered by item key in ascending manner.  
 No window change notifications.
 Data windows respect backpressure
 
 ```
 ReadRequest readRequest = 
                Requests
                .readRequest("test", "read")
                .asc()
                .windowWithSize(2)
                .orderByKey()
                .build();
                
                Flowable<NotifResponse> notificationsFlow = client
                                      .dataWindowNotifications(readRequest);
 ```
 Reads data windows of size `2` on path `\test\read`, ordered by item key in ascending manner, as stream   
 of `NotifResponse`, which is either `window change event` representing current data window item, or `next window` item for next window query.   
 Not yet consumed items are buffered on server.   
 
 Response payloads are represented as json strings. To convert them into typed ones there is `Transform` provided by `ClientFactory.transform()`, which can be used as follows:
 ```
 Transfrom transform = clientFactory.transform()
 Flowable<ReadResponse> resp = client.dataWindow(request);
 Flowable<TypedReadResponse<Data>> response = resp
                .observeOn(Schedulers.io())
                .flatMap(reply -> transform.dataWindowOf(Data.class).from(reply));

 Flowable<TypedNotifResponse<Data>> notifFlow = client
                .dataWindowNotifications(readRequest)
                .flatMap(reply -> transform.notificationsOf(Data.class).from(reply));          
 ```
 
###### Write
```
             Data data = new Data("w", "w");
                   WriteRequest<Data> writeRequest = Requests
                           .<Data>writeRequest("test", "write")
                           .data(data)
                           .build();
           
             Flowable<WriteResponse> writeResponse = client
                           .write(writeRequest);
 ```
 Pushes data onto path `test\write`. Response contains created key 
 
 ###### Delete
 ```
 Flowable<DeleteResponse> deleteFlow = client
                 .delete(Requests.deleteRequest("test", "delete")
                         .build());
 ```
 Removes data on path `test\delete`

 #### Logging

Minimal insights about server processing can be obtained with `Logger` implementation set   
as `ServerBuilder.logging(Logger logger)`. Server will report started, served and error requests.   
 
#### Authorization

Firebase rules are used for authorization, particularly, server setup requires authenticator, which
sets credentials [(docs)](https://firebase.google.com/docs/database/admin/start) via `ServerBuilder`.
Currently there are 2 implementations, both for property files - filesystem based (`ServerBuilder.fileSystemPropsAuth`) and classpath based (`ServerBuilder.classpathPropsAuth`) ones -both expect reference to credentials config file
 ```
   authFile=fir-rx-data-test-firebase-adminsdk-86b00-5d126dfc04.json
   dbUrl=https://fir-rx-data-test.firebaseio.com/
   dbUserId=firebase_test 
 ```
where `authFile` is Firebase service account file (get one as described in [docs](https://firebase.google.com/docs/admin/setup))    

#### Testing

Firebase database was not mocked out for pragmatic reasons, so all tests are performed against   
 real database. Sample data for read operations testing is provided by runnable `DataFixture` of `firebase-rsocket-test`.    
 It uses props based authenticator, credentials are expected to be in `resources/creds.properties`.    
 Very rough latency estimation can be done with `LatencyCheck` test
   
#### LICENSE
Copyright 2017 Maksym Ostroverkhov

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
