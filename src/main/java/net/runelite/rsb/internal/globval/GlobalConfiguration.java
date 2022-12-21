package net.runelite.rsb.internal.globval;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

public class GlobalConfiguration {

    public enum OperatingSystem {
        MAC, WINDOWS, LINUX, UNKNOWN
    }

    public static class Paths {

        public static class Resources {
            public static final String ICON = GlobalConfiguration.class.getResource("rsb/plugin/rsb.png").getPath();
        }

        /**
         * Retrieves the home directory using the operating system specific
         * method and concatenates it with our API specific path.
         * @return  the home directory of our API
         */
        public static String getOsrsBotDirectory() {
            final String env = System.getenv(GlobalConfiguration.NAME.toUpperCase() + "_HOME");
            if (env == null || env.isEmpty()) {
                String homeDirBuilder;
                switch(GlobalConfiguration.getCurrentOperatingSystem()) {
                    case LINUX:
                        homeDirBuilder = System.getProperty("user.home")
                                + File.separator + ".config";
                        break;
                    case WINDOWS:
                        homeDirBuilder = System.getProperty("user.home");
                        break;
                    default: //MAC etc
                        homeDirBuilder = Paths.getUnixHome();
                        break;
                }
                return (homeDirBuilder + File.separator + GlobalConfiguration.NAME);
            }
            return env;
        }

        public static String getLogsDirectory() {
            return Paths.getOsrsBotDirectory() + File.separator + "Logs";
        }

        public static String getScriptsDirectory() {
            return Paths.getOsrsBotDirectory() + File.separator + "Scripts";
        }

        public static String getCacheDirectory() {
            return Paths.getOsrsBotDirectory() + File.separator + "Cache";
        }

        public static String getUnixHome() {
            final String home = System.getProperty("user.home");
            return home == null ? "~" : home;
        }


        /**
         * Gets the directory where RuneLite installs the jagex cache.
         * @return  the jagex cache directory
         */
        public static String getRuneLiteGameCacheDirectory() {
            return System.getProperty("user.home") + File.separator + ".runelite" +
                File.separator + "jagexcache" + File.separator + "oldschool" + File.separator + "LIVE" + File.separator;
        }

        /**
         * Gets the bot directory where the object cache is stored.
         * @return  the object cache directory
         */
        public static String getObjectsCacheDirectory() {
            return Paths.getCacheDirectory() + File.separator + "Objects" + File.separator;
        }

        /**
         * Gets the bot directory where the sprites cache is stored.
         * @return  the sprites cache directory
         */
        public static String getSpritesCacheDirectory() {
            return Paths.getCacheDirectory() + File.separator + "Sprites" + File.separator;
        }

        /**
         * Gets the bot directory where the npcs cache is stored.
         * @return  the npcs cache directory
         */
        public static String getNPCsCacheDirectory() {
            return Paths.getCacheDirectory() + File.separator + "NPCs" + File.separator;
        }

        /**
         * Gets the bot directory where the items cache is stored.
         * @return  the items cache directory
         */
        public static String getItemsCacheDirectory() {
            return Paths.getCacheDirectory() + File.separator + "Items" + File.separator;
        }
    }

    public static final String NAME = "OsrsBot";
    private static final OperatingSystem CURRENT_OS;

    /**
     * When executed it starts up the general configurations and paths as well as determines what files will be auto-generated
     */
    static {
        final URL resource = GlobalConfiguration.class.getProtectionDomain().getCodeSource().getLocation();
        final String os = System.getProperty("os.name");
        if (os.contains("Mac")) {
            CURRENT_OS = OperatingSystem.MAC;
        } else if (os.contains("Windows")) {
            CURRENT_OS = OperatingSystem.WINDOWS;
        } else if (os.contains("Linux")) {
            CURRENT_OS = OperatingSystem.LINUX;
        } else {
            CURRENT_OS = OperatingSystem.UNKNOWN;
        }

        // This is where folders and files are generated on start-up
        final ArrayList<String> dirs = new ArrayList<>();
        dirs.add(Paths.getOsrsBotDirectory());
        dirs.add(Paths.getLogsDirectory());
        dirs.add(Paths.getScriptsDirectory());
        dirs.add(Paths.getCacheDirectory());

        for (final String name : dirs) {
            final File dir = new File(name);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
    }

    public static URL getResourceURL(final String path) throws MalformedURLException {
        return new File(path).toURI().toURL();
    }

    public static Image getImage(String resource) {
        try {
            return Toolkit.getDefaultToolkit().getImage(getResourceURL(resource));
        } catch (Exception e) { }
        return null;
    }

    public static OperatingSystem getCurrentOperatingSystem() {
        return GlobalConfiguration.CURRENT_OS;
    }

}
