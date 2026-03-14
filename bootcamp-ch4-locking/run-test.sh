#!/bin/bash

db_url="jdbc:postgresql://localhost:26257/bootcamp?sslmode=disable&allow_unsafe_internals=true"
db_user=root
db_password=cockroach

spring_profile="default,verbose"

####################################
# Do not edit past this line
####################################

case "$OSTYPE" in
  darwin*)
    rootdir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
    ;;
  *)
    rootdir="$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"
    ;;
esac

source ${rootdir}/../run.sh

fn_run_test
