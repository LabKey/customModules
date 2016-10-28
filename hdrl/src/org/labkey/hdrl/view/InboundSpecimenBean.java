/*
 * Copyright (c) 2015-2016 LabKey Corporation
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

import java.util.Date;

/**
 * Created by binalpatel on 5/12/15.
 */
public class InboundSpecimenBean
{
    private Integer _rowId;
    private String _customerBarCode;
    private Integer _dodId;
    private String _genderId;
    private String _initials;
    private Integer _numberOfContainers;
    private String _lastName;
    private String _firstName;
    private String _middleName;
    private Date _birthDate;
    private String _ssn;
    private Integer _inboundRequestId;
    private String _fmpCode;
    private Integer _fmpId;
    private String _ducCode;
    private Integer _dutyCodeId;
    private String _sotCode;
    private Integer _testingSourceId;
    private Date _drawDate;
    private String _specimenType = "Serum"; //for now.

    public String getCustomerBarCode()
    {
        return _customerBarCode;
    }

    public void setCustomerBarCode(String customerBarCode)
    {
        _customerBarCode = customerBarCode;
    }

    public String getLastName()
    {
        return _lastName;
    }

    public void setLastName(String lastName)
    {
        _lastName = lastName;
    }

    public String getFirstName()
    {
        return _firstName;
    }

    public void setFirstName(String firstName)
    {
        _firstName = firstName;
    }

    public String getMiddleName()
    {
        return _middleName;
    }

    public void setMiddleName(String middleName)
    {
        _middleName = middleName;
    }

    public Date getBirthDate()
    {
        return _birthDate;
    }

    public void setBirthDate(Date birthDate)
    {
        _birthDate = birthDate;
    }

    public String getSsn()
    {
        return _ssn;
    }

    public void setSsn(String ssn)
    {
        _ssn = ssn;
    }

    public String getFmpCode()
    {
        return _fmpCode;
    }

    public void setFmpCode(String fmpCode)
    {
        _fmpCode = fmpCode;
    }

    public Integer getDutyCodeId()
    {
        return _dutyCodeId;
    }

    public void setDutyCodeId(Integer dutyCodeId)
    {
        _dutyCodeId = dutyCodeId;
    }

    public Integer getFmpId()
    {
        return _fmpId;
    }

    public void setFmpId(Integer fmpId)
    {
        _fmpId = fmpId;
    }

    public Integer getTestingSourceId()
    {
        return _testingSourceId;
    }

    public void setTestingSourceId(Integer testingSourceId)
    {
        _testingSourceId = testingSourceId;
    }

    public String getDucCode()
    {
        return _ducCode;
    }

    public void setDucCode(String ducCode)
    {
        _ducCode = ducCode;
    }

    public String getSotCode()
    {
        return _sotCode;
    }

    public void setSotCode(String sotCode)
    {
        _sotCode = sotCode;
    }

    public Date getDrawDate()
    {
        return _drawDate;
    }

    public void setDrawDate(Date drawDate)
    {
        _drawDate = drawDate;
    }

    public String getSpecimenType()
    {
        return _specimenType;
    }

    public void setSpecimenType(String specimenType)
    {
        _specimenType = specimenType;
    }

    public Integer getInboundRequestId()
    {
        return _inboundRequestId;
    }

    public void setInboundRequestId(Integer inboundRequestId)
    {
        _inboundRequestId = inboundRequestId;
    }

    public Integer getRowId()
    {
        return _rowId;
    }

    public void setRowId(Integer rowId)
    {
        _rowId = rowId;
    }

    public Integer getDodId()
    {
        return _dodId;
    }

    public void setDodId(Integer dodId)
    {
        _dodId = dodId;
    }

    public String getGenderId()
    {
        return _genderId;
    }

    public void setGenderId(String genderId)
    {
        _genderId = genderId;
    }

    public String getInitials()
    {
        return _initials;
    }

    public void setInitials(String initials)
    {
        _initials = initials;
    }

    public Integer getNumberOfContainers()
    {
        return _numberOfContainers;
    }

    public void setNumberOfContainers(Integer numberOfContainers)
    {
        _numberOfContainers = numberOfContainers;
    }
}
