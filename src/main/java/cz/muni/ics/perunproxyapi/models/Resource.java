package cz.muni.ics.perunproxyapi.models;

import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * Resource object model.
 *
 * @author Peter Jancus <jancus@ics.muni.cz>
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public class Resource extends Model {
    @Getter
    private Long voId;
    @Getter
    private String name;
    @Getter
    private String description;
    @Getter
    private Vo vo;

    public Resource() {	}

    public Resource(Long id, Long voId, String name, String description) {
        super(id);
        this.setVoId(voId);
        this.setName(name);
        this.setDescription(description);
    }

    /**
     * Should be used when RichResource is obtained from Perun
     */
    public Resource(Long id, Long voId, String name, String description, Vo vo) {
        this(id, voId, name, description);
        this.setVo(vo);
    }

    public void setVoId(Long voId) {
        if (voId == null) {
            throw new IllegalArgumentException("voId can't be null");
        }

        this.voId = voId;
    }

    public void setName(String name) {
        if (Strings.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("name can't be null or empty");
        }

        this.name = name;
    }

    public void setDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("description can't be null");
        }

        this.description = description;
    }

    public void setVo(Vo vo) {
        this.vo = vo;
    }

}

