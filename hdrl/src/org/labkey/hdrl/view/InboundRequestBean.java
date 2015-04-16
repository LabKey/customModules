package org.labkey.hdrl.view;

/**
 * Created by susanh on 4/15/15.
 */
public class InboundRequestBean
{
    private int _requestId;
    private String _title;
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

    public String getTitle()
    {
        return _title;
    }

    public void setTitle(String title)
    {
        _title = title;
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
