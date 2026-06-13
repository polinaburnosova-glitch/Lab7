package server.database;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Менеджер подключения к базе данных PostgreSQL.
 * Обеспечивает получение соединения и его закрытие.
 * Настройки подключения читаются из файла .env.
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 */
public class DatabaseManager {
    private static Connection connection = null;

    /**
     * Формирует URL для подключения к БД из переменных окружения.
     *
     * @return строка подключения JDBC
     */
    public static String getUrl() {
        Dotenv dotenv = Dotenv.load();
        String host = dotenv.get("DB_HOST", "pg");
        String port = dotenv.get("DB_PORT", "5432");
        String dbName = dotenv.get("DB_NAME", "studs");
        return "jdbc:postgresql://" + host + ":" + port + "/" + dbName + "?allowEncodingChanges=true";
    }

    /**
     * Возвращает имя пользователя БД из .env.
     *
     * @return имя пользователя
     */
    private static String getUser() {
        return Dotenv.load().get("DB_USER", "studs");
    }

    /**
     * Возвращает пароль пользователя БД из .env.
     *
     * @return пароль
     */
    private static String getPassword() {
        return Dotenv.load().get("DB_PASSWORD", "");
    }

    /**
     * Возвращает активное соединение с БД.
     * Если соединение отсутствует или закрыто — создаёт новое.
     *
     * @return соединение с БД
     * @throws SQLException при ошибке подключения
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(getUrl(), getUser(), getPassword());
        }
        return connection;
    }

    /**
     * Закрывает текущее соединение с БД.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}