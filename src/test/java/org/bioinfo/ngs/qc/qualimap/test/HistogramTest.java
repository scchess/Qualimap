package org.bioinfo.ngs.qc.qualimap.test;

import org.bioinfo.ngs.qc.qualimap.beans.GenericHistogram;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by kokonech
 * Date: 10/5/12
 * Time: 11:33 AM
 */
public class HistogramTest {



 @Test
 public void testHistogram(){

     GenericHistogram hist = new GenericHistogram(2,true);

          int[] d1 = {1,1,1,1,1};
          int[] d2 = {1,1,1,1};

          hist.updateHistogram(d1);
          hist.updateHistogram(d2);

     double[] histData = hist.getHist();

     assertTrue(histData[0] == 1.1);
     assertTrue(histData[1] == 0.9);




 }


}