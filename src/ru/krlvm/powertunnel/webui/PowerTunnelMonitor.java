package ru.krlvm.powertunnel.webui;

import io.netty.handler.codec.http.HttpResponse;
import ru.krlvm.invader.Invader;
import ru.krlvm.invader.SnifferRecord;
import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.data.DataStore;
import ru.krlvm.powertunnel.frames.JournalFrame;
import ru.krlvm.powertunnel.utilities.HttpUtility;
import ru.krlvm.powertunnel.utilities.Utility;

import java.io.IOException;
import java.util.Map;

public class PowerTunnelMonitor {

    private static final String DEFAULT_HTML = "<html>\n" +
            "<head>\n" +
            "    <title>Invader Monitor</title>\n" +
            "    <style>\n" +
            "        html, body {\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "\n" +
            "        #lists, #lists > div {\n" +
            "            border-color: gray !important;\n" +
            "        }\n" +
            "\n" +
            "        #lists {\n" +
            "            padding-bottom: 5px;\n" +
            "            border-top: 5px solid;\n" +
            "            border-bottom: 5px solid;\n" +
            "            display: flex;\n" +
            "            flex-direction: row;\n" +
            "            justify-content: space-between;\n" +
            "            height: 74vh;\n" +
            "        }\n" +
            "        \n" +
            "        .list {\n" +
            "            width: 33%;\n" +
            "            height: 100%;\n" +
            "            padding: 5px;\n" +
            "        }\n" +
            "\n" +
            "        .list button {\n" +
            "            width: 100%;\n" +
            "        }\n" +
            "\n" +
            "        .list > h2 {\n" +
            "            text-decoration: underline;\n" +
            "        }\n" +
            "\n" +
            "        .list > select {\n" +
            "            width: 100%;\n" +
            "            height: 75%;\n" +
            "            margin-bottom: 5px;\n" +
            "        }\n" +
            "    </style>\n" +
            "    <script>\n" +
            "        function handleSelection(element) {\n" +
            "            var selected = [], option;\n" +
            "            for (var i = 0; i < element.options.length; i++) {\n" +
            "                option = element.options[i];\n" +
            "                if (option.selected) {\n" +
            "                    selected.push(option);\n" +
            "                }\n" +
            "            }\n" +
            "            var selection = selected[selected.length-1].value;\n" +
            "            element.value = selection;\n" +
            "            return selection;\n" +
            "        }\n" +
            "\n" +
            "        function doAction(action, selector) {\n" +
            "            var selection = handleSelection(document.getElementById(selector));\n" +
            "            location.href = action + '-' + selection;\n" +
            "        }\n" +
            "    </script>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <h1>Invader Monitor | <a href=\"sniffer\">Sniffer</a></h1>\n" +
            "    <div id=\"lists\">\n" +
            "        <div class=\"list\" id=\"blacklist\" style=\"float: left;\">\n" +
            "            <h2>Blacklist:</h2>\n" +
            "            <select id=\"black_selector\" onchange=\"handleSelection(this)\" multiple>\n" +
            "                {blacklist_content}\n" +
            "            </select>\n" +
            "            <button onclick=\"doAction('unblock', 'black_selector')\">Remove</button>\n" +
            "            <b>{blacklist_size} entries</b>\n" +
            "        </div>\n" +
            "        <div class=\"list\" id=\"journal\" style=\"border-right: 5px solid; border-left: 5px solid;\">\n" +
            "            <h2>Journal:</h2>\n" +
            "            <select id=\"journal_selector\" onchange=\"handleSelection(this)\" multiple>\n" +
            "                {journal_content}\n" +
            "            </select>\n" +
            "            <button onclick=\"doAction('block', 'journal_selector')\">Add to blacklist</button>\n" +
            "            <button onclick=\"doAction('white', 'journal_selector')\">Add to whitelist</button>\n" +
            "        </div>\n" +
            "        <div class=\"list\" id=\"whitelist\" style=\"float: right\">\n" +
            "            <h2>Whitelist:</h2>\n" +
            "            <select id=\"whitelist_selector\" onchange=\"handleSelection(this)\" multiple>\n" +
            "                {whitelist_content}\n" +
            "            </select>\n" +
            "            <button onclick=\"doAction('unwhite', 'whitelist_selector')\">Remove</button>\n" +
            "            <b>{whitelist_size} entries</b>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "    <br>\n" +
            "    <div id=\"about\">\n" +
            "        <b>Invader " + PowerTunnel.VERSION + "<br><a href=\"https://github.com/krlvm/Invader\">https://github.com/krlvm/Invader</a></b>\n" +
            "        <br>\n" +
            "        (c) krlvm, 2019-2020\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";
    private static String HTML = DEFAULT_HTML;

    private static final String DEFAULT_SNIFFER_HTML = "<html>\n" +
            "<head>\n" +
            "    <title>Invader Sniffer</title>\n" +
            "    <style>\n" +
            "        html, body {\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "\n" +
            "        #view {\n" +
            "            overflow-y: scroll;\n" +
            "            padding-bottom: 5px;\n" +
            "            border-top: 5px solid;\n" +
            "            border-bottom: 5px solid;\n" +
            "            justify-content: space-between;\n" +
            "            height: 74vh;\n" +
            "            width: 100%;\n" +
            "        }\n" +
            "\n" +
            "        .record {\n" +
            "            text-align: left;\n" +
            "            margin: 10px;\n" +
            "            padding: 5px;\n" +
            "            border: solid 2px gray;\n" +
            "        }\n" +
            "\n" +
            "        .record .code {\n" +
            "            width: 100%;\n" +
            "            height: 350px;\n" +
            "            border: solid 1px black;\n" +
            "            background-color: lightgray;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <h1>Invader <a href=\"/\">Monitor</a> | Sniffer</h1>\n" +
            "    <div id=\"view\">\n" +
            "        {content}\n" +
            "    </div>\n" +
            "    <br>\n" +
            "    <div id=\"about\">\n" +
            "        <b>Invader " + PowerTunnel.VERSION + "<br><a href=\"https://github.com/krlvm/Invader\">https://github.com/krlvm/Invader</a></b>\n" +
            "        <br>\n" +
            "        (c) krlvm, 2019-2020\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";
    private static String SNIFFER_HTML = DEFAULT_SNIFFER_HTML;

