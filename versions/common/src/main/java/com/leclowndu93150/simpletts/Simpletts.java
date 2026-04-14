package com.leclowndu93150.simpletts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Simpletts {
    public static final String MOD_ID = "simpletts";
    public static final String MOD_NAME = "Simple Text to Speech";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static void init() {
        LOGGER.info("{} initializing!", MOD_NAME);
    }
}
