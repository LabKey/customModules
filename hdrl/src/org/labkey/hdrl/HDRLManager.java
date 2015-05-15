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

package org.labkey.hdrl;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.labkey.api.data.Container;
import org.labkey.api.data.PropertyManager;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.TableSelector;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.User;
import org.labkey.hdrl.query.HDRLQuerySchema;
import org.labkey.hdrl.view.InboundRequestBean;
import org.labkey.hdrl.view.InboundSpecimenBean;

import java.util.List;
import java.util.Map;

public class HDRLManager
{
    private static final Logger LOG = Logger.getLogger(HDRLManager.class);
    private static final String HDRL_SENSITIVE_DATA_TIME_WINDOW = "hdrlSensitiveDataDeletionTimeWindow";
    private static final String NUM_OF_DAYS = "HDRLSensitiveDataDeletionWindow";
    private static final int DEFAULT_NUM_OF_DAYS = 30;

    private static final HDRLManager _instance = new HDRLManager();

    private HDRLManager()
    {
        // prevent external construction with a private default constructor
    }

    public static HDRLManager get()
    {
        return _instance;
    }

    public InboundRequestBean getInboundRequest(User user, Container container, Integer requestId)
    {
        UserSchema schema = QueryService.get().getUserSchema(user, container, HDRLQuerySchema.NAME);
        SQLFragment sql = new SQLFragment("SELECT r.RequestId, r.ShippingNumber, s.Name as RequestStatus, c.Name as ShippingCarrier, t.Name as TestType FROM ");
        sql.append("(SELECT * FROM hdrl.InboundRequest WHERE (Container = ?) AND (RequestId = ?)) r ");
        sql.add(container);
        sql.add(requestId);
        sql.append("LEFT JOIN hdrl.ShippingCarrier c on r.ShippingCarrierId = c.RowId ")
                .append("LEFT JOIN hdrl.TestType t on r.TestTypeId = t.RowId ")
                .append("LEFT JOIN hdrl.RequestStatus s on r.RequestStatusId = s.RowId ");


        SqlSelector sqlSelector = new SqlSelector(schema.getDbSchema(), sql);
        return sqlSelector.getObject(InboundRequestBean.class);
    }

    public List<InboundSpecimenBean> getInboundSpecimen(int requestId)
    {
        TableSelector selector = new TableSelector(org.labkey.hdrl.HDRLSchema.getInstance().getTableInfoInboundSpecimen(), new SimpleFilter(new FieldKey(null, "inboundRequestId"), requestId), null);
        return selector.getArrayList(InboundSpecimenBean.class);
    }

    public static void saveProperties(HDRLController.SensitiveDataForm sensitiveDataForm)
    {
        PropertyManager.PropertyMap map = PropertyManager.getNormalStore().getWritableProperties(HDRL_SENSITIVE_DATA_TIME_WINDOW, true);
        map.clear();
        map.put(NUM_OF_DAYS, String.valueOf(sensitiveDataForm.getTimeWindowInDays()));
        map.save();
    }

    private static Map<String, String> getProperties()
    {
        return PropertyManager.getNormalStore().getProperties(HDRL_SENSITIVE_DATA_TIME_WINDOW);
    }

    public static String getNumberOfDays()
    {
        String days = getProperties().get(NUM_OF_DAYS);

        if(StringUtils.isEmpty(days))
        {
            return String.valueOf(DEFAULT_NUM_OF_DAYS);
        }

        return days;
    }

}