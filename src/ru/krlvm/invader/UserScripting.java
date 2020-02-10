package ru.krlvm.invader;

import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ru.krlvm.powertunnel.utilities.HttpUtility;
import ru.krlvm.powertunnel.utilities.Utility;

import javax.script.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User scripting handler
 *
 * User hooks written in JavaScript allows
 * to capture, modify and reject packets
 * More info in the GitHub repository
 */
public class UserScripting {

    private static final ScriptStore hook = new ScriptStore("hook");
    private static final String GENERAL_API = "";

    private static Invocable INVOKER = null;
    private static boolean requestHandler = false;
    private static boolean responseHandler = false;

    public static void load() throws IOException, ScriptException {
        if(!hook.getFile().exists()) {
            Utility.print();
            Utility.print("[#] JavaScript Proxy Hook not found");
            Utility.print("[#] Check the GitHub repository to learn more");
            Utility.print();
            return;
        }
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
        INVOKER = ((Invocable) engine);

        hook.load();
        String inline = hook.inline();
        requestHandler = inline.contains("function onRequest");
        responseHandler = inline.contains("function onResponse");
        engine.eval(inline + GENERAL_API);
        Utility.print("[#] JavaScript Proxy Hook loaded");
    }

    /**
     * JavaScript code sample
     *
     * //If we need to modify headers
     * function onRequest(headers) {
     *     //modifying headers here
     *     return [ headers ];
     * }
     *
     * //If we need to reject request
     * function onRequest(headers) {
     *     return [ [], 'Rejected', 200 ];
     * }
     */
    public static HttpResponse onRequest(HttpRequest request) {
        if(INVOKER == null || !requestHandler) {
            return null;
        }
        Map<String, String> headers = getHeaders(request);
        try {
            List<Object> filtered = ((List<Object>) convertIntoJavaObject(INVOKER.invokeFunction("onRequest",
                    headers)));
            if(filtered.size() == 1) {
                request.headers().clear();
                for (Map.Entry<String, String> header : ((Map<String, String>) filtered.get(0)).entrySet()) {
                    request.headers().add(header.getKey(), header.getValue());
                }
                return null;
            } else {
                return HttpUtility.getResponse(((String) filtered.get(1)), ((int) filtered.get(2)), (Map<String, String>) filtered.get(0));
            }
        } catch (Exception ex) {
            Utility.print(" [x] Failed to pass request to hook: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * JavaScript code sample
     *
     * //If we need to modify headers or data
     * function onResponse(headers, host, data, status) {
     *     //modifying headers/data here
     *     return [ headers, data ];
     * }
     *
     * //If we need to reject response
     * function onResponse(headers, host, data, status) {
     *     return [ [], 'Rejected', 200 ];
     * }
     */
    public static HttpResponse onResponse(String host, String content, HttpResponse response) {
        if(INVOKER == null || !responseHandler) {
            System.out.println("\nNO Response\n");
            return null;
        }
        System.out.println("\nResponse\n");
        try {
            Map<String, String> headers = getHeaders(response);
            List<Object> filtered = ((List<Object>) convertIntoJavaObject(INVOKER.invokeFunction("onResponse",
                    headers, host, content, response.getStatus().code())));
            if(filtered.size() == 2) {
                response.headers().clear();
                for (Map.Entry<String, String> header : ((Map<String, String>) filtered.get(0)).entrySet()) {
                    response.headers().add(header.getKey(), header.getValue());
                }
                //response data = data
                return null;
            } else {
                return HttpUtility.getResponse(((String) filtered.get(1)), ((int) filtered.get(2)), (Map<String, String>) filtered.get(0));
            }
        } catch (Exception ex) {
            Utility.print(" [x] Failed to pass response to hook: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    private static Object convertIntoJavaObject(Object scriptObj) {
        if (scriptObj instanceof ScriptObjectMirror) {
            ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) scriptObj;
            if (scriptObjectMirror.isArray()) {
                List<Object> list = new ArrayList<>();
                for (Map.Entry<String, Object> entry : scriptObjectMirror.entrySet()) {
                    list.add(convertIntoJavaObject(entry.getValue()));
                }
                return list;
            } else {
                Map<String, Object> map = new HashMap<>();
                for (Map.Entry<String, Object> entry : scriptObjectMirror.entrySet()) {
                    map.put(entry.getKey(), convertIntoJavaObject(entry.getValue()));
                }
                return map;
            }
        } else {
            return scriptObj;
        }
    }

    private static Map<String, String> getHeaders(HttpMessage message) {
        Map<String, String> headers = new HashMap<>();
        for (Map.Entry<String, String> header : message.headers().entries()) {
            headers.put(header.getKey(), header.getValue());
        }
        return headers;
    }
}
