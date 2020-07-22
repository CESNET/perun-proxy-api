package cz.muni.ics.perunproxyapi.models;


import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * Facility object model.
 *
 * @author Peter Jancus <jancus@ics.muni.cz>
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public class Facility extends Model {

    @Getter
    private String name;
    @Getter
    private String description;

    public Facility() {
    }

    public Facility(Long id, String name, String description) {
        super(id);
        this.name = name;
        this.description = description;
    }


    public void setName(String name) {
        if (Strings.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("name cannot be null nor empty");
        }

        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

