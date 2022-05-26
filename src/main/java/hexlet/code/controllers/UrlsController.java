package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
            ctx.sessionAttribute("flash", "Incorrect URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.render("index.html");
            return;
        }

        String name = inputURL.getProtocol() + "://" + inputURL.getHost();
        if (inputURL.getPort() != -1) {
            name += ":" + inputURL.getPort();
        }

        if (findUrl(name) != null) {
            ctx.sessionAttribute("flash", "Page already exists");
            ctx.sessionAttribute("flash-type", "success");
        } else {
            Url url = new Url();
            url.setName(name);
            url.save();

            ctx.sessionAttribute("flash", "Page successfully added");
            ctx.sessionAttribute("flash-type", "success");
        }

        runCheck(findUrl(name).getId());

        ctx.redirect("/urls");
    };

    public static Handler createCheck = ctx -> {
        long id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        runCheck(id);

        ctx.redirect("/urls/" + id);
    };

    private static void runCheck(long id) {
        UrlCheck urlCheck = new UrlCheck();
        Url url = findUrl(id);
        //установка параметров
        urlCheck.setTitle("LOL2");
        urlCheck.setUrl(url);
        urlCheck.setStatusCode(200);

        List<UrlCheck> urlChecks = new ArrayList<>();
        urlChecks.add(urlCheck);

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
