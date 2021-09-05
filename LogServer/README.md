# How to start
This is the server application to handle the client request to query the log.<br />
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
Once you see "Your log server is up!" on the top, that means your log server is up and running<br />

# API Spec
The server use the Restful API call to commnuicate with client. Here is the API Spec:<br />
Method GET : http://hostname:port/log/query?fileName=[some name]&n=[some number]&keyWord=[some string] <br />

## Request Parameter
- "fileName" is the mandatory parameter. User has to specify the log file name to search from.<br />
- "n" is optional parameter. That's correspondent to the number of the event user want to query. The default value is 10. To prevent the system from being overloaded, I introduced a maximum limit number which is 100. That means no more than 100 log entry to be returned. If user specify any number which is exceed the limit, API will return "Invalid Input" error.
- "keyWord" is optional parameter. That indicates searching by the keyWord. The result set will be filtered by the keyword search.<br />

## Respond Body
The API will return a json format string. Here are the following fields:<br />
- "events" This object is a list of log entry, which includes the all the metadata of the log includes timestamp, thread, class name, level, and message text.
- "error" This indicates the error thrown by the server. If the request going well, then this field will be empty. Here are all the possible errors:
  - File not found
  - Wrong log format
  - Invalid input

# Some catch out of the scope
There are a few points I noticed which is not included in the requirement, but I think it's worth being added.
1. I make this application highly configurable. There are some key parameters of the system I made it configurable in the application.properties. For example: the log directory, log regex pattern, timestamp format and etc. The benefit of it is user is able to use one code base to handle various scenarios. Any changes can be done directly through the properties file configuration, instead of making any code deployment.
2. I make this application thread safe. This application can not only deal with the static unchanged log, but also can handle the dynamic log file. I tested with one process keep writting the same log file, when the client is querying the log. It can work concurrently. I also introduce one protection mechanism which is when the writing is much faster than the read. At that time log file will increase quite fast so the read will highly possible not able to finish. At that circumstance I will make the process stop after the log pass the timestamp when the request first came.
3. I introduced the in-memory cache. Since most of the time, the file will be static for the historic log. So it makes more sense to cache the user request, and setup the eviction policy. The performance will have much better improvement when the log file is very large. The expired time is also configurable in the properties file.
 
