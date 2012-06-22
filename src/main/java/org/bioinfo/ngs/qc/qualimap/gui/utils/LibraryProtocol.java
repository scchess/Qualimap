package org.bioinfo.ngs.qc.qualimap.gui.utils;

import java.util.Arrays;

/**
 * Created by kokonech
 * Date: 6/15/12
 * Time: 5:17 PM
 */
public enum LibraryProtocol {

    STRAND_NON_SPECIFIC, STRAND_SPECIFIC_FORWARD, STRAND_SPECIFIC_REVERSE;

    public static final String PROTOCOL_NON_STRAND_SPECIFIC = "non-strand-specific";
    public static final String PROTOCOL_FORWARD_STRAND = "strand-specific-forward";
    public static final String PROTOCOL_REVERSE_STRAND = "strand-specific-reverse";
    public static final String PROTOCOL_UNKNOWN = "unknown";


    public String toString() {
        if (this == STRAND_NON_SPECIFIC) {
            return PROTOCOL_FORWARD_STRAND;
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
            return STRAND_NON_SPECIFIC;
        } else if (protocolName.equals(PROTOCOL_REVERSE_STRAND)) {
            return STRAND_SPECIFIC_REVERSE;
        }

        throw new RuntimeException("Unknown library protocol name: " + protocolName + "\n" +
                "Supported protocols: " + Arrays.toString(getProtocolNames()) );
    }

    public static String[] getProtocolNames() {
        return new String[] {PROTOCOL_NON_STRAND_SPECIFIC, PROTOCOL_FORWARD_STRAND, PROTOCOL_REVERSE_STRAND };
    }

}
