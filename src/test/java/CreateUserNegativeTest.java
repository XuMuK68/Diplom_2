import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import methods.RequestSpec;
import methods.UserRequests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import models.User;

import java.util.UUID;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.equalTo;

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
        User body = new User(null, "Test12345", "Александр");
        userRequests.createUser(body)
                .then()
                .log().all()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message",
                        equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Регистрация без пароля")
    public void createUserWithoutPassword() {
        String email = "user_" + UUID.randomUUID() + "@ya.ru";
        User body = new User(email, null, "Александр");

        userRequests.createUser(body)
                .then()
                .log().all()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message",
                        equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Регистрация без имени")
    public void createUserWithoutName() {
        String email = "user_" + UUID.randomUUID() + "@ya.ru";
        User body = new User(email, "Test12345", null);

        userRequests.createUser(body)
                .then()
                .log().all()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message",
                        equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Регистрация уже существующего пользователя")
    public void createUserAlreadyExists() {
        String email = "user_" + UUID.randomUUID() + "@ya.ru";
        String password = "Test12345";
        String name = "Александр";

        User body = new User(email, password, name);

        // Первая регистрация пользователя
        Response createResponse = userRequests.createUser(body);

        createResponse.then()
                .log().all()
                .statusCode(SC_OK)
                .body("success", equalTo(true));

        accessToken = createResponse.jsonPath()
                .getString("accessToken");

        // Повторная регистрация с теми же данными
        userRequests.createUser(body)
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