# FarmaEtiquetasJavaSwing

Projeto Java (Maven) com **Swing** + **PDFBox** + **PostgreSQL JDBC** para gerar:
- **Etiqueta de Posologia**
- **Etiqueta por Pedido** (consulta no banco e gera PDF)

## Requisitos
- JDK 21
- Maven 3.8+
- Colocar `logo.jpg` em `src/main/resources/logo.jpg` (opcional).

## Build
```bash
mvn clean package
```
Gera:
```
target/farma-etiquetas-swing-1.0.0-jar-with-dependencies.jar
```

## Executar
```bash
java -jar target/farma-etiquetas-swing-1.0.0-jar-with-dependencies.jar
```

## Observações
- Credenciais PostgreSQL podem ser alteradas na tela do formulário.
- PDFs são gerados na pasta informada (padrão: `C:/Etiquetas`).
