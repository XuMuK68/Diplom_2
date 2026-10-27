import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import methods.RequestSpec;
import methods.UserRequests;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.apache.http.HttpStatus.*;

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
    @DisplayName("Регистрация без email")
    public void createUserWithoutEmail() {
        Map<String, String> body = new HashMap<>();
        body.put("password", "Test12345");
        body.put("name", "Александр");

        userRequests.createUserWithBody(body)
                .then()
                .log().all()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Регистрация без пароля")
    public void createUserWithoutPassword() {
        String email = "user_" + UUID.randomUUID() + "@ya.ru";

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("name", "Александр");

        userRequests.createUserWithBody(body)
                .then()
                .log().all()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Регистрация без имени")
    public void createUserWithoutName() {
        String email = "user_" + UUID.randomUUID() + "@ya.ru";

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", "Test12345");

        userRequests.createUserWithBody(body)
                .then()
                .log().all()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Регистрация уже существующего пользователя")
    public void createUserAlreadyExists() {
        String email = "user_" + UUID.randomUUID() + "@ya.ru";
        String password = "Test12345";
        String name = "Александр";

        // Формируем тело запроса с данными пользователя
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("name", name);

        // Сначала регистрируем пользователя
        Response createResponse =
                userRequests.createUserWithBody(body);

        createResponse.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true));

        accessToken = createResponse.jsonPath()
                .getString("accessToken");

        // Повторно регистрируем те же данные
        userRequests.createUserWithBody(body)
                .then()
                .log().all()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @After
    public void deleteUser() {
        if (accessToken != null && !accessToken.isEmpty()) {
            userRequests.deleteUser(accessToken)
                    .then()
                    .log().all()
                    .statusCode(SC_ACCEPTED)
                    .body("success", equalTo(true));
        }
    }
}