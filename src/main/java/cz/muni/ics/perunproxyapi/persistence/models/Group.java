package cz.muni.ics.perunproxyapi.persistence.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

/**
 * Group object model.
 *
 * @author Dominik Frantisek Bucik <bucik@.ics.muni.cz>
 * @author Ondrej Ernst <ondra.ernst@gmail.com>
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Group {

    @NonNull private Long id;
    private Long parentGroupId;
    @NonNull private String name;
    @NonNull private String description;
    private String uniqueGroupName; // voShortName + ":" + group name
    @NonNull private Long voId;

    public Group(Long id, Long parentGroupId, String name, String description, String uniqueGroupName, Long voId) {
        this.setId(id);
        this.setParentGroupId(parentGroupId);
        this.setName(name);
        this.setDescription(description);
        this.setUniqueGroupName(uniqueGroupName);
        this.setVoId(voId);
    }

    public void setName(@NonNull String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("name cannot be empty");
        }
        this.name = name;
    }

}

