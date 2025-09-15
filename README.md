# AREPT4 — Dockerized MicroSpringBoot

A minimal “Apache-like” **HTTP server in Java** with a tiny **IoC micro-framework** (annotations-based) to expose endpoints from **POJOs**, **containerized with Docker** and deployable to **AWS EC2**.  
Serves **HTML/CSS/JS/PNG/JPG** from the classpath (`src/main/resources/static`), routes **`/app/...`** to annotated controllers, supports **concurrency** (thread pool) and **graceful shutdown**.

---
## Architecture

### High-level overview
- **Custom HTTP/1.1 server** that:
  - Serves static assets from the classpath (`src/main/resources/static`).
  - Routes requests under **`/app/...`** to annotated controllers.
- **IoC micro-framework** via annotations (`@RestController`, `@GetMapping`, `@RequestParam`) using reflection.
- **Concurrency** with a thread pool (`ExecutorService`).
- **Graceful shutdown**: closes the `ServerSocket` and waits for in-flight tasks.
- **Config via env**: `PORT`.

### Main components

#### `RestServiceApplication`
- Bootstraps the server: discovers controllers (by package), sets the static folder, reads `PORT` from env, and calls `HttpServer.runServer(...)`.

#### `HttpServer`
- Accepts TCP sockets on `PORT`.
- **Router**:
  - Path starts with `/app` → invoke the corresponding controller method.
  - Otherwise → serve static file from classpath (fallback to filesystem in Docker).
- Builds HTTP responses with proper `Content-Type`, `Content-Length`, and `Connection: close`.
- Handles **timeouts** (`408` when applicable), **404** for missing routes/files, **500** for handler errors.

#### Annotations & IoC registry
- `@RestController` (class), `@GetMapping("/path")` (method), `@RequestParam(value, defaultValue)` (parameter).
- Route map `Map<String, Method>`: `"/path"` → handler method (recommendation: concurrent map).

#### Static file resolver
Resolution order:
1. `ClassLoader.getResourceAsStream(resourcePath)`
2. `ClassLoader.getResourceAsStream("/" + resourcePath)`
3. Filesystem fallback (e.g., `/usrapp/bin/classes/<resourcePath>` inside the container)

Minimal MIME map included (`html`, `css`, `js`, `png`, `jpg`, `jpeg`; extend with `svg`, `ico`, etc.).

## Getting Started

### Prerequisites

```
java -version          # Java 17+ (JDK)
mvn -v                 # Maven 3.9+
docker --version       # Docker 24+
```

### Installing

A step by step series of examples that tell you how to get a development env running

**1) Clone and build**

```
git clone https://github.com/JaiderVargas05/AREPT4-Docker.git
cd AREPT4-Docker
mvn -q -DskipTests package
```

**2) Run locally (without Docker)**

```
# Unix/macOS
export PORT=6000
java -cp target/classes:target/dependency/* edu.eci.arep.docker.RestServiceApplication

# Windows (PowerShell)
# $env:PORT=6000
# java -cp "target/classes;target/dependency/*" edu.eci.arep.docker.RestServiceApplication
```

**3) Smoke test**

```
curl -I http://localhost:6000/
curl -I http://localhost:6000/james.jpg
curl "http://localhost:6000/app/hello?name=Jaider"
```

**4) Build Docker image**

```
docker build -t ditovargas2005/dockeraws-arep:latest .
```

**5) Run the container**

```
docker run -d -p 34000:6000 --name firstdockercontainer ditovargas2005/dockeraws-arep:latest
```

**6) Verify the container**

<img width="1559" height="343" alt="image" src="https://github.com/user-attachments/assets/10a3ab58-c6b4-405d-85b3-04755e38a455" />

<img width="452" height="148" alt="image" src="https://github.com/user-attachments/assets/8c487b98-43b3-42ed-b4d7-ac1456f19fc4" />

## Running the tests

This suite boots the server **in-memory** before executing assertions and validates both **static assets** and **IoC endpoints**.

> **Requirements for tests**
> - Port **9000** must be free (the suite binds to it).
> - Static files present in `src/main/resources/static`: `index.html`, `styles.css`, `code.js`, `james.jpg`.

### Run all tests

```bash
mvn test
```

> A `@BeforeAll` hook starts the server on a daemon thread, waits up to **5 seconds** for `GET /` to return a status `>= 200`, and fails with `The server is not running` if the server doesn’t start in time.

