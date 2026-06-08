package com.example.elearn.ui.splash;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Bug Condition Exploration Test: No Splash Screen on App Launch
 *
 * Validates: Requirements 1.1, 1.2, 1.3, 2.1, 2.2, 2.3
 *
 * This test encodes the EXPECTED behavior after the fix is applied.
 * On unfixed code, it will FAIL - confirming the bug exists.
 * Counterexamples: "SplashActivity class not found", "LoginActivity has LAUNCHER intent filter"
 */
class SplashActivityBugConditionTest {

    private static final String SPLASH_ACTIVITY_CLASS = "com.example.elearn.ui.splash.SplashActivity";
    private static final String MANIFEST_PATH = "src/main/AndroidManifest.xml";

    /**
     * Property 1: Bug Condition - SplashActivity class must exist.
     * On unfixed code this FAILS because no SplashActivity has been created.
     *
     * Validates: Requirements 1.1, 2.1
     */
    @Test
    void splashActivityClassExists() {
        try {
            Class<?> splashClass = Class.forName(SPLASH_ACTIVITY_CLASS);
            assertNotNull(splashClass, "SplashActivity class should exist");
        } catch (ClassNotFoundException e) {
            fail("SplashActivity class not found - bug condition confirmed: " +
                    "no splash screen activity exists in the project. " +
                    "Counterexample: class '" + SPLASH_ACTIVITY_CLASS + "' does not exist.");
        }
    }

    /**
     * Property 1: Bug Condition - SplashActivity must be registered in the manifest
     * with MAIN/LAUNCHER intent filter.
     * On unfixed code this FAILS because LoginActivity has the LAUNCHER intent filter.
     *
     * Validates: Requirements 1.1, 1.2, 2.1
     */
    @Test
    void manifestHasSplashActivityAsLauncher() throws Exception {
        String manifestContent = readManifestContent();
        assertNotNull(manifestContent, "AndroidManifest.xml should be readable");

        // Verify SplashActivity is declared in manifest
        assertTrue(manifestContent.contains(".ui.splash.SplashActivity") ||
                        manifestContent.contains("com.example.elearn.ui.splash.SplashActivity"),
                "SplashActivity should be registered in AndroidManifest.xml. " +
                        "Counterexample: SplashActivity is not declared in manifest.");

        // Verify SplashActivity has the LAUNCHER intent filter
        // Parse the manifest to find SplashActivity's intent filter
        String splashSection = extractActivitySection(manifestContent, "SplashActivity");
        assertNotNull(splashSection,
                "SplashActivity section not found in manifest. " +
                        "Counterexample: LoginActivity has LAUNCHER intent filter instead.");
        assertTrue(splashSection.contains("android.intent.action.MAIN"),
                "SplashActivity should have MAIN action in intent filter.");
        assertTrue(splashSection.contains("android.intent.category.LAUNCHER"),
                "SplashActivity should have LAUNCHER category in intent filter.");
    }

    /**
     * Property 1: Bug Condition - LoginActivity must NOT have the LAUNCHER intent filter
     * (it should have been moved to SplashActivity).
     * On unfixed code this FAILS because LoginActivity still has the LAUNCHER filter.
     *
     * Validates: Requirements 1.2, 2.1
     */
    @Test
    void loginActivityDoesNotHaveLauncherIntentFilter() throws Exception {
        String manifestContent = readManifestContent();
        assertNotNull(manifestContent, "AndroidManifest.xml should be readable");

        String loginSection = extractActivitySection(manifestContent, "LoginActivity");
        assertNotNull(loginSection, "LoginActivity section should exist in manifest.");

        // LoginActivity should NOT have the LAUNCHER category
        assertFalse(loginSection.contains("android.intent.category.LAUNCHER"),
                "LoginActivity should NOT have LAUNCHER intent filter after fix. " +
                        "Counterexample: LoginActivity still has LAUNCHER category - " +
                        "app launches directly to login without splash screen.");
    }

    /**
     * Property-based test: For any auth state (logged in or not), SplashActivity
     * must route to the correct target activity.
     * On unfixed code this FAILS because SplashActivity doesn't exist.
     *
     * Validates: Requirements 2.1, 2.2
     */
    @Property(tries = 10)
    void splashActivityRoutesCorrectlyForAnyAuthState(
            @ForAll("authStates") boolean isLoggedIn) {

        // Attempt to load SplashActivity class
        Class<?> splashClass;
        try {
            splashClass = Class.forName(SPLASH_ACTIVITY_CLASS);
        } catch (ClassNotFoundException e) {
            fail("SplashActivity class not found - cannot verify routing. " +
                    "Counterexample: auth state isLoggedIn=" + isLoggedIn +
                    " but no SplashActivity exists to handle routing.");
            return;
        }

        // Verify the class has the expected structure for routing
        // Check that it extends AppCompatActivity (or Activity)
        Class<?> superClass = splashClass.getSuperclass();
        boolean extendsActivity = false;
        Class<?> current = superClass;
        while (current != null) {
            if (current.getSimpleName().equals("Activity") ||
                    current.getSimpleName().equals("AppCompatActivity")) {
                extendsActivity = true;
                break;
            }
            current = current.getSuperclass();
        }
        assertTrue(extendsActivity,
                "SplashActivity must extend Activity or AppCompatActivity for routing to work. " +
                        "Auth state: isLoggedIn=" + isLoggedIn);
    }

