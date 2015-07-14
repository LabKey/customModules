/*
 * Copyright (c) 2015 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.labkey.hdrl.query;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.DefaultQueryUpdateService;
import org.labkey.api.query.DuplicateKeyException;
import org.labkey.api.query.InvalidKeyException;
import org.labkey.api.query.QueryUpdateServiceException;
import org.labkey.api.query.ValidationException;
import org.labkey.api.security.User;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
            InboundRequestUpdateService.validateUniqueness((Integer) rows.get(0).get("InboundRequestId"));
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
            InboundRequestUpdateService.validateUniqueness((Integer) rows.get(0).get("InboundRequestId"));
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


    public static void validate(Map<String, Object> row) throws ValidationException
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date drawDate = null;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date today = calendar.getTime();
        List<String> errors = new ArrayList<String>();
        List<String> missingFields = new ArrayList<String>();
        if (row.get("FMPId") == null)
            missingFields.add("FMP");
        if (row.get("DrawDate") == null)
            missingFields.add("Draw Date");
        else
        {
            if (row.get("DrawDate") instanceof Date)
                drawDate = (Date) row.get("DrawDate");
            else
            {
                try
                {
                    drawDate = dateFormat.parse((String) row.get("DrawDate"));
                    if (drawDate.after(today))
                    {
                        errors.add("Draw date cannot be in the future");
                    }
                }
                catch (ParseException e)
                {
                    errors.add("Invalid draw date format");
                }
            }
        }
        if (row.get("BirthDate") != null)
        {
            Date birthDate = null;
            if (row.get("BirthDate") instanceof Date)
                birthDate = (Date) row.get("BirthDate");
            else
            {
                try
                {
                    birthDate = dateFormat.parse((String) row.get("BirthDate"));
                    if (birthDate.after(today))
                    {
                        errors.add("Birth date cannot be in the future");
                    }
                    else if ((drawDate != null) && (drawDate.before(birthDate)))
                    {
                        errors.add("Draw date cannot be before birth date");
                    }
                }
                catch (ParseException e)
                {
                    errors.add("Invalid birth date format");
                }
            }
        }
        if (row.get("SSN") == null || StringUtils.isEmpty(String.valueOf(row.get("SSN"))))
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
