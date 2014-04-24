#!/bin/bash
bash clean.sh
mvn exec:java -Dexec.mainClass=disease_comparison.DiseaseComparisonDriver -Dexec.args="../../test-files/simple-class-labels.txt ../../test-files/simple-class-to-class.txt ../../test-files/simple-individual-labels.txt ../../test-files/simple-individual-to-class.txt"
