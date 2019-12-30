package ru.krlvm.powertunnel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.mitm.RootCertificateException;
import ru.krlvm.invader.Invader;
import ru.krlvm.powertunnel.data.DataStore;
import ru.krlvm.powertunnel.data.DataStoreException;
import ru.krlvm.powertunnel.filter.ProxyFilter;
import ru.krlvm.powertunnel.frames.*;
import ru.krlvm.powertunnel.system.MirroredOutputStream;
import ru.krlvm.powertunnel.updater.UpdateNotifier;
import ru.krlvm.powertunnel.utilities.Debugger;
import ru.krlvm.powertunnel.utilities.URLUtility;
import ru.krlvm.powertunnel.utilities.Utility;
import ru.krlvm.swingdpi.SwingDPI;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Invader Bootstrap class
 *
 * This class initializes Invader, loads user scripts,
 * user lists, holds journal and controlling a LittleProxy Server
 *
 * Invader is build around PowerTunnel - a simple and effective anti-censorship solution
 * Base PowerTunnel version: 1.6 (code 7)
 * https://github.com/krlvm/PowerTunnel
 *
 * @author krlvm
 */
public class PowerTunnel {

    public static final String NAME = "Invader";
    public static final String VERSION = "1.0";
    public static final int VERSION_CODE = 1;
    public static final String REPOSITORY_URL = "https://github.com/krlvm/Invader";

    public static final String BASE_VERSION = "PowerTunnel v1.6";
    public static final int BASE_VERSION_CODE = 7;
    public static final String BASE_REPOSITORY = "https://github.com/krlvm/PowerTunnel";

    private static HttpProxyServer SERVER;
    private static boolean RUNNING = false;
    public static String SERVER_IP_ADDRESS = "127.0.0.1";
    public static int SERVER_PORT = 8085;

    public static final boolean FULL_OUTPUT_MIRRORING = false;

    private static final Map<String, String> JOURNAL = new LinkedHashMap<>();
    private static final SimpleDateFormat JOURNAL_DATE_FORMAT = new SimpleDateFormat("[HH:mm]: ");

    private static final Set<String> USER_BLACKLIST = new LinkedHashSet<>();
    private static final Set<String> USER_WHITELIST = new LinkedHashSet<>();

    private static MainFrame frame;
    public static LogFrame logFrame;
    public static JournalFrame journalFrame;
    public static UserListFrame[] USER_FRAMES;
    
    private static boolean CONSOLE_MODE = false;

