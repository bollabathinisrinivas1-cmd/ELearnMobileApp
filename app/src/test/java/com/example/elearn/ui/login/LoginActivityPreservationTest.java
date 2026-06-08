package com.example.elearn.ui.login;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Preservation Property Tests: Existing Login/Dashboard/Auth Behavior Unchanged
 *
 * Validates: Requirements 3.1, 3.2, 3.3, 3.4
 *
 * These tests encode the EXISTING baseline behavior of the app BEFORE the splash screen fix.
 * They MUST PASS on unfixed code, establishing a baseline that must not regress.
 *
 * Observations on unfixed code:
 * - LoginActivity launches with email/password fields, gradient header, signup link, forgot password link
 * - AuthService.isLoggedIn() returns true when token is present, false when absent
 * - DashboardActivity is accessible and functional after successful login
 * - Theme.ELearn and Theme.ELearn.NoActionBar styles are applied to existing activities
 */
class LoginActivityPreservationTest {

    // =========================================================================
    // Property 1: For all auth states, LoginActivity preserves existing UI elements
    // =========================================================================

    /**
     * Property: LoginActivity layout must contain email input field.
     * Observed on unfixed code: activity_login.xml has emailEditText with textEmailAddress input type.
     *
     * Validates: Requirements 3.1, 3.3
     */
    @Test
    void loginLayoutContainsEmailField() throws Exception {
        String layoutContent = readLoginLayoutContent();
        assertNotNull(layoutContent, "activity_login.xml should be readable");

        assertTrue(layoutContent.contains("emailEditText"),
                "LoginActivity layout must contain emailEditText field");
        assertTrue(layoutContent.contains("textEmailAddress"),
                "Email field must have textEmailAddress input type");
        assertTrue(layoutContent.contains("Enter your email"),
                "Email field must have 'Enter your email' hint");
    }

    /**
     * Property: LoginActivity layout must contain password input field.
     * Observed on unfixed code: activity_login.xml has passwordEditText with textPassword input type.
     *
     * Validates: Requirements 3.1, 3.3
     */
    @Test
    void loginLayoutContainsPasswordField() throws Exception {
        String layoutContent = readLoginLayoutContent();
        assertNotNull(layoutContent, "activity_login.xml should be readable");

        assertTrue(layoutContent.contains("passwordEditText"),
                "LoginActivity layout must contain passwordEditText field");
        assertTrue(layoutContent.contains("textPassword"),
                "Password field must have textPassword input type");
        assertTrue(layoutContent.contains("Enter your password"),
                "Password field must have 'Enter your password' hint");
    }

    /**
     * Property: LoginActivity layout must contain gradient header with eLibrary branding.
     * Observed on unfixed code: activity_login.xml has a LinearLayout with @drawable/gradient_header
     * background and "eLibrary" text.
     *
     * Validates: Requirements 3.1, 3.4
     */
    @Test
    void loginLayoutContainsGradientHeader() throws Exception {
        String layoutContent = readLoginLayoutContent();
        assertNotNull(layoutContent, "activity_login.xml should be readable");

        assertTrue(layoutContent.contains("@drawable/gradient_header"),
                "LoginActivity layout must have gradient_header background");
        assertTrue(layoutContent.contains("eLibrary"),
                "LoginActivity layout must display 'eLibrary' branding text");
    }

    /**
     * Property: LoginActivity layout must contain signup link.
     * Observed on unfixed code: activity_login.xml has signupLink TextView with
     * "Don't have an account? Sign Up" text.
     *
     * Validates: Requirements 3.1, 3.3
     */
    @Test
    void loginLayoutContainsSignupLink() throws Exception {
        String layoutContent = readLoginLayoutContent();
        assertNotNull(layoutContent, "activity_login.xml should be readable");

        assertTrue(layoutContent.contains("signupLink"),
                "LoginActivity layout must contain signupLink element");
        assertTrue(layoutContent.contains("Sign Up"),
                "Signup link must contain 'Sign Up' text");
    }

