package mx.miri.mislibros;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ¿El título que escribiste es el del catálogo? Misma regla que leer_digitales.coincide_con_el_archivo
 * (las pruebas comparan las dos con los mismos casos). Sirve para avisar mientras corriges el título:
 * si ya no se parece, quizá el ISBN está mal ligado en el catálogo ("40 Algorithms" para tu "50 Algorithms").
 */
public final class Titulos {
    private Titulos() {}

    static final double COINCIDENCIA_MINIMA = 0.6;
    private static final Set<String> VACIAS = new HashSet<>(Arrays.asList(
            "the a an and of for with to in on by from your you de la el y en con para los las".split(" ")));
    private static final Pattern NUMERO = Pattern.compile("\\b\\d+\\b");

    /** 'The Developer’s Playbook, 2nd Edition' -> 'developers playbook' (como base_datos.normalizar). */
    static String normalizar(String texto) {
        String t = Normalizer.normalize(texto == null ? "" : texto, Normalizer.Form.NFKD);
        t = t.replaceAll("[^\\p{ASCII}]", "").toLowerCase(Locale.ROOT);
        t = t.replaceAll("['`]", "");
        t = t.replaceAll("[^a-z0-9]+", " ");
        t = t.replaceAll("\\b(\\d+(st|nd|rd|th)?\\s+(ed|edition)|edicion|ed)\\b", " ");
        t = t.trim().replaceAll("^(the|a|an)\\s+", "");
        return t.replaceAll("\\s+", " ").trim();
    }

    static Set<String> numeros(String titulo) {
        Set<String> r = new HashSet<>();
        Matcher m = NUMERO.matcher(normalizar(titulo));
        while (m.find()) r.add(m.group());
        return r;
    }

    static Set<String> palabras(String titulo) {
        Set<String> r = new HashSet<>();
        for (String w : normalizar(titulo).split(" ")) {
            if (w.length() > 1 && !VACIAS.contains(w) && !w.matches("\\d+")) r.add(w);
        }
        return r;
    }

    public static boolean coinciden(String mio, String delCatalogo) {
        if (mio == null || mio.trim().isEmpty()) return true;        // sin título no hay con qué comparar
        Set<String> na = numeros(mio), nb = numeros(delCatalogo);
        Set<String> soloA = new HashSet<>(na), soloB = new HashSet<>(nb);
        soloA.removeAll(nb);
        soloB.removeAll(na);
        if (!soloA.isEmpty() && !soloB.isEmpty()) return false;     // números distintos: 50 contra 40
        Set<String> a = palabras(mio), b = palabras(delCatalogo);
        if (a.isEmpty() && b.isEmpty()) return true;                // títulos solo de números ("1984")
        Set<String> comunes = new HashSet<>(a);
        comunes.retainAll(b);
        return (double) comunes.size() / Math.max(1, Math.min(a.size(), b.size())) >= COINCIDENCIA_MINIMA;
    }
}
