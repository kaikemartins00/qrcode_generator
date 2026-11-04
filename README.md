# 📱 QR Code Generator API

API REST que recebe um texto ou link, gera um **QR Code** em imagem PNG, armazena o arquivo em um bucket do **AWS S3** e devolve a URL pública para acesso imediato.

Projeto desenvolvido com **Java 21** e **Spring Boot**, organizado no padrão **Ports & Adapters (Arquitetura Hexagonal)** e pronto para rodar em **Docker**.

---

## 🧭 Sumário

- [Como funciona](#-como-funciona)
- [Tecnologias](#-tecnologias)
- [Arquitetura](#-arquitetura)
- [Estrutura do projeto](#-estrutura-do-projeto)
- [Pré-requisitos](#-pré-requisitos)
- [Configuração](#-configuração)
- [Como executar](#-como-executar)
- [Documentação da API](#-documentação-da-api)
- [Aprendizados](#-aprendizados)
- [Autor](#-autor)

---

## ⚙️ Como funciona

```
Cliente ──POST /qrcode──▶ Controller ──▶ Service ──▶ ZXing (gera o QR Code)
                                            │
                                            ▼
                                       StoragePort
                                            │
                                            ▼
                                    S3StorageAdapter ──▶ AWS S3
                                            │
Cliente ◀────────── { "url": "..." } ◀──────┘
```

1. O cliente envia um texto (ou link) via `POST /qrcode`.
2. O serviço gera a imagem do QR Code usando a biblioteca **ZXing**.
3. A imagem é enviada ao **S3** por meio da interface `StoragePort`.
4. A API retorna a URL pública do arquivo gerado.

---

## 🛠️ Tecnologias

| Tecnologia | Uso |
|---|---|
| **Java 21** | Linguagem principal |
| **Spring Boot** | Framework da API REST |
| **ZXing** | Geração do QR Code |
| **AWS S3 (SDK v2)** | Armazenamento das imagens |
| **Maven** | Gerenciamento de dependências e build |
| **Docker** | Containerização (build multi-stage) |
| **Postman** | Testes da API |

---

## 🧱 Arquitetura

O projeto segue o padrão **Ports & Adapters**: a regra de negócio depende de uma **interface** (`StoragePort`), e não de uma implementação concreta de armazenamento.

Isso significa que trocar o S3 por outro provedor (Google Cloud Storage, Azure Blob, disco local etc.) exige apenas criar um novo adapter, **sem alterar o serviço que gera o QR Code**.

- **Port:** `StoragePort`: contrato de upload de arquivos.
- **Adapter:** `S3StorageAdapter`: implementação do contrato usando AWS S3.
- **Service:** `QrCodeGeneratorService`: gera o QR Code e delega o armazenamento ao port.

---

## 📂 Estrutura do projeto

```
src/main/java/com/martinskaike/qrcode
├── controller
│   └── QrCodeController.java
├── dto
│   ├── QrCodeGenerateRequest.java
│   └── QrCodeGenerateResponse.java
├── infrastructure
│   └── S3StorageAdapter.java
├── ports
│   └── StoragePort.java
├── service
│   └── QrCodeGeneratorService.java
└── QrcodeApplication.java

src/main/resources
└── application.yaml
```

---

## ✅ Pré-requisitos

- [Java 21](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/) (ou use o `mvnw` incluso)
- [Docker](https://www.docker.com/) (opcional, para rodar em container)
- Uma conta na **AWS** com:
  - Um **bucket S3** criado
  - Um usuário IAM com permissão de escrita (`s3:PutObject`) no bucket
  - **Access Key ID** e **Secret Access Key**

---

## 🔧 Configuração

A aplicação lê as configurações do S3 por **variáveis de ambiente** (`application.yaml`):

```yaml
server:
  port: 8081

aws:
  s3:
    region: ${AWS_REGION}
    bucket-name: ${AWS_BUCKET_NAME}
```

| Variável | Descrição | Exemplo |
|---|---|---|
| `AWS_REGION` | Região do bucket | `us-east-2` |
| `AWS_BUCKET_NAME` | Nome do bucket S3 | `meu-bucket-qrcode` |
| `AWS_ACCESS_KEY_ID` | Chave de acesso da AWS | `AKIA...` |
| `AWS_SECRET_ACCESS_KEY` | Chave secreta da AWS | `********` |

> ⚠️ **Nunca** versione suas credenciais da AWS. Mantenha-as em um arquivo `.env` (já listado no `.gitignore`) ou em variáveis de ambiente.

> 💡 Para que as imagens possam ser abertas pela URL retornada, o bucket precisa permitir leitura pública dos objetos (ajuste a *bucket policy* e as configurações de *Block Public Access* conforme a sua necessidade).

---

## ▶️ Como executar

### Clonando o repositório

```bash
git clone https://github.com/SEU-USUARIO/SEU-REPOSITORIO.git
cd SEU-REPOSITORIO
```

### Opção 1: Rodando localmente com Maven

```bash
# Linux / macOS
export AWS_REGION=us-east-2
export AWS_BUCKET_NAME=meu-bucket-qrcode
export AWS_ACCESS_KEY_ID=sua-access-key
export AWS_SECRET_ACCESS_KEY=sua-secret-key

./mvnw spring-boot:run
```

```powershell
# Windows (PowerShell)
$env:AWS_REGION="us-east-2"
$env:AWS_BUCKET_NAME="meu-bucket-qrcode"
$env:AWS_ACCESS_KEY_ID="sua-access-key"
$env:AWS_SECRET_ACCESS_KEY="sua-secret-key"

.\mvnw.cmd spring-boot:run
```

### Opção 2: Rodando com Docker

O `Dockerfile` usa **build multi-stage**: compila o projeto com Maven e executa o `.jar` em uma imagem JRE enxuta.

```bash
# Build da imagem
docker build -t qrcode-api .

# Execução do container
docker run -p 8081:8081 \
  -e AWS_REGION=us-east-2 \
  -e AWS_BUCKET_NAME=meu-bucket-qrcode \
  -e AWS_ACCESS_KEY_ID=sua-access-key \
  -e AWS_SECRET_ACCESS_KEY=sua-secret-key \
  qrcode-api
```

A API ficará disponível em `http://localhost:8081`.

---

## 📖 Documentação da API

### `POST /qrcode`

Gera um QR Code a partir de um texto e retorna a URL da imagem armazenada no S3.

**Request**

```http
POST http://localhost:8081/qrcode
Content-Type: application/json
```

```json
{
  "text": "https://github.com/seu-usuario"
}
```

**Response** `200 OK`

```json
{
  "url": "https://meu-bucket-qrcode.s3.us-east-2.amazonaws.com/6f1e826b-31de-486f-b91e-16912c1e5a1c"
}
```

**Exemplo com cURL**

```bash
curl -X POST http://localhost:8081/qrcode \
  -H "Content-Type: application/json" \
  -d '{"text": "https://github.com/seu-usuario"}'
```

Abra a URL retornada no navegador para visualizar o QR Code gerado. 🎉

---

## 📚 Aprendizados

- Integração de uma aplicação Spring Boot com o **AWS S3** (SDK v2)
- Gerenciamento de credenciais e configuração por **variáveis de ambiente**
- Aplicação de **Ports & Adapters** para desacoplar regra de negócio e infraestrutura
- Criação de **Dockerfile multi-stage** para imagens menores e builds reproduzíveis
- Geração de imagens em memória com **ZXing**

---

## 🚧 Próximos passos

- [ ] Parâmetros opcionais (tamanho e cor do QR Code)
- [ ] Testes unitários e de integração
- [ ] Documentação interativa com Swagger/OpenAPI
- [ ] Deploy em nuvem (ECS, EC2 ou similar)

---

## 👨‍💻 Autor

**Kaike Martins**

[![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/kaikemartins08)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/SEU-PERFIL)

---

⭐ Se este projeto te ajudou ou te inspirou, deixe uma estrela no repositório!
