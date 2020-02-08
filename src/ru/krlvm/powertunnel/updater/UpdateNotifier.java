package ru.krlvm.powertunnel.updater;

import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.utilities.Debugger;
import ru.krlvm.powertunnel.utilities.UIUtility;
import ru.krlvm.powertunnel.utilities.URLUtility;
import ru.krlvm.powertunnel.utilities.Utility;

import javax.swing.*;
import java.io.IOException;

public class UpdateNotifier {

    public static boolean ENABLED = true;

    private static final String LOG_TAG = "[Updater] ";
    private static final String URL = "https://raw.githubusercontent.com/krlvm/Invader/master/version.txt";

    public static void checkAndNotify() {
        if(!ENABLED) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String content = URLUtility.load(URL);
                    String[] data = content.split(";");
                    if (data.length != 2) {
                        failure("Malformed response: '" + content + "'");
                    } else {
                        int newVersionCode;
                        try {
                            newVersionCode = Integer.parseInt(data[0]);
                        } catch (NumberFormatException ex) {
                            failure("Invalid version code: '" + data[0] + "'");
                            return;
                        }
                        if (newVersionCode <= PowerTunnel.VERSION_CODE) {
                            info("You're running the latest version of " + PowerTunnel.NAME + "!");
                            return;
                        }
                        final String version = data[1];
                        info("" + PowerTunnel.NAME + " is ready to update!",
                                "Version: " + version,
                                "Changelog: " + PowerTunnel.REPOSITORY_URL + "/releases/tag/v" + version,
                                "Download: " + PowerTunnel.REPOSITORY_URL + "/releases/download/v" + version + "/" + PowerTunnel.NAME + ".jar",
                                "Visit GitHub repository: " + PowerTunnel.REPOSITORY_URL);
                        if (PowerTunnel.isUIEnabled()) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    if(PowerTunnel.isMainFrameVisible()) {
                                        JEditorPane message = UIUtility.getLabelWithHyperlinkSupport("" + PowerTunnel.NAME + " is ready to update!" +
                                                "<br><br>" +
                                                "Version: " + version + "<br>" +
                                                "<br>" +
                                                "Changelog: <a href=\"" + PowerTunnel.REPOSITORY_URL + "/releases/tag/v" + version + "\">view</a>" +
                                                "<br>" +
                                                "Download: <a href=\"" + PowerTunnel.REPOSITORY_URL + "/releases/download/v" + version + "/" + PowerTunnel.NAME + ".jar\">click here</a>" +
                                                "<br><br>" +
                                                "Visit <a href=\"" + PowerTunnel.REPOSITORY_URL + "\">GitHub repository</a>" +
                                                "</body></html>", null);
                                        JOptionPane.showMessageDialog(null, message, "" + PowerTunnel.NAME + " Updater", JOptionPane.INFORMATION_MESSAGE);
                                    } else {
                                        PowerTunnel.getTray().showNotification("An PowerTunnel update available");
                                    }
                                }
                            });
                        }
                    }
                } catch (IOException ex) {
                    failure("Cannot connect to the server: " + ex.getMessage());
                    Debugger.debug(ex);
                }
            }
        }, "Update Check Thread").start();
    }

    private static void info(String... messages) {
        Utility.print();
        for (String message : messages) {
            print(message);
        }
        Utility.print();
    }

    private static void failure(String... messages) {
        Utility.print();
        print("Failed to check for updates!");
        for (String message : messages) {
            print(message);
        }
        Utility.print();
    }

    private static void print(String message) {
        Utility.print(LOG_TAG + message);
    }
}
