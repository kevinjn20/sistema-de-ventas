# Sistema de Ventas — CRUD, Reportes y Dashboard

![Java](https://img.shields.io/badge/Java-17-blue) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-green) ![React](https://img.shields.io/badge/React-18-blue) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue) ![Docker](https://img.shields.io/badge/Docker-Compose-blue)

Sistema fullstack para gestionar **inventario, clientes, usuarios y ventas en tiempo real**, con **dashboard analítico** (Recharts) y autenticación **JWT + BCrypt**. Proyecto de portafolio con backend y frontend testeados y despliegue Docker.

## Qué hace

- **Productos:** CRUD con borrado lógico (`activo=false`), control de stock y validación (`@Valid`, `@Min`, `@NotBlank`).
- **Clientes / Usuarios:** CRUD completo. Usuarios con roles `ADMIN` / `VENDEDOR` y contraseñas cifradas con `BCryptPasswordEncoder`.
- **Ventas:** creación transaccional (`@Transactional`): cabecera + detalles + descuento de stock + cálculo con `BigDecimal`. Anulación lógica (no borra historial). Endpoint paginado `GET /api/ventas/paged`.
- **Dashboard:** agregados del negocio (ventas por periodo, top productos, stock bajo con umbral configurable `APP_DASHBOARD_STOCK_MINIMO`), con filtros `from/to`.
- **Auth:** `POST /api/auth/login` devuelve JWT Bearer. Rutas protegidas por rol. Errores estandarizados vía `@ControllerAdvice` (`400/401/403/404/409/500`).
- **Frontend:** SPA React + TypeScript estricto + Tailwind: Login, Dashboard (Recharts), Ventas, Productos, Clientes, Usuarios. Axios con interceptor JWT + toasts (`sonner`). Tests con Vitest.

## Stack

| Capa | Tecnología |
|---|---|
| Backend | Java 17, Spring Boot 3.2.5, Web, Data JPA, Validation, Security, JJWT 0.12.6, Springdoc OpenAPI, Lombok |
| Frontend | React 18, Vite 5, TypeScript 5.5 (estricto), Tailwind 3, React Router 6, Axios, Recharts, Sonner |
| DB | PostgreSQL 16 (Docker). H2 en memoria solo para tests |
| Tests | JUnit 5 + Mockito + MockMvc (60 tests) / Vitest + Testing Library (16 tests) |
| Despliegue | Docker multi-stage: `backend` (JRE 17) + `frontend` (Nginx) + `postgres` |

## Estructura

```
.
├── docker-compose.yml        # postgres + backend + frontend (fullstack)
├── .env.example              # copiar a .env (no commitear)
├── backend/
│   ├── Dockerfile
│   ├── src/main/java/com/sistema/ventas/
│   │   ├── controllers/ (+ dto/)  # Auth, Producto, Cliente, Usuario, Venta, Dashboard
│   │   ├── services/              # lógica (VentaService @Transactional)
│   │   ├── repositories/          # 5 JpaRepository
│   │   ├── entities/              # Cliente, Producto, Usuario, Venta, DetalleVenta
│   │   ├── security/              # JwtService, JwtAuthFilter, UserDetailsServiceImpl
│   │   └── config/                # SecurityConfig, GlobalExceptionHandler, DataInitializer
│   └── src/test/                  # 60 tests (unitarios, MockMvc, integración)
└── frontend/
    ├── Dockerfile + nginx.conf    # build Vite → Nginx con proxy /api → backend:8080
    └── src/
        ├── pages/                 # Login, Dashboard, Ventas, Productos, Clientes, Usuarios (+ tests)
        ├── components/            # forms, Navbar, ProtectedRoute, ConfirmDialog
        ├── services/              # axios + *Service.ts por módulo
        ├── hooks/ context/        # useCrud, AuthContext
```

## Requisitos

- Docker + Docker Compose (despliegue fullstack), o
- Desarrollo local: JDK 17, Node 20+, PostgreSQL 16 (o Docker solo para la DB).

> Nota: hay JDK 21 instalado en esta máquina y funciona para `test/package` (Spring Boot 3.2 admite 17–21), pero el target oficial del proyecto es **Java 17**.

## Puesta en marcha

### Opción A — Fullstack con Docker (recomendado)

```bash
cp .env.example .env   # ajusta JWT_SECRET y passwords en producción
docker compose up --build -d
```

- Web: http://localhost:5173 (Nginx → proxy `/api` al backend)
- API: http://localhost:8080 — Swagger: http://localhost:8080/swagger-ui.html
- Login dev: `admin / admin123` (creado por `DataInitializer`, cambiar en prod)
- Ver logs: `docker compose logs -f backend`
- Parar: `docker compose down` (con volumen: `down -v` borra la DB)

El backend usa `SPRING_JPA_HIBERNATE_DDL_AUTO=update` en Compose para crear el esquema en el primer arranque. En producción real con migraciones usa `validate` (ver `application.yml`).

### Opción B — Desarrollo local (3 terminales)

```bash
# 1) Solo la DB
docker compose up postgres -d

# 2) Backend (puerto 8080)
cd backend && ./mvnw spring-boot:run
# Windows: .\mvnw.cmd spring-boot:run

# 3) Frontend (puerto 5173, proxy /api → localhost:8080)
cd frontend && npm install && npm run dev
```

Frontend Docker usa el mismo truco: en dev Vite proxea `/api`, en prod Nginx proxea `/api → backend:8080`, por eso el código usa rutas relativas (`/api/...`) sin `VITE_API_URL`.

## Variables de entorno (`.env`)

| Variable | Default | Para qué |
|---|---|---|
| `POSTGRES_DB/USER/PASSWORD/PORT` | `sistema_ventas/postgres/postgres/5432` | DB local |
| `BACKEND_PORT / FRONTEND_PORT` | `8080 / 5173` | Puertos publicados |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` (compose) | `update` primer arranque, `validate` con migraciones |
| `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | CORS permitido (nunca `*` en prod) |
| `JWT_SECRET` (≥32 chars) / `JWT_EXPIRATION_MS` | placeholder / `86400000` | Firma y expiración JWT |
| `APP_ADMIN_USERNAME/PASSWORD` | `admin/admin123` | Admin inicial solo dev |
| `APP_DASHBOARD_STOCK_MINIMO` | `5` | Umbral stock bajo |

## API principal

Base: `http://localhost:8080` (Swagger en `/swagger-ui.html`).

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| POST | `/api/auth/login` | público | `{username,password}` → `{token, tokenType: Bearer, rol}` |
| POST | `/api/auth/register` | ADMIN | Alta de usuario |
| GET/POST | `/api/productos`, `/api/productos/{id}`, PUT/DELETE | auth | CRUD (DELETE lógico) |
| GET/POST | `/api/clientes`, `/api/clientes/{id}`, PUT/DELETE | auth | CRUD |
| GET/POST | `/api/usuarios`, `/api/usuarios/{id}`, PUT/DELETE | ADMIN (POST/PUT/DELETE) | CRUD usuarios |
| GET | `/api/ventas`, `/api/ventas/paged`, `/api/ventas/{id}` | auth | Listar / paginado / detalle |
| POST | `/api/ventas` | ADMIN, VENDEDOR | Crear venta `{clienteId, usuarioId, items:[{productoId, cantidad}]}` |
| DELETE | `/api/ventas/{id}` | ADMIN | Anulación lógica |
| GET | `/api/dashboard?from=&to=` | auth | Agregados, top productos, stock bajo |

Reglas aplicadas: `BigDecimal` para dinero (nunca `float/double`), `@Transactional` en ventas, borrado lógico en productos/ventas, CORS restringido al frontend, `HttpMessageNotReadableException → 400` para JSON malformado.

## Tests y calidad

```bash
cd backend && ./mvnw test          # 60 tests — JUnit5/Mockito/MockMvc/H2
cd frontend && npm run test        # 16 tests — Vitest (7 ficheros)
cd frontend && npm run lint        # ESLint (requiere .eslintrc.json incluido)
cd backend && ./mvnw clean package # jar en target/ventas-*.jar
cd frontend && npm run build       # dist/ (tsc estricto + vite)
```

Estado verificado en este cierre: **backend 60/60 ✅, frontend 16/16 ✅, `mvn package` ✅, `vite build` ✅, `eslint` ✅**. Se corrigieron 5 JSONs malformados en tests de controllers y se añadió handler `400` para JSON inválido.
