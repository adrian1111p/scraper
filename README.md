# 🗂️ Scraper - File Tree and Content Merger

This Java Spring Boot CLI tool recursively scans a given input directory, filters files based on configured rules, builds a beautiful tree-like folder structure using Unicode box-drawing characters (├──, └──), and merges the contents of valid files into a single output file.

---

## 📌 Features

- ✅ Recursive directory scanning
- ✅ Include / Exclude file extensions (e.g., `.java`, `.xml`, `.html`, etc.)
- ✅ Skip folders like `node_modules`, `target`, `.git`, etc.
- ✅ Exclude patterns like `*.lock.json`, `docker-compose.override.yml`, etc.
- ✅ Pretty `tree` structure output with file sizes and formatting
- ✅ Content merging with clear file headers
- ✅ Colorized CLI output (only in terminal)
- ✅ Spring Boot config via `application.yml`
- ✅ Fully tested with JUnit 5

---

## 📁 Example Output

```text
📁 D:/Site/brain/freesurfer-dev
├── 📁 Assets
│   ├── 📄 SliceShader.shader (4.5 KB)
│   └── 📄 VolumeShader.shader (3.2 KB)
├── 📁 Scripts
│   └── 📄 Loader.cs (7.8 KB)
└── 📄 README.md (1.1 KB)
```

---

## ⚙️ Configuration

All configurable paths, file extensions, and patterns are managed in `src/main/resources/application.yml`:

```yaml
scraper:
  input-folder: D:/Site/brain/freesurfer-dev
  output-file: D:/Site/brain/freesurfer-dev.txt
  include-extensions:
    - java
    - cs
    - shader
    - js
    - html
    - py
    - xml
  exclude-folders:
    - target
    - node_modules
    - .git
  exclude-file-patterns:
    - "*lock.json"
    - "docker-compose.override.yml"
  exclude-pattern-case-sensitive: false
```

---

## 🚀 Usage

### 1. 🧪 Run Locally

```bash
cd D:\Site\scraper
./mvnw spring-boot:run
```

On startup, it will:
- Generate a file tree
- Merge the content
- Save results to `output-file`

### 2. 🧪 Run Unit Test

```bash
./mvnw test
```

---

## 📦 Build JAR

```bash
./mvnw clean package
java -jar target/scraper-0.0.1-SNAPSHOT.jar
```

---

## 📂 Project Structure

```
src/
├── main/
│   ├── java/com/example/scraper/
│   │   ├── config/           # YML to POJO
│   │   ├── model/            # FileNode model
│   │   ├── service/          # Core logic: tree + merging
│   │   ├── util/             # TreeGenerator
│   │   └── ScraperApplication.java
│   └── resources/
│       └── application.yml   # Configurable paths + rules
└── test/
    └── java/com/example/scraper/
        └── ScraperApplicationTest.java
```

---

## ✅ Requirements

- Java 21
- Maven
- Spring Boot 3.5+
- IntelliJ or VS Code
- Tested on Windows (D:/ structure)

---

## 🧑‍💻 Author

**Adrian Calin Mihalea**  
Full‑Stack Developer • Cloud Technology Studio Kaiserslautern  
GitHub: [adrian1111p](https://github.com/adrian1111p)

---

## 🔒 License

Licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)