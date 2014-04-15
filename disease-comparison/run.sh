#!/bin/bash
bash clean.sh
mvn exec:java -Dexec.mainClass=disease_comparison.DiseaseComparisonDriver -Dexec.args="../../test-files/go-labels.txt ../../test-files/go-edges.txt ../../test-files/labels.txt ../../test-files/goa_outfile_FB-2k.txt"
