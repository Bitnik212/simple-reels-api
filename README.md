# 🎬 Simple Reels API

A high-performance, asynchronous Ktor-based microservice designed to fetch metadata and download Instagram Reels with ease. Featuring built-in proxy rotation and S3 storage integration.

[![Ktor](https://img.shields.io/badge/Ktor-2.3.x-blueviolet?style=for-the-badge&logo=ktor)](https://ktor.io/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.x-blue?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

---

## ✨ Features

- **🚀 Ktor Powered**: Fully asynchronous and lightweight.
- **🔄 Proxy Rotation**: Integrated with [ProxyLine](https://proxyline.net/) for seamless request distribution and anti-bot evasion.
- **📦 S3 Storage**: Automatically uploads downloaded Reels to your favorite S3-compatible storage.
- **🔍 Reel Metadata**: Extract detailed JSON metadata for any Instagram Reel.
- **🛠️ Easy Configuration**: Environment-variable driven configuration for S3 and ProxyLine.
- **📄 OpenAPI/Swagger**: Built-in interactive documentation.

---

## 🛠️ Tech Stack

- **Framework**: [Ktor](https://ktor.io/)
- **DI**: [Koin](https://insert-koin.io/)
- **Serialization**: [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)
- **Client**: [Ktor Client](https://ktor.io/docs/client.html) with OkHttp engine
- **Database**: [Exposed](https://github.com/JetBrains/Exposed) (configured)

---

## 🚀 Getting Started

### Prerequisites

- JDK 17+
- Docker (optional, for containerized deployment)
- S3-compatible storage (e.g., AWS S3, Minio, Yandex Object Storage)
- ProxyLine API Key (optional, for proxy rotation)

### Configuration

The service is configured via environment variables. Create a `.env` file or export them:

| Variable | Description | Default |
|----------|-------------|---------|
| `S3_BUCKET` | Your S3 bucket name | **Required** |
| `S3_ENDPOINT_URL` | S3 endpoint (e.g., `https://storage.yandexcloud.net`) | **Required** |
| `S3_REGION` | S3 region | `ru-central1` |
| `S3_ACCESS_KEY` | S3 Access Key | **Required** |
| `S3_SECRET_KEY` | S3 Secret Key | **Required** |
| `PROXYLINE_API_KEY` | Your ProxyLine API Key | **Required** |
| `PROXYLINE_TAG` | Tag to filter proxies in ProxyLine | `myreels` |

### Running the App

```bash
# Clone the repository
git clone https://github.com/Bitnik212/simple-reels-api.git
cd simple-reels-api

# Run the server
./gradlew run
```

---

## 📖 API Documentation

The service provides a Swagger UI for easy testing:
- **Swagger UI**: `http://localhost:8080/docs`
- **OpenAPI Spec**: `http://localhost:8080/docs/documentation.yaml`

### Endpoints

#### 1. Get Reel Info
Returns metadata for a specific Reel.

- **URL**: `GET /reels/{reel_id}`
- **Response**: `200 OK` (JSON)

#### 2. Download Reel
Downloads the Reel and stores it in S3.

- **URL**: `POST /reels/{reel_id}`
- **Parameters**:
    - `redirect` (Optional, default: `true`): If `true`, redirects to the S3 URL. If `false`, returns JSON with the URL.
- **Response**: `302 Found` or `200 OK` (JSON)

---

## 🐳 Docker Support

Build and run using Docker:

```bash
./gradlew buildImage
docker run -p 8080:8080 \
  -e S3_BUCKET=my-bucket \
  -e S3_ACCESS_KEY=xxx \
  ... simple-reels-api
```

---

## 🤝 Contributing

Feel free to open issues or submit pull requests. All contributions are welcome!

---

## 📜 License

Distributed under the MIT License. See `LICENSE` for more information.

---
Created with ❤️ by [BittMoe](https://moe.bitt)

