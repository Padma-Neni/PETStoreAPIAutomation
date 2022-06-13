package stepDefinitions;

import com.github.javafaker.Faker;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.Category;
import models.Pet;
import models.Tag;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.*;

public class stepDefinition {

    private Response response;
    static String jsonResponse;
    String BaseURL = "https://petstore.swagger.io/v2";
    Category category = new Category();
    Tag tag = new Tag();
    Pet pet = new Pet();


    private Pet petBody(int petid, String petname, String categoryName, String tagname) {
        List<String> photoURL = new ArrayList<String>();
        photoURL.add("https://images-pw.pixieset.com/elementfield/88752731/PPA-PJLPHOTOGRAPHY-0030-a39508af.jpg");
        category.setName(categoryName);

        tag.setName(tagname);
        List<Tag> tags = new ArrayList<>();
        tags.add(tag);

        pet.setId(petid);
        pet.setName(petname);
        pet.setCategory(category);
        pet.setPhotoUrls(photoURL);
        pet.setTags(tags);
        pet.setStatus("available");

        return pet;
    }

    @Given("I Set Pet Service api endpoint")
    public void i_set_pet_service_api_endpoint() {
        RestAssured.baseURI = BaseURL;
        RestAssured.basePath = "/pet";
    }

    @When("I Send GET HTTP request")
    public void i_send_get_http_request() {
        response = given()
                .queryParam("status", "available")
                .log().all()
                .when()
                .get("findByStatus");
    }

    @Then("I receive valid HTTP response code {int}")
    public void i_receive_valid_http_response_code(int statusCode) {
        System.out.printf("Actual Status code %s, Expected Status Code %s", response.getStatusCode(), statusCode);
        Assert.assertEquals("Actual Status code %s, Expected Status Code %s", response.getStatusCode(), statusCode);
    }

    @When("Send a POST HTTP request")
    public void send_a_post_http_request() {
        Faker fake = new Faker();
        response = given().log().all()
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .when()
                .body(petBody(fake.hashCode(), fake.dog().name(), fake.dog().breed(), fake.dog().memePhrase()))
                .post();
        jsonResponse = response.asString();
    }

    @Then("Return new pet record created")
    public void return_new_pet_record_created() {
        System.out.printf("Created Pet ID : " + JsonPath.read(jsonResponse, "$.id").toString() + "\n");
        System.out.printf("Created Category Name : " + JsonPath.read(jsonResponse, "$.category.name").toString() + "\n");
        System.out.printf("Created Pet Name : " + JsonPath.read(jsonResponse, "$.name").toString() + "\n");
        System.out.printf("Created Pet Status : " + JsonPath.read(jsonResponse, "$.status").toString() + "\n");
        System.out.printf("Created Pet Tag Name : " + JsonPath.read(jsonResponse, "$.tags[0].name").toString() + "\n");
        System.out.printf("Created Pet Photo URLS : " + JsonPath.read(jsonResponse, "$.photoUrls").toString() + "\n");
    }

    @Then("i receive valid Response body")
    public void i_receive_valid_response() {
        System.out.println("------------------- Returing new Pet Record ----------------------");
        System.out.println(response.getBody().asString());
        Assert.assertEquals("Validating Pet ID", JsonPath.read(jsonResponse, "$.id"), pet.getId());
        Assert.assertEquals("Validaitng Category name", JsonPath.read(jsonResponse, "$.category.name"), category.getName());
        Assert.assertEquals("Validating PET Name", JsonPath.read(jsonResponse, "$.name"), pet.getName());
        Assert.assertEquals("Validating PET Status", JsonPath.read(jsonResponse, "$.status"), pet.getStatus());
        Assert.assertEquals("Validating PhotoURLS", JsonPath.read(jsonResponse, "$.photoUrls"), pet.getPhotoUrls());
        Assert.assertEquals("Validating Tag name", JsonPath.read(jsonResponse, "$.tags[0].name"), tag.getName());
    }

    @When("I Send DELETE HTTP request for created PetID")
    public void i_send_delete_http_request_for_created_pet_id() {
        response = given().log().all()
                .header("accept", "application/json")
                .when()
                .delete(pet.getId().toString());
    }

    @When("I Send GET HTTP request for PetID")
    public void i_send_get_http_request_for_pet_id() {
        response = given().log().all()
                .when()
                .get(pet.getId().toString());
        jsonResponse = response.asString();

    }

    @Then("{string} message is displayed")
    public void message_is_displayed(String eMessage) {
        Assert.assertEquals(JsonPath.read(jsonResponse, "$.message"), eMessage);
    }

    @When("I send POST request to update name on created PETID")
    public void i_send_post_request_to_update_name_on_created_petid() {
        pet.setName("doggi");
        response = given().log().all()
                .header("accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .when()
                .body("name=" + pet.getName() + "&status=available")
                .post(pet.getId().toString());
    }

    @Then("Updated Petname is displayed in response body")
    public void updated_petname_is_displayed_in_response_body() {
        System.out.printf("Updated Pet Record after name Change \n" + response.prettyPrint());
        Assert.assertEquals("Validating PET Name", JsonPath.read(jsonResponse, "$.name"), pet.getName());
    }

}
