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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class CreateUserTest {

    private final String email;
    private final String password;
    private final String name;

    private User user;
    private UserRequests userRequests;
    private String accessToken;

    public CreateUserTest(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    @Parameterized.Parameters(name = "Создание пользователя: {0}")
    public static Object[][] getData() {
        Faker faker = new Faker();

        return new Object[][]{
                {
                        "user_" + UUID.randomUUID() + "@ya.ru",
                        faker.internet().password(),
                        faker.name().firstName()
                },
                {
                        "user_" + UUID.randomUUID() + "@ya.ru",
                        faker.internet().password(),
                        faker.name().firstName()
                },
                {
                        "user_" + UUID.randomUUID() + "@ya.ru",
                        faker.internet().password(),
                        faker.name().firstName()
                }
        };
    }

    @Before
    public void setUp() {
        RestAssured.requestSpecification =
                RequestSpec.requestSpecification();

        user = new User(email, password, name);
        userRequests = new UserRequests();
        accessToken = null;
    }

    @Test
    @DisplayName("Успешное создание пользователя")
    public void createUser() {
        Response response = userRequests.createUser(user);

        response.then()
                .statusCode(200)
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
                    .statusCode(202)
                    .body("success", equalTo(true));
        }

        RestAssured.requestSpecification = null;
    }
}
