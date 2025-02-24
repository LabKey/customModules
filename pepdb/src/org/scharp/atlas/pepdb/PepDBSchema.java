package org.scharp.atlas.pepdb;

import org.labkey.api.data.DbSchema;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.dialect.SqlDialect;

/**
 * Singleton for storing DB information
 *
 * @version $Id$
 */
public class PepDBSchema
{

    private static PepDBSchema _instance   = null;
    private static final String  SCHEMA_NAME = "pepdb";

    public static final String TABLE_PEPTIDE_GROUP     = "peptide_group";
    public static final String TABLE_GROUP_TYPE        = "group_type";
    public static final String TABLE_PROTEIN_CATEGORY  = "protein_category";
    public static final String TABLE_PEPTIDES          = "peptides";
    public static final String TABLE_PEPTIDE_POOL          = "peptide_pool";
    public static final String TABLE_POOL_ASSIGNMENT        = "peptide_pool_assignment";
    public static final String TABLE_SOURCE = "peptide_group_assignment";
    public static final String TABLE_PATHOGEN = "pathogen";
    public static final String TABLE_PARENT = "parent";
    public static final String TABLE_OPTIMAL_EPITOPE_LIST = "optimal_epitope_list";
    public static final String TABLE_POOL_TYPE = "pool_type";

    public static final String VIEW_GROUP_PEPTIDES     = "group_peptides";
    public static final String VIEW_POOL_PEPTIDES     = "pool_peptides";
    public static final String VIEW_PARENT_CHILD_DETAILS = "parent_child_details";
    public static final String VIEW_POOL_DETAILS = "pool_details";

    public static final String COLUMN_PEPTIDE_GROUP_ID = "peptide_group_id";
    public static final String COLUMN_PEPTIDE_GROUP_NAME = "peptide_group_name";
    public static final String COLUMN_PROTEIN_CAT_ID   = "protein_cat_id";
    public static final String COLUMN_GROUP_TYPE_ID    = "group_type_id";
    public static final String COLUMN_PEPTIDE_ID       = "peptide_id";
    public static final String COLUMN_PEPTIDE_POOL_ID = "peptide_pool_id";
    public static final String COLUMN_PARENT_POOL_ID = "parent_pool_id";
    public static final String COLUMN_PEPTIDE_SEQUENCE       = "peptide_sequence";
    public static final String COLUMN_OPTIMAL_EPITOPE_LIST_ID   = "optimal_epitope_list_id";
    public static final String COLUMN_AMINO_ACID_START_POS   = "amino_acid_start_pos";
    public static final String COLUMN_AMINO_ACID_END_POS   = "amino_acid_end_pos";
    public static final String COLUMN_IS_CHILD   = "child";
    public static final String COLUMN_PEPTIDE_ID_IN_GROUP   = "peptide_id_in_group";
    public static final String COLUMN_POOL_TYPE_ID = "pool_type_id";
    public static final String COLUMN_PARENT_ID = "parent_id";
    public static final String COLUMN_PARENT_SEQUENCE = "parent_sequence";
    public static final String COLUMN_CHILD_ID = "child_id";
    public static final String COLUMN_CHILD_SEQUENCE = "child_sequence";
    public static final String COLUMN_IN_CURRENT_FILE = "in_current_file";
    public static final String COLUMN_PEPTIDE_GROUP_ASSIGNMENT_ID = "peptide_group_assignment_id";
    public static final String SEQUENCE_PEPTIDE_TABLE = SCHEMA_NAME + ".peptides_peptide_id_seq";


    /**
     * Singleton
     *
     * @return the only instance allowed of this class
     */
    public synchronized static PepDBSchema getInstance()
    {
        if (_instance == null)
            _instance = new PepDBSchema();

        return _instance;
    }

    private PepDBSchema()
    {
        // private contructor to prevent instantiation from
        // outside this class.
    }

    public String getSchemaName()
    {
        return SCHEMA_NAME;
    }

    public DbSchema getSchema()
    {
        return DbSchema.get(SCHEMA_NAME);
    }

    public TableInfo getTableInfoParent()
    {
        return getSchema().getTable(PepDBSchema.TABLE_PARENT);
    }

    public TableInfo getTableInfoPeptideGroups()
    {
        return getSchema().getTable(PepDBSchema.TABLE_PEPTIDE_GROUP);
    }

    public TableInfo getTableInfoPeptideGroupTypes()
    {
        return getSchema().getTable(PepDBSchema.TABLE_GROUP_TYPE);
    }

    public TableInfo getTableInfoPeptides()
    {
        return getSchema().getTable(PepDBSchema.TABLE_PEPTIDES);
    }

    public TableInfo getTableInfoProteinCat()
    {
        return getSchema().getTable(PepDBSchema.TABLE_PROTEIN_CATEGORY);
    }

    public TableInfo getTableInfoViewGroupPeptides()
    {
        return getSchema().getTable(PepDBSchema.VIEW_GROUP_PEPTIDES);
    }

    public TableInfo getTableInfoPeptidePools()
    {
        return getSchema().getTable(PepDBSchema.TABLE_PEPTIDE_POOL);
    }
    public TableInfo getTableInfoPoolAssignment() {
        return getSchema().getTable(PepDBSchema.TABLE_POOL_ASSIGNMENT);
    }
    public TableInfo getTableInfoViewPoolPeptides()
    {
        return getSchema().getTable(PepDBSchema.VIEW_POOL_PEPTIDES);
    }

    public TableInfo getTableInfoSource()
    {
        return getSchema().getTable(PepDBSchema.TABLE_SOURCE);
    }

    public TableInfo getTableInfoPathogen()
    {
        return getSchema().getTable(PepDBSchema.TABLE_PATHOGEN);
    }
    public SqlDialect getSqlDialect() 
    {
        return getSchema().getSqlDialect();
    }

    public TableInfo getTableInfoOptimalEpitopeList()
    {
        return getSchema().getTable(PepDBSchema.TABLE_OPTIMAL_EPITOPE_LIST);
    }

     public TableInfo getTableInfoViewParentChildDetails()
    {
        return getSchema().getTable(PepDBSchema.VIEW_PARENT_CHILD_DETAILS);
    }

    public TableInfo getTableInfoPoolType()
    {
        return getSchema().getTable(PepDBSchema.TABLE_POOL_TYPE);
    }

    public TableInfo getTableInfoViewPoolDetails()
    {
        return getSchema().getTable(PepDBSchema.VIEW_POOL_DETAILS);
    }
}
