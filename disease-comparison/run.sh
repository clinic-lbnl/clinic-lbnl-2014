#!/bin/bash
bash clean.sh
mvn exec:java -Dexec.mainClass=disease_comparison.GraphTesting
