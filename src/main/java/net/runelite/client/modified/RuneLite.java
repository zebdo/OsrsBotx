        // // modified client specific:
        // if (options.has("proxy")) {
        //     String[] proxy = options.valueOf(optionSpecs[Options.PROXY_INFO.getIndex()].ofType(String.class)).split(":");

        //     if (proxy.length >= 2) {
        //         System.setProperty("socksProxyHost", proxy[0]);
        //         System.setProperty("socksProxyPort", proxy[1]);
        //     }

        //     if (proxy.length >= 4) {
        //         System.setProperty("java.net.socks.username", proxy[2]);
        //         System.setProperty("java.net.socks.password", proxy[3]);

        //         final String user = proxy[2];
        //         final char[] pass = proxy[3].toCharArray();

        //         Authenticator.setDefault(new Authenticator() {
        //             private PasswordAuthentication auth = new PasswordAuthentication(user, pass);

        //             protected PasswordAuthentication getPasswordAuthentication() {
        //                 return auth;
        //             }
        //         });
        //     }
        // }

package net.runelite.client.modified;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.applet.Applet;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.swing.SwingUtilities;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.ValueConversionException;
import joptsimple.ValueConverter;
import joptsimple.util.EnumConverter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.client.account.SessionManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.discord.DiscordService;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.externalplugins.ExternalPluginManager;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.rs.ClientLoader;
import net.runelite.client.rs.ClientUpdateCheckMode;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.FatalErrorDialog;
import net.runelite.client.ui.SplashScreen;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.WidgetOverlay;
import net.runelite.client.ui.overlay.tooltip.TooltipOverlay;
import net.runelite.client.ui.overlay.worldmap.WorldMapOverlay;
import net.runelite.client.util.OSType;
import net.runelite.client.util.ReflectUtil;
import net.runelite.http.api.RuneLiteAPI;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.LoggerFactory;

// Added:
import net.runelite.client.RuntimeConfig;
import net.runelite.client.TelemetryClient;
import net.runelite.client.ClientSessionManager;
import net.runelite.client.RuneLiteProperties;
import net.runelite.client.RuntimeConfigLoader;
import net.runelite.client.RuneLiteModule;

// Copy paste in ClassPreloader:
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import net.runelite.client.ui.FontManager;

