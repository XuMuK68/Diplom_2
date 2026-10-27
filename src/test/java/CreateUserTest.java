import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import methods.RequestSpec;
import methods.UserRequests;
import models.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CreateUserTest {

    private User user;
    private UserRequests userRequests;
    private String accessToken;

    @Before
    public void setUp() {
        RestAssured.requestSpecification =
                RequestSpec.requestSpecification();

        Faker faker = new Faker();

        String email = "user_" + UUID.randomUUID() + "@ya.ru";
        String password = faker.internet().password();
        String name = faker.name().firstName();

        user = new User(email, password, name);
        userRequests = new UserRequests();
        accessToken = null;
    }

    @Test
    @DisplayName("Успешное создание пользователя")
    public void createUser() {
        Response response = userRequests.createUser(user);

        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true));

        accessToken = response.jsonPath().getString("accessToken");

        assertNotNull(
                "Токен авторизации не должен быть null",
                accessToken
        );

        assertTrue(
                "Токен авторизации не должен быть пустым",
                !accessToken.trim().isEmpty()
        );
    }

    @After
    public void deleteUser() {
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            Response response = userRequests.deleteUser(accessToken);

            response.then()
                    .statusCode(SC_ACCEPTED)
                    .body("success", equalTo(true));
        }

        RestAssured.requestSpecification = null;
    }
}