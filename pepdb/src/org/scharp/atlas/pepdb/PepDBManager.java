package org.scharp.atlas.pepdb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.labkey.api.data.Container;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.Sort;
import org.labkey.api.data.SqlExecutor;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.Table;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableSelector;
import org.labkey.api.query.FieldKey;
import org.labkey.api.security.User;
import org.scharp.atlas.pepdb.model.OptimalEpitopeList;
import org.scharp.atlas.pepdb.model.Parent;
import org.scharp.atlas.pepdb.model.PeptideGroup;
import org.scharp.atlas.pepdb.model.PeptidePool;
import org.scharp.atlas.pepdb.model.PeptidePoolAssignment;
import org.scharp.atlas.pepdb.model.Peptides;
import org.scharp.atlas.pepdb.model.PoolType;
import org.scharp.atlas.pepdb.model.ProteinCategory;
import org.scharp.atlas.pepdb.model.Source;

import java.sql.SQLException;
import java.util.HashMap;

/**
 * Handles Data Operations.
 *
 * @version $Id: PeptideManager.java 34530 2009-08-10 20:06:30Z sravani $
 */
public class PepDBManager
{

    private static Logger log = LogManager.getLogger(PepDBManager.class);
    private static PepDBSchema schema = PepDBSchema.getInstance();
    /**
     * Static class
     */
    private PepDBManager()
    {
    }

    public static PeptideGroup[] getPeptideGroups()
    {
        Sort sort = new Sort(PepDBSchema.COLUMN_PEPTIDE_GROUP_ID);
        return new TableSelector(schema.getTableInfoPeptideGroups(), null, sort).getArray(PeptideGroup.class);
    }

    public static PeptidePool[] getPeptidePools()
    {
        Sort sort = new Sort(PepDBSchema.COLUMN_PEPTIDE_POOL_ID);
        return new TableSelector(schema.getTableInfoPeptidePools(), null, sort).getArray(PeptidePool.class);
    }

    public static PoolType[] getPoolTypes()
    {
        Sort sort = new Sort(PepDBSchema.COLUMN_POOL_TYPE_ID);
        return new TableSelector(schema.getTableInfoPoolType(), null, sort).getArray(PoolType.class);
    }

    public static ProteinCategory[] getProteinCategory()
    {
        return new TableSelector(schema.getTableInfoProteinCat(), null, new Sort(PepDBSchema.COLUMN_PROTEIN_CAT_ID)).getArray(ProteinCategory.class);
    }

    public static OptimalEpitopeList[] getOptimalEpitopeList()
    {
        return new TableSelector(schema.getTableInfoOptimalEpitopeList(), null, new Sort(PepDBSchema.COLUMN_OPTIMAL_EPITOPE_LIST_ID)).getArray(OptimalEpitopeList.class);
    }

    public static Peptides[] getPeptides()
    {
        TableInfo tInfo = schema.getTableInfoPeptides();
        return new TableSelector(tInfo, null, new Sort("peptide_id")).getArray(Peptides.class);
    }

    /**
     * @param peptideGroup
     * @return the number of peptides in a given group
     */
    public static Integer getCount(Integer peptideGroup)
    {
        SimpleFilter containerFilter = new SimpleFilter(PepDBSchema.COLUMN_PEPTIDE_GROUP_ID, peptideGroup);

        return (int)new TableSelector(PepDBSchema.getInstance().getTableInfoViewGroupPeptides(), new SimpleFilter("peptide_group_id", peptideGroup), null).getRowCount();
    }

    public static Peptides insertPeptide(User user, Peptides p) throws Exception
    {
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoPeptides();
        Peptides dbPeptide = peptideExists(p, tInfo);
        if(dbPeptide == null)
            dbPeptide = Table.insert(user,tInfo,p);
        return dbPeptide;
    }

    public static Peptides updatePeptide(User user, Peptides p) throws Exception
    {
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoPeptides();
        return Table.update(user, tInfo, p, p.getPeptide_id());
    }

    public static PeptidePool updatePeptidePool(User user, PeptidePool p) throws Exception
    {
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoPeptidePools();
        return Table.update(user,tInfo,p,p.getPeptide_pool_id());
    }