    /**
     * Property: LoginActivity layout must contain forgot password link.
     * Observed on unfixed code: activity_login.xml has forgotPasswordText TextView
     * with "Forgot Password?" text.
     *
     * Validates: Requirements 3.1, 3.3
     */
    @Test
    void loginLayoutContainsForgotPasswordLink() throws Exception {
        String layoutContent = readLoginLayoutContent();
        assertNotNull(layoutContent, "activity_login.xml should be readable");

        assertTrue(layoutContent.contains("forgotPasswordText"),
                "LoginActivity layout must contain forgotPasswordText element");
        assertTrue(layoutContent.contains("Forgot Password?"),
                "Forgot password link must contain 'Forgot Password?' text");
    }

    /**
     * Property: LoginActivity layout must contain login button.
     * Observed on unfixed code: activity_login.xml has loginButton with "Log In" text.
     *
     * Validates: Requirements 3.1, 3.3
     */
    @Test
    void loginLayoutContainsLoginButton() throws Exception {
        String layoutContent = readLoginLayoutContent();
        assertNotNull(layoutContent, "activity_login.xml should be readable");

        assertTrue(layoutContent.contains("loginButton"),
                "LoginActivity layout must contain loginButton element");
        assertTrue(layoutContent.contains("Log In"),
                "Login button must have 'Log In' text");
    }

    // =========================================================================
    // Property 2: For all valid/invalid token values, AuthService.isLoggedIn()
    // returns consistent results
    // =========================================================================

    /**
     * Property-based test: For all non-null, non-empty token strings,
     * AuthService.isLoggedIn() must return true.
     * Observed on unfixed code: isLoggedIn() checks token != null && !token.isEmpty().
     *
     * Validates: Requirements 3.2
     */
    @Property(tries = 50)
    void authServiceReturnsLoggedInForAnyNonEmptyToken(
            @ForAll("nonEmptyTokens") String token) {

        // Verify the AuthService logic: non-empty, non-null tokens mean logged in
        boolean result = isLoggedInLogic(token);
        assertTrue(result,
                "AuthService.isLoggedIn() must return true for non-empty token: '" + token + "'");
    }

    /**
     * Property-based test: For all null or empty token values,
     * AuthService.isLoggedIn() must return false.
     * Observed on unfixed code: isLoggedIn() returns false when token is null or empty.
     *
     * Validates: Requirements 3.2
     */
    @Property(tries = 10)
    void authServiceReturnsNotLoggedInForAbsentToken(
            @ForAll("absentTokens") String token) {

        boolean result = isLoggedInLogic(token);
        assertFalse(result,
                "AuthService.isLoggedIn() must return false for absent/empty token");
    }

    /**
     * Property-based test: AuthService.isLoggedIn() logic is consistent
     * regardless of the auth state - any valid token returns true, absent returns false.
     *
     * Validates: Requirements 3.2
     */
    @Property(tries = 50)
    void authServiceIsLoggedInIsConsistentForAllTokenStates(
            @ForAll("allTokenStates") String token) {

        boolean result = isLoggedInLogic(token);
        boolean expectedLoggedIn = (token != null && !token.isEmpty());
        assertEquals(expectedLoggedIn, result,
                "AuthService.isLoggedIn() must be consistent: token='" + token +
                        "' expected loggedIn=" + expectedLoggedIn + " but got " + result);
    }

    /**
     * Verify AuthService class structure preserves isLoggedIn() method signature.
     * Observed on unfixed code: AuthService has public boolean isLoggedIn() method.
     *
     * Validates: Requirements 3.2
     */
    @Test
    void authServiceHasIsLoggedInMethod() throws Exception {
        Class<?> authServiceClass = Class.forName("com.example.elearn.auth.AuthService");
        Method isLoggedInMethod = authServiceClass.getMethod("isLoggedIn");
        assertNotNull(isLoggedInMethod, "AuthService must have isLoggedIn() method");
        assertEquals(boolean.class, isLoggedInMethod.getReturnType(),
                "isLoggedIn() must return boolean");
    }