    public static void main(String[] args) {
        //Parse launch arguments
        //java -jar Invader.jar (-args)
        boolean startNow = false;
        boolean[] uiSettings = { true, true };
        if(args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (!arg.startsWith("-")) {
                    continue;
                }
                arg = arg.replaceFirst("-", "").toLowerCase();
                switch (arg) {
                    case "help": {
                        Utility.print("Available params:\n" +
                                " -help - display help\n" +
                                " -start - starts server right after load\n" +
                                " -console - console mode, without UI\n" +
                                " -ip [IP Address] - sets IP Address\n" +
                                " -port [Port] - sets port\n" +
                                " -disable-native-lf - disables native L&F (when UI enabled)\n" +
                                " -disable-ui-scaling - disables UI scaling (when UI enabled)\n" +
                                " -disable-updater - disables the update notifier\n" +
                                " -debug - enable debug");
                        System.exit(0);
                        break;
                    }
                    case "start": {
                        startNow = true;
                        break;
                    }
                    case "debug": {
                        Debugger.setDebug(true);
                        break;
                    }
                    case "console": {
                        CONSOLE_MODE = true;
                        break;
                    }
                    case "disable-ui-scaling": {
                        uiSettings[0] = false;
                        break;
                    }
                    case "disable-native-lf": {
                        uiSettings[1] = false;
                        break;
                    }
                    case "disable-updater": {
                        UpdateNotifier.ENABLED = false;
                        break;
                    }
                    default: {
                        if (args.length < i + 1) {
                            Utility.print("[!] Invalid input for option '%s'", arg);
                        } else {
                            String value = args[i + 1];
                            switch (arg) {
                                case "ip": {
                                    SERVER_IP_ADDRESS = value;
                                    Utility.print("[#] IP address set to '%s'", SERVER_IP_ADDRESS);
                                    break;
                                }
                                case "port": {
                                    try {
                                        SERVER_PORT = Integer.parseInt(value);
                                        Utility.print("[#] Port set to '%s'", SERVER_PORT);
                                    } catch (NumberFormatException ex) {
                                        Utility.print("[x] Invalid port, using default");
                                    }
                                    break;
                                }
                                default: {
                                    //it is an argument
                                    //Utility.print("[?] Unknown option: '%s'", arg);
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        if(!CONSOLE_MODE) {
            //Initialize UI
            if(uiSettings[1]) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ex) {
                    System.out.println("Failed to set native Look and Feel: " + ex.getMessage());
                    ex.printStackTrace();
                    System.out.println();
                }
            }
            if(uiSettings[0]) {
                SwingDPI.applyScalingAutomatically();
            }

            //Initializing main frame and system outputs mirroring
            logFrame = new LogFrame();
            if (FULL_OUTPUT_MIRRORING) {
                PrintStream systemOutput = System.out;
                PrintStream systemErr = System.err;
                System.setOut(new PrintStream(new MirroredOutputStream(new ByteArrayOutputStream(), logFrame, systemOutput)));
                System.setErr(new PrintStream(new MirroredOutputStream(new ByteArrayOutputStream(), logFrame, systemErr)));
            }

            journalFrame = new JournalFrame();
            frame = new MainFrame();

            //Initialize UI
            USER_FRAMES = new UserListFrame[] {
                    new BlacklistFrame(), new WhitelistFrame()
            };
        }

        Utility.print(NAME + " version " + VERSION);
        Utility.print("An effective MITM utility and script injector");
        Utility.print("Based on " + BASE_VERSION + " (" + BASE_REPOSITORY + ")");
        Utility.print(REPOSITORY_URL);
        Utility.print("(c) krlvm, 2019");
        Utility.print();

        //Allow us to modify 'HOST' request header
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        if(CONSOLE_MODE || startNow) {
            safeBootstrap();
        }

        UpdateNotifier.checkAndNotify();
    }

    public static String safeBootstrap() {
        try {
            PowerTunnel.bootstrap();
        } catch (UnknownHostException ex) {
            Utility.print("[x] Cannot use IP-Address '%s': %s", SERVER_IP_ADDRESS, ex.getMessage());
            Debugger.debug(ex);
            Utility.print("[!] Program halted");
            return "Cannot use IP Address '" + PowerTunnel.SERVER_IP_ADDRESS + "'";
        } catch (IOException ex) {
            Utility.print("[x] Something went wrong: " + ex.getMessage());
            ex.printStackTrace();
            return "Something went wrong: " + ex.getMessage();
        } catch (DataStoreException ex) {
            Utility.print("[x] Failed to load data store: " + ex.getMessage());
            ex.printStackTrace();
            return "Failed to load data store: " + ex.getMessage();
        } catch (RootCertificateException ex) {
            Utility.print("[x] Failed to initialize certificate: " + ex.getMessage());
            ex.printStackTrace();
            return "Failed to initialize certificate: " + ex.getMessage();
        }
        return null;
    }

    /**
     * PowerTunnel bootstrap
     */
    public static void bootstrap() throws DataStoreException, IOException, RootCertificateException {
        //Load data
        try {
            Invader.loadScripts();
            USER_BLACKLIST.addAll(new DataStore(DataStore.USER_BLACKLIST).load());
            USER_WHITELIST.addAll(new DataStore(DataStore.USER_WHITELIST).load());
            Utility.print();
        } catch (IOException ex) {
            throw new DataStoreException(ex.getMessage(), ex);
        }

        //Start server
        startServer();
    }

    /**
     * Starts LittleProxy server
     */
    private static void startServer() throws IOException, RootCertificateException {

        Utility.print("[.] Starting LittleProxy server on %s:%s", SERVER_IP_ADDRESS, SERVER_PORT);
        SERVER = DefaultHttpProxyServer.bootstrap().withFiltersSource(new HttpFiltersSourceAdapter() {
            @Override
            public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                return new ProxyFilter(originalRequest);
            }
            @Override
            public int getMaximumResponseBufferSizeInBytes() {
                return 10 * 1024 * 1024;
            }
        }).withManInTheMiddle(Invader.mitmManager())
                .withAddress(new InetSocketAddress(InetAddress.getByName(SERVER_IP_ADDRESS), SERVER_PORT))
                .withTransparent(true).start();
        RUNNING = true;
        Utility.print("[.] Server started");
        Utility.print();

        if(!CONSOLE_MODE) {
            frame.update();
        }
    }

    /**
     * Stops LittleProxy server
     */
    public static void stopServer() {
        Utility.print();
        Utility.print("[.] Stopping server...");
        SERVER.stop();
        Utility.print("[.] Server stopped");
        Utility.print();
        RUNNING = false;

        if(!CONSOLE_MODE) {
            frame.update();
        }
    }

    /**
     * Save data and goodbye
     */
    public static void stop() {
        stopServer();
        safeUserListSave();
        USER_BLACKLIST.clear();
        USER_WHITELIST.clear();
    }

    public static void safeUserListSave() {
        try {
            saveUserLists();
            Utility.print("[#] User blacklist and whitelist saved");
        } catch (IOException ex) {
            Utility.print("[x] Failed to save data: " + ex.getMessage());
            ex.printStackTrace();
            Utility.print();
        }
    }
    /**
     * Retrieve is LittleProxy server is running
     *
     * @return true if it is or false if it isn't
     */
    public static boolean isRunning() {
        return RUNNING;
    }

    /**
     * Retrieves is console mode disabled (therefore is the UI enabled)
     *
     * @return is UI enabled
     */
    public static boolean isUIEnabled() {
        return !CONSOLE_MODE;
    }

    /*
    Journal block
     */

    /**
     * Adds website address to journal
     *
     * @param address - website address
     */
    public static void addToJournal(String address) {
        JOURNAL.put(address, JOURNAL_DATE_FORMAT.format(new Date()));
    }

    /**
     * Retrieves the journal
     *
     * @return journal
     */
    public static Map<String, String> getJournal() {
        return JOURNAL;
    }

    /**
     * Clears the journal
     */
    public static void clearJournal() {
        JOURNAL.clear();
    }

    /*
    Government blacklist block
     */

    /*
    User lists block
     */

    /**
     * Writes user black and whitelist to data store
     *
     * @throws IOException - write failure
     * @see DataStore
     */
    public static void saveUserLists() throws IOException {
        new DataStore(DataStore.USER_BLACKLIST).write(USER_BLACKLIST);
        new DataStore(DataStore.USER_WHITELIST).write(USER_WHITELIST);
    }

    /**
     * Refills user list frames
     */
    public static void updateUserListFrames() {
        if(CONSOLE_MODE) {
            return;
        }
        for (UserListFrame frame : USER_FRAMES) {
            frame.refill();
        }
    }

    /*
    Blacklist
     */

    /**
     * Adds website to the user blacklist
     * and removes from the user whitelist if it's contains in it
     *
     * @param address - website address
     * @return true if address doesn't already contains in the user blacklist or false if it is
     */
    public static boolean addToUserBlacklist(String address) {
        address = address.toLowerCase();
        if(USER_BLACKLIST.contains(address)) {
            return false;
        }
        USER_WHITELIST.remove(address);
        USER_BLACKLIST.add(address);
        updateUserListFrames();
        Utility.print("\n[@] Blacklisted: '%s'\n", address);
        return true;
    }

    /**
     * Retrieve if user blocked website
     *
     * @param address - website address
     * @return true if user blocked website or false if he isn't
     */
    public static boolean isUserBlacklisted(String address) {
        return URLUtility.checkIsHostContainsInList(address.toLowerCase(), USER_BLACKLIST);
        //return USER_BLACKLIST.contains(address.toLowerCase());
    }

    /**
     * Removes website from the user blacklist
     *
     * @param address - website address
     * @return true if address contained in the user blacklist (and removed) or false if it isn't
     */
    public static boolean removeFromUserBlacklist(String address) {
        address = address.toLowerCase();
        if(!USER_BLACKLIST.contains(address)) {
            return false;
        }
        USER_BLACKLIST.remove(address);
        updateUserListFrames();
        Utility.print("\n[@] Removed from the blacklist: '%s'\n", address);
        return true;
    }

    /**
     * Retrieves the user blacklist
     *
     * @return the user blacklist
     */
    public static Set<String> getUserBlacklist() {
        return USER_BLACKLIST;
    }

    /*
    Whitelist
     */

    /**
     * Adds website to the user whitelist
     * and removes from the user blocklist if it's contains in it
     *
     * @param address - website address
     * @return true if address doesn't already contains in the user whitelist or false if it is
     */
    public static boolean addToUserWhitelist(String address) {
        address = address.toLowerCase();
        if(USER_WHITELIST.contains(address)) {
            return false;
        }
        USER_BLACKLIST.remove(address);
        USER_WHITELIST.add(address);
        updateUserListFrames();
        Utility.print("\n[@] Whitelisted: '%s'\n", address);
        return true;
    }

    /**
     * Retrieve if user whitelisted website
     *
     * @param address - website address
     * @return true if user whitelisted website or false if he isn't
     */
    public static boolean isUserWhitelisted(String address) {
        return URLUtility.checkIsHostContainsInList(address.toLowerCase(), USER_WHITELIST);
        //return USER_WHITELIST.contains(address.toLowerCase());
    }

    /**
     * Removes website from the user whitelist
     *
     * @param address - website address
     * @return true if address contained in the user whitelist (and removed) or false if it isn't
     */
    public static boolean removeFromUserWhitelist(String address) {
        address = address.toLowerCase();
        if(!USER_WHITELIST.contains(address)) {
            return false;
        }
        USER_WHITELIST.remove(address);
        updateUserListFrames();
        Utility.print("\n[@] Removed from the whitelist: '%s'\n", address);
        return true;
    }

    /**
     * Retrieves the user whitelist
     *
     * @return the user whitelist
     */
    public static Set<String> getUserWhitelist() {
        return USER_WHITELIST;
    }
}