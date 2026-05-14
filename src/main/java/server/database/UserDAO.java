package server.database;

import common.model.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class UserDAO {

    private static final String SHA256_PREFIX = "sha256:";

    private static String toHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    private static String sha256Hex(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return toHex(md.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 недоступен в JRE", e);
        }
    }

    /** Хэш для сохранения в БД (новые пользователи). */
    private static String hashPasswordForStorage(String password) {
        return SHA256_PREFIX + sha256Hex(password);
    }

    /** Старый формат (MD2), только для проверки уже существующих записей. */
    private static String legacyMd2Hex(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD2");
        return toHex(md.digest(password.getBytes(StandardCharsets.UTF_8)));
    }

    private static boolean passwordMatches(String password, String storedHash) {
        if (storedHash == null) {
            return false;
        }
        if (storedHash.startsWith(SHA256_PREFIX)) {
            return storedHash.equals(hashPasswordForStorage(password));
        }
        try {
            return storedHash.equals(legacyMd2Hex(password));
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

    public static boolean register(String username, String password) {
        String hash = hashPasswordForStorage(password);
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hash);
            stmt.executeUpdate();
            return true;
        }

        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static User login(String username, String password) {
        String sql = "SELECT username, password_hash FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (passwordMatches(password, storedHash)) {
                    return new User(rs.getString("username"), storedHash);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean userExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }

        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
