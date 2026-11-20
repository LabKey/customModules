/*
 * Copyright (c) 2008-2026 LabKey Corporation
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
package org.labkey.specimen.report;

import org.labkey.api.data.SimpleFilter;
import org.labkey.api.study.Visit;
import org.labkey.api.util.DemoMode;
import org.labkey.api.util.Formats;
import org.labkey.api.util.HtmlString;
import org.labkey.api.util.HtmlStringBuilder;
import org.labkey.api.view.ActionURL;
import org.labkey.specimen.actions.SpecimenController;
import org.labkey.specimen.actions.SpecimenReportActions;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import static org.labkey.api.util.LinkBuilder.simpleLink;

public class SpecimenTypeVisitReport extends SpecimenVisitReport<SummaryByVisitType>
{
    public SpecimenTypeVisitReport(String titlePrefix, Collection<? extends Visit> visits, SimpleFilter filter, SpecimenVisitReportParameters parameters)
    {
        super(titlePrefix, visits, filter, parameters);
    }

    @Override
    public Collection<Row> createRows()
    {
        SpecimenTypeLevel level = getTypeLevelEnum();
        SummaryByVisitType[] countSummary = SpecimenReportManager.get().getSpecimenSummaryByVisitType(_container, getUser(), _filter, isViewPtidList(), level, getBaseCustomView());
        Map<String, Row> rows = new TreeMap<>();

        for (SummaryByVisitType count : countSummary)
        {
            String key = count.getPrimaryType() + "/" +
                    (count.getDerivative() != null ? count.getDerivative() : "All") + "/" +
                    (count.getAdditive() != null ? count.getAdditive() : "All");
            Row row = rows.get(key);
            if (row == null)
            {
                String[] titleHierarchy = level.getTitleHierarchy(count);
                row = new Row(titleHierarchy);
                rows.put(key, row);
            }
            setVisitAsNonEmpty(count.getVisit());
            row.add(count);
        }

        return rows.values();
    }

    private String getCellSummaryText(SummaryByVisitType summary)
    {
        StringBuilder summaryString = new StringBuilder();
        if (isViewVialCount())
            summaryString.append(summary.getVialCount());
        if (isViewParticipantCount())
        {
            if (!summaryString.isEmpty())
                summaryString.append("/");
            summaryString.append(summary.getParticipantCount());
        }
        if (isViewVolume())
        {
            if (!summaryString.isEmpty())
                summaryString.append("/");
            //summaryString.append(summary.getTotalVolume());
            summaryString.append(Formats.f2.format(summary.getTotalVolume()));
        }
        return summaryString.toString();
    }

    @Override
    protected String[] getCellExcelText(Visit visit, SummaryByVisitType summary)
    {
        if (summary == null || summary.getVialCount() == null)
            return new String[] {};
        String summaryText = getCellSummaryText(summary);
        boolean hasSummaryText = summaryText != null && !summaryText.isEmpty();
        int ptidCount = isViewPtidList() ? summary.getParticipantIds().size() : 0;
        String[] strArray = new String[ptidCount + (hasSummaryText ? 1 : 0)];
        if (hasSummaryText)
            strArray[0] = summaryText;
        if (ptidCount > 0)
        {
            int currentIndex = (hasSummaryText ? 1 : 0);
            for (String s : summary.getParticipantIds())
                strArray[currentIndex++] = s;
        }
        return strArray;
    }

    @Override
    protected HtmlString getCellHtml(Visit visit, SummaryByVisitType summary)
    {
        if (summary == null || summary.getVialCount() == null)
            return HtmlString.NBSP;
        ActionURL link = SpecimenController.getSpecimensURL(_container, true);
        link = updateURLFilterParameter(link, "SpecimenDetail.Visit/SequenceNumMin", visit.getSequenceNumMin());

        link = updateURLFilterParameter(link, "SpecimenDetail.PrimaryType/Description", summary.getPrimaryType());
        SpecimenTypeLevel level = getTypeLevelEnum();
        if (level == SpecimenTypeLevel.Derivative || level == SpecimenTypeLevel.Additive)
            link = updateURLFilterParameter(link, "SpecimenDetail.DerivativeType/Description", summary.getDerivative());
        if (level == SpecimenTypeLevel.Additive)
            link = updateURLFilterParameter(link, "SpecimenDetail.AdditiveType/Description", summary.getAdditive());

        if (_filter != null)
            addFilterParameters(link, visit, summary);
        String summaryString = getCellSummaryText(summary);
        HtmlStringBuilder cellHtml = HtmlStringBuilder.of();
        if (!summaryString.isEmpty())
            cellHtml.append(simpleLink(summaryString, link));

        if (isViewPtidList())
        {
            if (!cellHtml.isEmpty())
                cellHtml.append(HtmlString.BR);
            if (summary.getParticipantIds() != null)
            {
                for (Iterator<String> it = summary.getParticipantIds().iterator(); it.hasNext();)
                {
                    String participantId = it.next();
                    ActionURL url = new ActionURL(SpecimenReportActions.TypeParticipantReportAction.class, _container);
                    url.addParameter("participantId", participantId);
                    url.addParameter(SpecimenVisitReportParameters.PARAMS.typeLevel, getTypeLevelEnum().name());
                    url.addParameter(SpecimenVisitReportParameters.PARAMS.statusFilterName, getStatusFilterName());
                    url.addParameter(SpecimenVisitReportParameters.PARAMS.viewVialCount, isViewVialCount());
                    url.addParameter(SpecimenVisitReportParameters.PARAMS.viewParticipantCount, isViewParticipantCount());
                    url.addParameter(SpecimenVisitReportParameters.PARAMS.viewVolume, isViewVolume());
                    if (getBaseCustomViewName() != null && !getBaseCustomViewName().isEmpty() &&
                        !SpecimenVisitReportParameters.DEFAULT_VIEW_ID.equals(getBaseCustomViewName()))
                    {
                        url.addParameter(SpecimenVisitReportParameters.PARAMS.baseCustomViewName, getBaseCustomViewName());
                    }
                    cellHtml.append(simpleLink(DemoMode.id(participantId, getContainer(), getUser()), url));
                    if (it.hasNext())
                        cellHtml.append(HtmlString.BR);
                }
            }
        }
        return cellHtml.getHtmlString();
    }
}
