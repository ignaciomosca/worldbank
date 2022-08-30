#!/bin/bash

LONG=dataload,results,help
OPTS=$(getopt -a -n worldbank --longoptions $LONG - "$@")

VALID_ARGUMENTS=$#

if [ "$VALID_ARGUMENTS" -eq 0 ]; then
  help
fi

help()
{
    echo "Usage: run [ --dataload ]
               [ --results ]
               [ --help  ]"
    exit 2
}

dataingestion()
{
     java -jar ./target/scala-2.13/worldbank.jar resetdatabase &
     sleep 5
     curl http://localhost:8080/ingestion
     exit 0
}

dataresults()
{
     java -jar ./target/scala-2.13/worldbank.jar &
     sleep 5
     echo "Year to Year Population Growth and Year to Year GDP Growth"
     curl http://localhost:8080/query
     exit 0
}

if [ "$VALID_ARGUMENTS" -eq 0 ]; then
  help
fi

eval set -- "$OPTS"

while :
do
  case "$1" in
    --dataload )
      echo "Ingesting Data"
      dataingestion
      ;;
    --results )
      echo "Processing Results"
      dataresults
      ;;
    --help)
      help
      ;;
    --)
      shift;
      break
      ;;
    *)
      echo "Unexpected option: $1"
      help
      ;;
  esac
done
