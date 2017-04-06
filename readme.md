# firebase-rsocket-server

Provides non-blocking interface to firebase database using [rsocket](https://github.com/rsocket/rsocket)  protocol   
Based on [reactivesocket-java](https://github.com/ReactiveSocket/reactivesocket-java) and [firebase-data-rxjava](https://github.com/mostroverkhov/firebase-data-rxjava)    
#### Supported transports   
Tcp and udp at the moment. Server relies on `reactivesocket-java`, which also provides
websockets transport out of the box. This one is not implemented yet, but can be added trivially by providing
`ClientTransport` and `ServerTransport` implementations 

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
        Client client = new ClientBuilder(
                new ClientTransportTcp(socketAddress))
                .build();
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
                 
                 Flowable<ReadResponse<Data>> dataWindowFlow = client
                                     .dataWindow(readRequest, Data.class);
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
                                      .dataWindowNotifications(readRequest, Data.class);
 ```
 Reads data windows of size `2` on path `\test\read`, ordered by item key in ascending manner, as stream   
 of `DataWindowChangeEvent`    interleaved with `NextWindow` item for next window query. 
 Not yet consumed items are buffered on server.
 
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
 
#### Security

Firebase rules are used for authorization, particularly, server setup requires authenticator, which
sets credentials [(docs)](https://firebase.google.com/docs/database/admin/start) via `CredentialsFactory`.
 Currently there is one impl - `PropsCredentialsFactory` for property file based credentials setup, file is
 expected to be on classpath (e.g. in `resources/` folder):
 ```
   authFile=fir-rx-data-test-firebase-adminsdk-86b00-5d126dfc04.json
   dbUrl=https://fir-rx-data-test.firebaseio.com/
   dbUserId=firebase_test 
 ```
For testing purposes one can use non-protected database, in that case `ServerBuilder.noAuth()` should be used

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
