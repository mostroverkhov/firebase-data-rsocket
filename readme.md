# firebase-rsocket-server

Non-blocking interface to Firebase database over TCP using [RSocket](https://github.com/rsocket/rsocket) based [R2 RPC](https://github.com/mostroverkhov/r2), and [firebase-data](https://github.com/mostroverkhov/firebase-data-rxjava).  

RSocket is an application protocol providing Reactive Streams semantics over an asynchronous, binary boundary.      
#### Usage
###### Setup
Server
```
     Mono<NettyContextCloseable> startedServer = new ServerBuilder(
                     SERVER_PORT)
                     .cacheReads()
                     .classpathPropsAuth("creds.properties")
                     .build()
                     .start();
```

This server caches reads by leveraging Firebase database in-memory caching capabilities (`DatabaseReference.keepSynced`).
Builtin implementation provides simple caching of every data window query for
5 seconds, and is enabled by default. To disable, use `ServerBuilder.noCacheReads()`, to customize - 
`ServerBuilder.cacheReads(NativeCache nativeCache,CacheDuration cacheDuration)`

Client
```
        Mono<Client> client = new ClientBuilder(HOST, SERVER_PORT)
                                .build();
        Mono<FirebaseService> = client.map(Client::request);                        
```
###### Read
 ```
              ReadRequest readRequest =
                 Req
                 .read("test", "read")
                 .asc()
                 .windowWithSize(2)
                 .orderByKey()
                 .build();
                 
                 Flux<ReadResponse> dataWindowFlow = client
                                     .request()
                                     .dataWindow(readRequest);
 ```
 Reads data windows of size `2` on path `\test\read`, ordered by item key in ascending manner.  
 No window change notifications. Emissions respect backpressure.
 
 ```
 ReadRequest readRequest = 
                Req
                .read("test", "read")
                .asc()
                .windowWithSize(2)
                .orderByKey()
                .build();
                
                Flux<NotifResponse> notificationsFlow = client
                                      .request()
                                      .dataWindowNotifications(readRequest);
 ```
 Reads data windows of size `2` on path `\test\read`, ordered by item key in ascending manner, as stream   
 of `NotifResponse`, which is either `window change event` representing current data window item, or `next window` item for next window query.   
 Not yet consumed items are buffered on server.   
 
 Response payloads are represented as json strings. To convert them into typed ones there is `Typed` utilities provided by `Client`, which can be used as follows:
 
 ```
 Typed typed = client.typed();
 
 Function<ReadResponse, TypedReadResponse<Data>> notifTransformer = typed.notificationsOf(Data.class);
 Function<NotifResponse, TypedNotifResponse<Data>> dataWindowTransformer = typed.dataWindowOf(Data.class);
         
 
 Flux<TypedNotifResponse<Data>> notifFlow = client.request()
                 .dataWindowNotifications(readRequest)
                 .map(notifTransformer);
                 
 Flux<TypedReadResponse<Data>> dataWindowFlow = client.request()
                 .dataWindow(readRequest)
                 .map(dataWindowTransformer);          
 ```
 
###### Write
```
   Data data = new Data("w", "w");
   WriteRequest<Data> writeRequest = Req
                   .<Data>write("test", "write")
                   .data(data)
                   .build();
   
   Flux<WriteResponse> writeResponse = client.request()
                   .write(writeRequest);
 ```
 Pushes data onto path `test\write`. Response contains created key 
 
 ###### Delete
 ```
 DeleteRequest delete = Req
                 .delete("test", "delete", "missing")
                 .build();
 Flux<DeleteResponse> deleted = client.request().delete(delete);
 ```
 Removes data on path `test\delete\missi`

#### Standalone binaries
`mvn package` on `server-binary-tcp` will produce runnable fat jar. Authorization credentials and port must be provided with `--config`, `--port` arguments.   

Server can be started as    
```
java -jar firebase-rsocket-server-binary-tcp-<VERSION>.jar --port <PORT> --config <CREDENTIALS_FILE>
```    

For example
```
java -jar firebase-rsocket-server-binary-tcp-0.2-SNAPSHOT.jar --port 8090 --config creds.properties
```
will start server on 8090 port, and assumes credentials file `creds.properties` is in same directory as `jar`. It's convenient to have firebase service account file in same directory aswell. Credentials file format is described in `Authorization` block below
    
#### Authorization

Firebase rules are used for authorization, particularly, server setup requires authenticator, which
sets credentials [(docs)](https://firebase.google.com/docs/database/admin/start) via `ServerBuilder`.
Currently there are 2 implementations, both for property files - filesystem based (`ServerBuilder.fileSystemPropsAuth`) and classpath based (`ServerBuilder.classpathPropsAuth`) ones - both expect reference to credentials config file
 ```
   authFile=fir-rx-data-test-firebase-adminsdk-86b00-5d126dfc04.json
   dbUrl=https://fir-rx-data-test.firebaseio.com/
   dbUserId=firebase_test 
 ```
where `authFile` is Firebase service account file (get one as described in [docs](https://firebase.google.com/docs/admin/setup))    

#### Testing

 Database was not mocked out for pragmatic reasons, so all tests are performed against   
 real database. Sample data for read operations testing is provided by runnable `DataFixture` of `firebase-rsocket-test`.    
 It uses props based authenticator, credentials are expected to be in `resources/creds.properties`.
   
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
