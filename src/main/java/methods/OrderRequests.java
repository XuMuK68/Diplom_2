package methods;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.Order;

import static constants.ApiConstants.INGREDIENTS;
import static constants.ApiConstants.ORDERS;

public class OrderRequests {

    @Step("Получение списка ингредиентов")
    public Response getIngredients() {
        return RequestSpec.requestSpecification()
                .when()
                .get(INGREDIENTS);
    }

    @Step("Создание заказа")
    public Response createOrder(Order order) {
        return RequestSpec.requestSpecification()
                .body(order)
                .when()
                .post(ORDERS);
    }

    @Step("Создание заказа авторизованным пользователем")
    public Response createOrder(Order order, String token) {
        return RequestSpec.requestSpecification()
                .header("Authorization", token)
                .body(order)
                .when()
                .post(ORDERS);
    }

    @Step("Получение заказов авторизованного пользователя")
    public Response getUserOrders(String token) {
        return RequestSpec.requestSpecification()
                .header("Authorization", token)
                .when()
                .get(ORDERS);
    }

    @Step("Получение заказов без авторизации")
    public Response getUserOrdersWithoutAuthorization() {
        return RequestSpec.requestSpecification()
                .when()
                .get(ORDERS);
    }
}
