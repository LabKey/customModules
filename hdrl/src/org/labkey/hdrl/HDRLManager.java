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

import org.labkey.api.data.Container;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.User;
import org.labkey.hdrl.query.HDRLQuerySchema;
import org.labkey.hdrl.view.InboundRequestBean;

public class HDRLManager
{
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
}