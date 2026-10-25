package com.jeff_media.jefflib.data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/** Exercises the relocated library from the installable JAR against the Folia API. */
class PackagedJeffLibIT {

    private final Path jarPath = Path.of(System.getProperty("packagedJar"));
    private static final String VERSION_CLASS = "de.jeff_media.drop2inventory.data.McVersion";

    @TempDir
    Path dataDirectory;

    @Test
    void packagesOnlyThePatchedVersionReaderAndDeclaresFoliaSupport() throws Exception {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            assertEquals(1, jar.stream().filter(entry -> entry.getName().equals(
                    VERSION_CLASS.replace('.', '/') + ".class")).count());
            assertNull(jar.getEntry("com/jeff_media/jefflib/data/McVersion.class"));
            assertNotNull(jar.getEntry(VERSION_CLASS.replace('.', '/') + "$CurrentVersion.class"));
            String metadata = new String(jar.getInputStream(jar.getEntry("plugin.yml")).readAllBytes(),
                    java.nio.charset.StandardCharsets.UTF_8);
            assertTrue(metadata.contains("version: 4.8.1"));
            assertTrue(metadata.contains("api-version: \"26.3\""));
            assertTrue(metadata.contains("folia-supported: true"));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "26.3, 26.3.build.5-alpha, 26, 3, 0",
            "26.1.2, 26.1.2.build.69-stable, 26, 1, 2",
            "1.21.5, 1.21.5-R0.1-SNAPSHOT, 1, 21, 5"
    })
    void initializesThePackagedJeffLibUsingTheGameVersion(
            String gameVersion, String apiVersion, int major, int minor, int patch) throws Exception {
        AtomicInteger gameVersionCalls = new AtomicInteger();
        PluginManager manager = proxy(PluginManager.class, (proxy, method, args) -> {
            if (method.getName().equals("getPlugin")) return null;
            throw new UnsupportedOperationException(method.toString());
        });
        Server server = proxy(Server.class, (proxy, method, args) -> switch (method.getName()) {
            case "getMinecraftVersion" -> {
                gameVersionCalls.incrementAndGet();
                yield gameVersion;
            }
            case "getBukkitVersion" -> apiVersion;
            case "getPluginManager" -> manager;
            case "getName" -> "Folia";
            case "getLogger" -> Logger.getLogger("Drop2Inventory-test");
            default -> throw new UnsupportedOperationException(method.toString());
        });
        Plugin plugin = proxy(Plugin.class, (proxy, method, args) -> switch (method.getName()) {
            case "getServer" -> server;
            case "getName" -> "Drop2InventoryPlus-Folia";
            case "getLogger" -> Logger.getLogger("Drop2Inventory-test");
            case "getDataFolder" -> dataDirectory.toFile();
            case "getResource" -> readResource((String) args[0]);
            case "saveResource" -> {
                try (InputStream resource = readResource((String) args[0])) {
                    Files.copy(resource, dataDirectory.resolve((String) args[0]));
                }
                yield null;
            }
            default -> throw new UnsupportedOperationException(method.toString());
        });

        // Avoid Bukkit.setServer's real-server startup banner, which requires ServerBuildInfo.
        Field serverField = Bukkit.class.getDeclaredField("server");
        serverField.setAccessible(true);
        Object previousServer = serverField.get(null);
        serverField.set(null, server);
        try (URLClassLoader loader = new URLClassLoader(new URL[]{jarPath.toUri().toURL()},
                getClass().getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                // Failsafe also puts the packaged JAR on its parent classpath.
                // Isolate JeffLib so each simulated server has its own version cache.
                if (!name.startsWith("de.jeff_media.drop2inventory.")) {
                    return super.loadClass(name, resolve);
                }
                synchronized (getClassLoadingLock(name)) {
                    Class<?> loaded = findLoadedClass(name);
                    if (loaded == null) loaded = findClass(name);
                    if (resolve) resolveClass(loaded);
                    return loaded;
                }
            }
        }) {
            Class<?> jeffLib = loader.loadClass("de.jeff_media.drop2inventory.JeffLib");
            jeffLib.getMethod("init", Plugin.class).invoke(null, plugin);
            Class<?> versionType = loader.loadClass(VERSION_CLASS);
            Object current = versionType.getMethod("current").invoke(null);
            assertEquals(major, versionType.getMethod("getMajor").invoke(current));
            assertEquals(minor, versionType.getMethod("getMinor").invoke(current));
            assertEquals(patch, versionType.getMethod("getPatch").invoke(current));
            assertTrue(gameVersionCalls.get() > 0, "Must use the Minecraft version accessor");
            assertSame(current, versionType.getMethod("current").invoke(null));
            assertEquals(jarPath.toRealPath(), Path.of(
                    versionType.getProtectionDomain().getCodeSource().getLocation().toURI()).toRealPath());

            // Main.loadRunCommands constructs this JeffLib config during startup.
            Class<?> configType = loader.loadClass("de.jeff_media.drop2inventory.data.Config");
            YamlConfiguration config = (YamlConfiguration) configType.getConstructor(String.class)
                    .newInstance("run-commands.yml");
            assertTrue(config.isConfigurationSection("inventory-full"));
            assertTrue(Files.isRegularFile(dataDirectory.resolve("run-commands.yml")));
        } finally {
            serverField.set(null, previousServer);
        }
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    private InputStream readResource(String name) throws Exception {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            return new ByteArrayInputStream(jar.getInputStream(jar.getJarEntry(name)).readAllBytes());
        }
    }
}
