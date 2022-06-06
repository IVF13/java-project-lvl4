package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public final class Url extends Model {

    @Id
    private long id;

    private String name;

    @OneToMany
    private List<UrlCheck> urlChecks;

    @WhenCreated
    private Date createdAt;

    public Url() {
    }

    public Url(String dbName, String resourceName) {
        super(dbName);
        this.name = resourceName;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Url setName(String name) {
        this.name = name;
        return this;
    }

    public List<UrlCheck> getUrlChecks() {
        return urlChecks.stream()
                .sorted(Comparator.comparing(UrlCheck::getId).reversed())
                .collect(Collectors.toList());
    }

    public Url setUrlChecks(List<UrlCheck> urlChecks) {
        this.urlChecks = urlChecks;
        return this;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getLastCheck() {
        if (!this.getUrlChecks().isEmpty()) {
            return this.getUrlChecks().get(this.getUrlChecks().size() - 1).getCreatedAt();
        }
        return null;
    }

    public Integer getLastStatusCode() {
        if (!this.getUrlChecks().isEmpty()) {
            return this.getUrlChecks().get(this.getUrlChecks().size() - 1).getStatusCode();
        }
        return null;
    }

}
