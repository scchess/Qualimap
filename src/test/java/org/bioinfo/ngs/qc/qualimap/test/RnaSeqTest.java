package org.bioinfo.ngs.qc.qualimap.test;

import org.bioinfo.ngs.qc.qualimap.main.NgsSmartMain;
import org.junit.Test;


public class RnaSeqTest {

    @Test
    public void testBamQC(){

    	String []args = {
    			"counts"
    			};
        try {
			NgsSmartMain.main(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
