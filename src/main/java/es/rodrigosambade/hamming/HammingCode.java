package es.rodrigosambade.hamming;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Operaciones generales con códigos Hamming.
 *
 * <p>Convención: las posiciones se numeran desde 1 de izquierda a derecha.
 * Las posiciones 1, 2, 4, 8, ... son bits de paridad.</p>
 */
public final class HammingCode {

    private HammingCode() {
    }

    /**
     * Codifica un valor no negativo usando exactamente {@code dataBitCount}
     * bits de datos y el número mínimo de bits de paridad Hamming.
     */
    public static String encode(BigInteger data, int dataBitCount, Parity parity) {
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(parity, "parity");

        if (data.signum() < 0) {
            throw new IllegalArgumentException("El dato debe ser no negativo.");
        }
        if (dataBitCount < 1) {
            throw new IllegalArgumentException("El número de bits de datos debe ser >= 1.");
        }
        if (data.bitLength() > dataBitCount) {
            throw new IllegalArgumentException(
                    "El valor necesita " + data.bitLength()
                    + " bits y no cabe en " + dataBitCount + " bits.");
        }

        int parityBits = requiredParityBits(dataBitCount);
        long totalLong = (long) dataBitCount + parityBits;
        if (totalLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "La palabra resultante excede el máximo direccionable por una String de Java.");
        }
        int totalBits = (int) totalLong;

        char[] code = new char[totalBits];
        java.util.Arrays.fill(code, '0');

        String raw = data.toString(2);
        int leadingZeros = dataBitCount - raw.length();
        int dataIndex = 0;

        for (int position = 1; position <= totalBits; position++) {
            if (!isParityPosition(position)) {
                char bit = dataIndex < leadingZeros ? '0' : raw.charAt(dataIndex - leadingZeros);
                code[position - 1] = bit;
                dataIndex++;
            }
        }

        for (long p = 1; p <= totalBits; p <<= 1) {
            int parityPosition = (int) p;
            int xorWithoutParity = 0;
            for (int position = 1; position <= totalBits; position++) {
                if (position != parityPosition && (position & parityPosition) != 0) {
                    xorWithoutParity ^= code[position - 1] - '0';
                }
            }
            code[parityPosition - 1] =
                    (char) ('0' + (xorWithoutParity ^ parity.expectedXor()));

            if (p > Integer.MAX_VALUE / 2L) {
                break;
            }
        }

        return new String(code);
    }

    /**
     * Analiza una palabra Hamming. Si el síndrome señala una posición existente,
     * corrige ese bit y extrae los datos.
     */
    public static HammingResult analyze(String received, Parity parity) {
        validateCodeword(received);
        Objects.requireNonNull(parity, "parity");

        int length = received.length();
        BigInteger syndrome = syndrome(received, parity);
        boolean errorDetected = syndrome.signum() != 0;

        String corrected = received;
        boolean correctable = true;

        if (errorDetected) {
            BigInteger maxPosition = BigInteger.valueOf(length);
            if (syndrome.compareTo(maxPosition) > 0) {
                correctable = false;
                corrected = null;
            } else {
                int position = syndrome.intValueExact();
                char[] bits = received.toCharArray();
                bits[position - 1] = bits[position - 1] == '0' ? '1' : '0';
                corrected = new String(bits);
            }
        }

        String dataBits = corrected == null ? null : extractDataBits(corrected);
        BigInteger dataValue =
                dataBits == null || dataBits.isEmpty() ? null : new BigInteger(dataBits, 2);

        return new HammingResult(
                received, syndrome, errorDetected, correctable, corrected, dataValue, dataBits);
    }

    /**
     * Calcula el síndrome. El valor 0 significa que todas las ecuaciones de
     * paridad se cumplen. Un valor no nulo señala la posición binaria del error
     * bajo la hipótesis de un único error.
     */
    public static BigInteger syndrome(String codeword, Parity parity) {
        validateCodeword(codeword);
        Objects.requireNonNull(parity, "parity");

        int length = codeword.length();
        BigInteger result = BigInteger.ZERO;

        for (long p = 1; p <= length; p <<= 1) {
            int parityPosition = (int) p;
            int xor = 0;
            for (int position = 1; position <= length; position++) {
                if ((position & parityPosition) != 0) {
                    xor ^= codeword.charAt(position - 1) - '0';
                }
            }
            if (xor != parity.expectedXor()) {
                result = result.or(BigInteger.valueOf(parityPosition));
            }

            if (p > Integer.MAX_VALUE / 2L) {
                break;
            }
        }

        return result;
    }

    /**
     * Extrae los bits que no ocupan posiciones de paridad.
     */
    public static String extractDataBits(String codeword) {
        validateCodeword(codeword);
        StringBuilder data = new StringBuilder(codeword.length());
        for (int position = 1; position <= codeword.length(); position++) {
            if (!isParityPosition(position)) {
                data.append(codeword.charAt(position - 1));
            }
        }
        return data.toString();
    }

    /**
     * Devuelve el número mínimo r tal que 2^r >= m + r + 1.
     */
    public static int requiredParityBits(int dataBits) {
        if (dataBits < 1) {
            throw new IllegalArgumentException("El número de bits de datos debe ser >= 1.");
        }
        int r = 0;
        while (BigInteger.ONE.shiftLeft(r)
                .compareTo(BigInteger.valueOf((long) dataBits + r + 1L)) < 0) {
            r++;
        }
        return r;
    }

    public static boolean isParityPosition(int position) {
        if (position < 1) {
            return false;
        }
        return (position & (position - 1)) == 0;
    }

    /**
     * Invierte un bit usando la numeración de posiciones de la API
     * (1 = bit más a la izquierda).
     */
    public static String flipBit(String codeword, BigInteger position) {
        validateCodeword(codeword);
        Objects.requireNonNull(position, "position");
        if (position.compareTo(BigInteger.ONE) < 0
                || position.compareTo(BigInteger.valueOf(codeword.length())) > 0) {
            throw new IllegalArgumentException("Posición fuera de rango: " + position);
        }
        int p = position.intValueExact();
        char[] bits = codeword.toCharArray();
        bits[p - 1] = bits[p - 1] == '0' ? '1' : '0';
        return new String(bits);
    }

    private static void validateCodeword(String codeword) {
        if (codeword == null || codeword.isEmpty()) {
            throw new IllegalArgumentException("La palabra Hamming no puede estar vacía.");
        }
        for (int i = 0; i < codeword.length(); i++) {
            char c = codeword.charAt(i);
            if (c != '0' && c != '1') {
                throw new IllegalArgumentException(
                        "La palabra solo puede contener 0 y 1. Carácter inválido en índice " + i + ".");
            }
        }
    }
}
