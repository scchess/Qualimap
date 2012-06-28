package org.bioinfo.ngs.qc.qualimap.main;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import org.apache.commons.cli.ParseException;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;

import java.io.File;

/**
 * Created by kokonech
 * Date: 3/1/12
 * Time: 2:35 PM
 */
public class GCContentTool extends NgsSmartTool  {

    GCContentTool() {
        super(Constants.TOOL_NAME_GC_CONTENT);
    }

    @Override
    protected void initOptions() {
        options.addOption( requiredOption("i", true, "mapping file (bam format)"));
    }

    @Override
    protected void checkOptions() throws ParseException {

    }

    @Override
    protected void execute() throws Exception {


        String pathToBamFile = commandLine.getOptionValue("i");

        SAMFileReader reader = new SAMFileReader(new File(pathToBamFile));

        SAMRecordIterator iter = reader.iterator();

        long numBases = 0;
        long numGC = 0;


        while(iter.hasNext()){

            SAMRecord read = null;

            try {
                read = iter.next();
            } catch (RuntimeException e) {
                logger.warn( e.getMessage() );
            }

            if (read == null) {
                continue;
            }


            byte[] readBases = read.getReadBases();

            for (byte base : readBases) {
                switch (base) {
                    case 'A':
                        numBases++;
                        break;
                    case 'C':
                        numBases++;
                        numGC++;
                        break;
                    case 'G':
                        numBases++;
                        numGC++;
                        break;
                    case 'T':
                        numBases++;
                        break;
                    case 'N':
                        numBases++;
                        break;
                }
            }

        }

        double gcContent = (double) numGC / (double) numBases;

        System.out.println("GC content is " + gcContent);



    }
}
