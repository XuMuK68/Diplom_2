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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LoginUserTest {

    private UserRequests userRequests;
    private String accessToken;

    @Before
    public void setUp() {
        RestAssured.requestSpecification =
                RequestSpec.requestSpecification();

        userRequests = new UserRequests();
        accessToken = null;
    }

    @Test
    @DisplayName("Успешный вход под существующим пользователем")
    public void loginExistingUser() {
        System.out.println("ALLURE TEST STARTED");
        String email = "user_" + UUID.randomUUID() + "@ya.ru";
        String password = new Faker().internet().password();
        String name = new Faker().name().firstName();

        User user = new User(email, password, name);

        // Создаём пользователя
        Response createResponse = userRequests.createUser(user);

        createResponse.then()
                .statusCode(200)
                .body("success", equalTo(true));

        accessToken = createResponse.jsonPath()
                .getString("accessToken");

        // Выполняем вход под созданным пользователем
        Login login = new Login(email, password);

        Response loginResponse = userRequests.loginUser(login);

        loginResponse.then()
                .statusCode(200)
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
    @DisplayName("Вход с неверными логином и паролем")
    public void loginWithIncorrectCredentials() {
        String email = "unknown_" + UUID.randomUUID() + "@ya.ru";
        String password = "WrongPassword123";

        Login login = new Login(email, password);

        userRequests.loginUser(login)
                .then()
                .log().all()
                .statusCode(401)
                .body("success", equalTo(false));
    }

    @After
    public void deleteUser() {
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            userRequests.deleteUser(accessToken)
                    .then()
                    .log().all()
                    .statusCode(202)
                    .body("success", equalTo(true));
        }

        RestAssured.requestSpecification = null;
    }
}
