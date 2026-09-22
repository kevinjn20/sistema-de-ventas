# Sistema de Ventas con CRUD, Reportes y Dashboard

Actua como un desarrollador senior. Un sistema de ventas completo y moderno diseñado para gestionar el inventario, registrar transacciones en tiempo real, administrar clientes y visualizar el rendimiento del negocio a través de un panel de control analítico. Este proyecto está diseñado como un portafolio personal de alta calidad para GitHub.

## Stack
- **Backend:** Java 17 + Spring Boot 3.x (Spring Web, Spring Data JPA, Spring Security / Bcrypt)
- **Frontend:** React (Vite + TypeScript v5.x en modo estricto) + Tailwind CSS (o Shadcn/ui)
- **Base de datos:** PostgreSQL 16 (orquestado localmente con Docker Compose)
- **Visualización/Dashboard:** Recharts (en React) para gráficos interactivos
- **Pruebas:** JUnit 5 y Mockito (Backend) / Vitest o Jest (Frontend)

## Comandos
- `docker-compose up -d` — Levanta la base de datos PostgreSQL localmente en segundo plano.
- `cd backend && ./mvnw spring-boot:run` — Arranca la API REST de Spring Boot en local (puerto 8080).
- `cd frontend && npm run dev` — Arranca el servidor de desarrollo de React (puerto 5173).
- `cd backend && ./mvnw test` — Ejecuta las pruebas unitarias y de integración del backend.
- `cd frontend && npm run test` — Ejecuta las pruebas unitarias del frontend.
- `cd backend && ./mvnw clean package` — Compila el backend de Spring Boot en un archivo .jar ejecutable.
- `cd frontend && npm run build` — Compila el frontend para producción.

## Estructura del proyecto
- `/backend/` — Proyecto completo de Spring Boot.
  - `src/main/java/com/sistema/ventas/entities/` — Modelos y entidades de base de datos JPA.
  - `src/main/java/com/sistema/ventas/repositories/` — Interfaces de JPA Repository para consultas SQL.
  - `src/main/java/com/sistema/ventas/services/` — Lógica de negocio (control de stock, cálculo de totales, etc.).
  - `src/main/java/com/sistema/ventas/controllers/` — Controladores REST que exponen los endpoints públicos y privados.
  - `src/main/resources/` — Configuraciones del sistema (`application.yml`) y scripts de migración.
- `/frontend/` — Proyecto completo de React en TypeScript.
  - `src/components/` — Componentes UI reutilizables (tablas, botones, modales, formularios).
  - `src/pages/` — Vistas principales (Dashboard, Ventas, Productos, Clientes).
  - `src/services/` — Clientes HTTP (Axios / Fetch) para comunicarse con la API de Spring Boot.
  - `src/hooks/` — Custom hooks para manejar el estado local y las peticiones de datos.
- `docker-compose.yml` — Archivo en la raíz para levantar la infraestructura local de base de datos.

## Convenciones
- **Naming:** 
  - Backend (Java): `camelCase` para variables y métodos, `PascalCase` para clases e interfaces, `UPPER_SNAKE_CASE` para constantes. Las tablas SQL y columnas se mapean en `snake_case`.
  - Frontend (TypeScript/React): `PascalCase` para componentes y archivos TSX, `camelCase` para variables, funciones y hooks personalizados.
- **Manejo de Errores:**
  - Backend: Usar `@ControllerAdvice` y `@ExceptionHandler` para capturar excepciones globales y devolver respuestas de error estandarizadas con código HTTP adecuado (ej: `400 Bad Request`, `404 Not Found`).
  - Frontend: Capturar errores de red en el cliente de Axios y mostrar notificaciones de alerta (Toasts) claras para el usuario.
- **Seguridad:** El guardado de contraseñas de usuario en base de datos debe ser cifrado utilizando `BCryptPasswordEncoder`.
- **Validación:** Validar los datos de entrada en el backend usando `@Valid` y anotaciones de Jakarta Bean Validation (como `@NotNull`, `@Min`, `@Size`).
- **Transaccionalidad:** Todo proceso de venta que involucre múltiples operaciones (insertar cabecera, insertar detalles, restar stock) debe ser decorado con `@Transactional` en la capa de servicios para evitar inconsistencia de datos.

## No hagas
- No subas credenciales, llaves secretas o archivos `.env` o `application.properties` con contraseñas de producción al repositorio de GitHub (agrega siempre estos archivos al `.gitignore`).
- No realices operaciones de eliminación física (`DELETE`) en cascada en las ventas o productos históricos; si un producto ya no se vende, usa una bandera de borrado lógico (`activo = false`).
- No uses la directiva `@CrossOrigin("*")` en producción; configúrala adecuadamente para que apunte solo al origen del frontend local o de producción.
- No realices cálculos de montos monetarios usando variables de tipo `float` o `double` en Java; utiliza siempre `BigDecimal` para evitar errores de redondeo de punto flotante.

## Flujo de trabajo
- **Paso 1: Inicialización.** Propón y genera el archivo `docker-compose.yml` para la base de datos y la estructura base de carpetas de Spring Boot y React. Espera mi OK antes de continuar.
- **Paso 2: Capa de Datos (Backend).** Crea primero las Entidades JPA y los esquemas de base de datos. Muestramelos para que los revisemos juntos.
- **Paso 3: CRUDs (Productos, Clientes, Usuarios).** Trabaja de manera incremental. Desarrolla un módulo por completo (backend -> frontend -> test de verificación) antes de iniciar con el siguiente.
- **Paso 4: Procesamiento de Ventas.** Diseña la lógica transaccional de venta en el backend. Pruébala intensamente con mocks antes de integrarla en el frontend.
- **Paso 5: Dashboard y Reportes.** Construye los endpoints de agregación de datos en Spring Boot y luego impleméntalos con Recharts en la vista principal del Dashboard.
- **Paso 6: Revisión final.** Formatea el código, optimiza el repositorio, y genera una propuesta de README para que luzca profesional en mi GitHub. de como desplegar que hace y de que esta compuesto.

## Documentación
- **Spring Boot Docs:** https://docs.spring.io/spring-boot/index.html
- **React + Vite Docs:** https://vite.dev/guide/
- **Tailwind CSS Docs:** https://tailwindcss.com/docs
- **Recharts API:** https://recharts.org