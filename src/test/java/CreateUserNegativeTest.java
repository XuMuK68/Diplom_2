import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import methods.RequestSpec;
import methods.UserRequests;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import static org.hamcrest.Matchers.equalTo;

import java.util.UUID;

public class CreateUserNegativeTest {

    private String accessToken;
    private UserRequests userRequests;

    @Before
    public void setUp() {
        RestAssured.requestSpecification =
                RequestSpec.requestSpecification();
        userRequests = new UserRequests();
    }

    @Test
    @DisplayName("Регистрация без пароля")
    public void createUserWithoutPassword() {
        String email = "user_" + UUID.randomUUID() + "@ya.ru";

        String body = "{"
                + "\"email\":\"" + email + "\","
                + "\"name\":\"Александр\""
                + "}";

        Response response =
                userRequests.createUserWithBody(body);

        response.then()
                .log().all()
                .statusCode(403);
    }

    @Test
    @DisplayName("Регистрация уже существующего пользователя")
    public void createUserAlreadyExists() {
        String email = "user_" + UUID.randomUUID() + "@ya.ru";
        String password = "Test12345";
        String name = "Александр";

        String body = "{"
                + "\"email\":\"" + email + "\","
                + "\"password\":\"" + password + "\","
                + "\"name\":\"" + name + "\""
                + "}";

        // Сначала регистрируем пользователя
        Response createResponse =
                userRequests.createUserWithBody(body);

        createResponse.then()
                .statusCode(200)
                .body("success", equalTo(true));

        accessToken = createResponse.jsonPath()
                .getString("accessToken");

        // Повторно регистрируем те же данные
        userRequests.createUserWithBody(body)
                .then()
                .log().all()
                .statusCode(403);
    }

    @After
    public void deleteUser() {
        if (accessToken != null && !accessToken.isEmpty()) {
            userRequests.deleteUser(accessToken)
                    .then()
                    .log().all()
                    .statusCode(202)
                    .body("success", equalTo(true));
        }
    }
}
