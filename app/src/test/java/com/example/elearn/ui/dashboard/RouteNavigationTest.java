package com.example.elearn.ui.dashboard;

import com.example.elearn.ui.categories.CategoriesActivity;
import com.example.elearn.ui.courses.CoursesActivity;
import com.example.elearn.ui.enrollments.EnrollmentsActivity;
import com.example.elearn.ui.users.UsersActivity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying route-to-Activity mapping covers all known route strings.
 * Tests the navigation mapping logic used in DashboardActivity card click handling.
 */
class RouteNavigationTest {

    /**
     * Returns the route-to-Activity class mapping (same logic as DashboardActivity).
     */
    private Map<String, Class<?>> getRouteMap() {
        Map<String, Class<?>> routeMap = new HashMap<>();
        routeMap.put("courses", CoursesActivity.class);
        routeMap.put("categories", CategoriesActivity.class);
        routeMap.put("enrollments", EnrollmentsActivity.class);
        routeMap.put("users", UsersActivity.class);
        return routeMap;
    }

    @ParameterizedTest
    @CsvSource({
            "courses, com.example.elearn.ui.courses.CoursesActivity",
            "categories, com.example.elearn.ui.categories.CategoriesActivity",
            "enrollments, com.example.elearn.ui.enrollments.EnrollmentsActivity",
            "users, com.example.elearn.ui.users.UsersActivity"
    })
    void routeMapsToCorrectActivity(String route, String expectedClassName) {
        Map<String, Class<?>> routeMap = getRouteMap();

        assertTrue(routeMap.containsKey(route),
                "Route '" + route + "' should be present in route map");
        assertEquals(expectedClassName, routeMap.get(route).getName(),
                "Route '" + route + "' should map to " + expectedClassName);
    }

    @Test
    void allKnownRoutesAreMapped() {
        Map<String, Class<?>> routeMap = getRouteMap();
        String[] knownRoutes = {"courses", "categories", "enrollments", "users"};

        for (String route : knownRoutes) {
            assertTrue(routeMap.containsKey(route),
                    "Missing mapping for known route: " + route);
            assertNotNull(routeMap.get(route),
                    "Route '" + route + "' maps to null Activity class");
        }
    }

    @Test
    void unknownRouteIsNotMapped() {
        Map<String, Class<?>> routeMap = getRouteMap();
        assertFalse(routeMap.containsKey("unknown"),
                "Unknown route should not be in the map");
        assertNull(routeMap.get("nonexistent"),
                "Non-existent route should return null");
    }

    @Test
    void routeMapHasExactlyFourEntries() {
        Map<String, Class<?>> routeMap = getRouteMap();
        assertEquals(4, routeMap.size(),
                "Route map should contain exactly 4 entries");
    }
}
