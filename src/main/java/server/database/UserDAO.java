package server.database;

import common.model.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

/**
 * Data Access Object для работы с таблицей users в БД.
 * Обеспечивает регистрацию, авторизацию и проверку существования пользователей.
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 */
public class UserDAO {

    /** Префикс для SHA-256 хэшей, хранящихся в БД. */
    private static final String SHA256_PREFIX = "sha256:";

    /**
     * Преобразует массив байт в шестнадцатеричную строку.
     *
     * @param bytes массив байт
     * @return шестнадцатеричная строка
     */
    private static String toHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    /**
     * Вычисляет SHA-256 хэш пароля.
     *
     * @param password пароль в открытом виде
     * @return шестнадцатеричная строка хэша
     */
    private static String sha256Hex(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return toHex(md.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 недоступен в JRE", e);
        }
    }

    /**
     * Формирует хэш для сохранения в БД (с префиксом SHA-256).
     *
     * @param password пароль в открытом виде
     * @return строка для сохранения в БД
     */
    private static String hashPassword(String password) {
        String hash = sha256Hex(password);
        return sha256Hex(password);
    }


    /**
     * Регистрирует нового пользователя в БД.
     *
     * @param username логин пользователя
     * @param password пароль в открытом виде
     * @return true если регистрация успешна, false иначе
     */
    public static boolean register(String username, String password) {
        String hash = hashPassword(password);
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hash);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Выполняет авторизацию пользователя.
     *
     * @param username логин пользователя
     * @param password пароль в открытом виде
     * @return объект User при успешной авторизации, null иначе
     */
    public static User login(String username, String password) {
        String hash = hashPassword(password);
        String sql = "SELECT username, password_hash FROM users WHERE username = ? AND password_hash = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hash);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(rs.getString("username"), rs.getString("password_hash"));
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Проверяет, существует ли пользователь с указанным логином.
     *
     * @param username логин пользователя
     * @return true если пользователь существует, false иначе
     */
    public static boolean userExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}