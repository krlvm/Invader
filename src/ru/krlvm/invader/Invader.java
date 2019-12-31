package ru.krlvm.invader;

import org.littleshoot.proxy.mitm.Authority;
import org.littleshoot.proxy.mitm.CertificateSniffingMitmManager;
import org.littleshoot.proxy.mitm.RootCertificateException;
import ru.krlvm.powertunnel.data.DataStore;
import ru.krlvm.powertunnel.data.DataStoreException;
import ru.krlvm.powertunnel.utilities.URLUtility;
import ru.krlvm.powertunnel.utilities.Utility;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The general class of Invader -
 * An effective MITM utility and script injector
 *
 * @author krlvm
 */
public class Invader {

    public static String MAIN_SCRIPT = null;
    private static Map<String, List<String>> SITE_SCRIPTS = new HashMap<>();

    private static final String SCRIPT_DIRECTORY = "scripts";
    private static final ScriptStore MAIN_SCRIPT_STORE = new ScriptStore(SCRIPT_DIRECTORY + "/main");
    private static final DataStore MAP_STORE = new DataStore("script-map");

    private static final DataStore CERTIFICATE_PASSWORD = new DataStore("cert-password");

    public static void loadScripts() throws DataStoreException, IOException {
        File directory = new File(SCRIPT_DIRECTORY);
        if(!directory.exists()) {
            directory.mkdir();
        }

        MAIN_SCRIPT_STORE.load();
        String unformattedMainScript = MAIN_SCRIPT_STORE.getScript();
        if(unformattedMainScript.isEmpty()) {
            MAIN_SCRIPT = null;
        } else {
            MAIN_SCRIPT = "<script>" + unformattedMainScript + "</script>";
        }

        int count = 0;
        MAP_STORE.load();
        for (String line : MAP_STORE.getLoadedLines()) {
            if(line.startsWith("#") || line.trim().isEmpty()) {
                continue;
            }
            String[] array = line.split(":");
            if(array.length != 2) {
                throw new DataStoreException("Malformed line in the map: " + line);
            }
            String url = URLUtility.clearHost(array[0]).toLowerCase();
            if(!SITE_SCRIPTS.containsKey(url)) {
                SITE_SCRIPTS.put(url, new ArrayList<String>());
            }
            ScriptStore script = new ScriptStore(SCRIPT_DIRECTORY + "/" + array[1]);
            script.load();
            SITE_SCRIPTS.get(url).add("<script>" + script.getScript() + "</script>");
            count++;
        }

        Utility.print("[Invader] Loaded main script and '%s' site scripts", count);
    }

    public static List<String> getScripts(String site) {
        return SITE_SCRIPTS.get(site.toLowerCase());
    }

    public static CertificateSniffingMitmManager mitmManager() throws RootCertificateException, IOException {
        CERTIFICATE_PASSWORD.load();
        StringBuilder builder = new StringBuilder();
        for (String line : CERTIFICATE_PASSWORD.getLoadedLines()) {
            builder.append(line);
        }
        String password = builder.toString();
        if(password.isEmpty()) {
            throw new IOException("Please come up with a strong password and write it in the 'cert-password.txt'");
        }
        return new CertificateSniffingMitmManager(new Authority(new File("."),
                "invader-mitm", password.toCharArray(), "Invader Root CA", "Invader MITM", "Invader", "Invader MITM", "Invader"));
    }

    public static String getInjection(String site) {
        if(site == null) {
            return MAIN_SCRIPT;
        }
        Collection<String> scripts = SITE_SCRIPTS.get(URLUtility.clearHost(site.toLowerCase()));
        if(scripts == null) {
            return MAIN_SCRIPT;
        }
        if(SITE_SCRIPTS.containsKey("*")) {
            scripts.addAll(SITE_SCRIPTS.get("*"));
        }
        StringBuilder script = new StringBuilder();
        if(MAIN_SCRIPT != null) {
            script.append(MAIN_SCRIPT);
        }
        for (String s : scripts) {
            script.append(s);
        }
        return script.toString();
    }
}
