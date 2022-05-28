package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.*;
import java.util.Date;

@Entity
public final class UrlCheck extends Model {

    @Id
    private long id;

    private int statusCode;

    private String title;

    private String h1;

    @Lob
    private String description;

    @ManyToOne
    @JoinColumn(name = "url_id")
    private Url url;

    @WhenCreated
    private Date createdAt;

    public UrlCheck() {
    }

    public UrlCheck(long id, int statusCode, String title, String h1, String description, Url url, Date createdAt) {
        this.id = id;
        this.statusCode = statusCode;
        this.title = title;
        this.h1 = h1;
        this.description = description;
        this.url = url;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public UrlCheck setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public UrlCheck setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getH1() {
        return h1;
    }

    public UrlCheck setH1(String h1) {
        this.h1 = h1;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public UrlCheck setDescription(String description) {
        this.description = description;
        return this;
    }

    public Url getUrl() {
        return url;
    }

    public UrlCheck setUrl(Url url) {
        this.url = url;
        return this;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

}
