package nl.quintor.studybits;

import io.restassured.http.ContentType;
import io.restassured.response.ResponseBody;
import nl.quintor.studybits.enums.ExchangeApplicationState;
import nl.quintor.studybits.enums.ExchangePositionState;
import nl.quintor.studybits.models.ExchangeApplicationModel;
import nl.quintor.studybits.models.ExchangePositionModel;
import nl.quintor.studybits.models.SchemaDefinitionModel;
import org.junit.Assert;
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
        createExchangePosition(false);
        Assert.assertEquals(1, getAllExchangePositionModels().size());
    }

    @Test
    public void testApplyForExchangePosition() {
        createExchangePosition(false);
        applyForExchangePosition();
        Assert.assertEquals(1, getAllExchangeApplicationModels().size());
    }

    @Test
    public void testAcceptNewExchangeApplicationWithMinimalRequirements() {
        createExchangePosition(false);
        applyForExchangePosition();
        acceptExchangeApplication();

        ExchangeApplicationState newState = getAllExchangeApplicationModels().get(0).getState();
        Assert.assertEquals(ExchangeApplicationState.ACCEPTED, newState);
    }

    @Test
    public void testAcceptNewExchangeApplicationWithFullRequirements() {
        createExchangePosition(true);
        applyForExchangePosition();
        acceptExchangeApplication();

        ExchangeApplicationState newState = getAllExchangeApplicationModels().get(0).getState();
        Assert.assertEquals(ExchangeApplicationState.ACCEPTED, newState);
    }

    private void createExchangePosition(Boolean withFullRequirements) {
        ExchangePositionModel model = getTranscriptPositionModel(EXCHANGE_UNIVERSITY_NAME, withFullRequirements);
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
        ExchangeApplicationModel application = applications.get(0);
        application.setState(ExchangeApplicationState.ACCEPTED);

        givenCorrectHeaders(UNIVERSITY_URL)
                .pathParam("universityName", EXCHANGE_UNIVERSITY_NAME)
                .pathParam("userName", ADMIN_NAME)
                .body(application)
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

    private ExchangePositionModel getTranscriptPositionModel(String exchangeUniversityName, Boolean withFullRequirements) {
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("degree", "Bachelor of Science, Marketing");
        attributes.put("status", "graduated");

        if (withFullRequirements)
            attributes.put("average", "7");

        return new ExchangePositionModel(
                exchangeUniversityName,
                new SchemaDefinitionModel("Transcript", "0.1", null),
                null,
                ExchangePositionState.OPEN,
                attributes
        );
    }
}
