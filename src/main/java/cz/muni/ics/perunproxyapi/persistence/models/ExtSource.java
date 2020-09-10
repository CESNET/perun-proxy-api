package cz.muni.ics.perunproxyapi.persistence.models;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * Model for ExtSource
 *
 * @author Dominik Frantisek Bucik <bucik@.ics.muni.cz>
 * @author Ondrej Ernst <ondra.ernst@gmail.com>
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ExtSource {

    @NonNull private Long id;
    @NonNull private String name;
    @NonNull private String type;

    public ExtSource(Long id, String name, String type) {
        this.setId(id);
        this.setName(name);
        this.setType(type);
    }

    public void setName(@NonNull String name) {
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("name cannot be empty");
        }

        this.name = name;
    }

    public void setType(@NonNull String type) {
        if (type.trim().isEmpty()) {
            throw new IllegalArgumentException("type cannot be empty");
        }

        this.type = type;
    }

}
