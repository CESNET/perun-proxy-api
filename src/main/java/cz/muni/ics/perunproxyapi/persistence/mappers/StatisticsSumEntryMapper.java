package cz.muni.ics.perunproxyapi.persistence.mappers;

import cz.muni.ics.perunproxyapi.persistence.models.statistics.LoginsPerDaySumEntry;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StatisticsSumEntryMapper implements RowMapper<LoginsPerDaySumEntry> {

    public static final String COL_ID = "id";
    public static final String COL_YEAR = "year";
    public static final String COL_MONTH = "month";
    public static final String COL_DAY = "day";
    public static final String COL_USERS = "users";
    public static final String COL_LOGINS = "logins";

    @Override
    public LoginsPerDaySumEntry mapRow(ResultSet rs, int i) throws SQLException {
        Long id = rs.getLong(COL_ID);
        int year = rs.getInt(COL_YEAR);
        int month = rs.getInt(COL_MONTH);
        int day = rs.getInt(COL_DAY);
        int users = rs.getInt(COL_USERS);
        int logins = rs.getInt(COL_LOGINS);
        return new LoginsPerDaySumEntry(id, year, month, day, users, logins);
    }

}
