package org.bioinfo.ngs.qc.qualimap.test;

import org.bioinfo.ngs.qc.qualimap.beans.PrincipleComponentAnalysis;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by kokonech
 * Date: 6/12/14
 * Time: 10:44 AM
 */
public class TestEjml {

    @Test
    public void simpleTest() {
        double [] data = {1,2.2,3.3,4};
        SimpleMatrix m = new SimpleMatrix(2,2,true, data);
        assertTrue(m.get(1,1) == 4);

    }

    @Test
    public void testPCA() {

        double[] x = {0.69, -1.31,  0.39, 0.09,  1.29,  0.49,  0.19, -0.81, -0.31, -0.71 };
        double[] y = {0.49, -1.21,  0.99, 0.29,  1.09,  0.79, -0.31, -0.81, -0.31, -1.01 };

        PrincipleComponentAnalysis pca = new PrincipleComponentAnalysis();
        pca.setup(10, 2);

        for (int i = 0; i < 10; ++i) {
            double[] sample = {x[i],y[i]};
            pca.addSample( sample );
        }

        pca.computeBasis(2);
        double[] pc1 = pca.getBasisVector(0);
        assertTrue(pc1.length == 2);
        System.out.println(pc1[0]);
        System.out.println(pc1[1]);


        /*for (int i = 0; i < 10; ++i) {
            double[] sample = {x[i],y[i]};
            double[] cS = pca.sampleToEigenSpace(sample);
            System.out.println("(" + cS[0] + " " + cS[1] + ")");
        } */

        double[] s1 = {x[0],y[0]};
        double[] c1 = pca.sampleToEigenSpace(s1);

        // Expecting (-0.8279702,  0.1751153)
        double c1Len = c1[0]*c1[0] + c1[1]*c1[1];
        assertTrue( c1Len  - 0.7162 < 0.00001);





    }

}
