# Master Server
This is a server application to bridge the client request towards
the target log server. Client will make the restful API request through
this master server by the keyword search and list of target machine.
This server is going to make rpc call to the target servers to collect all the
elidgible result set.

## How to start
For local testing:<br />
1. go to the project directory <br />
2. run command : mvnw spring-boot:run <br />

For remote machine:<br />
1. go to the project directory <br />
2. run command : mvn clean install <br />
3. go to the target folder and deploy the war file under the tomcat server on remote machine. <br />
   <br />
   The default port is 8080, if you want to change the port number, you can add the settings in application.properties <br />
   For example: server.port=8081 <br />
   <br />
   Once you see "Your master server is up!" on the top, that means your log server is up and running<br />
   
**Note**: 
- Your master server has to be hosted on the different machine/port number which needs to be separate from the log server(s).
For local testing, I put log server on port 8081, and master server runs on 8080.
- You need to specify the "log.server.cluster" value in application.properties file. This indicates all the cluster machine node hostname.
The API call have to give the machine host within this list, otherwise will throw the "Invalid Input" error.

## API Spec
The server use the Restful API call to commnuicate with client. Here is the API Spec:<br />

## Request Parameter
Method GET : http://hostname:port/master/search?keyWord=[some string]&n=[some number]&machines=[list of machine] <br />
This method is to query the log entry by keyword search into all log files by the specified remote server(s), result set is reverse time ordered.
- "keyWord" is the mandatory parameter. That indicates searching by the keyWord. The result set will be filtered by the keyword search.<br />
- "n" is optional parameter. That's correspondent to the number of the event user want to query. The default value is 10. To prevent the system from being overloaded, I introduced a maximum limit number which is 100. That means no more than 100 log entry to be returned. If user specify any number which is exceed the limit, API will return "Invalid Input" error.
- "machines" is mandatory parameter. That indicates a list of machine user want to search the log from.

## Respond Body
The API will return a json format string. Here are the following fields:<br />
- "events" This object is a list of log entry, which includes the all the metadata of the log includes timestamp, thread, class name, level, and message text.
- "error" This indicates the error thrown by the server. If the request going well, then this field will be empty. Here are all the possible errors:
    - File not found
    - Wrong log format
    - Invalid Input