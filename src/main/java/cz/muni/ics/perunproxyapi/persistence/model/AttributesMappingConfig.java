package cz.muni.ics.perunproxyapi.persistence.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * List of attributes from .yml file is mapped as instance of this class.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AttributesMappingConfig {

    private List<AttributeMapping> attributes;
}
