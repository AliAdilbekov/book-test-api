package examples;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BookingTest {
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
    }

    @Test
    public void createBookingTest() {
        String requestBody = """
                {
                    "firstname" : "Jim",
                    "lastname" : "Brown",
                    "totalprice" : 111,
                    "depositpaid" : true,
                    "bookingdates" : {
                        "checkin" : "2018-01-01",
                        "checkout" : "2019-01-01"
                    },
                    "additionalneeds" : "Breakfast"
                }
                """;

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/booking")
                .then()
                .statusCode(200)
                .extract().response();

        assertNotNull(response.jsonPath().getInt("bookingid"));
        int bookingId = response.jsonPath().getInt("bookingid");

        // Проверка сохранения бронирования с верными данными
        Response getResponse = given()
                .pathParam("id", bookingId)
                .when()
                .get("/booking/{id}")
                .then()
                .statusCode(200)
                .extract().response();

        assertEquals("Jim", getResponse.jsonPath().getString("firstname"));
        assertEquals("Brown", getResponse.jsonPath().getString("lastname"));
        assertEquals(111, getResponse.jsonPath().getInt("totalprice"));
        assertEquals(true, getResponse.jsonPath().getBoolean("depositpaid"));
        assertEquals("2018-01-01", getResponse.jsonPath().getString("bookingdates.checkin"));
        assertEquals("2019-01-01", getResponse.jsonPath().getString("bookingdates.checkout"));
        assertEquals("Breakfast", getResponse.jsonPath().getString("additionalneeds"));

        // Фильтрация списка ID с созданными бронированиями
        Response bookingIdsResponse = given()
                .when()
                .get("/booking")
                .then()
                .statusCode(200)
                .extract().response();

        assertNotNull(bookingIdsResponse.jsonPath().getList("bookingid"));

        // Проверка фильтрации по имени
        Response filterByNameResponse = given()
                .queryParam("firstname", "Jim")
                .queryParam("lastname", "Brown")
                .when()
                .get("/booking")
                .then()
                .statusCode(200)
                .extract().response();

        assertNotNull(filterByNameResponse.jsonPath().getList("bookingid"));

        // Проверка фильтрации по дате заезда и выезда
        Response filterByDateResponse = given()
                .queryParam("checkin", "2018-01-01")
                .queryParam("checkout", "2019-01-01")
                .when()
                .get("/booking")
                .then()
                .statusCode(200)
                .extract().response();

        assertNotNull(filterByDateResponse.jsonPath().getList("bookingid"));
    }
}
