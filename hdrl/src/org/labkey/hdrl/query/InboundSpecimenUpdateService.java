package org.labkey.hdrl.query;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.DefaultQueryUpdateService;
import org.labkey.api.query.DuplicateKeyException;
import org.labkey.api.query.InvalidKeyException;
import org.labkey.api.query.QueryUpdateServiceException;
import org.labkey.api.query.ValidationException;
import org.labkey.api.security.User;
import org.labkey.hdrl.HDRLSchema;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by susanh on 4/16/15.
 */
public class InboundSpecimenUpdateService extends DefaultQueryUpdateService
{
    public enum ValidationMode {OFF, ON, ONLY, WITH_UQ}

    private ValidationMode validationMode = ValidationMode.ON;

    public InboundSpecimenUpdateService(TableInfo queryTable, TableInfo dbTable)
    {
        super(queryTable, dbTable);
    }

    @Override
    public List<Map<String, Object>> insertRows(User user, Container container, List<Map<String, Object>> rows, BatchValidationException errors, @Nullable Map<Enum, Object> configParameters, Map<String, Object> extraScriptContext)
            throws DuplicateKeyException, QueryUpdateServiceException, SQLException
    {
        parseValidationMode(extraScriptContext);
        List<Map<String, Object>> ret = super.insertRows(user, container, rows, errors, configParameters, extraScriptContext);

        if (this.validationMode == ValidationMode.WITH_UQ)
        {
            validateUniqueness((Integer) rows.get(0).get("InboundRequestId"));
        }

        return ret;
    }

    private void parseValidationMode(Map<String,Object> extraScriptContext)
    {
        if (extraScriptContext != null && extraScriptContext.containsKey("validationMode"))
        {
            validationMode = ValidationMode.valueOf(extraScriptContext.get("validationMode").toString().toUpperCase());
        }
    }

    @Override
    public List<Map<String, Object>> updateRows(User user, Container container, List<Map<String, Object>> rows, List<Map<String, Object>> oldKeys, @Nullable Map<Enum, Object> configParameters, Map<String, Object> extraScriptContext) throws InvalidKeyException, BatchValidationException, QueryUpdateServiceException, SQLException
    {
        parseValidationMode(extraScriptContext);
        List<Map<String, Object>> ret = super.updateRows(user, container, rows, oldKeys, configParameters, extraScriptContext);

        if (this.validationMode == ValidationMode.WITH_UQ)
        {
            validateUniqueness((Integer) rows.get(0).get("InboundRequestId"));
        }

        return ret;
    }

    @Override
    protected Map<String, Object> _insert(User user, Container c, Map<String, Object> row)
            throws SQLException, ValidationException
    {
         if (validationMode != ValidationMode.OFF)
            validate(row);

        if (validationMode != ValidationMode.ONLY)
            return super._insert(user, c, row);
        else
            return row;
    }

    @Override
    protected Map<String, Object> _update(User user, Container c, Map<String, Object> row, Map<String, Object> oldRow, Object[] keys)
            throws SQLException, ValidationException
    {
        if (validationMode != ValidationMode.OFF)
            validate(row);

        if (validationMode != ValidationMode.ONLY)
            return super._update(user, c, row, oldRow, keys);
        else
            return row;

    }

    public static void validateUniqueness(Integer requestId) throws QueryUpdateServiceException
    {
        // check that there are no duplicate barcodes
        List<String> duplicates = findDuplicates(requestId, "CustomerBarcode");
        StringBuilder message = new StringBuilder();
        if (duplicates.size() > 0)
        {
            message.append("Request has specimens with duplicate fields: Customer Barcode - ").append(StringUtils.join(duplicates, ", "));
        }

        // check that all existing DODId in the specimens in this request are unique
        duplicates = findDuplicates(requestId, "DoDId");
        if (duplicates.size() > 0)
        {
            if (message.length() == 0)
                message.append("Request has specimens with duplicate fields: ");
            else
                message.append("; ");
            message.append("DoDID - ").append(StringUtils.join(duplicates, ", "));
        }
        // check that all the existing SSN + FMP pairs in this request are unique
        duplicates = findDuplicates(requestId, "SSN, FMPId");
        if (duplicates.size() > 0)
        {
            if (message.length() == 0)
                message.append("Request has specimens with duplicate fields: ");
            else
                message.append("; ");
            message.append("SSN + FMP - ").append(StringUtils.join(duplicates, ", "));
        }

        if (message.length() > 0)
            throw new QueryUpdateServiceException(message.toString());

    }

    /**
     * Returns a list of duplicates for the combination of fields given where the field values are not null.
     *
     * @param requestId id of the test request
     * @param fields comma-separated list of fields to check for unique combinations of
     * @return value of the fields that are duplicates; for the case of multiple fields the value of the first field in @fields will be in the returned list
     */
    private static List<String> findDuplicates(Integer requestId, String fields)
    {
        SQLFragment sql = new SQLFragment("SELECT ")
                .append(fields).append(" FROM hdrl.inboundspecimen WHERE inboundrequestid = ").append(requestId);
        for (String field : fields.split(", "))
        {
            sql.append(" AND ").append(field).append(" IS NOT NULL ");
        }
        sql.append(" GROUP BY ").append(fields)
                .append(" HAVING COUNT(*) > 1 ");
        SqlSelector sqlSelector = new SqlSelector(HDRLSchema.getInstance().getScope(), sql);
        return sqlSelector.getArrayList(String.class);

    }

    public static void validate(Map<String, Object> row) throws ValidationException
    {
        List<String> errors = new ArrayList<String>();
        List<String> missingFields = new ArrayList<String>();
        if (StringUtils.isEmpty(String.valueOf(row.get("CustomerBarcode"))))
            missingFields.add("Customer Barcode");
        if (row.get("FMPId") == null)
            missingFields.add("FMP");
        if (row.get("DrawDate") == null)
            missingFields.add("Draw Date");
        if (StringUtils.isEmpty(String.valueOf(row.get("SSN"))))
            missingFields.add("SSN");
        else
        {
            String ssn = String.valueOf(row.get("SSN"));
            if (!ssn.matches("^\\d{9}$"))
            {
                errors.add("Invalid SSN");
            }
        }
        if (row.get("DoDId") != null)
        {
            String dodId = String.valueOf(row.get("DoDId"));
            if (!dodId.matches("^\\d{10}$"))
            {
                errors.add("Invalid DoD Id");
            }
        }
        if (!missingFields.isEmpty())
        {
            errors.add("Required field(s) missing: " + StringUtils.join(missingFields, ", "));
        }

        if (!errors.isEmpty())
        {
            String message = StringUtils.join(errors, "; ");

            row.put("ValidationStatus", message);
            throw new ValidationException(message);
        }
        else
        {
            row.remove("ValidationStatus");
        }
    }
}
