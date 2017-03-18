# firebase-rsocket-server

Proof-of-concept for non-blocking backpressured interface to firebase database    
Based on `reactive-socket-java` [[link]](https://github.com/ReactiveSocket/reactivesocket-java) and `firebase-data-rxjava` [[link]](https://github.com/mostroverkhov/firebase-data-rxjava)    
#### Supported transports   
Tcp only at the moment. Server relies on `reactive-socket-java`, which provides
tcp, udp, websockets transports out of the box, some of those will be eventually 
supported   

#### Usage
###### Setup
Server
```
        InetSocketAddress socketAddress = new InetSocketAddress(8090);
         Server server = new ServerBuilder()
                 .socketAddress(socketAddress)
                 .credsFile("creds.properties")
                 .build();
         Completable serverStop = server.start();
```

Server supports reads caching by leveraging database in-memory caching capabilities (`DatabaseReference.keepSynced`).
Builtin implementation provides caching of every data window query for
5 seconds, and is enabled by default. To disable, use `ServerBuilder.noCacheReads()`, to customize - 
`ServerBuilder.cacheReads(NativeCache nativeCache,CacheDuration cacheDuration)`

Client
```
        Client client = new ClientBuilder()
                .socketAddress(socketAddress)
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
 ###### Delete
 ```
 Flowable<DeleteResponse> deleteFlow = client
                 .delete(Requests.deleteRequest("test", "delete")
                         .build());
 ```
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
#### Testing
Firebase database was not mocked out for pragmatic reasons, so all tests are performed against
 real database. Sample data for read operations testing is provided by runnable `DataFixture` of `firebase-rsocket-test`. It uses props based authenticator,
 credentials are expected to be in `resources/creds.properties`. Very rough latency estimation can be done with
 `LatencyCheck` test
   
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
