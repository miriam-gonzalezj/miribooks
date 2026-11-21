# Fragmentos · Code excerpts

Algunas piezas escogidas del código de MiriBooks. El código completo es privado; estos archivos muestran
cómo está escrito, pero solos no forman la app.

A few selected pieces of the MiriBooks source code. The full source is private; these files show how it is
written, but they don't make up the app on their own.

| Archivo · File | Qué muestra · What it shows |
|---|---|
| [`Isbn.java`](Isbn.java) | Reconoce el código de barras del libro (ISBN-13 con dígito verificador) y lo distingue del de precio o de tienda. · Recognizes the book's barcode (ISBN-13 with check digit) and tells it apart from price or store barcodes. |
| [`Titulos.java`](Titulos.java) · [`titulos.py`](titulos.py) | La regla que detecta un ISBN ligado al libro equivocado en los catálogos. La misma regla en Java (la app) y en Python (el cargador de libros digitales); las pruebas comparan las dos con los mismos casos. · The rule that detects an ISBN linked to the wrong book in public catalogs. The same rule in Java (the app) and Python (the e-book importer); the tests check both against the same cases. |
| [`PantallasTest-extracto.java`](PantallasTest-extracto.java) | Una prueba que usa la app como una persona: dibuja las pantallas, escanea, guarda y toma las capturas que se ven en el README. · A test that uses the app like a person would: renders the screens, scans, saves and takes the screenshots shown in the README. |

Prueba rápida · Quick try:

```bash
python titulos.py
```

© 2026 Miriam González Jaimes. Todos los derechos reservados · All rights reserved.
