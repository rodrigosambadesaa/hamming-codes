package es.rodrigosambade.hamming;

import java.math.BigInteger;

/**
 * Resultado del análisis de una palabra Hamming recibida.
 *
 * @param received palabra recibida
 * @param syndrome síndrome, interpretado como posición binaria del error
 * @param errorDetected true si el síndrome es distinto de cero
 * @param correctable true si el síndrome señala una posición existente
 * @param corrected palabra corregida, o null si no hay corrección fiable
 * @param dataValue datos extraídos de la palabra corregida (o recibida si no había error)
 * @param dataBits representación binaria de los bits de datos
 */
public record HammingResult(
        String received,
        BigInteger syndrome,
        boolean errorDetected,
        boolean correctable,
        String corrected,
        BigInteger dataValue,
        String dataBits) {

    public BigInteger errorPosition() {
        return syndrome;
    }
}
