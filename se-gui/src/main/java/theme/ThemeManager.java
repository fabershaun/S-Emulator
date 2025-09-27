package theme;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;

import java.util.HashMap;
import java.util.Map;

public class ThemeManager {
    private static ThemeManager instance;
    private final Map<String, String> themes = new HashMap<>();
    private final StringProperty currentTheme = new SimpleStringProperty();

    private ThemeManager() {
        // Define themes with correct paths
        themes.put("default", "/css/themes/default.css");
        themes.put("rainbow", "/css/themes/rainbow.css");
        themes.put("Dark", "/css/themes/dark.css");
    }

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    public void applyTheme(Scene scene, String themeName) {
        if (scene == null) return;

        String cssPath = themes.get(themeName);
        if (cssPath == null) {
            System.err.println("Theme not found: " + themeName);
            cssPath = themes.get("default"); // Fallback to default
            if (cssPath == null) return; // If default isn't defined either
        }

        try {
            // Clear previous themes
            scene.getStylesheets().clear();

            // Get the resource URL
            var resourceUrl = getClass().getResource(cssPath);
            if (resourceUrl == null) {
                System.err.println("Could not find resource: " + cssPath);
                return;
            }

            String resourcePath = resourceUrl.toExternalForm();
            scene.getStylesheets().add(resourcePath);
            currentTheme.set(themeName);

        } catch (Exception e) {
            System.err.println("Failed to load theme: " + themeName);
            e.printStackTrace();
        }
    }

    public StringProperty currentThemeProperty() {
        return currentTheme;
    }

    public String[] getAvailableThemes() {
        return themes.keySet().toArray(new String[0]);
    }
}