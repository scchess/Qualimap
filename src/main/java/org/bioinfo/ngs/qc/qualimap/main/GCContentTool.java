/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2016 Garcia-Alcalde et al.
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

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import org.apache.commons.cli.ParseException;
import org.bioinfo.ngs.qc.qualimap.common.Constants;

import java.io.File;

/**
 * Created by kokonech
 * Date: 3/1/12
 * Time: 2:35 PM
 */
public class GCContentTool extends NgsSmartTool  {

    GCContentTool() {
        super(Constants.TOOL_NAME_GC_CONTENT, false);
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
