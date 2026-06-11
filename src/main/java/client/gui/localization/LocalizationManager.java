package client.gui.localization;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Менеджер локализации для переключения языков интерфейса.
 * Поддерживает русский, немецкий, венгерский и испанский (Гватемала).
 *
 * @author Полина
 * @version 1.0
 */
public class LocalizationManager {

    private static ResourceBundle bundle;
    private static Locale currentLocale;

    private static final String BASE_NAME = "client.gui.localization.Messages";

    static {
        setLocale("Русский");
    }

    /**
     * Устанавливает язык интерфейса.
     *
     * @param language язык ("Русский", "Deutsch", "Magyar", "Español")
     */
    public static void setLocale(String language) {
        switch (language) {
            case "Русский":
                currentLocale = new Locale("ru");
                break;
            case "Deutsch":
                currentLocale = new Locale("de");
                break;
            case "Magyar":
                currentLocale = new Locale("hu");
                break;
            case "Español":
                currentLocale = new Locale("es");
                break;
            default:
                currentLocale = new Locale("ru");
        }
        bundle = ResourceBundle.getBundle(BASE_NAME, currentLocale, new Utf8Control());
    }

    private static final class Utf8Control extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format,
                                        ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            try (InputStream stream = loader.getResourceAsStream(resourceName)) {
                if (stream == null) {
                    return null;
                }
                try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    return new PropertyResourceBundle(reader);
                }
            }
        }
    }

    /**
     * Возвращает локализованную строку по ключу.
     *
     * @param key ключ в файле .properties
     * @return локализованная строка
     */
    public static String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return "???" + key + "???";
        }
    }

    /**
     * Возвращает текущую локаль.
     *
     * @return текущий объект Locale
     */
    public static Locale getCurrentLocale() {
        return currentLocale;
    }
}