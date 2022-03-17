# Blockchain service sample

Kotlin, Ktor.

# version 0.0.1-SNAPSHOT

--- 

### build and run 
1. just run app 
2. init first block and wallet by default 
3. send request:
```http request
POST http://localhost:8080/wallet/transfer
Content-Type: application/json
username: testUser

{"platform_name":"platform","date":1535015447,"from":"user1","to":"user2","sum":33}
```
`see more testcases/request.http` 

--- 
> Main threads:
1. https://kennycason.com/posts/2018-08-25-blockchain-kotlin.html  
2. https://medium.com/@vasilyf/lets-implement-a-cryptocurrency-in-kotlin-part-1-blockchain-8704069f8580
