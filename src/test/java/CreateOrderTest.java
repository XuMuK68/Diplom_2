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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CreateOrderTest {

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
    }

    // Получаем настоящие хеши ингредиентов из API
    private List<String> getIngredientIds() {
        Response response = orderRequests.getIngredients();

        response.then().statusCode(200);

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

    // Регистрируем пользователя для авторизованных запросов
    private void createAuthorizedUser() {
        String email = "user_" + UUID.randomUUID() + "@ya.ru";
        String password = new Faker().internet().password();
        String name = new Faker().name().firstName();

        User user = new User(email, password, name);

        Response response = userRequests.createUser(user);

        response.then()
                .statusCode(200)
                .body("success", equalTo(true));

        accessToken = response.jsonPath()
                .getString("accessToken");

        assertNotNull("Токен не должен быть null", accessToken);
        assertTrue(
                "Токен не должен быть пустым",
                !accessToken.trim().isEmpty()
        );
    }

    @Test
    @DisplayName("Создание заказа с авторизацией")
    public void createOrderWithAuthorization() {
        createAuthorizedUser();

        Order order = new Order(getIngredientIds());

        orderRequests.createOrder(order, accessToken)
                .then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    public void createOrderWithoutAuthorization() {
        Order order = new Order(getIngredientIds());

        orderRequests.createOrder(order)
                .then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание заказа с ингредиентами")
    public void createOrderWithIngredients() {
        List<String> ingredients = getIngredientIds();
        Order order = new Order(ingredients);

        orderRequests.createOrder(order)
                .then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    public void createOrderWithoutIngredients() {
        Order order = new Order(Collections.emptyList());

        orderRequests.createOrder(order)
                .then()
                .log().all()
                .statusCode(400)
                .body("success", equalTo(false));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиента")
    public void createOrderWithInvalidIngredientHash() {
        Order order = new Order(
                Collections.singletonList("invalid_hash_123")
        );

        orderRequests.createOrder(order)
                .then()
                .log().all()
                .statusCode(500);
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