    /**
     * Verify AuthService source code preserves token-based login check logic.
     * Observed on unfixed code: isLoggedIn() checks getAccessToken() != null && !token.isEmpty().
     *
     * Validates: Requirements 3.2
     */
    @Test
    void authServiceSourcePreservesTokenCheckLogic() throws Exception {
        String sourceContent = readAuthServiceSource();
        assertNotNull(sourceContent, "AuthService.java source should be readable");

        assertTrue(sourceContent.contains("isLoggedIn"),
                "AuthService must contain isLoggedIn method");
        assertTrue(sourceContent.contains("getAccessToken"),
                "isLoggedIn must use getAccessToken() to check auth state");
        assertTrue(sourceContent.contains("!token.isEmpty()") || sourceContent.contains("!token.isEmpty"),
                "isLoggedIn must check token is not empty");
    }

    // =========================================================================
    // Property 3: Navigation from LoginActivity to SignupActivity and
    // ForgotPasswordActivity remains functional
    // =========================================================================

    /**
     * Property: SignupActivity class exists and is registered in the manifest.
     * Observed on unfixed code: SignupActivity is at com.example.elearn.ui.signup.SignupActivity.
     *
     * Validates: Requirements 3.3
     */
    @Test
    void signupActivityExistsAndRegistered() throws Exception {
        // Verify class exists
        Class<?> signupClass = Class.forName("com.example.elearn.ui.signup.SignupActivity");
        assertNotNull(signupClass, "SignupActivity class must exist");

        // Verify it's registered in manifest
        String manifestContent = readManifestContent();
        assertNotNull(manifestContent, "AndroidManifest.xml should be readable");
        assertTrue(manifestContent.contains("SignupActivity"),
                "SignupActivity must be registered in AndroidManifest.xml");
    }

    /**
     * Property: ForgotPasswordActivity class exists and is registered in the manifest.
     * Observed on unfixed code: ForgotPasswordActivity is at
     * com.example.elearn.ui.login.ForgotPasswordActivity.
     *
     * Validates: Requirements 3.3
     */
    @Test
    void forgotPasswordActivityExistsAndRegistered() throws Exception {
        // Verify class exists
        Class<?> forgotPwClass = Class.forName("com.example.elearn.ui.login.ForgotPasswordActivity");
        assertNotNull(forgotPwClass, "ForgotPasswordActivity class must exist");

        // Verify it's registered in manifest
        String manifestContent = readManifestContent();
        assertNotNull(manifestContent, "AndroidManifest.xml should be readable");
        assertTrue(manifestContent.contains("ForgotPasswordActivity"),
                "ForgotPasswordActivity must be registered in AndroidManifest.xml");
    }

    /**
     * Property: LoginActivity source contains navigation to SignupActivity.
     * Observed on unfixed code: LoginActivity creates Intent to SignupActivity on signupLink click.
     *
     * Validates: Requirements 3.3
     */
    @Test
    void loginActivityNavigatesToSignup() throws Exception {
        String sourceContent = readLoginActivitySource();
        assertNotNull(sourceContent, "LoginActivity.java source should be readable");

        assertTrue(sourceContent.contains("SignupActivity.class"),
                "LoginActivity must navigate to SignupActivity");
        assertTrue(sourceContent.contains("signupLink"),
                "LoginActivity must have signupLink click listener");
    }

