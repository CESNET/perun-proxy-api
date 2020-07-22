package cz.muni.ics.perunproxyapi.persistence.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Basic model with ID. Should be extended by another specific models with specific variables.
 *
 * @author Peter Jancus <jancus@ics.muni.cz>
 */
@EqualsAndHashCode
public abstract class Model {

    @Getter
    private Long id;

    public Model() {
    }

    public Model(Long id) {
        super();
        this.setId(id);
    }

    public void setId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }

        this.id = id;
    }
}

