package com.frame.tool.uuid;

import java.util.UUID;

public class IdUtil {

    public static String simpleId() {
        return commonId().replace("-", "");
    }

    public static String commonId() {
        return UUID.randomUUID().toString();
    }

}
