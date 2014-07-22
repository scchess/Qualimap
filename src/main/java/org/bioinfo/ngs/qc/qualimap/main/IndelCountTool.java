/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2014 Garcia-Alcalde et al.
 * http://qualimap.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package org.bioinfo.ngs.qc.qualimap.main;

import net.sf.picard.PicardException;
import net.sf.picard.reference.IndexedFastaSequenceFile;
import net.sf.samtools.*;
import org.apache.commons.cli.ParseException;
import org.bioinfo.ngs.qc.qualimap.common.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by kokonech
 * Date: 6/26/12
 * Time: 3:52 PM
 */
public class IndelCountTool extends NgsSmartTool{


    int numDeletions, numInsertions;
    byte prevBase;
    boolean reportState;
    int homopolymerSize;
    boolean prevBaseInsideIndelRegion, homopolymerStartsInsideIndelRegion;
    String curReadName, curCigar;
    StringBuffer curSequence;
    char[] extendedCigarVector;
    int[] homopolymerIndels;
    private boolean saveHomopolymer;
    final static int MIN_HOMOPOLYMER_SIZE = 5;

    ArrayList<Region> intersectionRegions;


    class Region {
        int start;
        int length;
    }


    IndelCountTool() {
        super(Constants.TOOL_NAME_INDEL_COUNT, false);
        homopolymerIndels = new int[5];
    }

    @Override
    protected void initOptions() {
        options.addOption(requiredOption("i", true, "Mapping file in BAM format."));
        options.addOption(requiredOption("r", true, "Reference sequence in FASTA format."));
    }

    @Override
    protected void checkOptions() throws ParseException {

    }

    @Override
    protected void execute() throws Exception {

        // Init BAM file
        String pathToBamFile = commandLine.getOptionValue("i");
        SAMFileReader reader = new SAMFileReader(new File(pathToBamFile));

        // Init reference
        String  pathToRef = commandLine.getOptionValue("r");
        IndexedFastaSequenceFile refIndex = new IndexedFastaSequenceFile(new File(pathToRef));

        int numberOfProblematicReads = 0;
        int numReads = 0;
        SAMRecordIterator iter = reader.iterator();

        while (iter.hasNext()) {
            SAMRecord record = null;

            try {
                record = iter.next();
            } catch (RuntimeException e) {
                numberOfProblematicReads++;
            }

            if (record == null) {
                continue;
            }
            curReadName = record.getReadName();
            curSequence = new StringBuffer(record.getReadString());
            curCigar = record.getCigarString();

            processRecord(record, refIndex);

            if (++numReads % 100000 == 0) {
                System.err.println("Analyzed " + numReads + " records...");
            }
        }

        System.err.println("Analyzed " + numReads + " records");
        System.err.println("Number of problematic records: " + numberOfProblematicReads);

        System.out.println("polyA indels: " + homopolymerIndels[0]);
        System.out.println("polyC indels: " + homopolymerIndels[1]);
        System.out.println("polyG indels: " + homopolymerIndels[2]);
        System.out.println("polyT indels: " + homopolymerIndels[3]);
        System.out.println("polyN indels: " + homopolymerIndels[4]);
        int numHomopolymerIndels = 0;
        for (int val : homopolymerIndels) {
            numHomopolymerIndels += val;
        }
        int numIndels =  numDeletions + numInsertions;
        System.out.println("All indels: " + numIndels);
        System.out.println("% homopolymer indels: " +  100.0*( numHomopolymerIndels / (double) numIndels ) );


    }

