package imageprocessingapp.util;

import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Initialise JavaFX une seule fois pour l'ensemble de la suite de tests.
 */
public final class JavaFxTestInitializer {

    private static final Object LOCK = new Object();
    private static volatile boolean initialized = false;

    private JavaFxTestInitializer() {
    }

    public static void initToolkit() throws InterruptedException {
        if (initialized) {
            return;
        }
        synchronized (LOCK) {
            if (initialized) {
                return;
            }
            CountDownLatch latch = new CountDownLatch(1);
            try {
                Platform.startup(latch::countDown);
            } catch (IllegalStateException alreadyStarted) {
                // La plateforme est déjà initialisée dans la JVM courante.
                initialized = true;
                return;
            }
            assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX Platform failed to start in time");
            initialized = true;
        }
    }
}

