#!/bin/bash
bash clean.sh
mvn exec:java -Dexec.mainClass=disease_comparison.DiseaseComparisonDriver -Dexec.args="../../test-files/mouse-class-labels.txt ../../test-files/mouse-class-to-class.txt ../../test-files/mouse-individual-labels.txt ../../test-files/mouse-individual-to-class.txt"
