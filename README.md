# Keycloak and Spring Boot OAuth 2.0 and OpenID Connect (OIDC) Authentication

Projeto de estudos

- [Keycloak and Spring Boot OAuth 2.0 and OpenID Connect (OIDC) Authentication](https://medium.com/@nsalexamy/keycloak-and-spring-boot-oauth-2-0-and-openid-connect-oidc-authentication-304e7b511d02)

- [keycloak-spring-react-bff - ORIGINAL](https://github.com/nsalexamy/keycloak-spring-react-bff)

## Executando as aplicações com Docker

Os Dockerfiles usam builds multi-stage e devem ser executados a partir da **raiz do repositório**, pois o contexto de build precisa incluir os módulos `nsa2-gateway` e `nsa2-resource-server`.

### Pré-requisitos

- Docker Engine ou Docker Desktop com BuildKit habilitado.
- No Compose, o `nsa2-resource-server` é iniciado junto e permanece disponível apenas na rede interna; para a execução standalone, ele deve estar disponível na porta `8082`.

### Executar os dois serviços com Docker Compose

A partir da raiz do repositório:

```bash
docker compose up --build
```

Se o Docker CLI estiver disponível somente no WSL2, execute o mesmo comando pelo PowerShell com:

```powershell
wsl.exe -e docker compose up --build
```

O Compose constrói as duas aplicações e publica somente a porta `8080` do gateway. O resource-server é acessado internamente como `nsa2-resource-server:8082`.

```bash
curl http://localhost:8080/resource-server/blocking/1
```

Para visualizar os logs ou encerrar os serviços:

```bash
docker compose logs -f
docker compose down
```

### Construir as imagens individualmente

```bash
docker build -f nsa2-gateway/Dockerfile -t nsa2-gateway:latest .
docker build -f nsa2-resource-server/Dockerfile -t nsa2-resource-server:latest .
```

Se as imagens base já estiverem em cache, os comandos acima não forçam uma atualização. Use `--pull` somente quando o Docker Hub estiver acessível e for desejável atualizar as imagens base. Erros como `no route to host` ao resolver `docker.io` indicam problema de rede, proxy ou mirror do Docker, não erro no código dos Dockerfiles.

### Executar somente o gateway

```bash
docker run --rm --name nsa2-gateway -p 8080:8080 nsa2-gateway:latest
```

O gateway ficará disponível em `http://localhost:8080`. A imagem construída diretamente contém somente o gateway; o comando `docker compose up --build` também constrói e executa o resource-server.

### Executar o gateway com o resource-server no host

Como o valor padrão de `NSA2_RESOURCE_SERVER_URI` é `http://localhost:8082`, dentro do container `localhost` referencia o próprio container. Inicie o resource-server no host:

```powershell
cd nsa2-resource-server
.\mvnw.cmd spring-boot:run
```

Em seguida, no Docker Desktop (Windows/macOS):

```bash
docker run --rm --name nsa2-gateway -p 8080:8080 -e NSA2_RESOURCE_SERVER_URI=http://host.docker.internal:8082 nsa2-gateway:latest
```

No Linux, adicione `--add-host=host.docker.internal:host-gateway` ao comando do container:

```bash
docker run --rm --name nsa2-gateway --add-host=host.docker.internal:host-gateway -p 8080:8080 -e NSA2_RESOURCE_SERVER_URI=http://host.docker.internal:8082 nsa2-gateway:latest
```

Teste o encaminhamento do gateway:

```bash
curl http://localhost:8080/resource-server/blocking/1
```

Como este repositório não inclui um Eureka Server, a execução standalone pode registrar avisos de conexão recusada em `localhost:8761`; isso não impede a inicialização do container. O Compose desativa esse cliente não utilizado.
