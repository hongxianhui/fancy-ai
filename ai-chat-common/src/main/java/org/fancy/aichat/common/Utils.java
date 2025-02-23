package org.fancy.aichat.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Utils {
    private static final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(10);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void submit(Runnable runnable) {
        threadPool.submit(runnable);
    }

    public static void schedule(Runnable runnable, long delay) {
        threadPool.schedule(runnable, delay, TimeUnit.SECONDS);
    }

    public static void scheduleAtFixedRate(Runnable command, long initialDelay, long period) {
        threadPool.scheduleAtFixedRate(command, initialDelay, period, TimeUnit.SECONDS);
    }

    public static String serialize(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }

    public static <T> T deserialize(String json, Class<T> clazz) throws JsonProcessingException {
        return mapper.readValue(json, clazz);
    }
}
