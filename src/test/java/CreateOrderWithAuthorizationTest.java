import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import methods.OrderRequests;
import methods.RequestSpec;
import methods.UserRequests;
import models.Order;
import models.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CreateOrderWithAuthorizationTest {

    private OrderRequests orderRequests;
    private UserRequests userRequests;
    private String accessToken;

    @Before
    public void setUp() {
        RestAssured.requestSpecification =
                RequestSpec.requestSpecification();

        orderRequests = new OrderRequests();
        userRequests = new UserRequests();

        accessToken = null;

        createAuthorizedUser();
    }

    // Регистрируем пользователя перед выполнением теста
    private void createAuthorizedUser() {
        String email = "user_" + UUID.randomUUID() + "@ya.ru";
        String password = new Faker().internet().password();
        String name = new Faker().name().firstName();

        User user = new User(email, password, name);

        Response response = userRequests.createUser(user);

        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true));

        accessToken = response.jsonPath()
                .getString("accessToken");

        assertNotNull(
                "Токен не должен быть null",
                accessToken
        );

        assertFalse(
                "Токен не должен быть пустым",
                accessToken.trim().isEmpty()
        );
    }

    // Получаем настоящие хеши ингредиентов из API
    private List<String> getIngredientIds() {
        Response response = orderRequests.getIngredients();

        response.then().statusCode(SC_OK);

        List<String> ingredients = response.jsonPath()
                .getList("data._id", String.class);

        assertNotNull(
                "Список ингредиентов не должен быть null",
                ingredients
        );

        assertTrue(
                "В списке должно быть не менее двух ингредиентов",
                ingredients.size() >= 2
        );

        return ingredients.subList(0, 2);
    }

    @Test
    @DisplayName("Создание заказа с авторизацией")
    public void createOrderWithAuthorization() {
        Order order = new Order(getIngredientIds());

        orderRequests.createOrder(order, accessToken)
                .then()
                .log().all()
                .statusCode(SC_OK)
                .body("success", equalTo(true));
    }

    @After
    public void tearDown() {
        try {
            if (accessToken != null
                    && !accessToken.trim().isEmpty()) {

                userRequests.deleteUser(accessToken)
                        .then()
                        .log().all()
                        .statusCode(SC_ACCEPTED)
                        .body("success", equalTo(true));
            }
        } finally {
            RestAssured.requestSpecification = null;
        }
    }
}