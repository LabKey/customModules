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
package org.labkey.ms2extensions;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.exp.ExperimentDataHandler;
import org.labkey.api.exp.ExperimentException;
import org.labkey.api.exp.XarContext;
import org.labkey.api.exp.api.DataType;
import org.labkey.api.exp.api.ExpData;
import org.labkey.api.security.User;
import org.labkey.api.util.PepXMLFileType;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.ViewBackgroundInfo;

import java.io.File;
import java.io.OutputStream;
import java.util.List;

/**
 * Wrapper around the standard MS2 module's pepXML data handler that updates the peptide counts
 * after the import has completed. Delegates to the standard handler for all actual import, delete and other work.
 * Created by: jeckels
 * Date: 9/18/15
 */
public class PeptideCountPepXmlDataHandler implements ExperimentDataHandler
{
    private ExperimentDataHandler _realHandler;
    /** true if we're in the midst of finding the regular pepXML handler and shouldn't try to claim that we handle pepXML */
    private boolean _findingRealHandler = false;

    @Nullable
    @Override
    public DataType getDataType()
    {
        return null;
    }

    @Override
    public void importFile(ExpData data, File dataFile, ViewBackgroundInfo info, Logger log, XarContext context) throws ExperimentException
    {
        _realHandler.importFile(data, dataFile, info, log, context);
        PeptideCountUpdater updater = new PeptideCountUpdater();
        updater.update(data.getContainer(), info.getUser());
    }

    @Override
    public void exportFile(ExpData data, File dataFile, User user, OutputStream out) throws ExperimentException
    {
        _realHandler.exportFile(data, dataFile, user, out);
    }

    @Override
    public ActionURL getContentURL(ExpData data)
    {
        return _realHandler.getContentURL(data);
    }

    @Override
    public void beforeDeleteData(List<ExpData> datas) throws ExperimentException
    {
        _realHandler.beforeDeleteData(datas);
    }

    @Override
    public void deleteData(ExpData data, Container container, User user)
    {
        _realHandler.deleteData(data, container, user);
    }

    @Override
    public boolean hasContentToExport(ExpData data, File file)
    {
        return _realHandler.hasContentToExport(data, file);
    }

    @Override
    public void runMoved(ExpData newData, Container container, Container targetContainer, String oldRunLSID, String newRunLSID, User user, int oldDataRowID) throws ExperimentException
    {
        _realHandler.runMoved(newData, container, targetContainer, oldRunLSID, newRunLSID, user, oldDataRowID);
    }

    @Override
    public void beforeMove(ExpData oldData, Container container, User user) throws ExperimentException
    {
        _realHandler.beforeMove(oldData, container, user);
    }

    @Nullable
    @Override
    public Priority getPriority(ExpData data)
    {
        // We're trying to find the real pepXML handler, so don't make any claim on pepXML files
        if (_findingRealHandler)
        {
            return null;
        }

        PepXMLFileType ft = new PepXMLFileType();
        if (ft.isType(data.getFile()))
        {
            // We're being asked about a pepXML file
            if (_realHandler == null)
            {
                // Remember that we're asking, and check to find the MS2 module's pepXML handler
                _findingRealHandler = true;
                _realHandler = data.findDataHandler();
                if (_realHandler == this)
                {
                    throw new IllegalStateException("Found ourselves as the real handler.");
                }
                _findingRealHandler = false;
            }
            return Priority.HIGHEST;
        }
        return null;
    }
}
