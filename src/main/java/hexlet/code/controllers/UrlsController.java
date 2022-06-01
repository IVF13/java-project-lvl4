package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.HttpCode;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class UrlsController {

    public static Handler listUrls = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        int rowsPerPage = 10;

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urls = pagedUrls.getList();

        int lastPage = pagedUrls.getTotalPageCount() + 1;
        int currentPage = pagedUrls.getPageIndex() + 1;
        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .collect(Collectors.toList());

        ctx.attribute("urls", urls);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
        ctx.render("urls/index.html");
    };

    public static Handler showUrl = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        Url url = findUrl(id);

        ctx.attribute("url", url);
        ctx.render("urls/show.html");
    };

    public static Handler createUrl = ctx -> {
        URL inputURL;

        try {
            inputURL = new URL(ctx.formParam("url"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.status(HttpCode.BAD_REQUEST);
            ctx.render("index.html");
            return;
        }

        String name = inputURL.getProtocol() + "://" + inputURL.getHost();
        if (inputURL.getPort() != -1) {
            name += ":" + inputURL.getPort();
        }

        if (findUrl(name) != null) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "success");
        } else {
            Url url = new Url().setName(name);
            url.save();

            ctx.sessionAttribute("flash", "Страница успешно создана");
            ctx.sessionAttribute("flash-type", "success");
        }

        ctx.redirect("/urls");
    };

    public static Handler createCheck = ctx -> {
        long id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        runCheck(id);

        ctx.sessionAttribute("flash", "Страница успешно проверена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls/" + id);
    };

    private static void runCheck(long id) {
        Url url = findUrl(id);
        UrlCheck urlCheck = new UrlCheck().setUrl(url);
        HttpResponse<String> response = null;
        Document doc = null;

        try {
            response = Unirest.get(url.getName()).asString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (response != null) {
            doc = Jsoup.parseBodyFragment(response.getBody());
            urlCheck.setStatusCode(response.getStatus());
        } else {
            urlCheck.setStatusCode(503);
        }

        if (doc.selectFirst("title") != null && doc.selectFirst("title").hasText()) {
            urlCheck.setTitle(doc.selectFirst("title").text());
        }

        if (doc.selectFirst("h1") != null) {
            urlCheck.setH1(doc.selectFirst("h1").firstElementSibling().text());
        }

        if (!doc.getElementsByAttributeValueMatching("name", "description").isEmpty()) {
            urlCheck.setDescription(doc
                    .getElementsByAttributeValueMatching("name", "description")
                    .attr("content"));
        }


        urlCheck.save();
    }

    private static Url findUrl(long id) {
        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        return url;
    }

    private static Url findUrl(String name) {
        Url url = new QUrl()
                .name.equalTo(name)
                .findOne();

        return url;
    }

}