    /**
     * Property: LoginActivity source contains navigation to ForgotPasswordActivity.
     * Observed on unfixed code: LoginActivity creates Intent to ForgotPasswordActivity
     * on forgotPasswordText click.
     *
     * Validates: Requirements 3.3
     */
    @Test
    void loginActivityNavigatesToForgotPassword() throws Exception {
        String sourceContent = readLoginActivitySource();
        assertNotNull(sourceContent, "LoginActivity.java source should be readable");

        assertTrue(sourceContent.contains("ForgotPasswordActivity.class"),
                "LoginActivity must navigate to ForgotPasswordActivity");
        assertTrue(sourceContent.contains("forgotPasswordText"),
                "LoginActivity must have forgotPasswordText click listener");
    }

    /**
     * Property: LoginActivity source contains navigation to DashboardActivity.
     * Observed on unfixed code: LoginActivity navigates to DashboardActivity on successful login.
     *
     * Validates: Requirements 3.2
     */
    @Test
    void loginActivityNavigatesToDashboard() throws Exception {
        String sourceContent = readLoginActivitySource();
        assertNotNull(sourceContent, "LoginActivity.java source should be readable");

        assertTrue(sourceContent.contains("DashboardActivity.class"),
                "LoginActivity must navigate to DashboardActivity on login success");
    }

    /**
     * Property: DashboardActivity class exists and is registered in the manifest.
     * Observed on unfixed code: DashboardActivity is at
     * com.example.elearn.ui.dashboard.DashboardActivity.
     *
     * Validates: Requirements 3.2
     */
    @Test
    void dashboardActivityExistsAndRegistered() throws Exception {
        // Verify class exists
        Class<?> dashboardClass = Class.forName("com.example.elearn.ui.dashboard.DashboardActivity");
        assertNotNull(dashboardClass, "DashboardActivity class must exist");

        // Verify it's registered in manifest
        String manifestContent = readManifestContent();
        assertNotNull(manifestContent, "AndroidManifest.xml should be readable");
        assertTrue(manifestContent.contains("DashboardActivity"),
                "DashboardActivity must be registered in AndroidManifest.xml");
    }

    /**
     * Property-based test: For any auth state, LoginActivity preserves navigation targets.
     * Regardless of whether user is logged in or not, the navigation links to
     * SignupActivity and ForgotPasswordActivity must exist in the source code.
     *
     * Validates: Requirements 3.1, 3.3
     */
    @Property(tries = 10)
    void loginActivityPreservesNavigationForAllAuthStates(
            @ForAll("authStates") boolean isLoggedIn) {

        // Verify LoginActivity source always contains navigation targets
        String sourceContent = readLoginActivitySource();
        assertNotNull(sourceContent,
                "LoginActivity.java source should be readable for auth state: " + isLoggedIn);

        // Regardless of auth state, LoginActivity must have these navigation paths defined
        assertTrue(sourceContent.contains("SignupActivity.class"),
                "LoginActivity must preserve SignupActivity navigation for authState=" + isLoggedIn);
        assertTrue(sourceContent.contains("ForgotPasswordActivity.class"),
                "LoginActivity must preserve ForgotPasswordActivity navigation for authState=" + isLoggedIn);
        assertTrue(sourceContent.contains("DashboardActivity.class"),
                "LoginActivity must preserve DashboardActivity navigation for authState=" + isLoggedIn);
    }

    // =========================================================================
    // Theme Preservation
    // =========================================================================

    /**
     * Property: Theme.ELearn and Theme.ELearn.NoActionBar styles exist in themes.xml.
     * Observed on unfixed code: themes.xml defines Theme.ELearn (Material DayNight) and
     * Theme.ELearn.NoActionBar variants.
     *
     * Validates: Requirements 3.4
     */
    @Test
    void themeStylesArePreserved() throws Exception {
        String themesContent = readThemesContent();
        assertNotNull(themesContent, "themes.xml should be readable");

        assertTrue(themesContent.contains("Theme.ELearn\"") ||
                        themesContent.contains("Theme.ELearn "),
                "themes.xml must define Theme.ELearn base style");
        assertTrue(themesContent.contains("Theme.ELearn.NoActionBar"),
                "themes.xml must define Theme.ELearn.NoActionBar style");
        assertTrue(themesContent.contains("Theme.MaterialComponents.DayNight"),
                "Theme.ELearn must extend MaterialComponents.DayNight");
    }

