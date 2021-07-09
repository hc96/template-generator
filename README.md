### before running the project, please re-install the compiled files
in terminal, go to the directory, and run the following command:
```mvn clean install```

### run this project
after the successful build, you can run the project using:
```mvn exec:java```

### example api call

if you call the api in the Postman, you should also specify the Bearer Token.
 
http://localhost:8888/api/v1/generator?repository=amRepository&circle=all&startDate=1606499859712&endDate=1622134659714&sizeSmall=3&sizeLarge=4

