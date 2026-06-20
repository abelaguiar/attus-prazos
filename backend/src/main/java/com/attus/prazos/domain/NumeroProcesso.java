package com.attus.prazos.domain;

public final class NumeroProcesso {

    private NumeroProcesso() {
    }

    public static String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        return valor.replaceAll("\\D", "");
    }

    public static String formatar(String valor) {
        String digitos = normalizar(valor);
        if (digitos == null || digitos.length() != 20) {
            return valor;
        }
        return "%s-%s.%s.%s.%s.%s".formatted(
                digitos.substring(0, 7),
                digitos.substring(7, 9),
                digitos.substring(9, 13),
                digitos.substring(13, 14),
                digitos.substring(14, 16),
                digitos.substring(16, 20));
    }
}
