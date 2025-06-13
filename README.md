# code-with-quarkus

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/code-with-quarkus-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)

```
code-with-quarkus-clean
├─ .dockerignore
├─ .mvn
│  └─ wrapper
│     ├─ maven-wrapper.jar
│     ├─ maven-wrapper.properties
│     └─ MavenWrapperDownloader.java
├─ .qodo
├─ docker-compose.yml
├─ dockerfile
├─ mvnw
├─ mvnw.cmd
├─ pom.xml
├─ README.md
└─ src
   ├─ main
   │  ├─ docker
   │  │  ├─ Dockerfile.jvm
   │  │  ├─ Dockerfile.legacy-jar
   │  │  ├─ Dockerfile.native
   │  │  └─ Dockerfile.native-micro
   │  ├─ java
   │  │  └─ org
   │  │     └─ acme
   │  │        ├─ application
   │  │        │  ├─ ports
   │  │        │  │  ├─ input
   │  │        │  │  │  ├─ chatbot
   │  │        │  │  │  ├─ minio
   │  │        │  │  │  │  └─ MinioUseCase.java
   │  │        │  │  │  └─ textextraction
   │  │        │  │  │     └─ TextExtractionUseCase.java
   │  │        │  │  └─ output
   │  │        │  │     ├─ chatbot
   │  │        │  │     ├─ common
   │  │        │  │     │  └─ TextCompletionPort.java
   │  │        │  │     ├─ minio
   │  │        │  │     │  └─ MinioPort.java
   │  │        │  │     └─ textextraction
   │  │        │  │        └─ DocumentProcessorPort.java
   │  │        │  └─ services
   │  │        │     ├─ chatbot
   │  │        │     │  ├─ postmancollection
   │  │        │     │  │  └─ PostmanCollectionAdapter.java
   │  │        │     │  ├─ testfuctionalapi
   │  │        │     │  │  └─ ProcessContextApiAdapter.java
   │  │        │     │  ├─ testfuncional
   │  │        │     │  │  └─ ProcessContextAdapter.java
   │  │        │     │  └─ testfunctionalui
   │  │        │     │     └─ ProcessContextUiAdapter.java
   │  │        │     ├─ minio
   │  │        │     │  └─ MinioService.java
   │  │        │     └─ textextraction
   │  │        │        └─ TextExtractionService.java
   │  │        ├─ domain
   │  │        │  ├─ chatbot
   │  │        │  │  └─ model
   │  │        │  │     ├─ conversation
   │  │        │  │     │  ├─ Conversation.java
   │  │        │  │     │  ├─ Message.java
   │  │        │  │     │  └─ ValidationMessage.java
   │  │        │  │     ├─ testfuctionalapi
   │  │        │  │     ├─ testfunctional
   │  │        │  │     └─ testfunctionalui
   │  │        │  ├─ minio
   │  │        │  │  ├─ exception
   │  │        │  │  │  └─ MinioProcessingException.java
   │  │        │  │  └─ model
   │  │        │  │     └─ FileUploadResponse.java
   │  │        │  └─ textextraction
   │  │        │     ├─ exception
   │  │        │     │  └─ TextExtractionException.java
   │  │        │     └─ model
   │  │        │        ├─ DocumentProcessingRequest.java
   │  │        │        ├─ NewDocumentProcessingRequest.java
   │  │        │        ├─ SourceRequest.java
   │  │        │        ├─ SourceResponse.java
   │  │        │        └─ TextExtractionResult.java
   │  │        ├─ GreetingResource.java
   │  │        ├─ infrastructure
   │  │        │  ├─ adapters
   │  │        │  │  ├─ input
   │  │        │  │  │  ├─ minio
   │  │        │  │  │  │  └─ rest
   │  │        │  │  │  │     └─ MinioController.java
   │  │        │  │  │  ├─ rest
   │  │        │  │  │  └─ textextraction
   │  │        │  │  │     └─ rest
   │  │        │  │  │        └─ TextExtractionController.java
   │  │        │  │  └─ output
   │  │        │  │     ├─ chatbot
   │  │        │  │     │  ├─ mongo
   │  │        │  │     │  └─ tokens
   │  │        │  │     ├─ common
   │  │        │  │     │  ├─ gemini
   │  │        │  │     │  └─ openAi
   │  │        │  │     ├─ minio
   │  │        │  │     │  └─ MinioRepository.java
   │  │        │  │     └─ textextraction
   │  │        │  │        ├─ config
   │  │        │  │        ├─ handlers
   │  │        │  │        └─ utils
   │  │        │  └─ providers
   │  │        │     ├─ qualifers
   │  │        │     │  ├─ AIProviderQualifiers.java
   │  │        │     │  └─ DocumentProcessorQualifiers.java
   │  │        │     ├─ TesseractPortProvider.java
   │  │        │     └─ TextCompletionPortProvider.java
   │  │        └─ ports
   │  │           ├─ conversation_ports
   │  │           │  ├─ ConversationManagementPort.java
   │  │           │  └─ NameHistoryGeneratorPort.java
   │  │           ├─ output_ports
   │  │           │  ├─ LLMPort.java
   │  │           │  └─ MongoDBPort.java
   │  │           └─ test_cases_ports
   │  │              ├─ CountTokensPort.java
   │  │              ├─ PostmanCollectionPort.java
   │  │              ├─ ProcessContextApiPort.java
   │  │              ├─ ProcessContextPort.java
   │  │              └─ ProcessContextUiPort.java
   │  └─ resources
   │     └─ application.properties
   └─ test
      └─ java
         └─ org
            └─ acme
               ├─ GreetingResourceIT.java
               └─ GreetingResourceTest.java

```