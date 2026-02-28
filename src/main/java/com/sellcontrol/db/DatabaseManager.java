package com.sellcontrol.db;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Administra la conexión a SQLite y la inicialización del esquema.
 * Implementa Singleton para una sola conexión en toda la aplicación.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:sellcontrol.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {}

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Obtiene la conexión a la base de datos. Crea una nueva si no existe.
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            // Habilitar foreign keys en SQLite
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
        }
        return connection;
    }

    /**
     * Inicializa la base de datos: crea tablas y seed de admin.
     */
    public void initialize() {
        try {
            Connection conn = getConnection();
            createTables(conn);
            seedAdmin(conn);
            System.out.println("[DB] Base de datos inicializada correctamente.");
        } catch (SQLException e) {
            System.err.println("[DB] Error al inicializar la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crea todas las tablas del sistema si no existen.
     */
    private void createTables(Connection conn) throws SQLException {
        String[] sqls = {
            // Usuarios
            """
            CREATE TABLE IF NOT EXISTS usuarios (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre      TEXT NOT NULL,
                usuario     TEXT NOT NULL UNIQUE,
                contrasena  TEXT NOT NULL,
                rol         TEXT NOT NULL CHECK(rol IN ('ADMIN','CAJERO')),
                activo      INTEGER NOT NULL DEFAULT 1,
                creado_en   TEXT NOT NULL DEFAULT (datetime('now','localtime'))
            )
            """,

            // Productos
            """
            CREATE TABLE IF NOT EXISTS productos (
                id                INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre            TEXT NOT NULL,
                tipo              TEXT NOT NULL CHECK(tipo IN ('FRUTA','VERDURA','OTRO')),
                precio_por_kg     REAL,
                precio_por_unidad REAL,
                activo            INTEGER NOT NULL DEFAULT 1,
                creado_en         TEXT NOT NULL DEFAULT (datetime('now','localtime'))
            )
            """,

            // Ventas
            """
            CREATE TABLE IF NOT EXISTS ventas (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                fecha_hora     TEXT NOT NULL DEFAULT (datetime('now','localtime')),
                usuario_id     INTEGER NOT NULL,
                total          REAL NOT NULL DEFAULT 0,
                metodo_pago    TEXT NOT NULL CHECK(metodo_pago IN ('EFECTIVO','TARJETA','SINPE')),
                estado         TEXT NOT NULL DEFAULT 'COBRADA' CHECK(estado IN ('COBRADA','PENDIENTE')),
                cliente_nombre TEXT,
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
            )
            """,

            // Detalle de Venta
            """
            CREATE TABLE IF NOT EXISTS detalle_venta (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                venta_id    INTEGER NOT NULL,
                producto_id INTEGER NOT NULL,
                cantidad    REAL NOT NULL,
                tipo_unidad TEXT NOT NULL CHECK(tipo_unidad IN ('KG','UNIDAD')),
                subtotal    REAL NOT NULL,
                FOREIGN KEY (venta_id) REFERENCES ventas(id),
                FOREIGN KEY (producto_id) REFERENCES productos(id)
            )
            """,

            // Movimientos de Caja
            """
            CREATE TABLE IF NOT EXISTS movimientos_caja (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                tipo        TEXT NOT NULL CHECK(tipo IN ('INGRESO','CAMBIO')),
                monto       REAL NOT NULL,
                motivo      TEXT NOT NULL,
                usuario_id  INTEGER NOT NULL,
                fecha_hora  TEXT NOT NULL DEFAULT (datetime('now','localtime')),
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
            )
            """,

            // Gastos
            """
            CREATE TABLE IF NOT EXISTS gastos (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                tipo        TEXT NOT NULL CHECK(tipo IN ('ABASTECIMIENTO','EMPLEADOS')),
                monto       REAL NOT NULL,
                fecha_hora  TEXT NOT NULL DEFAULT (datetime('now','localtime')),
                descripcion TEXT
            )
            """,

            // Merma
            """
            CREATE TABLE IF NOT EXISTS mermas (
                id               INTEGER PRIMARY KEY AUTOINCREMENT,
                descripcion      TEXT NOT NULL,
                monto_aproximado REAL NOT NULL,
                fecha_hora       TEXT NOT NULL DEFAULT (datetime('now','localtime'))
            )
            """,

            // Auditoría
            """
            CREATE TABLE IF NOT EXISTS audit_log (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario_id  INTEGER NOT NULL,
                accion      TEXT NOT NULL,
                entidad     TEXT NOT NULL,
                entidad_id  INTEGER,
                fecha_hora  TEXT NOT NULL DEFAULT (datetime('now','localtime')),
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
            )
            """
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : sqls) {
                stmt.execute(sql);
            }
        }
        System.out.println("[DB] Tablas creadas/verificadas correctamente.");
    }

    /**
     * Crea el usuario administrador inicial si no existe.
     * Credenciales: admin / admin123
     */
    private void seedAdmin(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM usuarios WHERE usuario = 'admin'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String hashedPassword = BCrypt.hashpw("admin123", BCrypt.gensalt());
                String insertSql = "INSERT INTO usuarios (nombre, usuario, contrasena, rol, activo) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, "Administrador");
                    ps.setString(2, "admin");
                    ps.setString(3, hashedPassword);
                    ps.setString(4, "ADMIN");
                    ps.setInt(5, 1);
                    ps.executeUpdate();
                }
                System.out.println("[DB] Usuario administrador creado (admin / admin123).");
            }
        }
    }

    /**
     * Cierra la conexión a la base de datos.
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error al cerrar conexión: " + e.getMessage());
        }
    }
}