/**
 * Loads some slow to initialize classes (hopefully) before they are needed to streamline client startup
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "unused"})
class ClassPreloader
{
	static void preload()
	{
		// This needs to enumerate the system fonts for some reason, and that takes a while
		FontManager.getRunescapeSmallFont();

		// This needs to load a timezone database that is mildly large
		ZoneId.of("Europe/London");

		// This just needs to call 20 different DateTimeFormatter constructors, which are slow
		Object unused = DateTimeFormatter.BASIC_ISO_DATE;
	}
}


@Singleton
@Slf4j
@SuppressWarnings("removal")
public class RuneLite extends net.runelite.client.RuneLite
{
	public static final File RUNELITE_DIR = new File(System.getProperty("user.home"), ".runelite");
	public static final File CACHE_DIR = new File(RUNELITE_DIR, "cache");
	public static final File PLUGINS_DIR = new File(RUNELITE_DIR, "plugins");
	public static final File SCREENSHOT_DIR = new File(RUNELITE_DIR, "screenshots");
	public static final File LOGS_DIR = new File(RUNELITE_DIR, "logs");
	public static final File DEFAULT_SESSION_FILE = new File(RUNELITE_DIR, "session");
	public static final File DEFAULT_CONFIG_FILE = new File(RUNELITE_DIR, "settings.properties");

	private static final int MAX_OKHTTP_CACHE_SIZE = 20 * 1024 * 1024; // 20mb

	public static String USER_AGENT = "RuneLite/" + RuneLiteProperties.getVersion() + "-" + RuneLiteProperties.getCommit() + (RuneLiteProperties.isDirty() ? "+" : "");

	@Inject
	private PluginManager pluginManager;

	@Inject
	private ExternalPluginManager externalPluginManager;

	@Inject
	private EventBus eventBus;

	@Inject
	private ConfigManager configManager;

	@Inject
	private SessionManager sessionManager;

	@Inject
	private DiscordService discordService;

	@Inject
	private ClientSessionManager clientSessionManager;

	@Inject
	private ClientUI clientUI;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Provider<TooltipOverlay> tooltipOverlay;

	@Inject
	private Provider<WorldMapOverlay> worldMapOverlay;

	@Inject
	@Nullable
	private Applet applet;

	@Inject
	@Nullable
	private Client client;

	@Inject
	@Nullable
	private RuntimeConfig runtimeConfig;

	@Inject
	@Nullable
	private TelemetryClient telemetryClient;

	/**
	 * Launches a single instance of RuneLite
	 *
	 * @param args      The args for the programxdx
	 * @Throws Exception Any exception the client or RuneLite might throw
	 */
	public void launch(String[] args) throws Exception
	{
		Locale.setDefault(Locale.ENGLISH);

		final OptionParser parser = new OptionParser(false);
		parser.accepts("developer-mode", "Enable developer tools");
		parser.accepts("debug", "Show extra debugging output");
		parser.accepts("safe-mode", "Disables external plugins and the GPU plugin");
		parser.accepts("insecure-skip-tls-verification", "Disables TLS verification");
		parser.accepts("jav_config", "jav_config url")
			.withRequiredArg()
			.defaultsTo(RuneLiteProperties.getJavConfig());
		parser.accepts("disable-telemetry", "Disable telemetry");
		parser.accepts("profile", "Configuration profile to use").withRequiredArg();

		final ArgumentAcceptingOptionSpec<File> sessionfile = parser.accepts("sessionfile", "Use a specified session file")
			.withRequiredArg()
			.withValuesConvertedBy(new ConfigFileConverter())
			.defaultsTo(DEFAULT_SESSION_FILE);

		final ArgumentAcceptingOptionSpec<File> configfile = parser.accepts("config", "Use a specified config file (deprecated)")
			.withRequiredArg()
			.withValuesConvertedBy(new ConfigFileConverter())
			.defaultsTo(DEFAULT_CONFIG_FILE);

		final ArgumentAcceptingOptionSpec<ClientUpdateCheckMode> updateMode = parser
			.accepts("rs", "Select client type")
			.withRequiredArg()
			.ofType(ClientUpdateCheckMode.class)
			.defaultsTo(ClientUpdateCheckMode.AUTO)
			.withValuesConvertedBy(new EnumConverter<ClientUpdateCheckMode>(ClientUpdateCheckMode.class)
			{
				@Override
				public ClientUpdateCheckMode convert(String v)
				{
					return super.convert(v.toUpperCase());
				}
			});

		final ArgumentAcceptingOptionSpec<String> proxyInfo = parser
				.accepts("proxy", "Designates a proxy ip address to be used to make the bot server connections")
				.withRequiredArg().ofType(String.class);

		parser.accepts("help", "Show this text").forHelp();
		OptionSet options = parser.parse(args);

		if (options.has("help"))
		{
			parser.printHelpOn(System.out);
			System.exit(0);
		}

        // XXX PROXY STUFF?

		if (options.has("debug"))
		{
			final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
			logger.setLevel(Level.DEBUG);
		}

		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
		{
			log.error("Uncaught exception:", throwable);
			if (throwable instanceof AbstractMethodError)
			{
				log.error("Classes are out of date; Build with maven again.");
			}
		});

		final OkHttpClient okHttpClient = buildHttpClient(options.has("insecure-skip-tls-verification"));
		RuneLiteAPI.CLIENT = okHttpClient;

		SplashScreen.init();
		SplashScreen.stage(0, "Retrieving client", "");

		try
		{
			final RuntimeConfigLoader runtimeConfigLoader = new RuntimeConfigLoader(okHttpClient);
			final ClientLoader clientLoader = new ClientLoader(okHttpClient, options.valueOf(updateMode), runtimeConfigLoader, (String) options.valueOf("jav_config"));

			new Thread(() ->
			{
				clientLoader.get();
				ClassPreloader.preload();
			}, "Preloader").start();

			//final boolean developerMode = options.has("developer-mode") && RuneLiteProperties.getLauncherVersion() == null;
			final boolean developerMode = true;

			if (developerMode)
			{
				boolean assertions = false;
				assert assertions = true;
				if (!assertions)
				{
					SwingUtilities.invokeLater(() ->
						new FatalErrorDialog("Developers should enable assertions; Add `-ea` to your JVM arguments`")
							.addHelpButtons()
							.addBuildingGuide()
							.open());
					return;
				}
			}

			log.info("RuneLite {} (launcher version {}) starting up, args: {}",
				RuneLiteProperties.getVersion(), MoreObjects.firstNonNull(RuneLiteProperties.getLauncherVersion(), "unknown"),
				args.length == 0 ? "none" : String.join(" ", args));

			final RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
			// This includes arguments from _JAVA_OPTIONS, which are parsed after command line flags and applied to
			// the global VM args
			log.info("Java VM arguments: {}", String.join(" ", runtime.getInputArguments()));
			log.info("Disable telemetry: {}", options.has("disable-telemetry"));

			final long start = System.currentTimeMillis();
			var i = Guice.createInjector(new RuneLiteModule(
				okHttpClient,
				clientLoader,
				runtimeConfigLoader,
				developerMode,
				options.has("safe-mode"),
				options.has("disable-telemetry"),
				options.valueOf(sessionfile),
				options.valueOf(configfile),
				(String) options.valueOf("profile")
			));

            // XXX err this needs a comment about shadowing variables - or something
            setInjector(i);
			getInjector().getInstance(RuneLite.class).start();

			final long end = System.currentTimeMillis();
			final long uptime = runtime.getUptime();
			log.info("Client initialization took {}ms. Uptime: {}ms", end - start, uptime);

			log.info("RUNELITE_DIR {}", RUNELITE_DIR);
			log.info("CACHE_DIR {}", CACHE_DIR);
			log.info("PLUGINS_DIR {}", PLUGINS_DIR);
			log.info("SCREENSHOT_DIR {}", SCREENSHOT_DIR);
			log.info("DIR {}", LOGS_DIR);
			log.info("DEFAULT_SESSION_FILE {}", DEFAULT_SESSION_FILE);
			log.info("DEFAULT_CONFIG_FILE {}", DEFAULT_CONFIG_FILE);
			log.info("USER_AGENT {}", USER_AGENT);
		}
		catch (Exception e)
		{
			log.error("Failure during startup", e);
			SwingUtilities.invokeLater(() ->
				new FatalErrorDialog("RuneLite has encountered an unexpected error during startup.")
					.addHelpButtons()
					.open());
		}
		finally
		{
			SplashScreen.stop();
		}
	}

    /*
      inherit from Runelite
     */

    // public void start() throws Exception
    // public static void setInjector(Injector injector)

    /* copy pasted private methods from runelite: */

	private static class ConfigFileConverter implements ValueConverter<File>
	{
		@Override
		public File convert(String fileName)
		{
			final File file;

			if (Paths.get(fileName).isAbsolute()
				|| fileName.startsWith("./")
				|| fileName.startsWith(".\\"))
			{
				file = new File(fileName);
			}
			else
			{
				file = new File(RuneLite.RUNELITE_DIR, fileName);
			}

			if (file.exists() && (!file.isFile() || !file.canWrite()))
			{
				throw new ValueConversionException(String.format("File %s is not accessible", file.getAbsolutePath()));
			}

			return file;
		}

		@Override
		public Class<? extends File> valueType()
		{
			return File.class;
		}

		@Override
		public String valuePattern()
		{
			return null;
		}
	}

	@VisibleForTesting
	static OkHttpClient buildHttpClient(boolean insecureSkipTlsVerification)
	{
		OkHttpClient.Builder builder = new OkHttpClient.Builder()
			.pingInterval(30, TimeUnit.SECONDS)
			.addInterceptor(chain ->
			{
				Request request = chain.request();
				if (request.header("User-Agent") != null)
				{
					return chain.proceed(request);
				}

				Request userAgentRequest = request
					.newBuilder()
					.header("User-Agent", USER_AGENT)
					.build();
				return chain.proceed(userAgentRequest);
			})
			// Setup cache
			.cache(new Cache(new File(CACHE_DIR, "okhttp"), MAX_OKHTTP_CACHE_SIZE))
			.addNetworkInterceptor(chain ->
			{
				// This has to be a network interceptor so it gets hit before the cache tries to store stuff
				Response res = chain.proceed(chain.request());
				if (res.code() >= 400 && "GET".equals(res.request().method()))
				{
					// if the request 404'd we don't want to cache it because its probably temporary
					res = res.newBuilder()
						.header("Cache-Control", "no-store")
						.build();
				}
				return res;
			});

		try
		{
			if (insecureSkipTlsVerification || RuneLiteProperties.isInsecureSkipTlsVerification())
			{
				setupInsecureTrustManager(builder);
			}
			else
			{
				setupTrustManager(builder);
			}
		}
		catch (KeyStoreException | KeyManagementException | NoSuchAlgorithmException e)
		{
			log.warn("error setting up trust manager", e);
		}

		return builder.build();
	}

	private static void copyJagexCache()
	{
		Path from = Paths.get(System.getProperty("user.home"), "jagexcache");
		Path to = Paths.get(System.getProperty("user.home"), ".runelite", "jagexcache");
		if (Files.exists(to) || !Files.exists(from))
		{
			return;
		}

		log.info("Copying jagexcache from {} to {}", from, to);

		// Recursively copy path https://stackoverflow.com/a/50418060
		try (Stream<Path> stream = Files.walk(from))
		{
			stream.forEach(source ->
			{
				try
				{
					Files.copy(source, to.resolve(from.relativize(source)), COPY_ATTRIBUTES);
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			});
		}
		catch (Exception e)
		{
			log.warn("unable to copy jagexcache", e);
		}
	}

	private void setupSystemProps()
	{
		if (runtimeConfig == null || runtimeConfig.getSysProps() == null)
		{
			return;
		}

		for (Map.Entry<String, String> entry : runtimeConfig.getSysProps().entrySet())
		{
			String key = entry.getKey(), value = entry.getValue();
			log.debug("Setting property {}={}", key, value);
			System.setProperty(key, value);
		}
	}

	// region trust manager
	private static TrustManager[] loadTrustManagers(String trustStoreType) throws KeyStoreException, NoSuchAlgorithmException
	{
		// javax.net.ssl.trustStoreType controls which keystore implementation the TrustStoreManager uses
		String old;
		if (trustStoreType != null)
		{
			old = System.setProperty("javax.net.ssl.trustStoreType", trustStoreType);
		}
		else
		{
			old = System.clearProperty("javax.net.ssl.trustStoreType");
		}

		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init((KeyStore) null);

		TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

		// restore old value
		if (old == null)
		{
			System.clearProperty("javax.net.ssl.trustStoreType");
		}
		else
		{
			System.setProperty("javax.net.ssl.trustStoreType", old);
		}

		return trustManagers;
	}

	private static void setupTrustManager(OkHttpClient.Builder okHttpClientBuilder) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException
	{
		if (OSType.getOSType() != OSType.Windows)
		{
			return;
		}

		// Use the Windows Trusted Root Certificate Authorities in addition to the bundled cacerts.
		// Corporations, schools, antivirus, and malware commonly install root certificates onto
		// machines for security or other reasons that are not present in the JRE certificate store.
		TrustManager[] jreTms = loadTrustManagers(null);
		TrustManager[] windowsTms = loadTrustManagers("Windows-ROOT");

		TrustManager[] trustManagers = new TrustManager[jreTms.length + windowsTms.length];
		System.arraycopy(jreTms, 0, trustManagers, 0, jreTms.length);
		System.arraycopy(windowsTms, 0, trustManagers, jreTms.length, windowsTms.length);

		// Even though SSLContext.init() accepts TrustManager[], Sun's SSLContextImpl only picks the first
		// X509TrustManager and uses that.
		X509TrustManager combiningTrustManager = new X509TrustManager()
		{
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
			{
				CertificateException exception = null;
				for (TrustManager trustManager : trustManagers)
				{
					if (trustManager instanceof X509TrustManager)
					{
						try
						{
							((X509TrustManager) trustManager).checkClientTrusted(chain, authType);
							// accept if any of the trust managers accept the certificate
							return;
						}
						catch (CertificateException ex)
						{
							exception = ex;
						}
					}
				}

				if (exception != null)
				{
					throw exception;
				}

				throw new CertificateException("no X509TrustManagers present");
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
			{
				CertificateException exception = null;
				for (TrustManager trustManager : trustManagers)
				{
					if (trustManager instanceof X509TrustManager)
					{
						try
						{
							((X509TrustManager) trustManager).checkServerTrusted(chain, authType);
							// accept if any of the trust managers accept the certificate
							return;
						}
						catch (CertificateException ex)
						{
							exception = ex;
						}
					}
				}

				if (exception != null)
				{
					throw exception;
				}

				throw new CertificateException("no X509TrustManagers present");
			}

			@Override
			public X509Certificate[] getAcceptedIssuers()
			{
				List<X509Certificate> certificates = new ArrayList<>();
				for (TrustManager trustManager : trustManagers)
				{
					if (trustManager instanceof X509TrustManager)
					{
						certificates.addAll(Arrays.asList(((X509TrustManager) trustManager).getAcceptedIssuers()));
					}
				}
				return certificates.toArray(new X509Certificate[0]);
			}
		};

		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(null, new TrustManager[]{combiningTrustManager}, new SecureRandom());
		okHttpClientBuilder.sslSocketFactory(sc.getSocketFactory(), combiningTrustManager);
	}

	private static void setupInsecureTrustManager(OkHttpClient.Builder okHttpClientBuilder) throws NoSuchAlgorithmException, KeyManagementException
	{
		// the insecure trust manager trusts everything
		X509TrustManager trustManager = new X509TrustManager()
		{
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType)
			{
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType)
			{
			}

			@Override
			public X509Certificate[] getAcceptedIssuers()
			{
				return null;
			}
		};

		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(null, new TrustManager[]{trustManager}, new SecureRandom());
		okHttpClientBuilder.sslSocketFactory(sc.getSocketFactory(), trustManager);
	}
	// endregion
}
