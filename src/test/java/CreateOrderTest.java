import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import methods.OrderRequests;
import methods.RequestSpec;
import models.Order;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CreateOrderTest {

    private OrderRequests orderRequests;

    @Before
    public void setUp() {
        RestAssured.requestSpecification =
                RequestSpec.requestSpecification();

        orderRequests = new OrderRequests();
    }

    // Получаем настоящие хеши ингредиентов из API
    private List<String> getIngredientIds() {
        io.restassured.response.Response response =
                orderRequests.getIngredients();

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
    @DisplayName("Создание заказа без авторизации")
    public void createOrderWithoutAuthorization() {
        Order order = new Order(getIngredientIds());

        orderRequests.createOrder(order)
                .then()
                .log().all()
                .statusCode(SC_OK)
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
                .statusCode(SC_OK)
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    public void createOrderWithoutIngredients() {
        Order order = new Order(Collections.emptyList());

        orderRequests.createOrder(order)
                .then()
                .log().all()
                .statusCode(SC_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
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
                .statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @After
    public void tearDown() {
        RestAssured.requestSpecification = null;
    }
}