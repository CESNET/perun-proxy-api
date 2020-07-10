package cz.muni.ics.perunproxyapi.models;

import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.hibernate.annotations.NotFound;

import java.util.Objects;

/**
 * Model for ExtSource
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public class ExtSource extends Model {

    @Getter
    private String name;
    @Getter
    private String type;

    public ExtSource() {
    }

    public ExtSource(Long id, String name, String type) {
        super(id);
        this.setName(name);
        this.setType(type);
    }


    public void setName(String name) {
        if (Strings.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("name cannot be null nor empty");
        }

        this.name = name;
    }


    public void setType(String type) {
        if (Strings.isNullOrEmpty(type)) {
            throw new IllegalArgumentException("type cannot be null nor empty");
        }

        this.type = type;
    }
}
