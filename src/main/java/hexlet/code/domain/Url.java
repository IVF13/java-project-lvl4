package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.List;

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

    public void setName(String resourceName) {
        this.name = resourceName;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

}
