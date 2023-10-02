# Fetch Rewards Backend Software Engineering Intern Exercise

## The Project uses Java Spring Boot.

## Instructions to run the Project

### Prerequisites
- Install Docker. (https://www.docker.com/get-started/)
- Use command line arguments such as "curl" to test the REST API
- [Postman](https://chrome.google.com/webstore/detail/postman/fhbjgbiflinjbdggehcddcbncdddomop?hl=en) to test the REST API.


### Run the project using Docker

- Simply clone the repository. In your console:
- git clone https://github.com/wenjielee11/fetchBEinterview
- ```cd fetchBEinterview```
- Make sure you have docker installed first!
- Paste the following code:
```
docker build -t spring_server .
```
```
docker run -d -p 8080:8080 spring_server
```
- And that's it! You may use Postman or cURL to test the service routes.

### Run the project using IDE
You need the following:
- ```Java version 17: https://www.openlogic.com/openjdk-downloads```
- Install any appropriate Spring Boot IDE.
- Run FetchInterviewApplication.java from IDE
- Use Postman or other tools to test the api.
  
### Run the project using cmd/terminal
- Navigate to the fetchBEinterview folder
- ```mvn spring-boot:run```
- Test with the mentioned tools.

### REST Web Service Routes

1. Add points to a user's account
```
http://localhost:8080/add
```

Request to add points :
```
curl -H "Content-Type: application/json" -d '{"payer":"DANNON", "points":300,"timestamp":"2020-11-02T14:00:00Z"}' http://localhost:8080/add
```
Change the payer name, points, and timestamps accordingly.

2. Spend points

EndPoint :
```
http://localhost:8080/spend
```

Request :
```
curl -H "Content-Type: application/json" -d '{"points":100}' http://localhost:8080/spend
```

Response :
```
[
{ "payer": "UNILEVER", "points": -200 },
{ "payer": "MILLER COORS", "points": -4,700 }
{ "payer": "DANNON", "points": -100 },
]
```

3. Balance of user

EndPoint : 
```
http://localhost:8080/balance
```

Request : 
```
curl http://localhost:8080/balance
```

Response : 
```
{
    "UNILEVER" : 0,
    "MILLER COORS" : 5300,
    "DANNON" : 1000
}
```
### Stopping the docker process
In the command line,
```
docker ps  # Get the container ID
docker stop <container_id>
```
