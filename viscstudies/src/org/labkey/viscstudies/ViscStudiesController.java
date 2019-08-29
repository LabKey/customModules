/*
 * Copyright (c) 2011-2019 LabKey Corporation
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

package org.labkey.viscstudies;

import org.labkey.api.action.SimpleRedirectAction;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.security.RequiresPermission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.util.URLHelper;
import org.labkey.api.view.ActionURL;
import org.springframework.validation.Errors;

public class ViscStudiesController extends SpringActionController
{
    private static final DefaultActionResolver _actionResolver = new DefaultActionResolver(ViscStudiesController.class);

    public ViscStudiesController()
    {
        setActionResolver(_actionResolver);
    }

    @RequiresPermission(ReadPermission.class)
    public class BeginAction extends SimpleRedirectAction
    {
        @Override
        public URLHelper getRedirectURL(Object o)
        {
            return new ActionURL("viscstudies", "studydesigns", getContainer());
        }
    }
}