package iteration1;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class CreateUserTest {

    @BeforeAll
    public static void setUpRestAssured() {
        RestAssured.filters(
                List.of(
                        new RequestLoggingFilter(),
                        new ResponseLoggingFilter()
                )
        );
    }

    @Test
    public void adminCanCreateUserWithCorrectDataTest() {
        // create user
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "Kate200111",
                          "password": "Kate2000#",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("username", Matchers.equalTo("Kate200111"))
                .body("password", Matchers.not(Matchers.equalTo("Kate2000#")))
                .body("role", Matchers.equalTo("USER"));
    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                //username fill validation
                Arguments.of("  ", "Password33", "USER", "username", "Username cannot be blank"),
                Arguments.of("ab", "Password33", "USER", "username", "Username must be between 3 and 15 characters"),
                Arguments.of("abс$", "Password33", "USER", "username", "Username must be between 3 and 15 characters"));
    }

    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithIncorrectDataTest(String username, String password, String role, String errorKey, String errorValue) {
        String requestBody = String.format(
                """
                        {
                          "username": "%s",
                          "password": "%s",
                          "role": "%s"
                        }
                        """, username, password, role
        );

        // create user test
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(errorKey, Matchers.equalTo(errorValue));
    }
}
