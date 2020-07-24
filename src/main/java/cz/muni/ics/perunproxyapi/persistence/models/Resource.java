package cz.muni.ics.perunproxyapi.persistence.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * Resource object model.
 *
 * @author Dominik Frantisek Bucik <bucik@.ics.muni.cz>
 * @author Ondrej Ernst <ondra.ernst@gmail.com>
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Resource {

    @NonNull private Long id;
    @NonNull private Long voId;
    @NonNull private Long facilityId;
    @NonNull private String name;
    @NonNull private String description;
    private Vo vo;

    public Resource(Long id, Long voId, Long facilityId, String name, String description) {
        this.setId(id);
        this.setVoId(voId);
        this.setFacilityId(facilityId);
        this.setName(name);
        this.setDescription(description);
    }

    public Resource(Long id, Long voId, Long facilityId, String name, String description, Vo vo) {
        this(id, voId, facilityId, name, description);
        this.setVo(vo);
    }

    public void setName(String name) {
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("name cannot be empty");
        }

        this.name = name;
    }

}
