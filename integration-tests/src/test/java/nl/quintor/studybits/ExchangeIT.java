package nl.quintor.studybits;

import io.restassured.http.ContentType;
import io.restassured.response.ResponseBody;
import nl.quintor.studybits.enums.ExchangePositionState;
import nl.quintor.studybits.models.ExchangeApplicationModel;
import nl.quintor.studybits.models.ExchangePositionModel;
import nl.quintor.studybits.models.SchemaDefinitionModel;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ExchangeIT extends BaseIT {
    private String UNIVERSITY_NAME = "rug";
    private String EXCHANGE_UNIVERSITY_NAME = "gent";
    private String STUDENT_NAME = "lisa";
    private String ADMIN_NAME = "admin2-gent";

    @Before
    public void setUp() {
        super.setUp();

        setupStudentUniversityTriangle(STUDENT_NAME, UNIVERSITY_NAME, EXCHANGE_UNIVERSITY_NAME);
    }

    @Test
    public void testCreateExchangePosition() {
        createExchangePosition();
    }

    @Test
    public void testApplyForExchangePosition() {
        createExchangePosition();
        applyForExchangePosition();
    }

    @Test
    public void testAcceptNewExchangeApplication() {
        createExchangePosition();
        applyForExchangePosition();
        acceptExchangeApplication();
    }

    private void createExchangePosition() {
        ExchangePositionModel model = getTranscriptPositionModel(EXCHANGE_UNIVERSITY_NAME);
        givenCorrectHeaders(UNIVERSITY_URL)
                .pathParam("universityName", EXCHANGE_UNIVERSITY_NAME)
                .pathParam("userName", ADMIN_NAME)
                .body(model)
                .post("/{universityName}/admin/{userName}/positions")
                .then()
                .assertThat().statusCode(200);
    }

    private void applyForExchangePosition() {
        List<ExchangePositionModel> positions = getAllExchangePositionModels();
        givenCorrectHeaders(STUDENT_URL)
                .pathParam("studentUserName", STUDENT_NAME)
                .body(positions.get(0))
                .post("/student/{studentUserName}/positions")
                .then()
                .assertThat().statusCode(200);
    }

    private void acceptExchangeApplication() {
        List<ExchangeApplicationModel> applications = getAllExchangeApplicationModels();
        givenCorrectHeaders(UNIVERSITY_URL)
                .pathParam("universityName", EXCHANGE_UNIVERSITY_NAME)
                .pathParam("userName", ADMIN_NAME)
                .body(applications.get(0))
                .post("/{universityName}/admin/{userName}/applications")
                .then()
                .assertThat().statusCode(200);
    }

    private List<ExchangeApplicationModel> getAllExchangeApplicationModels() {
        ResponseBody body = givenCorrectHeaders(UNIVERSITY_URL)
                .pathParam("universityName", EXCHANGE_UNIVERSITY_NAME)
                .pathParam("userName", EXCHANGE_UNIVERSITY_NAME)
                .get("/{universityName}/admin/{userName}/applications")
                .then()
                .contentType(ContentType.JSON)
                .extract()
                .response()
                .getBody();

        return Arrays.asList(body.as(ExchangeApplicationModel[].class));
    }

    private List<ExchangePositionModel> getAllExchangePositionModels() {
        ResponseBody body = givenCorrectHeaders(UNIVERSITY_URL)
                .pathParam("universityName", EXCHANGE_UNIVERSITY_NAME)
                .pathParam("userName", STUDENT_NAME)
                .get("/{universityName}/student/{userName}/positions")
                .then()
                .contentType(ContentType.JSON)
                .extract()
                .response()
                .getBody();

        return Arrays.asList(body.as(ExchangePositionModel[].class));
    }

    private ExchangePositionModel getTranscriptPositionModel(String exchangeUniversityName) {
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("degree", "Bachelor of Science, Marketing");
        attributes.put("status", "graduated");

        return new ExchangePositionModel(
                exchangeUniversityName,
                new SchemaDefinitionModel("Transcript", "0.1", null),
                null,
                ExchangePositionState.OPEN,
                attributes
        );
    }
}
