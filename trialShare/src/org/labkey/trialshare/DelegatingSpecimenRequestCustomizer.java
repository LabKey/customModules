package org.labkey.trialshare;

import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.settings.AdminConsole;
import org.labkey.api.settings.OptionalFeatureService;
import org.labkey.api.study.SpecimenService;
import org.labkey.api.util.HtmlString;

/** Decides whether to supply the default behavior or an ITN-specific one based on an experimental feature flag */
public class DelegatingSpecimenRequestCustomizer implements SpecimenService.SpecimenRequestCustomizer
{
    private final SpecimenService.SpecimenRequestCustomizer _default;
    private final SpecimenService.SpecimenRequestCustomizer _itn = new ITNSpecimenRequestCustomizer();
    private SpecimenService.SpecimenRequestCustomizer _active;

    private static final String ITN_SPECIMEN_HANDLING_FEATURE_NAME = "ITNSpecimenHandling";

    public DelegatingSpecimenRequestCustomizer(SpecimenService.SpecimenRequestCustomizer defaultCustomizer)
    {
        _default = defaultCustomizer;

        _active = OptionalFeatureService.get().isFeatureEnabled(ITN_SPECIMEN_HANDLING_FEATURE_NAME) ? _itn : _default;

        AdminConsole.addExperimentalFeatureFlag(ITN_SPECIMEN_HANDLING_FEATURE_NAME, "ITN specimen behavior",
                "This feature allows empty specimen requests, adds ITN-specific messages, hides some reporting options, and other tweaks", false);

        OptionalFeatureService.get().addFeatureListener(ITN_SPECIMEN_HANDLING_FEATURE_NAME, (feature, enabled) -> _active = enabled ? _itn : _default);
    }

    @Override
    public boolean allowEmptyRequests()
    {
        return _active.allowEmptyRequests();
    }

    @Override
    public Integer getDefaultDestinationSiteId()
    {
        return _active.getDefaultDestinationSiteId();
    }

    @Override
    public boolean omitTypeGroupingsWhenReporting()
    {
        return _active.omitTypeGroupingsWhenReporting();
    }

    @Override
    public boolean canChangeStatus(User user)
    {
        return _active.canChangeStatus(user);
    }

    @Override
    public boolean hideRequestWarnings()
    {
        return _active.hideRequestWarnings();
    }

    @Override
    public HtmlString getSubmittedMessage(Container c, int requestId)
    {
        return _active.getSubmittedMessage(c, requestId);
    }
}
