
## Proyecto: POS SellControl (Verdulería)

## 1. CONTEXTO DEL NEGOCIO

- Tipo de negocio: Verdulería minorista
- Cantidad de locales: 1
- Cantidad de cajas: 1
- Usuarios del sistema: 4
- Roles: Administrador y Cajero
- Problema principal: 
  - Falta de control sobre ventas
  - Posibles pérdidas de dinero
  - Errores por facturación manual
  - Falta de control de caja y ganancias reales

Objetivo principal del sistema:
> Controlar ventas, caja, gastos y ganancias reales, reduciendo errores, pérdidas y riesgo de robo.

---

## 2. TIPO DE SISTEMA

- Aplicación de escritorio
- Uso local
- Una sola computadora
- Impresión de tickets en impresora térmica
- Pago único (sin suscripciones)

---

## 3. STACK TECNOLÓGICO OBLIGATORIO

- Lenguaje: Java
- UI: JavaFX
- Base de datos: SQLite (local)
- Persistencia: DAO / Repository
- Impresión: Java Print Service
- Exportación Excel: Apache POI
- Seguridad: Hash de contraseñas (BCrypt o equivalente)

---

## 4. ARQUITECTURA

Usar arquitectura en capas:

- UI (JavaFX)
- Services (Lógica de negocio)
- DAO / Repository (Persistencia)
- SQLite (Base de datos local)

Separar claramente cada capa.
No mezclar lógica de negocio en la UI.

---

## 5. ENTIDADES PRINCIPALES

### Usuario
- id
- nombre
- usuario
- contraseña (hasheada)
- rol (ADMIN | CAJERO)
- activo

### Producto
- id
- nombre
- tipo (FRUTA | VERDURA | OTRO)
- precioPorKg
- precioPorUnidad
- activo

### Venta
- id
- fechaHora
- usuarioId
- total
- metodoPago (EFECTIVO | TARJETA | SINPE)
- estado (COBRADA | PENDIENTE)

### DetalleVenta
- id
- ventaId
- productoId
- cantidad
- tipoUnidad (KG | UNIDAD)
- subtotal

### MovimientoCaja
- id
- tipo (INGRESO | CAMBIO)
- monto
- motivo
- usuarioId
- fechaHora

### Gasto
- id
- tipo (ABASTECIMIENTO | EMPLEADOS)
- monto
- fechaHora
- descripcion

### Merma
- id
- descripcion
- montoAproximado
- fechaHora

---

## 6. REQUISITOS FUNCIONALES

### Autenticación y Usuarios
- Login con usuario y contraseña
- Roles con permisos
- Auditoría de acciones (ventas y caja)

### Productos
- CRUD de productos
- Productos simples (sin códigos de barra)
- Precio por kg o por unidad

### Ventas
- Venta rápida
- Venta por kg o unidad
- Métodos de pago: efectivo, tarjeta, SINPE
- Ventas fiadas (pendientes de cobro)
- Cálculo automático de totales
- Impresión de ticket

### Caja
- Registro de movimientos:
  - Ingreso de efectivo
  - Cambio de billetes
- Registro de usuario, fecha y motivo
- Cierre diario
- Control por cajero

### Gastos y Merma
- Registro de gastos:
  - Abastecimiento
  - Empleados
- Registro de merma aproximada
- Ambos afectan la ganancia

### Reportes
- Reporte diario
- Reporte semanal
- Ventas totales
- Ventas por cajero
- Ventas por método de pago
- Productos más vendidos
- Ganancia real

### Exportación
- Exportar reportes a Excel
- Archivo autocompletado
- Datos por rango de fechas

---

## 7. REGLAS DE NEGOCIO

Ganancia = Ventas - Gastos - Merma

Toda venta debe:
- Tener usuario
- Tener fecha y hora
- Tener método de pago

Todo movimiento de caja debe:
- Tener motivo
- Tener usuario
- Quedar registrado

Ventas pendientes:
- No suman al efectivo hasta ser cobradas

---

## 8. REQUISITOS NO FUNCIONALES

- Optimizado para bajos recursos
- UI simple y clara
- Respuesta rápida
- Persistencia confiable
- Preparado para futuras ampliaciones:
  - Facturación electrónica
  - Inventario avanzado

---

## 9. ALCANCE EXPLÍCITO (NO IMPLEMENTAR)

- Facturación electrónica
- Multi-sucursal
- Inventario con proveedores
- App móvil
- Sistema en la nube
- Códigos de barras

---

## 10. ORDEN DE DESARROLLO SUGERIDO

1. Autenticación y roles
2. Gestión de productos
3. Ventas
4. Caja
5. Gastos y merma
6. Reportes
7. Exportación Excel
8. Integración impresora

---

## 11. OBJETIVO FINAL

El sistema debe permitir que el dueño, al final del día o semana, pueda responder con exactitud:

- ¿Cuánto se vendió?
- ¿Cuánto dinero entró realmente?
- ¿Cuánto se gastó?
- ¿Cuánto se ganó?
- ¿Quién realizó cada venta?
