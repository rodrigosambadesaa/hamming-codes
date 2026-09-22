# Hamming Codes

Herramienta Java para construir, comprobar y corregir códigos Hamming usando `BigInteger`.

## Características

- Proyecto Java importable directamente en Eclipse.
- Codificación Hamming con paridad par o impar.
- Soporte de palabras Hamming estándar y acortadas.
- Detección y corrección de errores de un bit mediante síndrome.
- Bits de paridad en las posiciones 1, 2, 4, 8, ... contando de izquierda a derecha.
- Entrada numérica mediante `BigInteger`, sin depender de los límites de `int` o `long`.
- Interfaz de consola interactiva y modo por argumentos.
- Pruebas automatizadas con JUnit 5.
- Integración continua con GitHub Actions.

## Requisitos

- Java 17 o superior.
- Maven 3.9+ para ejecutar las pruebas desde terminal.

## Eclipse

1. Abra `File > Import > Existing Projects into Workspace`.
2. Seleccione la carpeta del repositorio.
3. Eclipse reconocerá `.project` y `.classpath`.
4. Ejecute `src/main/java/es/rodrigosambade/hamming/Main.java` como **Java Application**.

También puede importarse como **Existing Maven Project**.

## Uso por terminal

Ejecutar las pruebas:

```bash
mvn test
```

Abrir la interfaz interactiva:

```bash
mvn -q exec:java
```

Codificar un entero no negativo:

```bash
java -cp target/classes es.rodrigosambade.hamming.Main encode \
  --data <entero-no-negativo> \
  --bits <numero-de-bits-de-datos> \
  --parity even
```

Analizar y, cuando proceda, corregir una palabra binaria:

```bash
java -cp target/classes es.rodrigosambade.hamming.Main check \
  --code <palabra-binaria> \
  --parity odd
```

Los valores admitidos para `--parity` son `even`/`par` y `odd`/`impar`.

## Convención de posiciones

La posición 1 corresponde al bit situado más a la izquierda. Las posiciones que son potencias de dos se reservan para bits de paridad.

## Precisión y límites

Los datos numéricos se representan con `BigInteger`, por lo que no existe un límite numérico equivalente al de `int` o `long`. El tamaño práctico de una palabra queda limitado por la memoria disponible y por el máximo tamaño direccionable de las estructuras usadas por la JVM.

## Corrección de errores

El código Hamming clásico permite corregir de forma fiable un único error de bit. Sin un bit de paridad global adicional, una palabra con varios bits alterados puede producir un síndrome compatible con un error simple, por lo que no debe interpretarse como corrección fiable de errores múltiples.
