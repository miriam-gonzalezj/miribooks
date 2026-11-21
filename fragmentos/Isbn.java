package mx.miri.mislibros;

/** El código de barras de atrás del libro. Misma regla que la versión web: solo ISBN-13 que empieza con 978 o 979. */
public final class Isbn {
    private Isbn() {}

    /** El ISBN si el código leído es el del libro; null si es otro código (precio, tienda, etc.). */
    public static String deCodigoDeBarras(String valor) {
        if (valor == null) return null;
        String d = valor.replaceAll("\\D", "");
        if (!d.matches("97[89]\\d{10}")) return null;
        return digitoCorrecto(d) ? d : null;
    }

    /** ¿El código de 13 cifras parece un ISBN aunque el dígito final no cuadre? (para dar un aviso útil) */
    public static boolean pareceIsbn(String valor) {
        return valor != null && valor.replaceAll("\\D", "").matches("97[89]\\d{10}");
    }

    static boolean digitoCorrecto(String d) {
        int suma = 0;
        for (int i = 0; i < 12; i++) suma += (d.charAt(i) - '0') * (i % 2 == 0 ? 1 : 3);
        return (10 - suma % 10) % 10 == d.charAt(12) - '0';
    }
}
