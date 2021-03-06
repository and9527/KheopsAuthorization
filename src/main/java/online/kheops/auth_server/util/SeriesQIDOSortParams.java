package online.kheops.auth_server.util;

import com.google.common.collect.Lists;
import online.kheops.auth_server.album.BadQueryParametersException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Comparator;
import java.util.HashSet;

import static online.kheops.auth_server.util.Consts.QUERY_PARAMETER_SORT;

public class SeriesQIDOSortParams {
    private static final HashSet<Integer> ACCEPTED_TAGS_FOR_SORTING = new HashSet<>(Lists.newArrayList(Tag.Modality, Tag.SeriesInstanceUID, Tag.SeriesNumber, Tag.NumberOfSeriesRelatedInstances));

    public static Comparator<Attributes> sortComparator(MultivaluedMap<String, String> queryParameters) throws BadQueryParametersException {

        boolean descending = false;
        int orderByTag = Tag.SeriesNumber;

        if (queryParameters.containsKey(QUERY_PARAMETER_SORT)) {
            descending = queryParameters.get(QUERY_PARAMETER_SORT).get(0).startsWith("-");
            final String orderByParameter = queryParameters.get(QUERY_PARAMETER_SORT).get(0).replace("-", "");
            orderByTag = org.dcm4che3.util.TagUtils.forName(orderByParameter);
            if (orderByTag == -1 || !ACCEPTED_TAGS_FOR_SORTING.contains(orderByTag)) {
                throw new BadQueryParametersException("sort: " + queryParameters.get(QUERY_PARAMETER_SORT).get(0));
            }
        }

        Comparator<Attributes> comparator;
        switch (orderByTag) {
            case Tag.Modality:
            case Tag.SeriesInstanceUID:
                comparator = getStringAttributesComparator(orderByTag);
                break;
            case Tag.SeriesNumber:
            case Tag.NumberOfSeriesRelatedInstances:
                comparator = getIntAttributesComparator(orderByTag);
                break;
            default:
                comparator = getIntAttributesComparator(Tag.SeriesNumber);
        }

        if (descending) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    private static Comparator<Attributes> getIntAttributesComparator(int tag) {
        return Comparator.comparingInt(value -> value.getInt(tag, 0));
    }

    private static Comparator<Attributes> getStringAttributesComparator(int tag) {
        return Comparator.comparing(value -> value.getString(tag, ""));
    }
}


