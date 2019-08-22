#!/usr/bin/env bash

# Name to give the docker network and stack. Update network instances in docker-compose.yml file if modified.
name="cdr-local"

helptext="\
Usage: deploy.sh [command]
Utility script for setting up a docker network and deploying a local instance of CDR into a docker swarm.
'deploy' is the default option if no command is given.

Commands:
  d, deploy\t\tcreate the docker network and stack, stopping if either already exists
  u, update\t\tattempts an in-place update of the deployed docker stack
  c, clean\t\tremoves the deployed docker stack and network (destructive)
  r, redeploy\t\tclean + deploy: completely removes the stack and network and deploys new instances (destructive)
"

# Evaluate the first parameter passed to the function every periodically.
# When the expression evaluates to an empty string, return.
function wait_for_empty_results() {
  sleep 1
  while [[ -n  $(eval $1) ]]; do
    sleep 1
    printf "." >&2
  done
}


# Removes the docker stack and network.
# Docker networks usually take a few seconds to go down, so it waits to prevent redeployment issues
function cleanup () {
    printf "Removing $name docker stack and network...\n"
    docker stack rm $name
#    docker network rm $name
#    printf "\nWaiting for docker network '$name' to go down."
#    wait_for_empty_results "docker network ls | grep -w $name"
    printf "\nDone!\n"
}

function assert_stack_is_not_deployed() {
    exists=$(docker stack ls | grep "$name")
    if [ ! -z "$exists" ]; then
        printf "[ERROR] '$name' stack already exists. Please run script with 'clean', 'redeploy', or 'help' for more info.\n"
        exit 1
    fi
}

# Waits for everything to start up
function wait_for_containers () {
    printf "\nWaiting for docker services to start (safe to exit)."
    wait_for_empty_results "docker service ls | grep $name_.*0/[1-9]"
    printf "\nDone!\n"
}

# Deploy docker-compose.yml file via docker stacks
function deploy_images () {
    printf "\nDeploying Docker Images...\n"
    docker stack deploy -c docker-compose.yml $name
}

# Check is network already does not exist or is not attachable.
function configure_network () {
    printf "Checking Docker Network...\n"
    network=`docker network ls | grep "$name"`
    networktype=$(docker network inspect $name 2>/dev/null | grep -i "\"driver\": \"overlay\"")
    attachable=$(docker network inspect $name 2>/dev/null | grep -i "\"attachable\": true")
    if [ -z "$network" ]; then
        printf "Network '$name' does not exist, creating new network... \n"
        create_new_network
    elif [ -z "$networktype" ] || [ -z "$attachable" ]; then
        printf  "ERROR: The docker network '$name' exists, but is not an attachable overlay network. Please run 'clean' or 'redeploy' to remove or update this network.\n"
        exit 1
    else
        printf "Network '$name' already exists and is attachable. \n"
    fi
}

function print_warning () {
    printf "\
====== WARNING: TEST DEPLOYMENT ======\n\
This docker deployment will create a local minio server with default secret and key. Make sure this isn't being deployed into production.\n\n"
}

# start of script
if [ -z $1 ]; then
    print_warning
    deploy_images
    wait_for_containers
    ./access-token.sh
else
   case $1 in
    deploy | d)
        print_warning
        assert_stack_is_not_deployed
        deploy_images
        wait_for_containers
        ./access-token.sh
        ;;
    clean | c)
        cleanup
        ;;
    update | u)
        deploy_images
        wait_for_containers
        ./access-token.sh
        ;;
    redeploy | r)
        print_warning
        cleanup
        deploy_images
        wait_for_containers
        ./access-token.sh
        ;;
    *)
        printf "$helptext"
        ;;
    esac
fi