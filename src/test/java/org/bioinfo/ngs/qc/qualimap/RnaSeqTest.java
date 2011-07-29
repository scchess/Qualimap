package org.bioinfo.ngs.qc.qualimap;

import java.io.File;

import org.bioinfo.ngs.qc.qualimap.main.NgsSmartMain;
import org.junit.Test;


public class RnaSeqTest {
	@Test
    public void testBamQC(){
		String outdir = "/home/lcruz/Escritorio/bamQC/";
        new File(outdir).mkdirs(); 
		
    	String []args = {
    			"rna-seq"
    			/*"bamqc",
    			"--gui",*/
/*    			"-o", outdir,
    			"--d1", "/home/lcruz/Escritorio/ficherosQualimap/counts.0.txt",
    			"--n1", "Samples 1",
    			"-i", "/home/lcruz/Escritorio/ficherosQualimap/human.61.genes.biotypes.txt"*/
                /*"--d2", "/home/lcruz/Escritorio/ficherosQualimap/counts.24.txt",*/
    			/*"--n2", "Samples 2",*/
                /*"-home", "/home/lcruz/workspace/qualimap/target/install"*/
    			};
        try {
			NgsSmartMain.main(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
