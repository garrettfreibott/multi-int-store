#!/usr/bin/env bash

printf "Fetching access token from Keycloak for admin:admin..."
token_response=$(curl -X POST "http://keycloak:8080/auth/realms/master/protocol/openid-connect/token" -insecure \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "username=admin" \
-d "password=admin" \
-d "grant_type=password" \
-d "client_id=login-client")

# parse out access token from JSON reponse
access_token=$(echo "$token_response" | sed -z "s/[{}\"]//g; s/:/ /g ;s/,/\n/g" | grep -i "access_token" | awk '{print $2}')

printf "\n$access_token\n"