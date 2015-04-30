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
package org.labkey.hdrl.view;

/**
 * Created by susanh on 4/15/15.
 */
public class InboundRequestBean
{
    private int _requestId;
    private String _shippingNumber;
    private String _requestStatus;
    private String _testType;
    private String _shippingCarrier;

    public int getRequestId()
    {
        return _requestId;
    }

    public void setRequestId(int requestId)
    {
        _requestId = requestId;
    }

    public String getShippingNumber()
    {
        return _shippingNumber;
    }

    public void setShippingNumber(String shippingNumber)
    {
        _shippingNumber = shippingNumber;
    }

    public String getRequestStatus()
    {
        return _requestStatus;
    }

    public void setRequestStatus(String requestStatus)
    {
        _requestStatus = requestStatus;
    }

    public String getTestType()
    {
        return _testType;
    }

    public void setTestType(String testType)
    {
        _testType = testType;
    }

    public String getShippingCarrier()
    {
        return _shippingCarrier;
    }

    public void setShippingCarrier(String shippingCarrier)
    {
        _shippingCarrier = shippingCarrier;
    }

}
