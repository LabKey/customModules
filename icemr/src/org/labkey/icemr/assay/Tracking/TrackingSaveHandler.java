package org.labkey.icemr.assay.Tracking;

import org.json.JSONObject;
import org.labkey.api.exp.api.ExpExperiment;
import org.labkey.api.exp.api.ExpMaterial;
import org.labkey.api.exp.api.ExpProtocol;
import org.labkey.api.query.ValidationException;

import org.labkey.api.study.assay.AssaySaveHandler;
import org.labkey.api.view.ViewContext;
import org.labkey.icemr.IcemrSaveHandler;

/**
  * User: Dax
 * Date: 10/9/13
 * Time: 11:43 AM
  */

//
// The TrackingSaveHandler handles both Drug Selection and Culture Adaptation protocols
// Currently no specific behavior is needed for Drug Selection.  Code in this class
// should be specific to both protocols
//
public class TrackingSaveHandler extends IcemrSaveHandler
{
    private AssaySaveHandler _delegate;

    public TrackingSaveHandler()
    {
    }

    @Override
    public void beforeSave(ViewContext context, JSONObject rootJson, ExpProtocol protocol)
    {
        if (protocol.getName().equalsIgnoreCase(AdaptationSaveHandler.ProtocolName))
        {
            _delegate = new AdaptationSaveHandler();
        }
    }

    @Override
    public ExpMaterial handleMaterial(ViewContext context, JSONObject materialObject) throws ValidationException
    {
        if (null != _delegate)
            _delegate.handleMaterial(context, materialObject);

        return super.handleMaterial(context, materialObject);
    }

    @Override
    public void afterSave(ViewContext context, ExpExperiment batch, ExpProtocol protocol) throws Exception
    {
        if (null != _delegate)
            _delegate.afterSave(context, batch, protocol);
    }
}
