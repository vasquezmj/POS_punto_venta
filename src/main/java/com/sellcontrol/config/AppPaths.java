package com.sellcontrol.config;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Centraliza las rutas de datos de la aplicación.
 * Soporta ejecución desde:
 * - Desarrollo (CWD)
 * - jpackage en Windows (carpeta portable junto al .exe)
 * - jpackage en Linux RPM (/opt/ es read-only → usa ~/.sellcontrol/)
 */
public class AppPaths {

    private static final Path BASE_DIR;

    static {
        Path resolved;
        String appDir = System.getProperty("app.dir");
        String osName = System.getProperty("os.name", "").toLowerCase();
        boolean isLinux = osName.contains("linux") || osName.contains("nux");

        if (appDir != null && !appDir.isBlank()) {
            // Ejecutándose desde jpackage
            if (isLinux) {
                // En Linux (RPM), /opt/ es read-only.
                // Los datos van en el directorio del usuario.
                resolved = Paths.get(System.getProperty("user.home"), ".sellcontrol");
                System.out.println("[AppPaths] Linux detectado, datos en: " + resolved);
            } else {
                // En Windows, la carpeta portable es escribible.
                resolved = Paths.get(appDir);
            }
        } else {
            // Modo desarrollo: usar directorio de trabajo actual
            resolved = Paths.get("").toAbsolutePath();
        }

        BASE_DIR = resolved;

        // Crear directorios necesarios al iniciar
        String[] dirs = { "data", "config", "reportes", "respaldos", "logs" };
        for (String dir : dirs) {
            File d = BASE_DIR.resolve(dir).toFile();
            if (!d.exists()) {
                boolean created = d.mkdirs();
                if (created) {
                    System.out.println("[AppPaths] Directorio creado: " + d.getAbsolutePath());
                } else {
                    System.err.println("[AppPaths] ERROR: No se pudo crear: " + d.getAbsolutePath());
                }
            }
        }

        System.out.println("[AppPaths] Directorio base: " + BASE_DIR.toAbsolutePath());
    }

    /** Ruta a la base de datos SQLite */
    public static Path getDbPath() {
        return BASE_DIR.resolve("data").resolve("sellcontrol.db");
    }

    /** Ruta al archivo de configuración del ticket */
    public static Path getConfigPath() {
        return BASE_DIR.resolve("config").resolve("ticket.properties");
    }

    /** Directorio para exportación de reportes Excel */
    public static Path getReportesDir() {
        return BASE_DIR.resolve("reportes");
    }

    /** Directorio de respaldos de la base de datos */
    public static Path getRespaldosDir() {
        return BASE_DIR.resolve("respaldos");
    }

    /** Directorio de logs */
    public static Path getLogsDir() {
        return BASE_DIR.resolve("logs");
    }

    /** Directorio base de la aplicación */
    public static Path getBaseDir() {
        return BASE_DIR;
    }
}
