package org.jboss.set.model.json;

import org.jboss.logging.Logger;
import org.jboss.set.exception.UnknownStreamException;

public enum Stream {
    EAP_7_3_X("EAP 7.3.x", "eap:7.3.x", "(7.3.z)", "12343254"),
    EAP_7_4_X("EAP 7.4.x", "eap:7.4.x", "(7.4.z)", "12354234"),
    EAP_8_0_X("EAP 8.0.x", "eap:8.0.x", "(8.0.z)", "12402757"),
    XP_4_0_X("XP 4.0.x", "eap-xp:4.0.x", "(xp-4.0.z)", "12382923"),
    XP_5_0_X("XP 5.0.x", "eap-xp:5.0.x", "(xp-5.0.z)", "12401140");

    static final Logger logger = Logger.getLogger(Stream.class);

    public final String frontEnd;
    public final String backEnd;
    public final String jiraPrefix;
    public final String targetRelease;

    Stream(String frontEnd, String backEnd, String jiraPrefix, String targetRelease) {
        this.frontEnd = frontEnd;
        this.backEnd = backEnd;
        this.jiraPrefix = jiraPrefix;
        this.targetRelease = targetRelease;
    }

    public static String getJsonStreamName(String streamFromFrontend) {
        for (Stream stream : Stream.values()) {
            if (stream.frontEnd.equals(streamFromFrontend) || stream.backEnd.equals(streamFromFrontend)) {
                return stream.backEnd;
            }
        }
        logger.errorf("Unknown stream: '%s'.", streamFromFrontend);
        throw new UnknownStreamException("Unknown stream: '" + streamFromFrontend + "'.");
    }

    public static String[] getJiraParametersByBackEnd(String backEnd) {
        for (Stream stream : Stream.values()) {
            if (stream.frontEnd.equals(backEnd) || stream.backEnd.equals(backEnd)) {
                return new String[] { stream.jiraPrefix, stream.targetRelease};
            }
        }
        logger.errorf("Unknown stream: '%s'.", backEnd);
        throw new UnknownStreamException("Unknown stream: '" + backEnd + "'.");
    }
}
