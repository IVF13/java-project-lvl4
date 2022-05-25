package hexlet.code.domain;

import io.ebean.annotation.WhenCreated;

import javax.persistence.*;
import java.util.Date;

@Entity
public final class UrlCheck {

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

    public UrlCheck(long id, int statusCode, String title, String h1, String description, Date createdAt) {
        this.id = id;
        this.statusCode = statusCode;
        this.title = title;
        this.h1 = h1;
        this.description = description;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getTitle() {
        return title;
    }

    public String getH1() {
        return h1;
    }

    public String getDescription() {
        return description;
    }

    public Url getUrl() {
        return url;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setH1(String h1) {
        this.h1 = h1;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
