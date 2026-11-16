<div align="center">

# 🔐 FantAsta — Auth Service

**Microservizio di autenticazione per la piattaforma FantAsta**







</div>

***

## 📖 Descrizione

`auth-service` è il microservizio responsabile della **registrazione**, **autenticazione** e **gestione degli utenti** all'interno della piattaforma **FantAsta**. Espone un'API REST stateless protetta con **JWT (JSON Web Token)**, utilizza **Spring Security** per la gestione della sicurezza e persiste i dati su un database **PostgreSQL**.

***

## 🏗️ Architettura

Il servizio segue un'architettura a livelli standard:

```
src/
└── main/
    └── java/com/fantasta/auth_service/
        ├── controller/       # REST Controllers (UtenteController, Ping)
        ├── service/          # Business logic (AuthService)
        ├── repository/       # JPA Repositories
        ├── model/            # Entità JPA (Utente → tabella app_users)
        ├── dto/              # Request/Response DTO
        ├── security/         # JWT Provider, Spring Security Config
        └── exception/        # Eccezioni custom
```

***

## 🚀 API Endpoints

Base path: `/api/user`

| Metodo | Endpoint | Descrizione | Auth richiesta |
|--------|----------|-------------|----------------|
| `POST` | `/register` | Registrazione nuovo utente | ❌ |
| `POST` | `/login` | Login e ottenimento JWT | ❌ |
| `GET`  | `/me` | Dati dell'utente corrente | ✅ `Bearer <token>` |
| `GET`  | `/api/auth/ping` | Health check del servizio | ❌ |

### Esempi

#### `POST /api/user/register`

**Request:**
```json
{
  "email": "mario.rossi@example.com",
  "name": "Mario",
  "surname": "Rossi",
  "password": "password123"
}
```

**Response `200 OK`:**
```json
{
  "id": 1,
  "email": "mario.rossi@example.com",
  "name": "Mario",
  "surname": "Rossi"
}
```

**Response `409 Conflict`:** Email già registrata.

***

#### `POST /api/user/login`

**Request:**
```json
{
  "email": "mario.rossi@example.com",
  "password": "password123"
}
```

**Response `200 OK`:**
```json
{
  "accessToken": "<jwt_token>",
  "userId": 1,
  "email": "mario.rossi@example.com",
  "name": "Mario",
  "surname": "Rossi"
}
```

**Response `401 Unauthorized`:** Credenziali errate.

***

#### `GET /api/user/me`

**Header:**
```
Authorization: Bearer <jwt_token>
```

**Response `200 OK`:**
```json
{
  "id": 1,
  "email": "mario.rossi@example.com",
  "name": "Mario",
  "surname": "Rossi"
}
```

**Response `401 Unauthorized`:** Credenziali Bearer assenti, scadute o non valide.

**Response `404 Not Found`:** Utente non trovato.

***

## 🗄️ Modello Dati

**Tabella:** `app_users`

| Colonna | Tipo | Vincoli |
|---------|------|---------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT |
| `email` | `VARCHAR` | NOT NULL, UNIQUE |
| `name` | `VARCHAR` | NOT NULL |
| `surname` | `VARCHAR` | NOT NULL |
| `password_hash` | `VARCHAR` | NOT NULL |
| `created_at` | `TIMESTAMP` | NOT NULL |

***

## ⚙️ Configurazione

Crea un file `src/main/resources/application.properties` (o `application.yml`) con le seguenti variabili:

```properties
# Server
server.port=7070

# Database PostgreSQL
spring.datasource.url=jdbc:postgresql://<host>:<port>/<database>
spring.datasource.username=<username>
spring.datasource.password=<password>
spring.jpa.hibernate.ddl-auto=update

# JWT
jwt.secret=<your-secret-key-base64>
jwt.expiration-ms=86400000
# Origini del frontend, separate da virgole
auth.cors.allowed-origins=http://localhost:4200
# Amministratori PlayerService, separati da virgola. Il valore predefinito è vuoto.
auth.admin-emails=admin@example.com
```

Il segreto deve contenere almeno 32 byte casuali codificati in Base64. Configura lo stesso valore su tutte le istanze di AuthService. In ambiente usa `JWT_SECRET` e `JWT_EXPIRATIONMS`. Il valore di `jwt.expiration-ms` è espresso in millisecondi e ha precedenza sul nome precedente `jwt.expiration`, ancora accettato per la migrazione.

Se il servizio proviene dalla versione che generava la chiave a ogni avvio, gli utenti devono effettuare nuovamente il login. Per consentire il frontend di produzione, imposta `auth.cors.allowed-origins` o `AUTH_CORS_ALLOWEDORIGINS` con le origini previste.

Per concedere le operazioni amministrative di PlayerService, configura esplicitamente `AUTH_ADMINEMAILS` con gli indirizzi email autorizzati. Gli utenti non presenti in questa lista restano `USER`, inclusi gli account già esistenti.

> ⚠️ **Non committare mai** il file `application.properties` contenente credenziali reali. Usa variabili d'ambiente in produzione.

***

## 🛠️ Tecnologie

| Tecnologia | Versione | Utilizzo |
|------------|----------|----------|
| Java | 17 | Linguaggio principale |
| Spring Boot | 3.4.5 | Framework applicativo |
| Spring Security | — | Gestione sicurezza e filtri |
| Spring Data JPA | — | ORM e accesso al database |
| PostgreSQL | — | Database relazionale |
| JJWT | 0.12.6 | Generazione e validazione JWT |
| Lombok | — | Riduzione boilerplate |
| Docker | — | Containerizzazione |

***

## 🐳 Docker

### Build & Run manuale

```bash
# Build dell'immagine
docker build -t fantasta-auth-service .

# Avvio del container
docker run -p 7070:7070 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://<host>/<db> \
  -e SPRING_DATASOURCE_USERNAME=<user> \
  -e SPRING_DATASOURCE_PASSWORD=<password> \
  -e JWT_EXPIRATIONMS=86400000 \
  -e JWT_SECRET=<secret> \
  fantasta-auth-service
```

Il `Dockerfile` utilizza un **multi-stage build**:
1. **Stage 1 (`builder`)**: compila il progetto con Maven 3.9 + JDK 17
2. **Stage 2**: esegue il JAR con un'immagine minimale `eclipse-temurin:17-jre-alpine`

***

## 🧑‍💻 Sviluppo locale

### Prerequisiti

- Java 17+
- Maven 3.9+
- PostgreSQL in esecuzione

### Avvio

```bash
# Clona il repository
git clone https://github.com/francesc05Cervera/BackEnd-FantAsta-AuthService.git
cd BackEnd-FantAsta-AuthService

# Configura application.properties (vedi sezione Configurazione)

# Avvia con Maven Wrapper
./mvnw spring-boot:run
```

Il servizio sarà disponibile su `http://localhost:7070`.

### Test

```bash
./mvnw test
```

***

## 📁 Struttura del Progetto

```
BackEnd-FantAsta-AuthService/
├── src/
│   ├── main/
│   │   ├── java/com/fantasta/auth_service/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── Dockerfile
├── pom.xml
├── mvnw
└── mvnw.cmd
```

***

## 👤 Autore

**Francesco Cervera** — [@francesc05Cervera](https://github.com/francesc05Cervera)

***

<div align="center">
  <sub>Parte del progetto <strong>FantAsta</strong> — Backend Microservices</sub>
</div>
