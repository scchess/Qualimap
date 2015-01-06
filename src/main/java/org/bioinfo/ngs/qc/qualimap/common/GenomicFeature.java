/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2015 Garcia-Alcalde et al.
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
package org.bioinfo.ngs.qc.qualimap.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.sf.picard.util.Interval;

/**
 * Created by kokonech
 * Date: 6/8/12
 * Time: 6:00 PM
 */
public class GenomicFeature {

    Interval interval;
    Map<String,String> attributes;

    public GenomicFeature(String seqName, int start, int end, boolean isNegative, String featureName ) {
        interval = new Interval(seqName, start, end, isNegative, featureName);
        attributes = new HashMap<String, String>();
    }

    public int getStart() {
        return interval.getStart();
    }

    public int getEnd() {
        return interval.getEnd();
    }

    public String getSequenceName() {
        return interval.getSequence();
    }

    public boolean isPositiveStrand() {
        return interval.isPositiveStrand();
    }


    public void addAttribute(String name, String value) {
        attributes.put(name, value);
    }

    public String getFeatureName() {
        return interval.getName();
    }

    public String getAttribute(String attrName) {
        return attributes.get(attrName);
    }

    public Collection<String> getAttributeNames() {
        return attributes.keySet();
    }
}