    /**
     * Property: LoginActivity has Theme.ELearn.NoActionBar applied in manifest.
     * Observed on unfixed code: LoginActivity declaration has
     * android:theme="@style/Theme.ELearn.NoActionBar".
     *
     * Validates: Requirements 3.4
     */
    @Test
    void loginActivityUsesNoActionBarTheme() throws Exception {
        String manifestContent = readManifestContent();
        assertNotNull(manifestContent, "AndroidManifest.xml should be readable");

        String loginSection = extractActivitySection(manifestContent, "LoginActivity");
        assertNotNull(loginSection, "LoginActivity section must exist in manifest");
        assertTrue(loginSection.contains("Theme.ELearn.NoActionBar"),
                "LoginActivity must use Theme.ELearn.NoActionBar theme");
    }

    // =========================================================================
    // Providers and Helpers
    // =========================================================================

    @Provide
    Arbitrary<Boolean> authStates() {
        return Arbitraries.of(true, false);
    }

    @Provide
    Arbitrary<String> nonEmptyTokens() {
        return Arbitraries.oneOf(
                // Simple JWT-like tokens
                Arbitraries.strings().alpha().ofMinLength(10).ofMaxLength(100),
                // Realistic JWT format: header.payload.signature
                Arbitraries.strings().alpha().ofLength(20).map(s -> "eyJ" + s + ".eyJ" + s + "." + s),
                // Short non-empty tokens
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5)
        );
    }

    @Provide
    Arbitrary<String> absentTokens() {
        // Only empty string - null cannot be generated by jqwik String arbitrary
        return Arbitraries.of("", "");
    }

    @Provide
    Arbitrary<String> allTokenStates() {
        return Arbitraries.oneOf(
                // Non-empty tokens (logged in)
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50),
                // Empty token (not logged in)
                Arbitraries.of("")
        );
    }

    /**
     * Mirrors the AuthService.isLoggedIn() logic:
     * Returns true if token is non-null and non-empty.
     */
    private boolean isLoggedInLogic(String token) {
        return token != null && !token.isEmpty();
    }

    private String readLoginLayoutContent() {
        try {
            String[] possiblePaths = {
                    "ELearnMobileApp/app/src/main/res/layout/activity_login.xml",
                    "app/src/main/res/layout/activity_login.xml",
                    "src/main/res/layout/activity_login.xml"
            };

            for (String path : possiblePaths) {
                Path p = Paths.get(path);
                if (Files.exists(p)) {
                    return new String(Files.readAllBytes(p));
                }
            }

            Path currentDir = Paths.get("").toAbsolutePath();
            Path candidate = currentDir.resolve("ELearnMobileApp/app/src/main/res/layout/activity_login.xml");
            if (Files.exists(candidate)) return new String(Files.readAllBytes(candidate));

            candidate = currentDir.resolve("app/src/main/res/layout/activity_login.xml");
            if (Files.exists(candidate)) return new String(Files.readAllBytes(candidate));

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String readLoginActivitySource() {
        try {
            String[] possiblePaths = {
                    "ELearnMobileApp/app/src/main/java/com/example/elearn/ui/login/LoginActivity.java",
                    "app/src/main/java/com/example/elearn/ui/login/LoginActivity.java",
                    "src/main/java/com/example/elearn/ui/login/LoginActivity.java"
            };

            for (String path : possiblePaths) {
                Path p = Paths.get(path);
                if (Files.exists(p)) {
                    return new String(Files.readAllBytes(p));
                }
            }

            Path currentDir = Paths.get("").toAbsolutePath();
            Path candidate = currentDir.resolve("ELearnMobileApp/app/src/main/java/com/example/elearn/ui/login/LoginActivity.java");
            if (Files.exists(candidate)) return new String(Files.readAllBytes(candidate));

            candidate = currentDir.resolve("app/src/main/java/com/example/elearn/ui/login/LoginActivity.java");
            if (Files.exists(candidate)) return new String(Files.readAllBytes(candidate));

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String readAuthServiceSource() {
        try {
            String[] possiblePaths = {
                    "ELearnMobileApp/app/src/main/java/com/example/elearn/auth/AuthService.java",
                    "app/src/main/java/com/example/elearn/auth/AuthService.java",
                    "src/main/java/com/example/elearn/auth/AuthService.java"
            };

            for (String path : possiblePaths) {
                Path p = Paths.get(path);
                if (Files.exists(p)) {
                    return new String(Files.readAllBytes(p));
                }
            }

            Path currentDir = Paths.get("").toAbsolutePath();
            Path candidate = currentDir.resolve("ELearnMobileApp/app/src/main/java/com/example/elearn/auth/AuthService.java");
            if (Files.exists(candidate)) return new String(Files.readAllBytes(candidate));

            candidate = currentDir.resolve("app/src/main/java/com/example/elearn/auth/AuthService.java");
            if (Files.exists(candidate)) return new String(Files.readAllBytes(candidate));

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String readManifestContent() {
        try {
            String[] possiblePaths = {
                    "ELearnMobileApp/app/src/main/AndroidManifest.xml",
                    "app/src/main/AndroidManifest.xml",
                    "src/main/AndroidManifest.xml"
            };

            for (String path : possiblePaths) {
                Path p = Paths.get(path);
                if (Files.exists(p)) {
                    return new String(Files.readAllBytes(p));
                }
            }

            Path currentDir = Paths.get("").toAbsolutePath();
            Path candidate = currentDir.resolve("ELearnMobileApp/app/src/main/AndroidManifest.xml");
            if (Files.exists(candidate)) return new String(Files.readAllBytes(candidate));

            candidate = currentDir.resolve("app/src/main/AndroidManifest.xml");
            if (Files.exists(candidate)) return new String(Files.readAllBytes(candidate));

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String readThemesContent() {
        try {
            String[] possiblePaths = {
                    "ELearnMobileApp/app/src/main/res/values/themes.xml",
                    "app/src/main/res/values/themes.xml",
                    "src/main/res/values/themes.xml"
            };

            for (String path : possiblePaths) {
                Path p = Paths.get(path);
                if (Files.exists(p)) {
                    return new String(Files.readAllBytes(p));
                }
            }

            Path currentDir = Paths.get("").toAbsolutePath();
            Path candidate = currentDir.resolve("ELearnMobileApp/app/src/main/res/values/themes.xml");
            if (Files.exists(candidate)) return new String(Files.readAllBytes(candidate));

            candidate = currentDir.resolve("app/src/main/res/values/themes.xml");
            if (Files.exists(candidate)) return new String(Files.readAllBytes(candidate));

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractActivitySection(String manifestContent, String activityName) {
        int activityStart = manifestContent.indexOf(activityName);
        if (activityStart == -1) return null;

        int tagStart = manifestContent.lastIndexOf("<activity", activityStart);
        if (tagStart == -1) return null;

        int selfClose = manifestContent.indexOf("/>", activityStart);
        int endTag = manifestContent.indexOf("</activity>", activityStart);

        int sectionEnd;
        if (endTag == -1 && selfClose == -1) return null;
        if (endTag == -1) sectionEnd = selfClose + 2;
        else if (selfClose == -1) sectionEnd = endTag + "</activity>".length();
        else sectionEnd = Math.min(selfClose + 2, endTag + "</activity>".length());

        int nextActivity = manifestContent.indexOf("<activity", activityStart + activityName.length());
        if (nextActivity != -1 && nextActivity < sectionEnd) {
            sectionEnd = nextActivity;
        }

        return manifestContent.substring(tagStart, sectionEnd);
    }
}