### Run a specific test/class

```bash
# Only the DockerTest class
mvn -Dtest=DockerTest test
```

---

### End-to-end test breakdown

What each test validates and why:

1) **`serverIndexConnectionOK`** — server up & HTML index MIME  
   - Ensures `GET /` returns **200** and `Content-Type: text/html`.

2) **`servirIndexHTMLOK`** — exact `index.html` content  
   - Compares the **response body** of `GET /index.html` with the file on disk  
     (`src/main/resources/static/index.html`), ignoring whitespace.

3) **`serverCSSOK`** — CSS served with correct MIME and exact bytes  
   - Verifies **200** and `Content-Type: text/css`.  
   - Compares the body with `static/styles.css` (whitespace-insensitive).

4) **`serverJSOK`** — JS served with correct MIME and exact bytes  
   - Verifies **200** and `Content-Type: application/javascript`.  
   - Compares the body with `static/code.js` (whitespace-insensitive).

5) **`serverJPGOK`** — JPEG served and not empty  
   - Verifies **200** and `Content-Type: image/jpeg`.  
   - Asserts response bytes and the file `static/james.jpg` are **non-empty**.

6) **`fileNotFound`** — 404 for non-existent paths  
   - `GET /eci.com` must respond with **404**.

7) **`greetingDefaultOK`** — IoC endpoint with `@RequestParam` default value  
   - `GET /app/greeting` → **200**, `Content-Type: text/plain`, body **"Hello, World!"**.

8) **`greetingWithParamOK`** — IoC endpoint with query parameter  
   - `GET /app/greeting?name=Jaider` → **200**, `Content-Type: text/plain`, body contains **"Jaider"**.

> Internally, the suite uses `HttpConnection` helpers to:
> - `stablishConnection(method, path)` → open `HttpURLConnection` and read status/headers.  
> - `makeRequest(method, path)` → read body as `String`.  
> - `makeRequestBytes(method, path)` → read body as `byte[]`.

## Deployment


**A) Publish to Docker Hub**

```
docker login
docker build -t ditovargas2005/dockeraws-arep:latest .
docker push ditovargas2005/dockeraws-arep:latest
```

**B) AWS EC2 prepare host**

```
# Install Docker
sudo yum update -y
sudo yum install docker
sudo service docker start
sudo usermod -a -G docker ec2-user
```

**C) Security Group**  
Open **Inbound TCP 42000** (0.0.0.0/0 for demo).

**D) Run on EC2**

```
docker pull ditovargas2005/dockeraws-arep:latest
docker run -d -p 42000:6000 --name firstdockerimageaws ditovargas2005/dockeraws-arep:latest
```

**E) Test deployment**

<img width="883" height="187" alt="image" src="https://github.com/user-attachments/assets/84e8d833-d13c-4969-a277-0f11971925ae" />

<img width="1172" height="666" alt="image" src="https://github.com/user-attachments/assets/bcee4f7c-5f4e-4c71-9fef-7e3327844b1e" />


**Graceful shutdown**

```
docker stop firstdockerimageaws
```

> _Screenshot placeholder (EC2 running)_  
<img width="1199" height="163" alt="image" src="https://github.com/user-attachments/assets/aa66b082-4e16-42a6-b831-293a70ead2f7" />


---

## Built With

* [Java 17 (OpenJDK)](https://openjdk.org/) — Runtime
* [Maven](https://maven.apache.org/) — Dependency Management
* [Docker](https://www.docker.com/) — Packaging & Runtime
* [AWS EC2](https://aws.amazon.com/ec2/) — Hosting

---

## Versioning

We use Github for versioning. For the versions available, see the [tags on this repository](https://github.com/JaiderVargas05/AREPT4-Docker/tags). 

---

## Authors

* **Jaider Vargas** — *Initial work* — [JaiderVargas05](https://github.com/JaiderVargas05)


---


**Overview**
- `HttpServer` — Listens on `PORT`, parses request line & headers, and routes:
  - `/app/...` → invokes controller method annotated with `@GetMapping` (`@RestController` class)
  - otherwise → serves static files from classpath (`/static`) or fallback path in Docker
- **IoC** via reflection: registry `Map<String, Method>` mapping `"/path"` → handler method
- **Concurrency**: thread pool (`ExecutorService`)
- **Graceful shutdown**: closes `ServerSocket` and waits for tasks

