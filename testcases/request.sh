#!/bin/bash
for i in {1..100}
do
   curl -X POST --location "http://localhost:8080/wallet/transfer" \
       -H "Content-Type: application/json" \
       -H "username: testUser" \
       -d "{\"platform_name\":\"platform\",\"date\":1535015447,\"from\":\"user1\",\"to\":\"user2\",\"sum\":33}"
done