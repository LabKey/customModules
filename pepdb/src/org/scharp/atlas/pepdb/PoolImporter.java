package org.scharp.atlas.pepdb;

import org.labkey.api.attachments.AttachmentFile;
import org.labkey.api.security.User;
import org.scharp.atlas.pepdb.PepDBBaseController.FileForm;
import org.scharp.atlas.pepdb.model.PeptideGroup;
import org.scharp.atlas.pepdb.model.PeptidePool;
import org.scharp.atlas.pepdb.model.PeptidePoolAssignment;
import org.scharp.atlas.pepdb.model.Peptides;
import org.scharp.atlas.pepdb.model.PoolType;
import org.scharp.atlas.pepdb.model.Source;
import org.springframework.validation.Errors;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Oct 16, 2007
 * Time: 8:30:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class PoolImporter
{
    private HashMap<String,PeptidePool> peptidePoolMap;
    private HashMap<String,PoolType> poolTypeMap;
    private HashMap<String,Peptides> peptideSequenceMap;
    private HashMap<String, PeptideGroup> peptideGroupMap;
    public HashMap<String, PeptidePool> getPeptidePoolMap() throws SQLException
    {
        if(peptidePoolMap == null)
            peptidePoolMap = PepDBManager.getPeptidePoolMap();
        return peptidePoolMap;
    }

    public void setPeptidePoolMap(HashMap<String, PeptidePool> peptidePoolMap)
    {
        this.peptidePoolMap = peptidePoolMap;
    }

    public HashMap<String, PoolType> getPoolTypeMap() throws SQLException
    {
        if(poolTypeMap == null)
            poolTypeMap = PepDBManager.getPoolTypeMap();
        return poolTypeMap;
    }

    public void setPoolTypeMap(HashMap<String, PoolType> poolTypeMap)
    {
        this.poolTypeMap = poolTypeMap;
    }

    public HashMap<String, Peptides> getPeptideSequenceMap() throws SQLException
    {
        if(peptideSequenceMap == null)
            peptideSequenceMap = PepDBManager.getPeptideSequenceMap();
        return peptideSequenceMap;
    }

    public void setPeptideSequenceMap(HashMap<String, Peptides> peptideSequenceMap)
    {
        this.peptideSequenceMap = peptideSequenceMap;
    }

    public HashMap<String, PeptideGroup> getPeptideGroupMap() throws SQLException
    {
        if(peptideGroupMap == null)
            peptideGroupMap = PepDBManager.getPeptideGroupMap();
        return peptideGroupMap;
    }

    public void setPeptideGroupMap(HashMap<String, PeptideGroup> peptideGroupMap)
    {
        this.peptideGroupMap = peptideGroupMap;
    }

    public boolean process(User user, FileForm form, AttachmentFile poolFile,Errors errors) throws SQLException
    {
        String actionType = form.getActionType();
        PeptidePool [] peptidePools = PepDBManager.getPeptidePools();
        //HashMap<Integer,PeptidePool> peptidepoolMap = new HashMap<Integer,PeptidePool>();
        /*for(PeptidePool pPool : peptidePools)
        {
            peptidepoolMap.put(pPool.getPeptide_pool_id(),pPool);
        }*/
        if(actionType.equalsIgnoreCase("POOLDESC"))
        {
            try{
                InputStreamReader inStream = new InputStreamReader(poolFile.openInputStream());
                BufferedReader br = new BufferedReader(inStream);
                String line = br.readLine();
                boolean validFile = validateFirstLine(line,actionType,errors);
                if(!validFile)
                    return false;
                getPeptidePoolMap();
                getPoolTypeMap();
                ArrayList<PeptidePool> newPPools = new ArrayList<PeptidePool>();
                int lineNo =1;
                while((line = br.readLine()) != null)
                {
                    lineNo++;
                    if(line.trim().length()>0)
                    {
                        if(validateLine(line,errors,lineNo))
                        {
                            PeptidePool pPool = createPeptidePool(line);
                            if(pPool != null)
                                newPPools.add(pPool);
                        }
                    }
                }
                if(errors.getErrorCount() >0)
                {
                    errors.reject(null,"File Import Failed.\nThere are "+errors.getErrorCount()+" errors in the file : "+poolFile.getFilename());
                    return false;
                }
                for(PeptidePool pPool : newPPools)
                {
                    if(pPool.getPool_type_id() != poolTypeMap.get("POOL").getPool_type_id())
                    pPool.setParent_pool_id(peptidePoolMap.get(pPool.getParent_pool_name()).getPeptide_pool_id());
                    PeptidePool dbPeptidePool = null;
                    if(peptidePoolMap.get(pPool.getPeptide_pool_name().trim().toUpperCase()).getPeptide_pool_id() == null || peptidePoolMap.get(pPool.getPeptide_pool_name().trim().toUpperCase()).getPeptide_pool_id().toString().length() == 0)
                        dbPeptidePool= PepDBManager.insertPeptidePool(user,pPool);
                    if(dbPeptidePool != null)
                        peptidePoolMap.put(dbPeptidePool.getPeptide_pool_name().trim().toUpperCase(),dbPeptidePool);
                }
            }
            catch(Exception e)
            {
                errors.reject(null,e.getMessage());
                return false;
            }
        }
        if(actionType.equalsIgnoreCase("POOLPEPTIDES"))
        {
            try{
                InputStreamReader inStream = new InputStreamReader(poolFile.openInputStream());
                BufferedReader br = new BufferedReader(inStream);
                String line = br.readLine();
                boolean validFile = validateFirstLine(line,form.getActionType(),errors);
                if(!validFile)
                    return false;
                int lineNo =1;
                getPeptidePoolMap();
                getPeptideSequenceMap();
                getPeptideGroupMap();
                ArrayList<PeptidePoolAssignment> newpeptidesInPools = new ArrayList<PeptidePoolAssignment>();
                HashMap<Integer, ArrayList<Integer>> newpoolPeptides = new HashMap<Integer,ArrayList<Integer>>();
                while((line = br.readLine()) != null)
                {
                    lineNo++;
                    if(line.length()>0)
                    {
                        if(validatePPLine(line,errors,lineNo,newpoolPeptides))
                        {
                            PeptidePoolAssignment poolAssign = createPoolAssignment(line);
                            if(poolAssign != null)
                            {
                                newpeptidesInPools.add(poolAssign);
                                if(newpoolPeptides.containsKey(poolAssign.getPeptide_pool_id()))
                                     newpoolPeptides.get(poolAssign.getPeptide_pool_id()).add(poolAssign.getPeptide_id());
                                else
                                {
                                  ArrayList<Integer> peps = new ArrayList<Integer>();
                                  peps.add(poolAssign.getPeptide_id());
                                  newpoolPeptides.put(poolAssign.getPeptide_pool_id(),peps);
                                }

                            }
                        }
                    }

                }
                if(errors.getErrorCount()>0)
                {
                    errors.reject(null,"File Import Failed.\nThere are "+errors.getErrorCount()+" errors in the file : "+poolFile.getFilename());
                    return false;
                }
                for(PeptidePoolAssignment pAssignment : newpeptidesInPools)
                {
                    PepDBManager.insertPeptidesInPool(user,pAssignment);
                }
            }
            catch(Exception e)
            {
                errors.reject(null,e.getMessage());
                return false;
            }
        }
        return true;
    }

    private PeptidePoolAssignment createPoolAssignment(String line) throws SQLException
    {
        String [] fields = new String[3];
        for(int i =0;i<line.split("\t",3).length;i++)
        {
            fields[i]=line.split("\t",3)[i];
        }
        PeptidePoolAssignment poolAssign = new PeptidePoolAssignment();
        poolAssign.setPeptide_pool_id(peptidePoolMap.get(fields[0].trim().toUpperCase()).getPeptide_pool_id());
        poolAssign.setPeptide_id(peptideSequenceMap.get(fields[1].trim().toUpperCase()).getPeptide_id());
        Source src = PepDBManager.getSource(peptideSequenceMap.get(fields[1].trim().toUpperCase()).getPeptide_id(),peptideGroupMap.get(fields[2].trim().toUpperCase()).getPeptide_group_id());
        poolAssign.setPeptide_group_assignment_id(src.getPeptide_group_assignment_id());
        return poolAssign;
    }
    private PeptidePool createPeptidePool(String line)
    {
        String [] fields = new String[4];
        for(int i =0;i<line.split("\t",4).length;i++)
        {
            fields[i]=line.split("\t",4)[i];
        }
        PeptidePool pPool = new PeptidePool();
        pPool.setPeptide_pool_name(fields[0].trim());
        pPool.setPool_type_id(poolTypeMap.get(fields[1].trim().toUpperCase()).getPool_type_id());
        if(fields[2] != null && fields[2].trim().toUpperCase() != null && fields[2].trim().toUpperCase().length() != 0)
        pPool.setParent_pool_name(fields[2].trim().toUpperCase());
        if(fields[1].trim().toUpperCase().equalsIgnoreCase("MATRIX"))
        pPool.setMatrix_peptide_pool_id(fields[3].trim());
        peptidePoolMap.put(fields[0].trim().toUpperCase(),pPool);
        //pPool.setComment("Non SCHARP defined peptide pool");
        return pPool;

    }

    private boolean validateLine(String line,Errors errors,int lineNo)
    {
        String [] fields = new String[4];
        for(int i =0;i<line.split("\t",4).length;i++)
        {
            fields[i]=line.split("\t",4)[i];
        }
        if(fields.length < 2 )
            errors.reject(null,"Line number : "+lineNo+"must have 2 fields.The Pool Description File has to be tab delimited and must have the fields 'POOL NAME' and 'POOL TYPE'.");
        else
        {
            if((fields[0] == null || fields[0].trim().length() == 0) || (fields[1] == null || fields[1].trim().length() == 0))
                errors.reject(null,"Line number : "+lineNo+" is missing one of the required field values \n" +
                        "'POOL NAME' and 'POOL TYPE'.");
            else   {
                if(peptidePoolMap.containsKey(fields[0].trim().toUpperCase()))
                    errors.reject(null,"Line number : "+lineNo+" contains the 'POOL NAME' " + fields[0] +
                            " that already exists in the database. Check the file and upload again.");
                if(!poolTypeMap.containsKey(fields[1].trim().toUpperCase()))
                    errors.reject(null,"Line number : "+lineNo+" contains the 'POOL TYPE' " + fields[1] +
                            " that does not exist in the database. Check the file and upload again or Contact SCHARP to add new Pool Type.");
                else
                {
                    if (fields[1].trim().toUpperCase().equalsIgnoreCase("MATRIX") && (fields[3] == null || fields[3].trim().length() == 0))
                     errors.reject(null,"Line number : "+lineNo+" is missing 'MATRIX POOL ID' " + fields[1] +
                            " for 'POOL TYPE' 'MATRIX'. Check the file and upload again or Contact SCHARP to add new Pool Type.");

                    if (!fields[1].trim().toUpperCase().equalsIgnoreCase("MATRIX") && (fields[3] != null && fields[3].trim().length() != 0))
                     errors.reject(null,"Line number : "+lineNo+" has 'MATRIX POOL ID' " + fields[1] +
                            " for 'POOL TYPE' other than 'MATRIX' which is not expected. Check the file and upload again or Contact SCHARP to add new Pool Type.");

                    if (fields[1].trim().toUpperCase().equalsIgnoreCase("POOL") && (fields[2] != null && fields[2].trim().length() != 0))
                     errors.reject(null,"Line number : "+lineNo+" has 'PARENT POOL NAME' " + fields[1] +
                            " for 'POOL TYPE' 'POOL' which is not expected. Check the file and upload again or Contact SCHARP to add new Pool Type.");

                    if ((!fields[1].trim().toUpperCase().equalsIgnoreCase("POOL") && !fields[1].trim().toUpperCase().equalsIgnoreCase("OTHER")) && (fields[2] == null || fields[2].trim().length() == 0))
                     errors.reject(null,"Line number : "+lineNo+" is missing 'PARENT POOL NAME' " + fields[1] +
                            " for 'POOL TYPE' other than 'POOL' and 'OTHER'. Check the file and upload again or Contact SCHARP to add new Pool Type.");

                    else if(fields[2] != null && fields[2].trim().length() != 0)
                    {
                    if(!peptidePoolMap.containsKey(fields[2].trim().toUpperCase()))
                      errors.reject(null,"Line number : "+lineNo+" contains the 'PARENT POOL NAME' = '" + fields[2] +
                            "' that does not exist in the database or in this file prior to this entry. Parent Pool Name should exist in the database or in the same file prior to thisrow. Check the file and upload again.");
                    else
                    {
                         int poolt =  poolTypeMap.get("POOL").getPool_type_id();
                         int pt =   peptidePoolMap.get(fields[2].trim().toUpperCase()).getPool_type_id();
                        if(poolt != pt &&  poolTypeMap.get("SUB-POOL").getPool_type_id() != pt && poolTypeMap.get("OTHER").getPool_type_id() != pt)
                        {
                            errors.reject(null, "Line number : " + lineNo + " contains the 'PARENT POOL NAME' = '" + fields[2] +
                                    "' that exists in the database but not of 'POOL' or 'SUB-POOL' type. Only pool type 'POOL' or 'SUB-POOL' can have child pool. Check the file and upload again.");
                        }
                    }
                }
            }
        }
        }
        if(errors.getErrorCount() >0)
            return false;
        return true;
    }

    private boolean validatePPLine(String line,Errors errors,int lineNo,HashMap<Integer, ArrayList<Integer>> newPoolPeptides) throws SQLException
    {
        String [] fields = new String[3];
        for(int i =0;i<line.split("\t",3).length;i++)
        {
            fields[i]=line.split("\t",3)[i];
        }
        if(fields.length != 3 )
            errors.reject(null,"Line number : "+lineNo+"must have 3 fields.The Peptides In Pool File has to be tab delimited and must have the fields 'POOL NAME', 'PEPTIDE SEQUENCE' and , 'PEPTIDE GROUP'.");
        else
        {
            if((fields[0] == null || fields[0].trim().length() == 0) || (fields[1] == null || fields[1].trim().length() == 0) || (fields[2] == null || fields[2].trim().length() == 0))
                errors.reject(null,"Line number : "+lineNo+" is missing one of the field values \n" +
                        "'POOL NAME', 'PEPTIDE SEQUENCE' and , 'PEPTIDE GROUP'.");
            else   {
                if(!peptidePoolMap.containsKey(fields[0].trim().toUpperCase()))
                    errors.reject(null,"Line number : "+lineNo+" contains the 'POOL NAME' = '" + fields[0] +
                            "' that does not exist in the database. Check the file and upload again or Try to upload Pool Description file first..");
                if(!peptideSequenceMap.containsKey(fields[1].trim().toUpperCase()))
                    errors.reject(null,"Line number : "+lineNo+" contains the 'PEPTIDE SEQUENCE' = '" + fields[1] +
                            "' that does not exist in the database. Check the file and upload again or Upload Peptides first before uploading pools.");
                if(!peptideGroupMap.containsKey(fields[2].trim().toUpperCase()))
                    errors.reject(null,"Line number : "+lineNo+" contains the 'PEPTIDE GROUP' = '" + fields[2] +
                            "' that does not exist in the database. Check the file and upload again or Enter Peptide Group first before uploading pools.");
                if(peptideSequenceMap.containsKey(fields[1].trim().toUpperCase()) && peptideGroupMap.containsKey(fields[2].trim().toUpperCase()))
                {
                    Source src = PepDBManager.getSource(peptideSequenceMap.get(fields[1].trim().toUpperCase()).getPeptide_id(),peptideGroupMap.get(fields[2].trim().toUpperCase()).getPeptide_group_id());
                    if (src == null)
                     errors.reject(null,"Line number : "+lineNo+" contains the 'PEPTIDE GROUP' = '"+ fields[2] + "' and 'PEPTIDE SEQUENCE' = '"+fields[1]+
                            "' which are not associated in the database. Check the file and upload again or Add Peptide Sequence to the Peptide Group first before uploading pools.");
                    if(peptidePoolMap.containsKey(fields[0].trim().toUpperCase()))
                    {
                        PeptidePool pp = peptidePoolMap.get(fields[0].trim().toUpperCase());
                        Peptides p = peptideSequenceMap.get(fields[1].trim().toUpperCase());
                        if(pp.getParent_pool_id() != null)
                        {
                            Integer[] peptidesInParent = PepDBManager.getPeptidesInPool(pp.getParent_pool_id());

                            if( (peptidesInParent == null && (newPoolPeptides.size() == 0 || !newPoolPeptides.containsKey(pp.getParent_pool_id())))||(peptidesInParent != null && !Arrays.asList( peptidesInParent).contains(p.getPeptide_id())) ||
                                    (newPoolPeptides.size() != 0 && newPoolPeptides.containsKey(pp.getParent_pool_id()) && !newPoolPeptides.get(pp.getParent_pool_id()).contains(p.getPeptide_id())))
                                errors.reject(null,"Line number : "+lineNo+" contains the 'PEPTIDE SEQUENCE' = '" + fields[1] +
                            "' which is not in parent pool in the database or prior to this row. Check the file and upload again.");

                        }
                    }
                }

            }
        }
        if(errors.getErrorCount() >0)
            return false;
        return true;
    }

    private boolean validateFirstLine(String line,String importType,Errors errors)
    {
        String [] fields = line.split("\t");
        if(importType.equalsIgnoreCase("POOLDESC"))
        {
            if(fields.length != 4)
                errors.reject(null,"Line number : 1 must be tab delimited with the fields 'Pool Name','Pool Type','Parent Pool Name' and 'Matrix Pool Id' in the Pool Descriptions file.\n"+
                        "The header line does not match this format. Please check the file and try to upload again.");
            else
            {
                if(fields[0] ==null || fields[0].trim().length() == 0
                        || fields[1] ==null || fields[1].trim().length() == 0
                        || fields[2] ==null || fields[2].trim().length() == 0
                        || fields[3] ==null || fields[3].trim().length() == 0)
                    errors.reject(null,"Line number : 1 must be tab delimited with the fields 'Pool Name','Pool Type','Parent Pool Name' and 'Matrix Pool Id' in the Pool Descriptions file.\n"+
                            "One of the fields in header line is null or empty. Please check the file and try to upload again.");
                else
                {
                    if(fields[0] != null && !fields[0].trim().equalsIgnoreCase("POOL NAME"))
                        errors.reject(null,"Pool Descriptions File Import Failed.\nThe first field in the first line (Header) must be 'POOL NAME'");
                    if(fields[1] != null && !fields[1].trim().equalsIgnoreCase("POOL TYPE"))
                        errors.reject(null,"Pool Descriptions File Import Failed.\nThe Second field in the first line (Header) must be 'POOL TYPE'");
                    if(fields[2] != null && !fields[2].trim().equalsIgnoreCase("PARENT POOL NAME"))
                        errors.reject(null,"Pool Descriptions File Import Failed.\nThe Third field in the first line (Header) must be 'PARENT POOL NAME'");
                    if(fields[2] != null && !fields[3].trim().equalsIgnoreCase("MATRIX POOL ID"))
                        errors.reject(null,"Pool Descriptions File Import Failed.\nThe Fourth field in the first line (Header) must be 'MATRIX POOL ID'");
                }
            }
            if(errors.getErrorCount() > 0)
                return false;
            return true;
        }
        if(importType.equalsIgnoreCase("POOLPEPTIDES"))
        {
            if(fields.length != 3)
                errors.reject(null,"Line number : 1 must be tab delimited with the fields 'POOL NAME','PEPTIDE SEQUENCE'and 'PEPTIDE GROUP' in the Peptides in Pool file.\n"+
                        "The header line does not match this format. Please check the file and try to upload again.");
            else
            {
                if(fields[0] ==null || fields[0].trim().length() == 0 ||fields[1] ==null || fields[1].trim().length() == 0 || fields[2] ==null || fields[2].trim().length() == 0)
                    errors.reject(null,"Line number : 1 must be tab delimited with the fields 'POOL NAME','PEPTIDE SEQUENCE' and 'PEPTIDE GROUP'in the Peptides in Pool file.\n"+
                            "One of the fields in header line is null or empty. Please check the file and try to upload again.");
                else
                {
                    if(fields[0] != null && !fields[0].trim().equalsIgnoreCase("POOL NAME"))
                        errors.reject(null,"Peptides in Pool File Import Failed.\nThe first field in the first line (Header) must be 'POOL NAME'");
                    if(fields[1] != null && !fields[1].trim().equalsIgnoreCase("PEPTIDE SEQUENCE"))
                        errors.reject(null,"Peptides in Pool File Import Failed.\nThe Second field in the first line (Header) must be 'PEPTIDE SEQUENCE'");
                    if(fields[2] != null && !fields[2].trim().equalsIgnoreCase("PEPTIDE GROUP"))
                        errors.reject(null,"Peptides in Pool File Import Failed.\nThe Second field in the first line (Header) must be 'PEPTIDE GROUP'");
                }
            }
            if(errors.getErrorCount() > 0)
                return false;
            return true;
        }
        return true;
    }
}
