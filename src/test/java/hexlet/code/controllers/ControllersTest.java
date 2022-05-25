package hexlet.code.controllers;

import hexlet.code.App;
import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.*;


import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ControllersTest {
    private static Javalin app;
    private static String baseUrl;
    private static Url existingUrl;
    private static Transaction transaction;

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        existingUrl = new Url();
        existingUrl.setName("https://ru.hexlet.io");
        existingUrl.save();
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @BeforeEach
    void beforeEach() {
        transaction = DB.beginTransaction();
    }

    @AfterEach
    void afterEach() {
        transaction.rollback();
    }

    @Test
    void testInit() {
        assertTrue(true);
    }

    @Nested
    class RootControllerTest {

        @Test
        void testIndex() {
            HttpResponse<String> response = Unirest.get(baseUrl).asString();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("Page analyzer");
        }

    }

    @Nested
    class UrlControllerTest {

        @Test
        void testListUrls() {
            HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("Resources");
        }

        @Test
        void testShowUrl() {
            HttpResponse<String> response = Unirest.get(baseUrl + "/urls/1").asString();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("https://ru.hexlet.io");
        }

        @Test
        void testCreate() {
            String inputURL = "https://www.example.com";
            HttpResponse<String> response = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputURL)
                    .asString();

            Url url = new QUrl().name.equalTo(inputURL).findOne();

            assertThat(response.getStatus()).isEqualTo(302);
            assertNotNull(url);
        }

        @Test
        void testAlreadyExists() {
            String inputURL = "https://www.example.com";

            Unirest.post(baseUrl + "/urls")
                    .field("url", inputURL)
                    .asString();

            HttpResponse<String> response = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputURL)
                    .asString();

            List<Url> urls= new QUrl().name.equalTo(inputURL).findList();

            assertThat(response.getStatus()).isEqualTo(302);
            assertThat(urls.size()).isEqualTo(1);
        }

        @Test
        void testIncorrectURL() {
            String inputURL = "htgfhfg";
            HttpResponse<String> response = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputURL)
                    .asString();

            Url url = new QUrl().name.equalTo(inputURL).findOne();

            assertThat(response.getStatus()).isEqualTo(200);
            assertNull(url);
        }

    }

}
