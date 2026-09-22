package es.rodrigosambade.hamming;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Interfaz de consola para la calculadora Hamming.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                interactive();
                return;
            }

            switch (args[0].toLowerCase()) {
                case "encode" -> encodeCommand(parseOptions(args));
                case "check", "correct", "decode" -> checkCommand(parseOptions(args));
                case "help", "--help", "-h" -> printHelp();
                default -> {
                    System.err.println("Comando desconocido: " + args[0]);
                    printHelp();
                    System.exit(2);
                }
            }
        } catch (IllegalArgumentException ex) {
            System.err.println("Error: " + ex.getMessage());
            System.exit(2);
        }
    }

    private static void interactive() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("=== Calculadora de códigos Hamming ===");
            System.out.println("1) Codificar datos");
            System.out.println("2) Comprobar/corregir una palabra recibida");
            System.out.print("Opción: ");
            String option = scanner.nextLine().trim();

            switch (option) {
                case "1" -> {
                    System.out.print("Valor decimal no negativo (BigInteger): ");
                    BigInteger data = new BigInteger(scanner.nextLine().trim());
                    System.out.print("Número de bits de datos: ");
                    int bits = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Paridad (par/impar): ");
                    Parity parity = Parity.parse(scanner.nextLine());
                    printEncoding(data, bits, parity);
                }
                case "2" -> {
                    System.out.print("Palabra binaria recibida: ");
                    String code = scanner.nextLine().trim();
                    System.out.print("Paridad (par/impar): ");
                    Parity parity = Parity.parse(scanner.nextLine());
                    printAnalysis(HammingCode.analyze(code, parity));
                }
                default -> throw new IllegalArgumentException("Opción inválida.");
            }
        }
    }

    private static void encodeCommand(Map<String, String> options) {
        String dataText = require(options, "data");
        String bitsText = require(options, "bits");
        String parityText = options.getOrDefault("parity", "even");

        BigInteger data = new BigInteger(dataText);
        int bits = Integer.parseInt(bitsText);
        Parity parity = Parity.parse(parityText);
        printEncoding(data, bits, parity);
    }

    private static void checkCommand(Map<String, String> options) {
        String code = require(options, "code");
        Parity parity = Parity.parse(options.getOrDefault("parity", "even"));
        printAnalysis(HammingCode.analyze(code, parity));
    }

    private static void printEncoding(BigInteger data, int bits, Parity parity) {
        String code = HammingCode.encode(data, bits, parity);
        System.out.println("Dato decimal : " + data);
        System.out.println("Bits de datos: " + leftPad(data.toString(2), bits));
        System.out.println("Paridad      : " + (parity == Parity.EVEN ? "par" : "impar"));
        System.out.println("Código       : " + code);
        System.out.println("Longitud     : " + code.length());
        System.out.println("Bits paridad : " + HammingCode.requiredParityBits(bits));
    }

    private static void printAnalysis(HammingResult result) {
        System.out.println("Recibido       : " + result.received());
        System.out.println("Síndrome       : " + result.syndrome()
                + " (binario " + result.syndrome().toString(2) + ")");
        if (!result.errorDetected()) {
            System.out.println("Estado         : sin error detectable");
        } else if (result.correctable()) {
            System.out.println("Estado         : error de 1 bit compatible con la posición "
                    + result.errorPosition());
            System.out.println("Corregido      : " + result.corrected());
        } else {
            System.out.println("Estado         : síndrome fuera de la palabra; no es corregible"
                    + " de forma fiable como error único");
        }

        if (result.dataBits() != null) {
            System.out.println("Bits de datos  : " + result.dataBits());
            System.out.println("Dato decimal   : " + result.dataValue());
        }
    }

    private static Map<String, String> parseOptions(String[] args) {
        Map<String, String> options = new LinkedHashMap<>();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if (!arg.startsWith("--")) {
                throw new IllegalArgumentException("Opción inválida: " + arg);
            }
            String key = arg.substring(2);
            if (i + 1 >= args.length) {
                throw new IllegalArgumentException("Falta el valor de --" + key);
            }
            options.put(key, args[++i]);
        }
        return options;
    }

    private static String require(Map<String, String> options, String key) {
        String value = options.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Falta --" + key);
        }
        return value;
    }

    private static String leftPad(String value, int width) {
        if (value.length() >= width) {
            return value;
        }
        return "0".repeat(width - value.length()) + value;
    }

    private static void printHelp() {
        System.out.println("""
                Uso:
                  encode --data <entero> --bits <n> [--parity even|odd]
                  check  --code <binario> [--parity even|odd]

                Sin argumentos se abre el menú interactivo.

                Convención: posición 1 = bit más a la izquierda.
                Los bits de paridad están en 1, 2, 4, 8, ...
                """);
    }
}
