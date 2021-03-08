package cz.muni.ics.perunproxyapi.persistence.mappers;

import cz.muni.ics.perunproxyapi.persistence.models.statistics.RpSumEntry;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RpSumEntryMapper implements RowMapper<RpSumEntry> {

    public static final String COL_LOGINS = "logins";
    public static final String COL_IDENTIFIER = "identifier";
    public static final String COL_NAME = "name";

    @Override
    public RpSumEntry mapRow(ResultSet rs, int i) throws SQLException {
        String name = rs.getString(COL_NAME);
        String identifier = rs.getString(COL_IDENTIFIER);
        int logins = rs.getInt(COL_LOGINS);
        return new RpSumEntry(name, identifier, logins);
    }

}
