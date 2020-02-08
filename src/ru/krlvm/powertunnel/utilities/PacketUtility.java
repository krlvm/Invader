package ru.krlvm.powertunnel.utilities;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

/**
 * Experimental injection framework
 * Doesn't work properly and should not be used yet
 */
public class PacketUtility {

    public static void editContent(FullHttpResponse response, String content) {
        try {
            Field contentField;
            if (response.getClass().getSimpleName().equals("DefaultHttpContent") || response.getClass().getSimpleName().equals("DefaultFullHttpResponse")) {
                contentField = response.getClass().getDeclaredField("content");
            } else {
                contentField = response.getClass().getSuperclass().getDeclaredField("content");
            }
            boolean accessibility = contentField.isAccessible();
            contentField.setAccessible(true);
            contentField.set(response, Unpooled.copiedBuffer(content.getBytes()));
            recalculateContentLength(response);
            contentField.setAccessible(accessibility);
        } catch (Exception ex) {
            Utility.print("[x] Edition failed: " + ex.getMessage());
            Debugger.debug(ex);
        }
    }

    public static void recalculateContentLength(FullHttpResponse response) {
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().toString(StandardCharsets.UTF_8).length());
    }
}