    public static Parent insertParent(User user,Parent parent) throws Exception
    {
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoParent();
        return Table.insert(user,tInfo,parent);
    }

    public static Parent parentExists(Parent p)
    {
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoParent();
        Parent dbParent = null;
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("peptide_id"), p.getPeptide_id());
        sFilter.addCondition(FieldKey.fromParts("linked_parent"), p.getLinked_parent());
        Parent[] dbParents = new TableSelector(tInfo, tInfo.getColumns("peptide_id,linked_parent"), sFilter, null).getArray(Parent.class);
        if (dbParents.length > 0)
            dbParent = dbParents[0];
        return dbParent;
    }

    public static Peptides peptideExists(Peptides p, TableInfo tInfo)
    {
        Peptides dbPeptide = null;
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("peptide_sequence"), p.getPeptide_sequence());
        Peptides[] dbPeptides = new TableSelector(tInfo, sFilter, null).getArray(Peptides.class);
        if(dbPeptides.length > 0)
            dbPeptide = dbPeptides[0];
        return dbPeptide;
    }

    public static Source insertSource(User user, Source src) throws Exception
    {
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoSource();
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("peptide_group_id"), src.getPeptide_group_id());
        sFilter.addCondition(FieldKey.fromParts("peptide_id"), src.getPeptide_id());
        Source dbSrc ;
        Source[] dbSrcs = new TableSelector(tInfo, sFilter, null).getArray(Source.class);
        if(dbSrcs.length > 0)
            dbSrc = dbSrcs[0];
        else
            dbSrc = Table.insert(user,tInfo,src);
        dbSrc.setIn_current_file(true);
        java.sql.Timestamp date = new java.sql.Timestamp(System.currentTimeMillis());
        String sql = "UPDATE pepdb.peptide_group_assignment SET in_current_file = true,modified = ?,modifiedby = ? WHERE peptide_group_assignment_id=?";
        Object[] params = { date,user.getUserId(),dbSrc.getPeptide_group_assignment_id() };
        new SqlExecutor(schema.getSchema()).execute(sql, params);
        return dbSrc;
    }

    public static PeptidePool insertPeptidePool(User u,PeptidePool pPool) throws Exception
    {
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoPeptidePools();
        return Table.insert(u,tInfo,pPool);
    }

    public static PeptidePoolAssignment insertPeptidesInPool(User user,PeptidePoolAssignment src) throws Exception
    {
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoPoolAssignment();
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("peptide_pool_id"), src.getPeptide_pool_id());
        sFilter.addCondition(FieldKey.fromParts("peptide_id"), src.getPeptide_id());
        PeptidePoolAssignment dbSrc ;
        PeptidePoolAssignment[] dbSrcs = new TableSelector(tInfo, sFilter, null).getArray(PeptidePoolAssignment.class);
        if(dbSrcs.length > 0)
            return null;
        else
            dbSrc = Table.insert(user,tInfo,src);
        return dbSrc;
    }

    public static Source[] getSourcesForPeptide(String peptideId)
    {
        Source[] sources = null;
        try
        {
            SimpleFilter sfilter = new SimpleFilter(FieldKey.fromParts("peptide_id"), Integer.parseInt(peptideId));
            TableInfo tInfo = PepDBSchema.getInstance().getTableInfoViewGroupPeptides();
            sources = new TableSelector(tInfo, tInfo.getColumns("peptide_group_id,peptide_id_in_group,peptide_group_name,frequency_number,frequency_number_date"), sfilter, null).getArray(Source.class);
            if (sources.length < 1)
                return null;
        }
        catch (NumberFormatException e)
        {
            log.error(e.getMessage(), e);
        }
        return sources;
    }

    public static PeptidePool[] getPoolsForPeptide(String peptideId)
    {
        SimpleFilter sfilter = new SimpleFilter(FieldKey.fromParts("peptide_id"), Integer.parseInt(peptideId));
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoViewPoolPeptides();
        PeptidePool[] pools = new TableSelector(tInfo, sfilter, null).getArray(PeptidePool.class);
        if (pools.length < 1)
            return null;
        return pools;
    }

    public static PeptideGroup insertGroup(Container c, User user, PeptideGroup pg) throws SQLException
    {
        PeptideGroup resultGroup = Table.insert(user, PepDBSchema.getInstance().getTableInfoPeptideGroups(), pg);
        return resultGroup;
    }

    public static PeptideGroup  updatePeptideGroup(User user,PeptideGroup pg) throws SQLException
    {
        return Table.update(user, PepDBSchema.getInstance().getTableInfoPeptideGroups(),pg,pg.getPeptide_group_id());
    }

    public static HashMap<String,Peptides> getPeptideSequenceMap()
    {
        HashMap<String,Peptides> peptideSequenceMap = new HashMap<>();
        Peptides[] peptides = getPeptides();
        for(Peptides peptide : peptides)
            peptideSequenceMap.put(peptide.getPeptide_sequence().trim().toUpperCase(), peptide);
        return peptideSequenceMap;
    }

    public static HashMap<String,PeptideGroup> getPeptideGroupMap()
    {
        HashMap<String,PeptideGroup> groupsMap = new HashMap<>();
        PeptideGroup[] peptideGroups = getPeptideGroups();
        for(PeptideGroup pGroup:peptideGroups)
            groupsMap.put(pGroup.getPeptide_group_name().trim().toUpperCase(), pGroup);
        return groupsMap;
    }

    public static HashMap<String,PeptidePool> getPeptidePoolMap()
    {
        HashMap<String,PeptidePool> poolsMap = new HashMap<>();
        PeptidePool[] peptidePools = getPeptidePools();
        for(PeptidePool pPool:peptidePools)
            poolsMap.put(pPool.getPeptide_pool_name().trim().toUpperCase(), pPool);
        return poolsMap;
    }

    public static HashMap<String,PoolType> getPoolTypeMap()
    {
        HashMap<String,PoolType> poolTypeMap = new HashMap<>();
        PoolType[] poolTypes = getPoolTypes();
        for(PoolType pType : poolTypes)
            poolTypeMap.put(pType.getPool_type_desc().trim().toUpperCase(), pType);
        return poolTypeMap;
    }

    public static HashMap<String,ProteinCategory> getProteinCatMap()
    {
        HashMap<String,ProteinCategory> proCatMap = new HashMap<>();
        ProteinCategory[] proCats = getProteinCategory();
        for(ProteinCategory proCat : proCats)
            proCatMap.put(proCat.getProtein_cat_desc().trim().toUpperCase(), proCat);
        return proCatMap;
    }

    public static HashMap<Integer,ProteinCategory> getProteinCatIDMap()
    {
        HashMap<Integer,ProteinCategory> proCatMap = new HashMap<>();
        ProteinCategory[] proCats = getProteinCategory();
        for(ProteinCategory proCat : proCats)
            proCatMap.put(proCat.getProtein_cat_id(), proCat);
        return proCatMap;
    }

    public static HashMap<String,OptimalEpitopeList> getOptimalEpitopeListMap()
    {
        HashMap<String,OptimalEpitopeList> optListMap = new HashMap<>();
        OptimalEpitopeList[] optLists = getOptimalEpitopeList();
        for(OptimalEpitopeList optList : optLists)
            optListMap.put(optList.getOptimal_epitope_list_desc().trim().toUpperCase(), optList);
        return optListMap;
    }

    public static Peptides getPeptideById(Integer peptideId)
    {
        TableInfo tInfo = schema.getTableInfoPeptides();
        SimpleFilter sFilter = new SimpleFilter(PepDBSchema.COLUMN_PEPTIDE_ID, peptideId);
        return new TableSelector(tInfo, sFilter, null).getObject(Peptides.class);
    }

    public static Peptides[] getParentPeptides(Peptides p) throws SQLException
    {
        TableInfo tInfo = schema.getTableInfoPeptides();
        String sql = "select * from pepdb.peptides where peptides.protein_cat_id = ? and ? between peptides.amino_acid_start_pos-2 and peptides.amino_acid_end_pos\n" +
                "and ? between peptides.amino_acid_start_pos and peptides.amino_acid_end_pos+2 and child = false";
        Peptides[] peptides = new SqlSelector(schema.getSchema(), sql, new Object[]{p.getProtein_cat_id(),p.getAmino_acid_start_pos(),p.getAmino_acid_end_pos()}).getArray(Peptides.class);
        return peptides;
    }

    public static Peptides[] getHyphanatedParents(Peptides p) throws SQLException
    {
        String sql = "select * from "+schema.getTableInfoPeptides()+" where peptides.protein_cat_id = ? " +
                " and peptides.peptide_sequence LIKE '%"+p.getPeptide_sequence()+"%' " +
                " and child = false";
        Peptides[] peptides = new SqlSelector(schema.getSchema(), sql, new Object[]{p.getProtein_cat_id()}).getArray(Peptides.class);
        return peptides;
    }

    public static PeptideGroup getPeptideGroupByID(Integer groupId)
    {
        TableInfo tInfo = schema.getTableInfoPeptideGroups();
        SimpleFilter sFilter = new SimpleFilter(PepDBSchema.COLUMN_PEPTIDE_GROUP_ID,groupId);
        return new TableSelector(tInfo, sFilter, null).getObject(PeptideGroup.class);
    }

    public static ProteinCategory getProCatByID(Integer procatId)
    {
        TableInfo tInfo = schema.getTableInfoProteinCat();
        SimpleFilter sFilter = new SimpleFilter(PepDBSchema.COLUMN_PROTEIN_CAT_ID,procatId);
        return new TableSelector(tInfo, sFilter, null).getObject(ProteinCategory.class);
    }

    public static PeptideGroup getPeptideGroupByName(PeptideGroup pg)
    {
        PeptideGroup [] groups = null;
        TableInfo tInfo = schema.getTableInfoPeptideGroups();
        SQLFragment sql = new SQLFragment("SELECT * FROM "+tInfo+" WHERE UPPER(peptide_group_name) = ? ");
        sql.add(pg.getPeptide_group_name().trim().toUpperCase());
        if(pg.getPeptide_group_id() != null)
        {
                sql.append("AND peptide_group_id != ?");
                sql.add(pg.getPeptide_group_id());
        }
        groups = new SqlSelector(schema.getSchema(), sql).getArray(PeptideGroup.class);

        if (groups.length > 0)
            return groups[0];
        else
            return null;
    }

    public static PeptidePool getPeptidePoolByID(Integer poolId)
    {
        TableInfo tInfo = schema.getTableInfoPeptidePools();
        SimpleFilter sFilter = new SimpleFilter(PepDBSchema.COLUMN_PEPTIDE_POOL_ID,poolId);
        return new TableSelector(tInfo, sFilter, null).getObject(PeptidePool.class);
    }

    public static int updateInCurrentFile(User user)
    {
        java.sql.Timestamp date = new java.sql.Timestamp(System.currentTimeMillis());
        String sql = "UPDATE pepdb.peptide_group_assignment SET in_current_file = false,modified = ?,modifiedby = ?";
        Object[] params = { date,user.getUserId() };
        return new SqlExecutor(PepDBSchema.getInstance().getSchema()).execute(sql,params);
    }

    public static Source getSource(Integer peptideId, Integer groupId)
    {
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoSource();
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("peptide_group_id"), groupId);
        sFilter.addCondition(FieldKey.fromParts("peptide_id"), peptideId);
        Source dbSrc ;
        Source[] dbSrcs = new TableSelector(tInfo, sFilter, null).getArray(Source.class);
        if(dbSrcs.length > 0)
            dbSrc = dbSrcs[0];
        else
            dbSrc = null;
        return dbSrc;
    }

    public static PeptidePool[] getChildrenPools(String peptidePoolId)
    {
        SimpleFilter sfilter = new SimpleFilter(FieldKey.fromParts("parent_pool_id"), Integer.parseInt(peptidePoolId));
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoViewPoolDetails();
        PeptidePool[] pools = new TableSelector(tInfo, sfilter, null).getArray(PeptidePool.class);
        if (pools.length < 1)
            return null;
        return pools;
    }

    public static Integer[] getPeptidesInPool(Integer peptidePoolId)
    {
        SimpleFilter sfilter = new SimpleFilter(FieldKey.fromParts("peptide_pool_id"), peptidePoolId);
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoPoolAssignment();
        Integer[] peptideIds = new TableSelector(tInfo.getColumn("peptide_id"), sfilter, null).getArray(Integer.class);
        if (peptideIds.length < 1)
            return null;
        return peptideIds;
    }
}