import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import methods.RequestSpec;
import methods.UserRequests;
import models.Login;
import models.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LoginUserTest {

    private UserRequests userRequests;
    private String accessToken;
    private String email;
    private String password;
    private String name;

    @Before
    public void setUp() {
        RestAssured.requestSpecification =
                RequestSpec.requestSpecification();

        userRequests = new UserRequests();
        accessToken = null;

        email = "user_" + UUID.randomUUID() + "@ya.ru";
        password = new Faker().internet().password();
        name = new Faker().name().firstName();

        User user = new User(email, password, name);

        // Создаём пользователя перед тестом
        Response createResponse = userRequests.createUser(user);

        createResponse.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true));

        accessToken = createResponse.jsonPath()
                .getString("accessToken");
    }

    @Test
    @DisplayName("Успешный вход под существующим пользователем")
    public void loginExistingUser() {
        // Выполняем вход под созданным пользователем
        Login login = new Login(email, password);

        Response loginResponse = userRequests.loginUser(login);

        loginResponse.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true));

        String loginAccessToken = loginResponse.jsonPath()
                .getString("accessToken");

        assertNotNull(
                "Токен после входа не должен быть null",
                loginAccessToken
        );

        assertTrue(
                "Токен после входа не должен быть пустым",
                !loginAccessToken.trim().isEmpty()
        );
    }

    @Test
    @DisplayName("Вход с неверным логином")
    public void loginWithIncorrectEmail() {
        String incorrectEmail =
                "unknown_" + UUID.randomUUID() + "@ya.ru";

        Login login = new Login(incorrectEmail, password);

        userRequests.loginUser(login)
                .then()
                .log().all()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Вход с неверным паролем")
    public void loginWithIncorrectPassword() {
        String incorrectPassword = "WrongPassword123";

        Login login = new Login(email, incorrectPassword);

        userRequests.loginUser(login)
                .then()
                .log().all()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false));
    }

    @After
    public void deleteUser() {
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            userRequests.deleteUser(accessToken)
                    .then()
                    .log().all()
                    .statusCode(SC_ACCEPTED)
                    .body("success", equalTo(true));
        }

        RestAssured.requestSpecification = null;
    }
}