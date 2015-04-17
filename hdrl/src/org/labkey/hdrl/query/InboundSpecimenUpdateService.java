package org.labkey.hdrl.query;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.TableInfo;
import org.labkey.api.gwt.client.util.StringUtils;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.DefaultQueryUpdateService;
import org.labkey.api.query.DuplicateKeyException;
import org.labkey.api.query.QueryUpdateServiceException;
import org.labkey.api.query.ValidationException;
import org.labkey.api.security.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by susanh on 4/16/15.
 */
public class InboundSpecimenUpdateService extends DefaultQueryUpdateService
{
    public enum ValidationMode {OFF, ON, ONLY};
    private ValidationMode validationMode = ValidationMode.ON;

    public InboundSpecimenUpdateService(TableInfo queryTable, TableInfo dbTable)
    {
        super(queryTable, dbTable);
    }

    @Override
    public List<Map<String, Object>> insertRows(User user, Container container, List<Map<String, Object>> rows, BatchValidationException errors, @Nullable Map<Enum, Object> configParameters, Map<String, Object> extraScriptContext)
            throws DuplicateKeyException, QueryUpdateServiceException, SQLException
    {
        if (configParameters != null && configParameters.containsKey("validationMode"))
        {
            validationMode = ValidationMode.valueOf(configParameters.get("validationMode").toString().toUpperCase());
        }
        return super.insertRows(user, container, rows, errors, configParameters, extraScriptContext);
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


    private void validate(Map<String, Object> row) throws ValidationException
    {
        // TODO doValidation that bar codes are unique and SSN+FMP+SOT is unique?
        List<String> errors = new ArrayList<String>();
        List<String> missingFields = new ArrayList<String>();
        if (row.get("CustomerBarcode") == null)
            missingFields.add("Customer Barcode");
        if (row.get("FMPId") == null)
            missingFields.add("FMP");
        if (row.get("DrawDate") == null)
            missingFields.add("Draw Date");
        if (row.get("SSN") == null)
            missingFields.add("SSN");
        else
        {
            String ssn = (String) row.get("SSN");
            if (!ssn.matches("\\d{3}-\\d{2}-\\d{4}"))
            {
                errors.add("Invalid SSN");
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