    void processRecord(SAMRecord record, IndexedFastaSequenceFile refIndex) {
        // precompute total size of alignment
        int totalSize = 0;
        List<CigarElement> elementList = record.getCigar().getCigarElements();

        byte[] refBytes = null;

        if (curReadName.equals("IL30_5428:3:21:3146:1335#4")) {
            System.out.println("DEBUG!");
        }

        for(CigarElement element : elementList){
            if (element.getOperator() == CigarOperator.I) {
                numInsertions++;
            } else if (element.getOperator() == CigarOperator.D) {
                numDeletions++;
                //char[] padding = new char[element.getLength()];
                //Arrays.fill(padding, '-' );
                //curSequence.insert( totalSize, padding );
                try {
                    refBytes = refIndex.getSubsequenceAt(record.getReferenceName(),
                        record.getAlignmentStart(), record.getAlignmentEnd()).getBases();
                } catch (PicardException e) {
                    System.err.println("Bad request to reference index: (" + record.getReferenceName() +
                            ", " + record.getAlignmentStart() +
                            ", " + record.getAlignmentEnd() + ")" );
                    return;
                }
            }

            totalSize += element.getLength();

        }

        // compute extended cigar
        extendedCigarVector = new char[totalSize];
        int mpos = 0;
        int npos;
        for(CigarElement element : elementList){
            npos = mpos + element.getLength();
            Arrays.fill(extendedCigarVector, mpos, npos, element.getOperator().name().charAt(0));
            mpos = npos;
        }

        // init extended cigar portion
        // char[] extendedCigarVector = extended; // Arrays.copyOfRange(extended,0,mpos);

        int readPos = 0;
        int alignmentPos = 0;
        byte[] readBases = record.getReadBases();

        resetCounters();

        for ( int i = 0; i < extendedCigarVector.length; ++i ) {
            char cigarChar = extendedCigarVector[i];
            // M
            if (cigarChar == 'M') {
                byte base = readBases[readPos];
                readPos++;
                alignmentPos++;
                collectBase(base, false);
            }
            // I
            else if (cigarChar == 'I') {
                byte base = readBases[readPos];
                collectBase(base, true);
                readPos++;
            }
            // D
            else if (cigarChar == 'D') {
                byte base = refBytes[alignmentPos];
                //curSequence.insert(i, (char) base);
                collectBase(base, true);
                alignmentPos++;

            }
            // N
            else if (cigarChar == 'N') {
                alignmentPos++;
            }
            // S
            else if (cigarChar == 'S') {
                readPos++;
            }
            // H
            else if (cigarChar == 'H') {

            }
            // P
            else if (cigarChar == 'P') {
                alignmentPos++;
            }
        }

        debugOutput();
    }

    void resetCounters() {
        prevBase = 'X';
        homopolymerSize = 0;
        saveHomopolymer = false;
    }

    void collectBase(byte base, boolean  insideIndelRegion) {
        if (base == prevBase) {
            homopolymerSize ++;
        } else {
            if (  prevBaseInsideIndelRegion || homopolymerStartsInsideIndelRegion ) {
                saveHomopolymerData();
            }
            homopolymerSize = 1;
            homopolymerStartsInsideIndelRegion = insideIndelRegion;
        }

        /*if (!insideIndelRegion && homopolymerSize == 1) {
            saveHomopolymer = false;
        }*/

        prevBase = base;
        prevBaseInsideIndelRegion = insideIndelRegion;
    }


    static String charArrayToString(char[] array) {
        String res = "";
        for (char c : array) {
            res += c;
        }
        return res;

    }

    private void saveHomopolymerData() {

        if (homopolymerSize >= MIN_HOMOPOLYMER_SIZE) {
            if (prevBase == 'A') {
                homopolymerIndels[0]++;

            } else if (prevBase == 'C') {
                //reportState = true;
                homopolymerIndels[1]++;
            } else if (prevBase == 'G') {
                homopolymerIndels[2]++;
            } else if (prevBase == 'T') {
                homopolymerIndels[3]++;
            } else if (prevBase == 'N') {
                homopolymerIndels[4]++;
            }
            saveHomopolymer = false;
        }

    }


    private void debugOutput() {
        if (reportState) {
        System.out.println("Homopolymer indel in " + curReadName + ", " + " sequence is \n"
                                           + curSequence + "\n" +
                                           charArrayToString(extendedCigarVector) + "\n"
                                           + curCigar + "\n");
        reportState = false;
        }
    }



}
