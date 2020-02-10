package ru.krlvm.powertunnel.filter;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.littleshoot.proxy.HttpFiltersAdapter;
import ru.krlvm.invader.ComplexProxyToClientResponse;
import ru.krlvm.invader.Invader;
import ru.krlvm.invader.SnifferRecord;
import ru.krlvm.invader.UserScripting;
import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.utilities.Debugger;
import ru.krlvm.powertunnel.utilities.HttpUtility;
import ru.krlvm.powertunnel.utilities.Utility;
import ru.krlvm.powertunnel.webui.PowerTunnelMonitor;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

/**
 * Implementation of LittleProxy filter
 *
 * @author krlvm
 */
public class ProxyFilter extends HttpFiltersAdapter {

    public ProxyFilter(HttpRequest originalRequest, ChannelHandlerContext ctx) {
        super(originalRequest, ctx);
    }

    public ProxyFilter(HttpRequest originalRequest) {
        super(originalRequest);
    }

    /**
     * Filtering client to proxy request
     */
    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        if (httpObject instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) httpObject;
            if(PowerTunnel.isSnifferEnabled() && PowerTunnelMonitor.checkUri(request.getUri())) {
                Utility.print("[i] Accepted Web UI connection");
                return PowerTunnelMonitor.getResponse(request.getUri());
            }
            String host = HttpUtility.formatHost(request.headers().get("Host"));

            PowerTunnel.addToJournal(host);
            Utility.print("[i] %s / %s", request.getMethod(), host);

            HttpResponse hook = UserScripting.onRequest(request);
            if(hook != null) {
                Utility.print(" [!] Request rejected by user hook: " + host);
                return hook;
            }

            if(!PowerTunnel.isUserWhitelisted(host) && PowerTunnel.isUserBlacklisted(host)) {
                Utility.print(" [!] Access denied by user: " + host);
                return HttpUtility.getStub("This website is blocked by user");
            }
        }

        return null;
    }

    @Override
    public HttpObject proxyToClientResponse(ComplexProxyToClientResponse complexResponse) {
        HttpObject httpObject = complexResponse.getHttpObject();
        if(httpObject instanceof FullHttpResponse) {
            FullHttpResponse response = ((FullHttpResponse) httpObject);
            String content = response.content().toString(StandardCharsets.UTF_8);
            String host = complexResponse.getServerHostAndPort() == null ?
                    "INVADER_UNKNOWN" : complexResponse.getServerHostAndPort();
            if(PowerTunnel.isSnifferEnabled()) {
                Invader.SNIFFER_RECORDS.add(new SnifferRecord(host, response.headers(), content));
            }
            HttpResponse hook = UserScripting.onResponse(host, content, response);
            if(hook != null) {
                Utility.print("[!] Response rejected by user hook: " + host);
                return hook;
            }
            if(content.endsWith("</html>") || content.endsWith("</HTML>")) {
                String injection = Invader.getInjection(complexResponse.getServerHostAndPort());
                if(injection != null) {
                    content = content + injection;
                    try {
                        Field contentField;
                        if (httpObject.getClass().getSimpleName().equals("DefaultHttpContent") || httpObject.getClass().getSimpleName().equals("DefaultFullHttpResponse")) {
                            contentField = response.getClass().getDeclaredField("content");
                        } else {
                            contentField = response.getClass().getSuperclass().getDeclaredField("content");
                        }
                        boolean accessibility = contentField.isAccessible();
                        contentField.setAccessible(true);
                        contentField.set(response, Unpooled.copiedBuffer(content.getBytes()));
                        contentField.setAccessible(accessibility);
                        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, content.getBytes().length);
                    } catch (Exception ex) {
                        Utility.print("[x] Failed to make an injection: " + ex.getMessage());
                        Debugger.debug(ex);
                    }
                }
            }
        }
        return httpObject;
    }

    @Override
    public HttpResponse proxyToServerRequest(HttpObject httpObject) {
        if (httpObject instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) httpObject;
            if(request.headers().contains("Via")) {
                request.headers().remove("Via");
            }
        }
        return null;
    }
}
