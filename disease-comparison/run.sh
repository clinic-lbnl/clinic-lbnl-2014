#!/bin/bash
bash clean.sh
mvn exec:java -Dexec.mainClass=disease_comparison.DiseaseComparisonDriver -Dexec.args="../../test-files/small-class-labels.txt ../../test-files/small-class-to-class.txt ../../test-files/small-individual-labels.txt ../../test-files/small-individual-to-class.txt"
