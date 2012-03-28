package ru.concerteza.util.buildnumber;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: plevart
 * Date: 3/28/12
 */
public class BuildTimestampFactory {
    public static final String DEFAULT_BUILD_TIMESTAMP_FORMAT = "yyyyMMdd'T'HHmmss";

    public static String createBuildTimestamp(String buildTimestampFormat) {
        return new SimpleDateFormat(buildTimestampFormat)
            .format(new Date());
    }
}
