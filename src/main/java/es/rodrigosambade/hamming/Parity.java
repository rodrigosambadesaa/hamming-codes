package es.rodrigosambade.hamming;

/**
 * Paridad utilizada en los grupos de comprobación de un código Hamming.
 */
public enum Parity {
    EVEN(0),
    ODD(1);

    private final int expectedXor;

    Parity(int expectedXor) {
        this.expectedXor = expectedXor;
    }

    public int expectedXor() {
        return expectedXor;
    }

    public static Parity parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("La paridad no puede ser null.");
        }
        return switch (value.trim().toLowerCase()) {
            case "even", "par", "0" -> EVEN;
            case "odd", "impar", "1" -> ODD;
            default -> throw new IllegalArgumentException(
                    "Paridad desconocida: " + value + ". Use even/par u odd/impar.");
        };
    }
}
