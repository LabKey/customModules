/*
 * Copyright (c) 2013 LabKey Corporation
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
package org.labkey.icemr;

import org.apache.log4j.Logger;
import org.labkey.api.collections.CaseInsensitiveHashMap;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerFilterable;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.DeferredUpgrade;
import org.labkey.api.data.Results;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableSelector;
import org.labkey.api.data.UpgradeCode;
import org.labkey.api.exp.ChangePropertyDescriptorException;
import org.labkey.api.exp.ExperimentException;
import org.labkey.api.exp.PropertyType;
import org.labkey.api.exp.api.ExpProtocol;
import org.labkey.api.exp.api.ExpSampleSet;
import org.labkey.api.exp.api.ExperimentService;
import org.labkey.api.exp.property.Domain;
import org.labkey.api.exp.property.DomainProperty;
import org.labkey.api.exp.property.Lookup;
import org.labkey.api.exp.property.PropertyService;
import org.labkey.api.module.ModuleContext;
import org.labkey.api.module.ModuleUpgrader;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.*;
import org.labkey.api.security.SecurityManager;
import org.labkey.api.study.assay.AssayProtocolSchema;
import org.labkey.api.study.assay.AssayProvider;
import org.labkey.api.study.assay.AssaySaveHandler;
import org.labkey.api.study.assay.AssayService;
import org.labkey.icemr.assay.IcemrSaveHandler;
import org.labkey.icemr.assay.Tracking.AdaptationSaveHandler;
import org.labkey.icemr.assay.Tracking.TrackingSaveHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Dax
 * Date: 10/16/13
 * Time: 5:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class IcemrUpgradeCode implements UpgradeCode
{
    private static final Logger _log = Logger.getLogger(IcemrUpgradeCode.class);
    private static final String UNKNOWN_SCIENTIST_ACCOUNT = "UnknownScientist@labkey.com";
    private static final String ADAPTATION_SAMPLE_SET = "Adaptation Flasks";
    private static final String SELECTION_SAMPLE_SET = "Selection Flasks";
    private Map<String, User> _mapUsers;

    // called from 13.20 - 13.21 to convert the Scientist property from a text field to
    // an actual column
    @SuppressWarnings({"UnusedDeclaration"})
    @DeferredUpgrade
    public void upgradeScientistToUser(final ModuleContext context)
    {
        if (context.isNewInstall())
            return;

        ModuleUpgrader.getLogger().info("Started upgrading ICEMR module");
        upgradeExperiments(context);
        ModuleUpgrader.getLogger().info("Finished upgrading ICEMR module");
    }

    private void upgradeExperiments(ModuleContext context)
    {
        // get all the ICEMR assay instances
        for (ExpProtocol protocol : ExperimentService.get().getAllExpProtocols())
        {
            AssayProvider provider = AssayService.get().getProvider(protocol);
            if (provider != null)
            {
                AssaySaveHandler sh = provider.getSaveHandler();
                if (sh != null)
                {
                    if (sh instanceof IcemrSaveHandler)
                        upgradeAssay(context, protocol, provider);

                    // Tracking assays must have a sample set
                    if (sh instanceof TrackingSaveHandler)
                        upgradeSampleSets(context, protocol);
                }
            }
        }
    }

    private void upgradeSampleSets(ModuleContext context, ExpProtocol protocol)
    {
        for(Container container : protocol.getExpRunContainers())
        {
            for (ExpSampleSet sampleSet : ExperimentService.get().getSampleSets(container, context.getUpgradeUser(), true))
            {
                if (sampleSet.getName().equalsIgnoreCase(ADAPTATION_SAMPLE_SET) ||
                    sampleSet.getName().equalsIgnoreCase(SELECTION_SAMPLE_SET))
                {
                    upgradeSampleSet(context, protocol, container, sampleSet);
                }
            }
        }
    }

    private void upgradeAssay(ModuleContext context, ExpProtocol protocol, AssayProvider provider)
    {
        UpgradeItem upgradeItem = new AssayUpgradeItem(protocol, provider);
        doUpgrade(context.getUpgradeUser(), protocol.getContainer(), upgradeItem);
    }

    private void upgradeSampleSet(ModuleContext context, ExpProtocol protocol, Container container, ExpSampleSet sampleSet)
    {
        SampleSetUpgradeItem upgradeItem = new SampleSetUpgradeItem(protocol, sampleSet);
        doUpgrade(context.getUpgradeUser(), container, upgradeItem);
    }

    private void doUpgrade(User user, Container container, UpgradeItem upgradeItem)
    {
        if (!shouldUpgradeDomain(upgradeItem.getDomain()))
        {
            return;
        }
        ExpProtocol protocol = upgradeItem.getProtocol();

        ModuleUpgrader.getLogger().info("Upgrading " + upgradeItem.getName() + " : " + protocol.getName() +
                " in folder: " + container.getPath());

        try (DbScope.Transaction transaction = ExperimentService.get().getSchema().getScope().ensureTransaction())
        {
            //
            // first, add a result property to hold the Scientist as a user
            //
            addScientistUserProperty(user, upgradeItem);

            //
            // second, map the old scientist column to the new user column
            //
            mapScientistToUser(user, upgradeItem);

            //
            // third, get rid of the old scientist text column and rename the new scientist user column
            //
            migrateScientistProperty(user, upgradeItem.getDomain());

            transaction.commit();
        }
        catch (Exception e)
        {
            // fail upgrading the run but continue on to subsequent runs
            _log.error("An error occurred upgrading " + upgradeItem.getName() + " : " + protocol.getName() + " in folder: " + container.getPath(), e);
        }
    }

    private void mapScientistToUser(User user, UpgradeItem upgradeItem)
            throws Exception
    {
        for(Container container : upgradeItem.getProtocol().getExpRunContainers())
        {
            TableInfo ti = upgradeItem.getTableInfo(user, container);
            if (ti == null)
                continue;

            List<Map<String, Object>> rows = getRowsToUpdate(ti, upgradeItem);
            if (rows.size() > 0)
            {

                try
                {
                    upgradeItem.beforeUpdate();
                    QueryUpdateService qus = ti.getUpdateService();
                    qus.updateRows(user, container, rows, rows, null);
                }
                finally
                {
                    upgradeItem.afterUpdate();
                }
            }
        }
    }

    private List<Map<String, Object>> getRowsToUpdate(TableInfo ti, UpgradeItem upgradeItem) throws Exception
    {
        TableSelector ts = new TableSelector(ti, null, null);
        List<Map<String, Object>> updateRows = new ArrayList<>();

        try (Results rsData = ts.getResults())
        {
            while(rsData.next())
            {
                Map<String, Object> row = new CaseInsensitiveHashMap<>();
                String displayName = rsData.getString(IcemrSaveHandler.SCIENTIST);
                upgradeItem.addKey(rsData, row);
                row.put(IcemrSaveHandler.SCIENTIST_USER, getUserId(displayName));
                updateRows.add(row);
            }
        }
        return updateRows;
    }

    //
    // retrieve the actual user id.  If the user is not found then return the id of
    // the 'UnknownScientist' accout
    //
    private int getUserId(String displayName) throws Exception
    {
        User u = null;

        if (null == _mapUsers)
        {
            _mapUsers = new CaseInsensitiveHashMap<>();
        }

        if (_mapUsers.containsKey(displayName))
        {
            u = _mapUsers.get(displayName);
        }
        else
        {
            // getUserByDisplayName doesn't use the UserCache so lazily stash away users as we find them
            u = UserManager.getUserByDisplayName(displayName);
            if (u != null)
            {
                _mapUsers.put(displayName, u);
            }
            else
            {
                // we didn't find the user so map to our UnknownScientist
                u = ensureUnknownScientist();
            }
        }

        return u.getUserId();
    }

    //
    // create an UnknownScientist guest account if we didn't find the scientist
    //
    private User ensureUnknownScientist() throws ValidEmail.InvalidEmailException, SecurityManager.UserManagementException
    {
        ValidEmail email = new ValidEmail(UNKNOWN_SCIENTIST_ACCOUNT);
        User u = UserManager.getUser(email);
        if (u == null)
        {
            SecurityManager.NewUserStatus status = SecurityManager.addUser(email);
            SecurityManager.verify(email, status.getVerification());
            SecurityManager.setVerification(email, null);
            SecurityManager.setPassword(email, SecurityManager.createTempPassword());
            u = status.getUser();
        }

        return u;
    }

    private void addScientistUserProperty(User user, UpgradeItem upgradeItem) throws ChangePropertyDescriptorException
    {
        Domain domain = upgradeItem.getDomain();
        DomainProperty p = domain.getPropertyByName(IcemrSaveHandler.SCIENTIST_USER);
        if (p == null)
        {
            p = domain.addProperty();
            p.setName(IcemrSaveHandler.SCIENTIST_USER);
            p.setLabel(IcemrSaveHandler.SCIENTIST_LABEL);
            p.setPropertyURI(domain.getTypeURI() + "#" + IcemrSaveHandler.SCIENTIST_USER);
            p.setType(PropertyService.get().getType(domain.getContainer(), PropertyType.INTEGER.getXmlName()));
            p.setLookup(new Lookup(upgradeItem.getProtocol().getContainer(), "core", "users"));
            domain.save(user);
        }
    }

    private void migrateScientistProperty(User user, Domain domain)  throws ChangePropertyDescriptorException
    {
        DomainProperty pNew = domain.getPropertyByName(IcemrSaveHandler.SCIENTIST_USER);
        DomainProperty pOld = domain.getPropertyByName(IcemrSaveHandler.SCIENTIST);

        if (null != pNew)
        {
            // at this point, the property should be required
            pNew.setRequired(true);
            pNew.setName(IcemrSaveHandler.SCIENTIST);
            pNew.setPropertyURI(domain.getTypeURI() + "#" + IcemrSaveHandler.SCIENTIST);
        }

        if (null != pOld)
        {
            pOld.delete();
        }

        domain.save(user);
    }

    //
    // if this assay or sampleset has a Scientist column of type text then it needs to be
    // upgraded
    //
    private boolean shouldUpgradeDomain(Domain domain)
    {
        DomainProperty p = domain.getPropertyByName(IcemrSaveHandler.SCIENTIST);
        if (p != null)
        {
            return (p.getType().getLabel().equalsIgnoreCase("string"));
        }

        return false;
    }

    interface UpgradeItem
    {
        ExpProtocol getProtocol();
        String getName();
        Domain  getDomain();
        TableInfo getTableInfo(User user, Container container);
        void addKey(Results rs, Map<String, Object> row) throws SQLException;
        void beforeUpdate() throws ExperimentException;
        void afterUpdate() throws ExperimentException;
    }

    abstract class BaseUpgradeItem implements UpgradeItem
    {
        protected ExpProtocol _protocol;
        protected Domain _domain;

        BaseUpgradeItem(ExpProtocol protocol)
        {
            _protocol = protocol;
        }

        @Override
        public ExpProtocol getProtocol()
        {
            return _protocol;
        }

        @Override
        public void beforeUpdate() throws ExperimentException
        {
        }

        @Override
        public void afterUpdate() throws ExperimentException
        {
        }
    }

    class AssayUpgradeItem extends BaseUpgradeItem
    {
        private AssayProvider _provider;
        boolean _editableResults;

        AssayUpgradeItem(ExpProtocol protocol, AssayProvider provider)
        {
            super(protocol);
            _provider = provider;
        }

        @Override
        public String getName()
        {
            return _provider.getName();
        }

        @Override
        public Domain getDomain()
        {
            if (null == _domain)
                _domain = _provider.getResultsDomain(_protocol);

            return  _domain;
        }

        @Override
        public TableInfo getTableInfo(User user, Container container)
        {
            AssayProtocolSchema schema = _provider.createProtocolSchema(user, container,  _protocol, null);
            ContainerFilterable ft = schema.createDataTable();
            return ft;
        }

        @Override
        public void addKey(Results rs, Map<String, Object> row) throws SQLException
        {
            row.put("rowid", rs.getInt("rowid"));
        }

        @Override
        public void beforeUpdate() throws ExperimentException
        {
            _editableResults = _provider.isEditableResults(_protocol);
            _provider.setEditableResults(_protocol, true);
        }

        @Override
        public void afterUpdate() throws ExperimentException
        {
            _provider.setEditableResults(_protocol, _editableResults);
        }
    }

    class SampleSetUpgradeItem extends BaseUpgradeItem
    {
        private ExpSampleSet _sampleSet;
        SampleSetUpgradeItem(ExpProtocol protocol, ExpSampleSet sampleSet)
        {
            super(protocol);
            _sampleSet = sampleSet;
        }

        @Override
        public String getName()
        {
            return _sampleSet.getName();
        }

        @Override
        public Domain getDomain()
        {
            if (null == _domain)
                _domain = _sampleSet.getType();

            return  _domain;
        }

        @Override
        public TableInfo getTableInfo(User user, Container container)
        {
            UserSchema schema = QueryService.get().getUserSchema(user, container, "Samples");
            TableInfo ti = schema.getTable(_sampleSet.getName());
            return ti;
        }

        @Override
        public void addKey(Results rs, Map<String, Object> row) throws SQLException
        {
            row.put(AdaptationSaveHandler.SampleId, rs.getString(AdaptationSaveHandler.SampleId));
        }
    }
}
