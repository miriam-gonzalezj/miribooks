"""¿El título del catálogo es el de mi libro?  /  Does the catalog's title match my book?

Los catálogos públicos a veces tienen un ISBN ligado al libro equivocado: Open Library devolvió
"40 Algorithms..." para "50 Algorithms...". Esta regla decide si se puede confiar en el catálogo.
Es la misma que Titulos.java en la app de Android; las pruebas comparan las dos con los mismos casos.

Public catalogs sometimes link an ISBN to the wrong book. This rule decides whether the catalog can be
trusted. It is the twin of Titulos.java in the Android app; the tests check both against the same cases.
"""
import re
import unicodedata

PALABRAS_VACIAS = set("the a an and of for with to in on by from your you de la el y en con para los las".split())
COINCIDENCIA_MINIMA = 0.6   # en mis libros: los correctos dan 1.0 y el mal ligado 0.33


def normalizar(texto: str | None) -> str:
    """'The Developer’s Playbook, 2nd Edition' -> 'developers playbook'."""
    t = unicodedata.normalize("NFKD", texto or "").encode("ascii", "ignore").decode().lower()
    t = re.sub(r"[’'`]", "", t)
    t = re.sub(r"[^a-z0-9]+", " ", t)
    t = re.sub(r"\b(\d+(st|nd|rd|th)?\s+(ed|edition)|edicion|ed)\b", " ", t)
    t = re.sub(r"^(the|a|an)\s+", "", t.strip())
    return re.sub(r"\s+", " ", t).strip()


def numeros(titulo: str | None) -> set[str]:
    """'50 Algorithms Every Programmer Should Know 2nd ed' -> {'50'} (la edición no cuenta)."""
    return set(re.findall(r"\b\d+\b", normalizar(titulo)))


def palabras(titulo: str | None) -> set[str]:
    return {w for w in normalizar(titulo).split() if w not in PALABRAS_VACIAS and not w.isdigit() and len(w) > 1}


def coinciden(mio: str | None, del_catalogo: str | None) -> bool:
    if not mio or not mio.strip():
        return True                                    # sin título no hay con qué comparar
    na, nb = numeros(mio), numeros(del_catalogo)
    if na - nb and nb - na:
        return False                                   # números distintos: 50 contra 40
    a, b = palabras(mio), palabras(del_catalogo)
    if not a and not b:
        return True                                    # títulos solo de números ("1984")
    return len(a & b) / max(1, min(len(a), len(b))) >= COINCIDENCIA_MINIMA


if __name__ == "__main__":
    for mio, cat in [("50 Algorithms Every Programmer Should Know", "40 Algorithms Every Programmer Should Know"),
                     ("Command-Line Rust", "Systems Programming with Rust"),
                     ("The Pragmatic Programmer, 20th Anniversary Edition", "Pragmatic Programmer"),
                     ("Cien años de soledad", "Cien anos de soledad"),
                     ("1984", "1984")]:
        print(f"{'coinciden   ' if coinciden(mio, cat) else 'NO coinciden'}  {mio!r} / {cat!r}")
