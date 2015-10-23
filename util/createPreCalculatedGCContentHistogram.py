import sys
import argparse
from Bio import SeqIO
from Bio.Seq import Seq
import Bio.SeqUtils as SeqUtils
import HTSeq
import numpy as np


def idsContainGiven(givenId, transcriptIds):
    for tId in transcriptIds:
        if givenId.find(tId) != -1:
            return True

    return False

if __name__ == "__main__":
    
    descriptionText = "The script creates a pre-calculated GC-content histogram from FASTA reference dataset. The pre-calucated GC-content histogram is applied in a BAM QC plot \"GC content distribution\" when comparison with a known GC-distribution is activated."

    parser = argparse.ArgumentParser(description = descriptionText,formatter_class=argparse.RawDescriptionHelpFormatter)
    
    parser.add_argument("-i", action="store", required="true", dest="fastaFile",
        help="Input genome sequence. ")
    parser.add_argument("-o", action="store", dest="outFile", default="GC_content.txt",
        help="Output file. Default is annotations.txt")

    #parser.add_argument("--ignore-strange-chrom", action="store_true", default=False,
    #    dest="ignoreStrangeChromosomes", help="All chromosomes except numbered and X,Y,MT are ignored ")
    
    args = parser.parse_args()
    
    print args
    
    fastaFileName = args.fastaFile
    outFileName = args.outFile
    windowSize = 1001

    seqData = SeqIO.parse(fastaFileName, "fasta")
    
   

    numSegments = 0
    window = []

    for i in range(windowSize):
        window.append(0)

    for chromosome in seqData:
        
        k = 0
        gcSize = 0
        seq = ""
        for c in chromosome.seq.upper():
            if c == "N":
                continue
            if c == "G" or c == "C":
                gcSize +=1
            k += 1

            if k == windowSize - 1:
                #print SeqUtils.GC(seq)
                #print (gcSize)
                window[gcSize] += 1
                seq = ""
                k = 0
                gcSize = 0
                numSegments += 1
                #break
            
            #seq += c
            
        print "Chromosome %s is analyzed" % chromosome.name
                    
    print "Analysis is finished, normalizing and reporting resutls..."
    #print window

    normalizedWindow = []

    for item in window:
        normalizedWindow.append( float(item)/numSegments )
    
    outFile = open(outFileName, "w")

    header = "#GC_CONTENT_HISTOGRAM WINDOW_SIZE=%d NUM_SEGMENTS=%d\n" % (windowSize, numSegments)
    outFile.write(header)
 
    for i in range(len(normalizedWindow)):
        outFile.write("%d : %.6f\n" % (i, normalizedWindow[i]) )


    outFile.close()

    print "Done."

  