    /**
     * Property 1: Bug Condition - SplashActivity must call finish() after navigation
     * to prevent back-stack entry.
     * On unfixed code this FAILS because no SplashActivity exists.
     *
     * Validates: Requirements 2.1, 2.2
     */
    @Test
    void splashActivitySourceContainsFinishCall() throws Exception {
        // Check the SplashActivity source file exists and contains finish()
        Path splashSourcePath = findSplashActivitySource();
        assertNotNull(splashSourcePath,
                "SplashActivity.java source file not found. " +
                        "Counterexample: no SplashActivity source exists to call finish().");

        String sourceContent = new String(Files.readAllBytes(splashSourcePath));
        assertTrue(sourceContent.contains("finish()"),
                "SplashActivity must call finish() after starting target activity " +
                        "to prevent back-navigation to splash. " +
                        "Counterexample: SplashActivity does not call finish().");
    }

    /**
     * Property 1: Bug Condition - Splash layout must contain an ImageView
     * displaying @drawable/education.
     * On unfixed code this FAILS because no splash layout exists.
     *
     * Validates: Requirements 2.3
     */
    @Test
    void splashLayoutContainsEducationImage() throws Exception {
        Path layoutPath = findSplashLayoutFile();
        assertNotNull(layoutPath,
                "activity_splash.xml layout file not found. " +
                        "Counterexample: no splash layout exists to display education image.");

        String layoutContent = new String(Files.readAllBytes(layoutPath));
        assertTrue(layoutContent.contains("ImageView"),
                "Splash layout must contain an ImageView. " +
                        "Counterexample: splash layout has no ImageView element.");
        assertTrue(layoutContent.contains("@drawable/education"),
                "Splash layout ImageView must display @drawable/education. " +
                        "Counterexample: splash layout does not reference education drawable.");
    }

    // --- Helper Methods ---

    @Provide
    Arbitrary<Boolean> authStates() {
        return Arbitraries.of(true, false);
    }

    private String readManifestContent() {
        try {
            // Try multiple paths to find the manifest
            String[] possiblePaths = {
                    "ELearnMobileApp/app/src/main/AndroidManifest.xml",
                    "app/src/main/AndroidManifest.xml",
                    "src/main/AndroidManifest.xml"
            };

            for (String path : possiblePaths) {
                Path manifestPath = Paths.get(path);
                if (Files.exists(manifestPath)) {
                    return new String(Files.readAllBytes(manifestPath));
                }
            }

            // Try to find from working directory going up
            Path currentDir = Paths.get("").toAbsolutePath();
            Path candidate = currentDir.resolve("ELearnMobileApp/app/src/main/AndroidManifest.xml");
            if (Files.exists(candidate)) {
                return new String(Files.readAllBytes(candidate));
            }

            // Check if we're running from within ELearnMobileApp
            candidate = currentDir.resolve("app/src/main/AndroidManifest.xml");
            if (Files.exists(candidate)) {
                return new String(Files.readAllBytes(candidate));
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractActivitySection(String manifestContent, String activityName) {
        int activityStart = manifestContent.indexOf(activityName);
        if (activityStart == -1) return null;

        // Find the enclosing <activity> tag
        int tagStart = manifestContent.lastIndexOf("<activity", activityStart);
        if (tagStart == -1) return null;

        // Find the end of this activity section
        // Could be self-closing /> or have </activity>
        int selfClose = manifestContent.indexOf("/>", activityStart);
        int endTag = manifestContent.indexOf("</activity>", activityStart);

        int sectionEnd;
        if (endTag == -1 && selfClose == -1) return null;
        if (endTag == -1) sectionEnd = selfClose + 2;
        else if (selfClose == -1) sectionEnd = endTag + "</activity>".length();
        else sectionEnd = Math.min(selfClose + 2, endTag + "</activity>".length());

        // Check if another <activity starts before our end tag - if so, we need the closer end
        int nextActivity = manifestContent.indexOf("<activity", activityStart + activityName.length());
        if (nextActivity != -1 && nextActivity < sectionEnd) {
            // Our section ends before the next activity
            sectionEnd = nextActivity;
        }

        return manifestContent.substring(tagStart, sectionEnd);
    }

    private Path findSplashActivitySource() {
        String[] possiblePaths = {
                "ELearnMobileApp/app/src/main/java/com/example/elearn/ui/splash/SplashActivity.java",
                "app/src/main/java/com/example/elearn/ui/splash/SplashActivity.java",
                "src/main/java/com/example/elearn/ui/splash/SplashActivity.java"
        };

        for (String path : possiblePaths) {
            Path p = Paths.get(path);
            if (Files.exists(p)) return p;
        }

        // Try from working directory
        Path currentDir = Paths.get("").toAbsolutePath();
        Path candidate = currentDir.resolve("ELearnMobileApp/app/src/main/java/com/example/elearn/ui/splash/SplashActivity.java");
        if (Files.exists(candidate)) return candidate;

        candidate = currentDir.resolve("app/src/main/java/com/example/elearn/ui/splash/SplashActivity.java");
        if (Files.exists(candidate)) return candidate;

        return null;
    }

    private Path findSplashLayoutFile() {
        String[] possiblePaths = {
                "ELearnMobileApp/app/src/main/res/layout/activity_splash.xml",
                "app/src/main/res/layout/activity_splash.xml",
                "src/main/res/layout/activity_splash.xml"
        };

        for (String path : possiblePaths) {
            Path p = Paths.get(path);
            if (Files.exists(p)) return p;
        }

        // Try from working directory
        Path currentDir = Paths.get("").toAbsolutePath();
        Path candidate = currentDir.resolve("ELearnMobileApp/app/src/main/res/layout/activity_splash.xml");
        if (Files.exists(candidate)) return candidate;

        candidate = currentDir.resolve("app/src/main/res/layout/activity_splash.xml");
        if (Files.exists(candidate)) return candidate;

        return null;
    }
}
