package com.rayshan.locations.common;

import java.time.format.DateTimeFormatter;

public interface Constants {
    DateTimeFormatter COMMON_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    DateTimeFormatter DATE_FORMATTER_YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
}
