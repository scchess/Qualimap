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
        System.out.println(m.get(1,1));
        assertTrue(m.get(1,1) == 4);

    }

    @Test
    public void testPCA() {
        double[] s1 = {1,2,3};
        double[] s2 = {1,2,3};
        PrincipleComponentAnalysis pca = new PrincipleComponentAnalysis();
        pca.addSample(s1);
        pca.addSample(s2);
        pca.computeBasis(2);
        double[] vec = pca.getBasisVector(1);
        System.out.println(vec[0]);


    }

}
