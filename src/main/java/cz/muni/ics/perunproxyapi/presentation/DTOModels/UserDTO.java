package cz.muni.ics.perunproxyapi.presentation.DTOModels;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Model representing User DTO object which is being returned by API methods
 *
 * @author Pavol Pluta <pavol.pluta1@gmail.com>
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class UserDTO {

    @NonNull private String login;
    private Map<String, JsonNode> attributes = new HashMap<>();

    public UserDTO(String login, Map<String, JsonNode> attributes) {
        this.setLogin(login);
        this.setAttributes(attributes);
    }

    public void setLogin(@NonNull String login) {
        if (!StringUtils.hasText(login)) {
            throw new IllegalArgumentException("Login cannot be empty nor NULL");
        }

        this.login = login;
    }

    public void setAttributes(@NonNull Map<String, JsonNode> attributes) {
        this.attributes.clear();
        this.attributes.putAll(attributes);
    }

}
