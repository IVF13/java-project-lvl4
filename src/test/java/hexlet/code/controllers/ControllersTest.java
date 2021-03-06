package hexlet.code.controllers;

import hexlet.code.App;
import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static hexlet.code.controllers.UrlsController.runCheck;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ControllersTest {
    private static Javalin app;
    private static String baseUrl;
    private static Url existingUrl;
    private static Transaction transaction;
    private static String inputURL = "https://www.example.com";

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        existingUrl = new Url().setName("https://ru.hexlet.io");
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
            assertThat(response.getBody()).contains("???????????????????? ??????????????");
        }

    }

    @Nested
    class UrlControllerTest {

        @Test
        void testListUrls() {
            HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("??????????");
        }

        @Test
        void testShowUrl() {
            HttpResponse<String> response = Unirest.get(baseUrl + "/urls/1").asString();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("https://ru.hexlet.io");
        }

        @Test
        void testCreate() {
            HttpResponse<String> response = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputURL)
                    .asString();

            Url url = new QUrl().name.equalTo(inputURL).findOne();

            assertThat(response.getStatus()).isEqualTo(302);
            assertThat(url.getName()).contains("https://www.example.com");
            assertNotNull(url);
        }

        @Test
        void testAlreadyExists() {
            Unirest.post(baseUrl + "/urls")
                    .field("url", inputURL)
                    .asString();

            HttpResponse<String> response = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputURL)
                    .asString();

            List<Url> urls = new QUrl().name.equalTo(inputURL).findList();

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

            assertThat(response.getStatus()).isEqualTo(400);
            assertNull(url);
        }

        @Test
        void testCreateCheck() throws IOException {

            MockWebServer server = new MockWebServer();

            String serverUrl = server.url("/")
                    .toString()
                    .substring(0, server.url("").toString().length() - 1);

            MockResponse mockResponse = new MockResponse()
                    .setBody("Response Body");

            server.enqueue(mockResponse);
            server.enqueue(mockResponse);

            Unirest.post(baseUrl + "/urls").field("url", serverUrl).asString();

            Url mockUrl = new QUrl().name.equalTo(serverUrl).findOne();

            HttpResponse<String> response = Unirest
                    .post(baseUrl + "/urls/{id}/checks")
                    .routeParam("id", String.valueOf(mockUrl.getId()))
                    .asString();

            List<UrlCheck> urlChecks = new QUrlCheck().url.equalTo(mockUrl).findList();

            assertThat(response.getStatus()).isEqualTo(302);
            assertEquals(1, urlChecks.size());

            server.close();
        }

        @Test
        void testCreateCheckUnknownHost() {
            String inputURL = "https://ru.hexhgfhlet.io";

            Unirest.post(baseUrl + "/urls").field("url", inputURL).asString();

            Url url = new QUrl().name.equalTo(inputURL).findOne();

            HttpResponse<String> response = Unirest
                    .post(baseUrl + "/urls/{id}/checks")
                    .routeParam("id", String.valueOf(url.getId()))
                    .asString();

            List<UrlCheck> urlChecks = new QUrlCheck().url.equalTo(url).findList();

            assertThat(response.getStatus()).isEqualTo(302);
            assertEquals(0, urlChecks.size());
        }

        @Test
        void testRunCheck() throws Exception {

            MockWebServer server = new MockWebServer();

            String serverUrl = server.url("/")
                    .toString()
                    .substring(0, server.url("").toString().length() - 1);

            MockResponse mockResponse = new MockResponse()
                    .setResponseCode(302)
                    .setBody("<title>Title</title>");

            server.enqueue(mockResponse);
            server.enqueue(mockResponse);

            Unirest.post(baseUrl + "/urls").field("url", serverUrl).asString();

            Url mockUrl = new QUrl().name.equalTo(serverUrl).findOne();

            runCheck(mockUrl.getId());

            List<UrlCheck> urlChecks = new QUrlCheck().url.equalTo(mockUrl).findList();

            assertEquals(urlChecks.get(0).getStatusCode(), 302);
            assertEquals(urlChecks.get(0).getTitle(), "Title");
            assertNull(urlChecks.get(0).getH1());
            assertNull(urlChecks.get(0).getDescription());
            server.close();
        }

    }

    @Nested
    class UrlEntityUtilTest {

        @Test
        void testGetLastCheck() {
            existingUrl.setUrlChecks(List.of(new UrlCheck(1, 200, "title1",
                            "h11", "description1", existingUrl, new Date()),
                    new UrlCheck(2, 302, "title2", "h12",
                            "description2", existingUrl, new Date(100))));

            assertEquals(existingUrl.getLastCheck().getTime(), new Date(100).getTime());
        }

        @Test
        void testGetLastStatusCode() {
            existingUrl.setUrlChecks(List.of(new UrlCheck(1, 200, "title1",
                            "h11", "description1", existingUrl, new Date()),
                    new UrlCheck(2, 302, "title2", "h12",
                            "description2", existingUrl, new Date(100))));

            assertEquals(existingUrl.getLastStatusCode(), 302);
        }
    }

}


