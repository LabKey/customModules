package org.labkey.hdrl.view;

import java.util.Date;

/**
 * Created by susanh on 7/8/15.
 */
public class RequestResultBean
{
    private int _requestId;
    private int _requestStatusId;
    private String _customerNote;
    private Date _received;
    private Date _completed;
    private String _container;
    private int _createdBy;
    private Date _created;
    private int _modifiedBy;
    private Date _modified;

    public Date getCompleted()
    {
        return _completed;
    }

    public void setCompleted(Date completed)
    {
        _completed = completed;
    }

    public String getContainer()
    {
        return _container;
    }

    public void setContainer(String container)
    {
        _container = container;
    }

    public Date getCreated()
    {
        return _created;
    }

    public void setCreated(Date created)
    {
        _created = created;
    }

    public int getCreatedBy()
    {
        return _createdBy;
    }

    public void setCreatedBy(int createdBy)
    {
        _createdBy = createdBy;
    }

    public String getCustomerNote()
    {
        return _customerNote;
    }

    public void setCustomerNote(String customerNote)
    {
        _customerNote = customerNote;
    }

    public Date getModified()
    {
        return _modified;
    }

    public void setModified(Date modified)
    {
        _modified = modified;
    }

    public int getModifiedBy()
    {
        return _modifiedBy;
    }

    public void setModifiedBy(int modifiedBy)
    {
        _modifiedBy = modifiedBy;
    }

    public Date getReceived()
    {
        return _received;
    }

    public void setReceived(Date received)
    {
        _received = received;
    }

    public int getRequestId()
    {
        return _requestId;
    }

    public void setRequestId(int requestId)
    {
        _requestId = requestId;
    }

    public int getRequestStatusId()
    {
        return _requestStatusId;
    }

    public void setRequestStatusId(int requestStatusId)
    {
        _requestStatusId = requestStatusId;
    }
}
