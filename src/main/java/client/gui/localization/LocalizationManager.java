package client.gui.localization;

import common.model.Mood;
import common.model.WeaponType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Менеджер локализации. Ресурсы хранятся в {@code Messages*.properties}
 * и загружаются через этот класс.
 */
public final class LocalizationManager {

    public static final String LANG_RU = "Русский";
    public static final String LANG_DE = "Deutsch";
    public static final String LANG_HU = "Magyar";
    public static final String LANG_ES = "Español";

    private static ResourceBundle bundle;
    private static Locale currentLocale;
    private static String currentLanguageName = LANG_RU;

    private static final String BASE_NAME = "client.gui.localization.Messages";

    private LocalizationManager() {
    }

    static {
        setLocale(LANG_RU);
    }

    public static void setLocale(String language) {
        currentLanguageName = language;
        switch (language) {
            case LANG_DE:
                currentLocale = Locale.forLanguageTag("de");
                break;
            case LANG_HU:
                currentLocale = Locale.forLanguageTag("hu");
                break;
            case LANG_ES:
                currentLocale = new Locale("es", "GT");
                break;
            case LANG_RU:
            default:
                currentLocale = Locale.forLanguageTag("ru");
                currentLanguageName = LANG_RU;
                break;
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

    public static String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return "???" + key + "???";
        }
    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    public static String getCurrentLanguageName() {
        return currentLanguageName;
    }

    public static String formatNumber(Number value) {
        if (value == null) {
            return "";
        }
        return NumberFormat.getNumberInstance(currentLocale).format(value);
    }

    public static double parseNumber(String text) throws ParseException {
        return NumberFormat.getNumberInstance(currentLocale).parse(text.trim()).doubleValue();
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withLocale(currentLocale)
                .format(dateTime);
    }

    public static String formatBoolean(Boolean value) {
        if (value == null) {
            return "";
        }
        return value ? getString("value.yes") : getString("value.no");
    }

    public static String formatMood(Mood mood) {
        if (mood == null) {
            return getString("mood.none");
        }
        return getString("mood." + mood.name());
    }

    public static String formatWeaponType(WeaponType weaponType) {
        if (weaponType == null) {
            return "";
        }
        return getString("weapon." + weaponType.name());
    }
}
