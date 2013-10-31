/*
 * Copyright (c) 2009-2013 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
var _allStudies = null;
var _selectedStudies = null;
var _isViscUser = false;

LABKEY.NavTrail.setTrail("Browse Study Designs");
function getStudyDesigns(config)
{
    var params = {includeSubfolders:config.includeSubfolders};
    Ext.Ajax.request({
        url : LABKEY.ActionURL.buildURL('study-designer', 'getStudyDesigns', config.containerPath, params),
        method : 'GET',
        success: LABKEY.Utils.getCallbackWrapper(LABKEY.Utils.getOnSuccess(config), config.scope),
        failure: LABKEY.Utils.getCallbackWrapper(LABKEY.Utils.getOnFailure(config), config.scope, true)
    });

    LABKEY.Security.getUserPermissions({userId:LABKEY.Security.currentUser.id,
        successCallback:function(userPermsInfo) {
            if (userPermsInfo.container.groups && userPermsInfo.container.groups.length > 0)
            {
                for (var i = 0; i < userPermsInfo.container.groups.length; i++)
                {
                    if ("Users" == userPermsInfo.container.groups[i].name)  //Really any project level group, but can't tell proj from site gruops
                    {
                        _isViscUser = true;
                        return;
                    }
                }
            }
        }
    });
}

    Ext.onReady(function() {
    Ext.QuickTips.init();
        getStudyDesigns({successCallback:initStudyDesigns, includeSubfolders:true });
    });

    function initStudyDesigns(result) {
        _allStudies = result.studyDesigns;
        _selectedStudies = _allStudies;
        showStudyDesigns(_selectedStudies);
        initFacets();
    }

    function showStudyDesigns(designs) {
        if (null == designs || designs.length == 0)
        {
            Ext.get("result").update("<div style='padding-bottom:1em;width:800px'>No studies match the current filter. Try showing more options.</div>");
            return;
        }
        designs.sort(function (a, b) {
            return a.studyDefinition.name.localeCompare(b.studyDefinition.name);
        });
        Ext.get("result").update("");
        for (var i = 0; i < designs.length; i++)
        {
            //var design = result.studyDesigns[i].studyDefinition;
            Ext.get("result").insertHtml("beforeEnd", getStudyOverview(designs[i]));
        }
    }

    function h(s) {
        if (null == s)
            return "";
        else
            return LABKEY.Utils.encodeHtml(s);
    }

    function getStudyOverview(info) {
        var design = info.studyDefinition;
        var html = "<div id='" + design.name + "_outer' style='padding-bottom:1em;width:800px'><table width='100%' class='labkey-no-spacing'><tr><td class='study-header'>" +"" +
                   "<a href='" + LABKEY.ActionURL.buildURL("project", "begin", info.container.id) + "'>" +
                   h(design.name) + "</a></td>";
        html += "<td align=center width='33%' class='study-header'>" + (design.animalSpecies ? h(design.animalSpecies) : "&nbsp;") + "</td>";
        html += "<td align=right width='33%' class='study-header'><span style=''>" + h(design.investigator) + "</span></td></tr>";
        html += "<tr><td colspan=3>";

        if (design.description)
                    html+= h(design.description) +"<br><br>";

        html += "Immunogens: ";
        var immunogenNames = [];
        if (design.immunogens) {
            for (var i = 0; i < design.immunogens.length; i++)
                immunogenNames.push(immunogenSummary(design.immunogens[i]));
        }
        html += (immunogenNames.length ? immunogenNames.join(", ")  : "(none)") + "<br>";

        html += "Adjuvants: ";
        var adjuvantNames = [];
        if (design.adjuvants) {
            for (var i = 0; i < design.adjuvants.length; i++)
                adjuvantNames.push(design.adjuvants[i].name);
        }
        html += (adjuvantNames.length ? h(adjuvantNames.join(", ")) : "(none)") + "<br><br>";

        html += graphicalVaccinationSummary(design);
        return html;

    }

function immunogenSummary(immunogen)
{
    var ret = "<b>" + h(immunogen.name) + "</b>";
    if (immunogen.type)
        ret += ", " + immunogen.type;
    if (immunogen.admin)
        ret += ", " + immunogen.admin;

    if (immunogen.antigens)
    {
        var a = [];
        for (var i = 0; i < immunogen.antigens.length; i++)
            if (immunogen.antigens[i])
                a.push(h(immunogen.antigens[i].name));

        if (a.length)
            ret += " (" + a.join(", ") + ") ";
    }
    return ret;
}

function graphicalVaccinationSummary(design)
{
    if (null == design.immunizations || design.immunizations.length == 0)
        return "";

    var html = "";
    var timepoints = [];
    var timepointMap = {};
    for (var i = 0; i < design.immunizations.length; i++)
    {
        var immunization = design.immunizations[i];
        //Some "blank" immunizations have crept in.
        if (null == immunization.adjuvants && null == immunization.immunogens)
            continue;
        if (timepointMap[immunization.timepoint.days])
            continue;
        timepointMap[immunization.timepoint.days] = immunization.timepoint;
        timepoints.push(immunization.timepoint);
    }
    if (timepoints.length == 0)
        return "";

    timepoints.sort(function(a, b) {
        return a.days - b.days;
    });

    var PIXELS_PER_DAY = 8;
    var IMG_WIDTH = 16;
    var ZERO_OFFSET = 150;
    var imgSrc = LABKEY.contextPath + "/viscstudies/syringeSmall.gif";
    var left = Ext.get("result").getLeft();
    html += "<div style='height:20px'><span style='position:absolute;white-space:nowrap;width:150px;overflow:hidden'><b>Week</b></span>";
    for (var tpi = 0; tpi < timepoints.length; tpi++)
    {
        var week = Math.round(timepoints[tpi].days / 7);
         var iconLeft = left + ZERO_OFFSET + (week * 7 * PIXELS_PER_DAY )- (IMG_WIDTH /2);
        html += "<span style='position:absolute;left:" + iconLeft + "px'>" + week + "</span>";
    }
    html += "</div>";

    for (var ci = 0; ci < design.cohorts.length; ci++)
    {
        html += "<div style='height:20px'><span style='position:absolute;white-space:nowrap;width:150px;overflow:hidden'><b>" + h(design.cohorts[ci].name) + "</b> (" + design.cohorts[ci].count  + ")" + "</span>";
        for (var tpi = 0; tpi < timepoints.length; tpi++)
        {
            var vaccination = findVaccination(design, design.cohorts[ci], timepoints[tpi]);
            if (null != vaccination)
            {
                var components = [];
                if (vaccination.immunogens)
                    components = components.concat(vaccination.immunogens);
                if (vaccination.adjuvants)
                    components = components.concat(vaccination.adjuvants);
                var iconLeft = left + ZERO_OFFSET + (vaccination.timepoint.days * PIXELS_PER_DAY )- (IMG_WIDTH /2);
                html += "<span style='position:absolute;left:" + iconLeft + "px'><img src='" + imgSrc + "' title='" + vaccination.timepoint.days + " days, " + h(components.join(" | ")) + "'>"
                        + "<div style='display:inline;position:absolute;height:12px;background:white;width:500px;left:" + (IMG_WIDTH) + "px'>" +  components.join(" | ") + "</div>"
                        +"</span>";
            }
        }
        html += "</div>";

    }

    return html + "</table>";

//    var html = "<table><tr><td>header</td><td><img src='" + LABKEY.contextPath + "/viscstudies/syringe.jpg' style='position:relative;left:300'></td></tr></table>";
//    return html;

}
    function vaccinationSummary(design) {
        if (null == design.immunizations || design.immunizations.length == 0)
            return "";

        var html = "<table class='labkey-data-region labkey-show-borders'><tr><th></th>";
        var timepoints = [];
        var timepointMap = {};
        for (var i = 0; i < design.immunizations.length; i++)
        {
            var immunization = design.immunizations[i];
            //Some "blank" immunizations have crept in.
            if (null == immunization.adjuvants && null == immunization.immunogens)
                continue;
            if (timepointMap[immunization.timepoint.days])
                continue;
            timepointMap[immunization.timepoint.days] = immunization.timepoint;
            timepoints.push(immunization.timepoint);
        }
        if (timepoints.length == 0)
            return "";
        
        timepoints.sort(function(a, b) {
            return a.days - b.days;
        });

        for (var tpi = 0; tpi < timepoints.length; tpi++)
            html += "<th>" + h(timepoints[tpi].name) + "</th>";
        html += "</tr>";

        for (var ci = 0; ci < design.cohorts.length; ci++)
        {
            html += "<tr><td style='white-space:nowrap'><b>" + h(design.cohorts[ci].name) + "</b> (" + design.cohorts[ci].count  + ")" + "</td>";
            for (var tpi = 0; tpi < timepoints.length; tpi++)
            {
                var vaccination = findVaccination(design, design.cohorts[ci], timepoints[tpi]);
                if (null == vaccination)
                    html += "<td></td>";
                else
                {
                    var components = [];
                    if (vaccination.immunogens)
                        components = components.concat(vaccination.immunogens);
                    if (vaccination.adjuvants)
                        components = components.concat(vaccination.adjuvants);
                    html += "<td style='padding-left:5px;padding-right:5px'>" + components.join(" | ") + "</td>";
                }
            }
            html += "</tr>";

        }

        return html + "</table><br><br>";
        

    }

function findVaccination(design, cohort, timepoint)
{
    for (var i = 0; i < design.immunizations.length; i++)
    {
        var immunization = design.immunizations[i];
        if (immunization.groupName == cohort.name && immunization.timepoint.days == timepoint.days)
            return immunization;
    }
    return null;
}

function addIfNotPresent(array, value)
{
    var isobj = typeof value == 'object';
    var id =  isobj ? value.id : null;

    for (var i = 0; i < array.length; i++)
    {
        if (value == array[i] || (isobj && id == array[i].id))
            return;
    }
    
    array.push(value);
}

function Facet(id, caption, getOptionsFn) {
    this.allOptions = [];
    this.id = id;
    this.caption = caption;
    this.getOptionsForStudy = getOptionsFn;
    this.selected = {};
};

Facet.prototype = {
    allOptions:[],

    setSelected:function(index, selected) {
        this.selected[this.allOptions[index].name] = selected;
    },

    isSelected:function(index) {
        return this.selected[this.allOptions[index].name];
    },

    selectAll:function() {
        for (var i = 0; i < this.allOptions.length; i++)
            this.selected[this.allOptions[i].name] = true;
    },

    selectOnly:function(index) {
        var sel = {};
        sel[this.allOptions[index].name] = true;
        this.selected = sel;
    },

    init:function(infos) {
        var seen = {};
        for (var i = 0; i < infos.length; i++)
        {
            var optionsForStudy = this.getOptionsForStudy(infos[i]);
            for (var j = 0; j < optionsForStudy.length; j++)
            {
                if(!seen[optionsForStudy[j].name])
                {
                    seen[optionsForStudy[j].name] = true;
                    this.allOptions.push(optionsForStudy[j]);
                }
            }
        }
        this.allOptions.sort(function(a, b) { return a.name.localeCompare(b.name); });
        this.selected = seen; //All selected initially
    },

    getMatchingDesigns:function (infos) {
        var ret = [];
        for (var i = 0; i < infos.length; i++)
        {
            var optionsForStudy = this.getOptionsForStudy(infos[i]);
            for (var j = 0; j < optionsForStudy.length; j++)
            {
                if (this.selected[optionsForStudy[j].name])
                {
                    ret.push(infos[i]);
                    break;
                }
            }
        }
        return ret;
    }
};

var investigatorFacet = new Facet("investigators", "Investigators", function(info) {
        return [{name:info.studyDefinition.investigator}];
    });

var speciesFacet = new Facet("species", "Species", function (info) {
    return [{name:info.studyDefinition.animalSpecies ? info.studyDefinition.animalSpecies : "(none)"}];
});

var adjuvantFacet = new Facet("adjuvants", "Adjuvants", function (info) {
    var design = info.studyDefinition;
    var options = [];
    if (design.adjuvants) {
        for (var i = 0; i < design.adjuvants.length; i++)
            options.push({name:design.adjuvants[i].name});
    }
    else
        options.push({name:"(none)"});
    
    return options;
});

var immunogenFacet = new Facet("immunogens", "Immunogens", function (info) {
    var design = info.studyDefinition;
    var options = [];
    if (design.immunogens) {
        for (var i = 0; i < design.immunogens.length; i++)
            options.push({name:design.immunogens[i].name});
    }
    else
        options.push({name:"(none)"});

    return options;
});

var facets = [immunogenFacet, investigatorFacet, speciesFacet, adjuvantFacet];
function initFacets()
{
    for (var f = 0; f < facets.length; f++)
    {
        var facet = facets[f];
        var header = "<span style='display:inline-block;padding:8px;border-right:1px solid lightgray'><h3 style='margin-bottom:0px;padding-top:0;margin-top:0' id='facet_header_" + facet.id +"' >" + h(facet.caption) + "</h3><span id='facet_summary_" + facet.id +"'>Showing all " + h(facet.caption) + " </span></span>";
        var html = "<span  id='facet_body_" + facet.id + "'>";
        html += "<a href='#' onclick='selectAll(\"" + facet.id + "\");return false;'>Show All</a><br><br>" ;
        facet.init(_allStudies);
        html += "<table><tr><td valign=top>";
        var colLength = Math.round(facet.allOptions.length / 3);
        for (var i = 0; i < facet.allOptions.length; i++)
        {
            var option = facet.allOptions[i];
            html += "<input type='checkbox' id='" + checkboxId(facet, i) + "' checked onclick='optionClicked(this, \"" + facet.id + "\", " + i + ")'>";
            html += "<a href='#' onclick='selectOnly(\"" + facet.id + "\"," +i + ");return false;'>"+ h(option.name) + "</a><br>";
            if (i == colLength || i == colLength * 2)
                html += "</td><td valign=top>";

        }
        html += "</td></tr></table></span>";
        Ext.get("facets").insertHtml("beforeEnd", header);

        var tip = new LABKEY.ext.CalloutTip({target:"facet_header_" + facet.id, html:html, closable:true});
        tip.on("render", function(t) {
            t.getEl().alignTo("facet_header_" + facet.id, "tl-bl");
        });
    }
}

function checkboxId(facet, index) {
    return facet.id + index + "_check";
}

function optionClicked(cb, facetId, index)
{
    var facet = getFacet(facetId);
    facet.setSelected(index, cb.checked);
    updateSummary(facet);
    updateVisibleStudies();
}

function selectOnly(facetId, index)
{
    var facet = getFacet(facetId);
    facet.selectOnly(index);
    updateCheckboxes(facet);
    updateSummary(facet);
    updateVisibleStudies();
}

function selectAll(facetId)
{
    var facet = getFacet(facetId);
    facet.selectAll();
    updateCheckboxes(facet);
    updateSummary(facet);
    updateVisibleStudies();
}

function updateCheckboxes(facet) {
    for (var i = 0; i < facet.allOptions.length; i++)
        Ext.getDom(checkboxId(facet, i)).checked = facet.isSelected(i);
}

function updateVisibleStudies()
{
    var infos = _allStudies;
    var facet = null;
    for (var i = 0; i < facets.length; i++)
    {
        facet = facets[i];
        infos = facet.getMatchingDesigns(infos);
    }
    _selectedStudies = infos;
    showStudyDesigns(_selectedStudies);
}

function generateDesignSpreadsheet() {

    var infos = _selectedStudies;
    var rows = [["# One row per antigen/adjuvant. A single vaccination may be described in several rows"],["Study", "Cohort", "Days", "Component", "Name", "Admin", "Dose", "Type", "Antigen Name", "Gene", "GenBank Id", "SubType", "Sequence"]];
    for (var i = 0; i < infos.length; i++)
    {
        var info = infos[i];
        var design = info.studyDefinition;
        if (!design.immunizations)
            continue;

        for (var vi = 0; vi < design.immunizations.length; vi++)
        {
            vaccination = design.immunizations[vi];
            if (vaccination.immunogens)
            {
                for (var immi = 0; immi < vaccination.immunogens.length; immi++)
                {
                    var immunogen = findImmunogen(design, vaccination.immunogens[immi]);
                    if (immunogen.antigens && immunogen.antigens.length) {

                        for (var anti = 0; anti < immunogen.antigens.length; anti++)
                        {
                            var antigen = immunogen.antigens[anti];
                            rows.push([info.label, vaccination.groupName, vaccination.timepoint.days, "Immunogen", immunogen.name, immunogen.admin, immunogen.dose, immunogen.type, antigen.name, antigen.gene, antigen.genBankId, antigen.subtype, antigen.sequence]);
                        }

                    }
                    else
                        rows.push([info.label, vaccination.groupName, vaccination.timepoint.days, "Immunogen", immunogen.name, immunogen.admin, immunogen.dose, immunogen.type]);
                }
            }
            if (vaccination.adjuvants)
            {
                for (var adji = 0; adji < vaccination.adjuvants.length; adji++)
                {
                    var adjuvant = findAdjuvant(design, vaccination.adjuvants[adji]);
                    rows.push([info.label, vaccination.groupName, vaccination.timepoint.days, "Adjuvant", adjuvant.name, adjuvant.admin, adjuvant.dose]);
                }
            }
        }

    }

    var workbook = {fileName:"ProtocolDescriptions", sheets:[{name:"Vaccine Summary", data:rows}]} ;
    LABKEY.Utils.convertToExcel(workbook);

    function findAdjuvant(design, adjuvantName)
    {
        for (var i = 0; i < design.adjuvants.length; i++)
            if (design.adjuvants[i].name == adjuvantName)
                return design.adjuvants[i];

        return adjuvantName + " (no description found)";
    }

    function findImmunogen(design, immunogenName)
    {
        for (var i = 0; i < design.immunogens.length; i++)
            if (design.immunogens[i].name == immunogenName)
                return design.immunogens[i];

        return immunogenName + " (no description found)";
    }
}

function updateSummary(facet)
{
    var elem = Ext.get("facet_summary_" + facet.id);
    var selCount = 0;
    var allSelected = [];
    for (var key in facet.selected)
    {
        allSelected.push(key);
        selCount++;
    }

    if (selCount == facet.allOptions.length)
    {
        elem.update("Showing all " + facet.caption);
        return;
    }

    var extraText = "";
    allSelected.sort(function (a, b) {return a.localeCompare(b)});
    if (selCount > 3)
    {
        allSelected = allSelected.slice(0, 3);
        extraText = ", " + (selCount - allSelected.length) + " more";
    }
    elem.update("Showing " + allSelected.join(", ") + extraText);
}

function getFacet(facetId)
{
    for (var i = 0; i < facets.length; i++)
        if (facets[i].id == facetId)
            return facets[i];
}