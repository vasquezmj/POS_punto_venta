# 🛒 POS SellControl

**Sistema de Punto de Venta** para comercio minorista, desarrollado en Java con JavaFX. Diseñado para verdulería, pero adaptable a cualquier tipo de negocio de venta directa.

> Control total de ventas, caja, gastos y ganancias reales desde una aplicación de escritorio.

---

## ✨ Características Principales

| Módulo | Funcionalidad |
|---|---|
| **Autenticación** | Login con roles (Admin/Cajero), contraseñas hasheadas con BCrypt |
| **Productos** | CRUD completo, venta por kg o por unidad, categorías (Fruta, Verdura, Legumbres, Otros) |
| **Ventas** | Venta rápida, múltiples métodos de pago (Efectivo, Tarjeta, SINPE), ventas fiadas |
| **Caja** | Registro de ingresos, cambios y retiros, control por cajero, cierre diario |
| **Gastos y Merma** | Registro de gastos operativos y merma, impacto en ganancia real |
| **Reportes** | Reportes diarios/semanales, exportación a Excel (.xlsx), ganancia real |
| **Tickets** | Impresión en impresora térmica 58mm (ESC/POS), cabecera y pie configurables |
| **Instaladores** | Scripts para generar instaladores nativos Windows (.exe) y Fedora Linux (.rpm) |

---

## 🏗️ Arquitectura

Arquitectura en capas con separación clara de responsabilidades:

```
┌─────────────────────────────────────────┐
│             UI (JavaFX + FXML)          │
├─────────────────────────────────────────┤
│          Controllers (FXML)             │
├─────────────────────────────────────────┤
│        Services (Lógica de negocio)     │
├─────────────────────────────────────────┤
│          DAO (Acceso a datos)           │
├─────────────────────────────────────────┤
│           SQLite (Base de datos)        │
└─────────────────────────────────────────┘
```

## 🛠️ Stack Tecnológico

| Tecnología | Uso |
|---|---|
| **Java 21+** | Lenguaje principal |
| **JavaFX 23** | Interfaz gráfica de usuario |
| **SQLite** | Base de datos local embebida |
| **Apache POI** | Exportación de reportes a Excel |
| **BCrypt** | Hash seguro de contraseñas |
| **Java Print API** | Impresión en impresora térmica ESC/POS |
| **Maven** | Gestión de dependencias y build |
| **jpackage** | Generación de instaladores nativos |

---

## 📁 Estructura del Proyecto

```
punto_de_venta/
├── src/main/java/com/sellcontrol/
│   ├── App.java                    # Punto de entrada
│   ├── config/
│   │   └── AppPaths.java           # Gestión centralizada de rutas
│   ├── controller/                 # Controladores JavaFX
│   │   ├── LoginController.java
│   │   ├── DashboardController.java
│   │   ├── VentaController.java
│   │   ├── ProductoController.java
│   │   ├── CajaController.java
│   │   ├── GastoController.java
│   │   ├── ReporteController.java
│   │   ├── UsuarioController.java
│   │   └── TicketConfigController.java
│   ├── model/                      # Entidades del dominio
│   ├── dao/                        # Capa de acceso a datos
│   ├── db/
│   │   └── DatabaseManager.java    # Conexión y esquema SQLite
│   └── service/                    # Lógica de negocio
│       ├── VentaService.java
│       ├── TicketPrintService.java  # Impresión ESC/POS
│       ├── ExcelExportService.java  # Exportación a Excel
│       └── ...
├── src/main/resources/
│   └── com/sellcontrol/
│       ├── fxml/                    # Vistas FXML
│       └── css/styles.css           # Estilos
├── build-installer.ps1              # Script para generar .exe (Windows)
├── build-linux.sh                   # Script para generar .rpm (Fedora)
├── pom.xml                          # Configuración Maven
└── planProyecto.md                  # Documento de requisitos
```

---

## 🚀 Cómo Ejecutar

### Requisitos previos
- **JDK 21** o superior
- **Maven 3.8+**

### Desde código fuente
```bash
# Clonar el repositorio
git clone https://github.com/vasquezmj/POS_punto_venta.git
cd POS_punto_venta

# Compilar y ejecutar
mvn javafx:run
```

### Credenciales por defecto
| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | Administrador |

---

## 📦 Generar Instaladores

### Windows (.exe portable)
```powershell
.\build-installer.ps1
# Resultado: target\installer\POS SellControl\POS SellControl.exe
```

### Fedora Linux (.rpm)
```bash
chmod +x build-linux.sh && ./build-linux.sh
sudo dnf install target/installer/POS-SellControl-1.0.0-1.x86_64.rpm
```

Ambos instaladores incluyen **JRE embebido** — no requieren Java instalado en la máquina destino.

---

## 🖨️ Impresora Térmica

El sistema está configurado para impresoras térmicas de **58mm** con protocolo **ESC/POS** (probado con HOP-H58).

- **Windows**: Se conecta vía USB, se detecta automáticamente por nombre.
- **Linux**: Requiere CUPS configurado. El nombre de la impresora se configura en `~/.sellcontrol/config/ticket.properties`.

---

## 📊 Fórmula de Ganancia

```
Ganancia Real = Ventas Cobradas − Gastos − Merma
```

---

## 📄 Licencia

Este proyecto es de uso privado y fue desarrollado como solución funcional para una verdulería minorista en Costa Rica.

---

<p align="center">
  Desarrollado con ☕ Java + 💚 JavaFX
</p>
