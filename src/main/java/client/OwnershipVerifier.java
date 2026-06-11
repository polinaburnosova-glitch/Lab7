package client;

import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import common.network.ResponseStatus;
import java.io.IOException;

/**
 * Проверка прав на элемент коллекции до ввода данных или создания объекта.
 */
public final class OwnershipVerifier {

    private OwnershipVerifier() {
    }

    /**
     * @return {@code true}, если элемент существует и принадлежит текущему пользователю
     */
    public static boolean verify(SimpleClient client, long id, String forbiddenMessage)
            throws IOException, ClassNotFoundException {
        Request request = new Request(
                CommandType.CHECK_OWNERSHIP,
                new Object[]{id, forbiddenMessage},
                client.getCurrentUser());
        client.sendRequest(request);
        Response response = client.receiveResponse();

        if (response.getStatus() == ResponseStatus.OK) {
            return true;
        }
        System.err.println("Ошибка: " + response.getMessage());
        return false;
    }
}
