package es.rodrigosambade.hamming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class HammingCodeTest {

    @Test
    void roundTripWithEvenParity() {
        BigInteger value = new BigInteger("13579ACE02468F", 16);
        String encoded = HammingCode.encode(value, 64, Parity.EVEN);

        HammingResult result = HammingCode.analyze(encoded, Parity.EVEN);

        assertFalse(result.errorDetected());
        assertEquals(value, result.dataValue());
    }

    @Test
    void everySingleBitErrorIsCorrected() {
        BigInteger value = new BigInteger("A53E7F", 16);
        String original = HammingCode.encode(value, 24, Parity.EVEN);

        for (int position = 1; position <= original.length(); position++) {
            String damaged = HammingCode.flipBit(
                    original, BigInteger.valueOf(position));
            HammingResult result = HammingCode.analyze(damaged, Parity.EVEN);

            assertTrue(result.errorDetected());
            assertTrue(result.correctable());
            assertEquals(BigInteger.valueOf(position), result.errorPosition());
            assertEquals(original, result.corrected());
            assertEquals(value, result.dataValue());
        }
    }

    @Test
    void detectsAndCorrectsErrorInParityBit() {
        BigInteger value = new BigInteger("2A5B6C", 16);
        String original = HammingCode.encode(value, 24, Parity.EVEN);
        BigInteger parityPosition = BigInteger.valueOf(16);
        String damaged = HammingCode.flipBit(original, parityPosition);

        HammingResult result = HammingCode.analyze(damaged, Parity.EVEN);

        assertEquals(parityPosition, result.syndrome());
        assertEquals(original, result.corrected());
        assertEquals(value, result.dataValue());
    }

    @Test
    void supportsValuesBeyondLongRange() {
        BigInteger value = BigInteger.ONE.shiftLeft(300)
                .add(BigInteger.ONE.shiftLeft(211))
                .add(new BigInteger("314159265358979323846264338327950288419716939937510"));

        String encoded = HammingCode.encode(value, 384, Parity.EVEN);
        HammingResult clean = HammingCode.analyze(encoded, Parity.EVEN);

        assertFalse(clean.errorDetected());
        assertEquals(value, clean.dataValue());

        BigInteger errorPosition = BigInteger.valueOf(257);
        String damaged = HammingCode.flipBit(encoded, errorPosition);
        HammingResult corrected = HammingCode.analyze(damaged, Parity.EVEN);

        assertEquals(errorPosition, corrected.errorPosition());
        assertEquals(encoded, corrected.corrected());
        assertEquals(value, corrected.dataValue());
    }

    @Test
    void supportsOddParityRoundTrip() {
        BigInteger value = new BigInteger("271828182845904523536028747135266249775724709369995");
        String encoded = HammingCode.encode(value, 192, Parity.ODD);

        HammingResult result = HammingCode.analyze(encoded, Parity.ODD);

        assertFalse(result.errorDetected());
        assertEquals(value, result.dataValue());
    }

    @Test
    void requiredParityBitsSatisfiesHammingBound() {
        int dataBits = 1000;
        int parityBits = HammingCode.requiredParityBits(dataBits);

        BigInteger capacity = BigInteger.ONE.shiftLeft(parityBits);
        BigInteger required = BigInteger.valueOf((long) dataBits + parityBits + 1L);
        assertTrue(capacity.compareTo(required) >= 0);

        if (parityBits > 0) {
            BigInteger previousCapacity = BigInteger.ONE.shiftLeft(parityBits - 1);
            BigInteger previousRequired =
                    BigInteger.valueOf((long) dataBits + parityBits);
            assertTrue(previousCapacity.compareTo(previousRequired) < 0);
        }
    }

    @Test
    void rejectsValueThatDoesNotFitRequestedDataBits() {
        BigInteger tooLarge = BigInteger.ONE.shiftLeft(80);
        assertThrows(IllegalArgumentException.class,
                () -> HammingCode.encode(tooLarge, 64, Parity.EVEN));
    }
}
