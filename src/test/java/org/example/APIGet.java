package org.example;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import jdk.jfr.Category;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.util.List;
import java.util.Map;

import static io.restassured.path.json.JsonPath.from;
import static io.restassured.path.json.JsonPath.given;
import static org.hamcrest.Matchers.*;

public class APIGet {
    private Response response;
    private ExtentReports extent;
    private ExtentTest test;
   @BeforeSuite
   // 1. Send a GET request to retrieve the product details
    public void sendGetRequest() {
       RestAssured.baseURI = "https://run.mocky.io";
       response = RestAssured.given().when().get("/v3/0e2de2af-4793-4c89-af96-bdc1d52e9212");
       ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter("test-output/ExtentReport.html");
       extent = new ExtentReports();
       extent.attachReporter(htmlReporter);
   }

    @BeforeMethod
    public void setup() {
        test = extent.createTest(getClass().getSimpleName());
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            test.fail(result.getThrowable());
        } else if (result.getStatus() == ITestResult.SKIP) {
            test.skip("Test skipped: " + result.getThrowable());
        }
    }

    @AfterSuite
    public void afterSuite() {
        extent.flush();
    }

   //2. Validate that the response has a status code 200
    @Test(priority = 1)
    public void testGetProductDetail() {
           Assert.assertEquals(response.getStatusCode(), 200, "Invalid status code");

   }

    //3. Verify that the "name" field in the response matches the expected value "ProductX"
    @Test(priority = 2)
    public void testProductName() {
       String expectedName = "Product X";
       String actualName = response.jsonPath().getString("name");

       Assert.assertEquals(actualName, expectedName,"Product name doesn't match with expected val");

    }

    //4. Check if the "price" field is numeric value and greater than zero
    @Test(priority = 3)
    public void testPrice() {
       String prcresponse = response.then().extract().response().asString();
        JsonPath jsonPath = new JsonPath(prcresponse);
        Double price = jsonPath.getDouble("price");
        Assert.assertTrue(jsonPath.get("price") !=null, "Price field is missing");
        Assert.assertTrue(price > 0, "Price is not greater than zero");

    }

    //5. Validate that the "inventory" field is present and has a boolean values for "available"
    @Test(priority = 4)
    public void testInventoryPresentAndValue() {
       String responseBody = response.then().extract().response().getBody().asString();
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(responseBody).getAsJsonObject();
        Assert.assertTrue(jsonObject.has("inventory"),"Inventory field is no present");
        JsonObject invetoryObject = jsonObject.getAsJsonObject("inventory");
        Assert.assertTrue(invetoryObject.has("available"), "Available is no present");
        boolean isAvailable = invetoryObject.get("available").getAsBoolean();
        Assert.assertTrue(isAvailable, "'available' value is not 'true'");

    }

    //6. Extract the "quantity" value from the "inventory" field and verify that it is a numeric value greater than zero
    @Test(priority = 5)
    public void testQuantityVal() {
       String responseBody = response.then().extract().response().getBody().asString();
       JsonPath jsonPath = new JsonPath(responseBody);
       Integer quantityVal = jsonPath.getInt("inventory.quantity");
       Assert.assertNotNull(quantityVal, "Quantity Value is Null");
       Assert.assertTrue(quantityVal > 0, "Quantity Value Greater Than Zero");

    }

    //7. Verify that the "categories" field contains at least one category and each category has an "id" and a "name" field
    @Test(priority = 6)
    public void testCategoriesContainsIdAndName() {
       String responseBody = response.then().extract().response().getBody().asString();
       JsonPath jsonPath = new JsonPath(responseBody);
       List<Map<String, Object>> categories = jsonPath.getList("categories");
       Assert.assertNotNull(categories, "Categories array is missing");
       Assert.assertNotNull(categories.isEmpty(), "Categories array is empty");

       for (Map<String, Object> category: categories){
           Assert.assertNotNull(category.get("id"), "Category id is missing");
           Assert.assertNotNull(category.get("name"), "Category name is missing");
       }

    }

    //8. Extract the "reviews" field and ensure it is an array containing at least one review
    @Test(priority = 7)
    public void testReviewFieldPresenceAndArraySize(){
       response.then().assertThat().contentType("application/json")
               .body("reviews", notNullValue())
               .body("reviews", instanceOf(List.class))
               .body("reviews", hasSize(greaterThan(0)));

//    String responseBody = response.then().extract().response().getBody().asString();
//    JsonParser jsonParser = new JsonParser();
//    JsonObject jsonObject = jsonParser.parse(responseBody).getAsJsonObject();
//    Assert.assertTrue(jsonObject.has("reviews"), "Review field is not present in the JSON response");
//    JsonArray reviewsArray = jsonObject.getAsJsonArray("review");
//    Assert.assertTrue(reviewsArray.size() > 0, "The reviews  array doesnot contain any review");

    }


    //9. Validate that each review in the "reviews" field has an "id", "user", "rating" and "comment field
    @Test(priority = 8)
    public void  testReviewFieldsPresence(){
    String responseBody = response.then().extract().response().getBody().asString();
    JsonParser jsonParser = new JsonParser();
    JsonObject jsonObject = jsonParser.parse(responseBody).getAsJsonObject();
    JsonArray reviewsArray = jsonObject.getAsJsonArray("reviews");
    Assert.assertTrue(reviewsArray.size() >= 1, "Thereview arrray  does not contain at least one review");
    for (JsonElement reviewElement : reviewsArray) {
        JsonObject reviewObject = reviewElement.getAsJsonObject();

        Assert.assertTrue(reviewObject.has("id"), "Review dont have id");
        Assert.assertTrue(reviewObject.has("user"), "Review  dont have user");
        Assert.assertTrue(reviewObject.has("rating"), "Review dont have rating");
        Assert.assertTrue(reviewObject.has("comment"),"Review dont have comment");
    }
    }
}