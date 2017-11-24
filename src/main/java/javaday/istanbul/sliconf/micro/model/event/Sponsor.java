package javaday.istanbul.sliconf.micro.model.event;

import java.util.Objects;

public class Sponsor {

    private String id;
    private String logo;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof Sponsor) {
            Sponsor sponsor = (Sponsor) obj;

            if (Objects.nonNull(this.getName()) && this.getName().equals(sponsor.getName()) &&
                    Objects.nonNull(this.getLogo()) && this.getLogo().equals(sponsor.getLogo())) {
                return true;
            }
        }

        return false;
    }
}
