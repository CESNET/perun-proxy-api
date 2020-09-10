package cz.muni.ics.perunproxyapi.persistence.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

/**
 * Facility object model.
 *
 * @author Dominik Frantisek Bucik <bucik@.ics.muni.cz>
 * @author Ondrej Ernst <ondra.ernst@gmail.com>
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Facility {

    @NonNull private Long id;
    @NonNull private String name;
    @NonNull private String description = "";
    @NonNull private String rpIdentifier;

    public Facility(Long id, String name, String description) {
        this.setId(id);
        this.setName(name);
        this.setDescription(description);
    }

    public Facility(Long id, String name, String description, String rpIdentifier) {
        this(id, name, description);
        this.setRpIdentifier(rpIdentifier);
    }

    public void setName(@NonNull String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("name cannot be empty");
        }

        this.name = name;
    }

    public void setRpIdentifier(@NonNull String rpIdentifier) {
        if (!StringUtils.hasText(rpIdentifier)) {
            throw new IllegalArgumentException("rpIdentifier cannot be empty");
        }

        this.name = rpIdentifier;
    }

}
