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
package org.bioinfo.ngs.qc.qualimap.common;

import java.util.Arrays;

/**
 * Created by kokonech
 * Date: 6/15/12
 * Time: 5:17 PM
 */
public enum LibraryProtocol {

    NON_STRAND_SPECIFIC, STRAND_SPECIFIC_FORWARD, STRAND_SPECIFIC_REVERSE;

    public static final String PROTOCOL_NON_STRAND_SPECIFIC = "non-strand-specific";
    public static final String PROTOCOL_FORWARD_STRAND = "strand-specific-forward";
    public static final String PROTOCOL_REVERSE_STRAND = "strand-specific-reverse";
    public static final String PROTOCOL_UNKNOWN = "unknown";

    public String toString() {
        if (this == NON_STRAND_SPECIFIC) {
            return PROTOCOL_NON_STRAND_SPECIFIC;
        } else if (this == STRAND_SPECIFIC_FORWARD) {
            return PROTOCOL_FORWARD_STRAND;
        } else if (this == STRAND_SPECIFIC_REVERSE) {
            return PROTOCOL_REVERSE_STRAND;
        } else {
            return PROTOCOL_UNKNOWN;
        }

    }

    public static LibraryProtocol getProtocolByName(String protocolName) {
        if (protocolName.equals(PROTOCOL_FORWARD_STRAND)) {
            return STRAND_SPECIFIC_FORWARD;
        } else if (protocolName.equals(PROTOCOL_NON_STRAND_SPECIFIC)) {
            return NON_STRAND_SPECIFIC;
        } else if (protocolName.equals(PROTOCOL_REVERSE_STRAND)) {
            return STRAND_SPECIFIC_REVERSE;
        }

        throw new RuntimeException("Unknown library protocol name: " + protocolName + "\n" +
                "Supported protocols: " + Arrays.toString(getProtocolNames()) );
    }

    public static String[] getProtocolNames() {
        return new String[] {PROTOCOL_NON_STRAND_SPECIFIC, PROTOCOL_FORWARD_STRAND, PROTOCOL_REVERSE_STRAND };
    }

    public static String getProtocolNamesString() {
        return  LibraryProtocol.PROTOCOL_FORWARD_STRAND + ", "
                + LibraryProtocol.PROTOCOL_REVERSE_STRAND + " or "
                + LibraryProtocol.PROTOCOL_NON_STRAND_SPECIFIC;
    }


}
