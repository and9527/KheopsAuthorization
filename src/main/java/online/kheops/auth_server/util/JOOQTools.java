package online.kheops.auth_server.util;

import com.mchange.v2.c3p0.C3P0Registry;
import online.kheops.auth_server.album.BadQueryParametersException;
import org.jooq.Condition;
import org.jooq.Field;

import javax.sql.DataSource;
import javax.ws.rs.core.MultivaluedMap;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.OptionalInt;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class JOOQTools {

    private JOOQTools() { throw new IllegalStateException("Utility class"); }

    private static final Logger LOG = Logger.getLogger(JOOQTools.class.getName());

    public static OptionalInt getLimit(MultivaluedMap<String, String> queryParameters) throws BadQueryParametersException {
        final Integer limit;
        try {
            limit = Integer.parseInt(queryParameters.get(Consts.QUERY_PARAMETER_LIMIT).get(0));
        } catch (Exception e) {
            throw new BadQueryParametersException(Consts.QUERY_PARAMETER_LIMIT + ": " + queryParameters.get(Consts.QUERY_PARAMETER_LIMIT).get(0));
        }
        if (limit < 1) {
            throw new BadQueryParametersException(Consts.QUERY_PARAMETER_LIMIT + ": " + queryParameters.get(Consts.QUERY_PARAMETER_LIMIT).get(0));
        }
        return OptionalInt.of(limit);
    }

    public static OptionalInt getOffset(MultivaluedMap<String, String> queryParameters) throws BadQueryParametersException{
        final Integer offset;
        try {
            offset = Integer.parseInt(queryParameters.get(Consts.QUERY_PARAMETER_OFFSET).get(0));
        } catch (Exception e) {
            throw new BadQueryParametersException(Consts.QUERY_PARAMETER_OFFSET + ": " + queryParameters.get(Consts.QUERY_PARAMETER_OFFSET).get(0));
        }

        if (offset < 0) {
            throw new BadQueryParametersException(Consts.QUERY_PARAMETER_OFFSET + ": " + queryParameters.get(Consts.QUERY_PARAMETER_OFFSET).get(0));
        }
        return OptionalInt.of(offset);
    }


    public static void checkDate(String date) throws BadQueryParametersException {
        if (!date.matches("^([0-9]{4})(0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])$")) {
            throw new BadQueryParametersException("Bad date format : yyyyMMdd");
        }
    }

    public static Condition createDateCondition(String parameter, Field<Timestamp> column)
            throws BadQueryParametersException {

        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        if (parameter.contains("-")) {
            String[] parameters = parameter.split("-");
            if (parameters.length == 2) {
                try {
                    if(parameters[0].length() == 0) {
                        parameters[0] = "00010101";
                    }
                    checkDate(parameters[0]);
                    checkDate(parameters[1]);

                    Date parsedDateBegin = dateFormat.parse(parameters[0]+"000000");
                    Timestamp begin = new java.sql.Timestamp(parsedDateBegin.getTime());
                    Date parsedDateEnd = dateFormat.parse(parameters[1] + "235959");
                    Timestamp end = new java.sql.Timestamp(parsedDateEnd.getTime());
                    return column.between(begin, end);
                } catch (ParseException | BadQueryParametersException e) {
                    throw new BadQueryParametersException(column.getName() + ": " + parameter);
                }
            } else if(parameters.length == 1) {
                try {
                checkDate(parameters[0]);
                Date parsedDateBegin = dateFormat.parse(parameters[0]+"000000");
                Timestamp begin = new java.sql.Timestamp(parsedDateBegin.getTime());
                Date parsedDateEnd = dateFormat.parse("99991231235959");
                Timestamp end = new java.sql.Timestamp(parsedDateEnd.getTime());
                return column.between(begin, end);
                } catch (ParseException | BadQueryParametersException e) {
                    throw new BadQueryParametersException(column.getName() + ": " + parameter);
                }
            } else {
                throw new BadQueryParametersException(column.getName() + ": " + parameter);
            }
        } else {
            try {
                checkDate(parameter);
                Date parsedDateBegin = dateFormat.parse(parameter+"000000");
                Timestamp begin = new java.sql.Timestamp(parsedDateBegin.getTime());
                Date parsedDateEnd = dateFormat.parse(parameter+"235959");
                Timestamp end = new java.sql.Timestamp(parsedDateEnd.getTime());
                return column.between(begin, end);
            } catch (ParseException | BadQueryParametersException e) {
                throw new BadQueryParametersException(column.getName() + ": " + parameter);
            }
        }
    }

    public static DataSource getDataSource() {
        Iterator iterator = C3P0Registry.getPooledDataSources().iterator();

        if (!iterator.hasNext()) {
            throw new RuntimeException("No C3P0 DataSource available");
        }
        DataSource dataSource = (DataSource) iterator.next();
        if (iterator.hasNext()) {
            LOG.log(Level.SEVERE, "More than one C3P0 Datasource present, picked the first one");
        }

        return dataSource;
    }

}
