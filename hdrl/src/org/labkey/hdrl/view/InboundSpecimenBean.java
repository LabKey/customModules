package org.labkey.hdrl.view;

public class InboundSpecimenBean
{
    private String _customerBarCode;
    private String _lastName;
    private String _firstName;
    private String _middleName;
    private String _birthDate;
    private String _ssn;
    private String _fmpId;
    private String _dutyCodeId;
    private String _testingSourceId;
    private String _drawDate;
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

    public String getBirthDate()
    {
        return _birthDate;
    }

    public void setBirthDate(String birthDate)
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

    public String getFmpId()
    {
        return _fmpId;
    }

    public void setFmpId(String fmpId)
    {
        _fmpId = fmpId;
    }

    public String getDutyCodeId()
    {
        return _dutyCodeId;
    }

    public void setDutyCodeId(String dutyCodeId)
    {
        _dutyCodeId = dutyCodeId;
    }

    public String getTestingSourceId()
    {
        return _testingSourceId;
    }

    public void setTestingSourceId(String testingSourceId)
    {
        _testingSourceId = testingSourceId;
    }

    public String getDrawDate()
    {
        return _drawDate;
    }

    public void setDrawDate(String drawDate)
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

}
