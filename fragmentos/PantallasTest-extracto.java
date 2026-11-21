// Extracto de PantallasTest.java (MiriBooks, código completo privado).
// Excerpt from PantallasTest.java (MiriBooks; full source is private).
//
// La app se prueba como la usa una persona: Robolectric dibuja las pantallas de verdad, el escáner
// recibe un código de barras y la prueba revisa lo que se ve y guarda una captura de cada paso.
// Las capturas del README salen de aquí. Corre contra el servidor real con BigQuery e internet falsos.
//
// The app is tested the way a person uses it: Robolectric renders the real screens, the scanner receives
// a barcode, and the test checks what is shown and saves a screenshot of every step. The README
// screenshots come from here. It runs against the real server code with a fake BigQuery and internet.

/**
 * La app como la usa Miri, con pantallas de verdad (Robolectric dibuja las vistas) contra el servidor de prueba.
 * Guarda una captura de cada paso en app/build/capturas/ para revisar cómo se ve.
 * Lo que NO puede probar: la cámara real, la huella y el Keystore (eso se prueba en el celular).
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = 34, qualifiers = "es-rMX-w393dp-h852dp-xhdpi")      // celular en español
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
public class PantallasTest {
    private String servidor;
    private File carpeta;
    // ...

    @Test
    public void recorridoCompleto() throws Exception {
        try (ActivityScenario<MainActivity> esc = ActivityScenario.launch(MainActivity.class)) {
            esc.onActivity(a -> {
                // 1. Primera vez: pide escanear el código de alta
                assertEquals(MainActivity.Modo.ALTA, a.modo);
                assertTrue(texto(a, R.id.textoEntrada).contains("subir_app.py --qr"));
                captura(a, null, "1-alta");
            });

            Api api = sesionDePrueba();
            for (String[] l : new String[][]{{"Cien años de soledad", "Recámara"}, {"Pedro Páramo", "Librero de la sala, repisa 2"}}) {
                api.guardar(new JSONObject().put("titulo", l[0]).put("ubicacion", l[1]).put("procedencia", "Goodwill"));
            }

            esc.onActivity(a -> a.entrarConApi(api));
            esperar(esc, a -> ((LinearLayout) a.findViewById(R.id.listaRecientes)).getChildCount() > 0);
            esc.onActivity(a -> {
                // 2. El escáner con el lugar fijo arriba y los últimos guardados
                assertEquals(MainActivity.Modo.APP, a.modo);
                assertEquals("Librero de la sala, repisa 2", texto(a, R.id.botonLugar));
                assertTrue(texto(a, R.id.tituloRecientes).startsWith("Últimos guardados ("));
                captura(a, null, "2-escaner");

                // Un código que no es de libro: avisa y sigue leyendo
                a.codigoDeLibro("7501055300075");
                assertTrue(texto(a, R.id.estadoCamara).contains("978 o 979"));
                assertTrue(a.leyendoCodigos());

                // 3. Libro nuevo (Open Library)
                a.codigoDeLibro("9781593274245");
                assertFalse(a.leyendoCodigos());
            });
            esperar(esc, a -> a.hojaAbierta() != null);
            esc.onActivity(a -> {
                View h = hoja(a);
                assertEquals("No lo tienes. Datos de Open Library: revísalos.", texto(h, R.id.estadoLibro));
                assertEquals("Think Like a Programmer", texto(h, R.id.titulo));
                assertEquals("Librero de la sala, repisa 2", texto(h, R.id.ubicacion));
                captura(a, a.hojaAbierta(), "3-ficha-libro-nuevo");
                ((EditText) h.findViewById(R.id.procedencia)).setText("Goodwill");
                h.findViewById(R.id.guardar).performClick();
            });
            esperar(esc, a -> a.findViewById(R.id.listo).getVisibility() == View.VISIBLE);
            esperar(esc, a -> texto(a, R.id.tituloRecientes).contains("(3 en papel)")
                    || texto(a, R.id.tituloRecientes).contains("en papel)") && primerReciente(a).equals("Think Like a Programmer"));
            esc.onActivity(a -> {
                // 4. Guardado: aviso verde, la ficha se cierra y la cámara sigue leyendo (escaneo seguido)
                assertNull(a.hojaAbierta());
                assertTrue(a.leyendoCodigos());
                assertTrue(texto(a, R.id.listo).startsWith("«Think Like a Programmer». Guardado en «Librero de la sala, repisa 2»."));
                assertEquals("Think Like a Programmer", primerReciente(a));
                captura(a, null, "4-guardado");

                // Tocar un libro de los recientes lo abre en la Biblioteca
                ((LinearLayout) a.findViewById(R.id.listaRecientes)).getChildAt(0).performClick();
                android.content.Intent abierta = shadowOf(a).getNextStartedActivity();
                assertEquals(BibliotecaActivity.class.getName(), abierta.getComponent().getClassName());
                assertNotNull(abierta.getStringExtra(BibliotecaActivity.LIBRO));

                // El mismo libro sigue frente a la cámara: no se vuelve a abrir solo
                a.codigoDeLibro("9781593274245");
                assertNull(a.hojaAbierta());
                assertTrue(a.leyendoCodigos());

                // 5. Uno que ya tienes en PDF (la procedencia del anterior, Goodwill, no se arrastra)
                a.codigoDeLibro("9781098176501");
            });
            esperar(esc, a -> a.hojaAbierta() != null);
            esc.onActivity(a -> {
                assertEquals("Ya lo tienes: PDF", texto(hoja(a), R.id.estadoLibro));
                assertEquals("", texto(hoja(a), R.id.procedencia));
                assertEquals(View.VISIBLE, hoja(a).findViewById(R.id.verEnBiblioteca).getVisibility());
                captura(a, a.hojaAbierta(), "5-ya-lo-tienes");
            // ... (Biblioteca, corregir, cambiar de lugar, eliminar, Acerca de, modo oscuro)
        }
    }
    // ...
}