    public static final String FAKE_ADDRESS = "invadermitmmonitor.info";
    private static final String[] FORMAT = new String[] {
            "http://", "www."
    };

    private static final DataStore HTML_STORE = new DataStore("webui", DEFAULT_HTML) {
        @Override
        public String getFileFormat() {
            return "html";
        }
    };

    private static final DataStore SNIFFER_STORE = new DataStore("snifferui", SNIFFER_HTML) {
        @Override
        public String getFileFormat() {
            return "html";
        }
    };

    public static void load() throws IOException {
        HTML_STORE.load();
        HTML = HTML_STORE.inline();

        SNIFFER_STORE.load();
        SNIFFER_HTML = SNIFFER_STORE.inline();
        Utility.print("[!] Sniffer is enabled and available at http://" + FAKE_ADDRESS);
    }

    public static boolean checkUri(String uri) {
        return formatUri(uri).startsWith(FAKE_ADDRESS);
    }

    public static HttpResponse getResponse(String uri) {
        uri = formatUri(uri);
        if(uri.equals(FAKE_ADDRESS) || uri.equals(FAKE_ADDRESS + "/")) {
            StringBuilder blacklist = new StringBuilder();
            for (String s : PowerTunnel.getUserBlacklist()) {
                blacklist.append("<option>").append(s).append("</option>");
            }
            StringBuilder journal = new StringBuilder();
            for (String s : JournalFrame.getVisited()) {
                journal.append("<option>").append(s.replace(" ", "")).append("</option>");
            }
            StringBuilder whitelist = new StringBuilder();
            for (String s : PowerTunnel.getUserWhitelist()) {
                whitelist.append("<option>").append(s).append("</option>");
            }
            return HttpUtility.getResponse(HTML.replace("{blacklist_size}", String.valueOf(PowerTunnel.getUserBlacklist().size()))
                    .replace("{whitelist_size}", String.valueOf(PowerTunnel.getUserWhitelist().size()))
                    .replace("{blacklist_content}", blacklist.toString())
                    .replace("{journal_content}", journal.toString())
                    .replace("{whitelist_content}", whitelist.toString()));
        } else {
            String[] uriArray = uri.split("/");
            if(uriArray.length < 2) {
                return HttpUtility.getResponse("Invalid request");
            }
            String query = uriArray[1].toLowerCase();
            if(query.equals("sniffer")) {
                int id = 0;
                StringBuilder snifferContent = new StringBuilder();
                for (SnifferRecord record : Invader.SNIFFER_RECORDS) {
                    snifferContent.append("<div class=\"record\"><b><u>");
                    snifferContent.append(record.getSource());
                    snifferContent.append("</u></b><hr><b><u>Headers:</u></b><br>");
                    for (Map.Entry<String, String> header : record.getHeaders()) {
                        snifferContent.append(header.getKey()).append(": ").append(header.getValue()).append("<br>");
                    }
                    int currentId = id++;
                    snifferContent.append("<hr><b><u>Content:</u></b><br><span id=\"content").append(currentId).append("\" style=\"display:none\">");
                    if(!PowerTunnel.SNIFFER_UI_RENDER) {
                        snifferContent.append("<div class=\"code\">");
                        snifferContent.append(record.getContent().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"));
                        snifferContent.append("</div>");
                    } else {
                        snifferContent.append(record.getContent());
                    }
                    snifferContent.append("</span><span style=\"cursor:pointer;color:blue;text-decoration:underline;\" onclick=\"document.getElementById('content").append(currentId).append("').style.display='block';this.style.display='none';\">View</span></div>");
                }
                return HttpUtility.getResponse(SNIFFER_HTML.replace("{content}", snifferContent.toString()));
            }
            String[] queryArray = query.split("-");
            if(queryArray.length < 2) {
                return HttpUtility.getResponse("Invalid query");
            }
            String action = queryArray[0].toLowerCase();
            String address = queryArray[1];
            switch (action) {
                case "unblock": {
                    PowerTunnel.removeFromUserBlacklist(address);
                    break;
                }
                case "white": {
                    PowerTunnel.addToUserWhitelist(address.split(":")[2]);
                    break;
                }
                case "block": {
                    PowerTunnel.addToUserBlacklist(address.split(":")[2]);
                    break;
                }
                case "unwhite": {
                    PowerTunnel.removeFromUserWhitelist(address);
                    break;
                }
                default: {
                    return HttpUtility.getResponse("Unknown action");
                }
            }
            return HttpUtility.getResponse("<script>location.href = '/';</script>OK");
        }
    }

    private static String formatUri(String uri) {
        for (String s : FORMAT) {
            if(uri.startsWith(s)) {
                uri = uri.replace(s, "");
            }
        }
        return uri;
    }
}
