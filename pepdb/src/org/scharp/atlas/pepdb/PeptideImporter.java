package org.scharp.atlas.pepdb;

import org.labkey.api.attachments.AttachmentFile;
import org.labkey.api.security.User;
import org.scharp.atlas.pepdb.model.OptimalEpitopeList;
import org.scharp.atlas.pepdb.model.Parent;
import org.scharp.atlas.pepdb.model.PeptideGroup;
import org.scharp.atlas.pepdb.model.Peptides;
import org.scharp.atlas.pepdb.model.ProteinCategory;
import org.scharp.atlas.pepdb.model.Source;
import org.springframework.validation.Errors;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jan 8, 2010
 * Time: 10:51:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class PeptideImporter
{
    private HashMap<String,PeptideGroup> peptideGroupMap;
    private HashMap<String, ProteinCategory> proteinCategoryMap;
    private HashMap<Integer, ProteinCategory> proteinCatIDMap;
    private HashMap<String,OptimalEpitopeList> optimalElitopeListMap;
    ArrayList<Peptides> peptideIdList;

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

    public HashMap<String, ProteinCategory> getProteinCategoryMap() throws SQLException
    {
        if(proteinCategoryMap == null)
            proteinCategoryMap = PepDBManager.getProteinCatMap();
        return proteinCategoryMap;
    }

    public void setProteinCategoryMap(HashMap<String, ProteinCategory> proteinCategoryMap)
    {
        this.proteinCategoryMap = proteinCategoryMap;
    }

    public HashMap<Integer, ProteinCategory> getProteinCatIDMap() throws SQLException
    {
        if(proteinCatIDMap == null)
            proteinCatIDMap = PepDBManager.getProteinCatIDMap();
        return proteinCatIDMap;
    }

    public void setProteinCatIDMap(HashMap<Integer, ProteinCategory> proteinCatIDMap)
    {
        this.proteinCatIDMap = proteinCatIDMap;
    }

    public HashMap<String, OptimalEpitopeList> getOptimalElitopeListMap() throws SQLException
    {
        if(optimalElitopeListMap == null)
            optimalElitopeListMap = PepDBManager.getOptimalEpitopeListMap();
        return optimalElitopeListMap;
    }

    public void setOptimalElitopeListMap(HashMap<String, OptimalEpitopeList> optimalElitopeListMap)
    {
        this.optimalElitopeListMap = optimalElitopeListMap;
    }

    public ArrayList<Peptides> getPeptideIdList() throws SQLException
    {
        if(peptideIdList == null)
        {
            peptideIdList = new ArrayList<Peptides>();
            Peptides[] peptides = PepDBManager.getPeptides();
            for(Peptides p: peptides)
            {
                peptideIdList.add(p);
            }
        }
        return peptideIdList;
    }

    public void setPeptideIdList(ArrayList<Peptides> peptideIdList)
    {
        this.peptideIdList = peptideIdList;
    }

    public boolean process(User user, AttachmentFile peptideFile, Errors errors, List<Peptides> resultPeptides) throws SQLException
    {
        try{
            String fileName = peptideFile.getFilename();
            InputStreamReader inStream = new InputStreamReader(peptideFile.openInputStream());
            BufferedReader br = new BufferedReader(inStream);
            String line = br.readLine();
            boolean validFile = validateFirstLine(line,errors);
            if(!validFile)
                return false;
            getPeptideGroupMap();
            getProteinCategoryMap();
            getOptimalElitopeListMap();
            HashMap<String,Peptides> peptideSequenceMap = PepDBManager.getPeptideSequenceMap();
            int lineNo =1;
            ArrayList<Peptides> newpeptidesList =new ArrayList<Peptides>();
            HashMap<String,String> lineMap = new HashMap<String,String>();
            while((line = br.readLine()) != null)
            {
                lineNo++;
                if(line.length()>0)
                {
                    if(validateLine(line,errors,lineNo))
                    {
                        Peptides peptide = createPeptide(line,fileName);
                        if(peptide != null)
                        {
                            newpeptidesList.add(peptide);
                            lineMap.put(peptide.getPeptide_sequence(), line);
                        }
                    }
                }
            }
            if(errors.getErrorCount() >0)
            {
                errors.reject(null,"File Import Failed.\nThere are "+errors.getErrorCount()+" errors in the file : "+fileName);
                return false;
            }
            getPeptideIdList();
            PepDBManager.updateInCurrentFile(user);
            for(Peptides p : newpeptidesList)
            {
                if(peptideSequenceMap.containsKey(p.getPeptide_sequence()))
                {
                    if(p.getPeptide_id() == null || p.getPeptide_id().toString().length() == 0)
                        p.setPeptide_id(peptideSequenceMap.get(p.getPeptide_sequence()).getPeptide_id());
                    insertGroups(p,user);
                    resultPeptides.add(p);
                }
                else
                {
                    Peptides dbPep = PepDBManager.insertPeptide(user,p);
                    peptideSequenceMap.put(p.getPeptide_sequence(),dbPep);
                    peptideIdList.add(dbPep);
                    p.setPeptide_id(dbPep.getPeptide_id());
                    insertGroups(p,user);
                    resultPeptides.add(p);
                }
            }
            getProteinCatIDMap();
            for(Peptides p : peptideIdList)
            {
                if(p.isChild())
                {
                    Peptides[] parents;
                    if(proteinCatIDMap.get(p.getProtein_cat_id()).getProtein_cat_desc().contains("-"))
                    parents = PepDBManager.getHyphanatedParents(p);
                    else
                    parents = PepDBManager.getParentPeptides(p);
                    for(Peptides par : parents)
                    {
                        Parent parent = new Parent();
                        parent.setLinked_parent(par.getPeptide_id());
                        parent.setPeptide_id(p.getPeptide_id());
                        Parent dbParent = PepDBManager.parentExists(parent);
                        if(dbParent == null)
                            dbParent = PepDBManager.insertParent(user,parent);
                        if(!par.isParent())
                        {
                            par.setParent(true);
                            PepDBManager.updatePeptide(user,par);
                        }
                    }
                }
            }

        }
        catch(Exception e)
        {
            errors.reject(null,e.getMessage());
            return false;
        }
        return true;
    }

    private void insertGroups(Peptides p,User user) throws Exception
    {
        Source src = p.getSrc();
        src.setPeptide_id(p.getPeptide_id());
        PepDBManager.insertSource(user,src);
    }

    private boolean validateLine(String line,Errors errors,int lineNo)
    {
        String [] fields = new String[10];
        for(int i =0;i<line.split("\t",10).length;i++)
        {
            fields[i]=line.split("\t",10)[i];
        }
        if(fields.length < 8 )
            errors.reject(null,"Line number : "+lineNo+"must have 8-10 fields.The Peptides File has to be tab delimited and should have atleast 8 fields.");
        else if((fields[0] == null || fields[0].length() == 0) || (fields[1] == null || fields[1].length() == 0)||
                (fields[2] == null || fields[2].length() == 0) || (fields[3] == null || fields[3].length() == 0)||
                (fields[4] == null || fields[4].length() == 0) || (fields[5] == null || fields[5].length() == 0)||
                (fields[6] == null || fields[6].length() == 0) || (fields[7] == null || fields[7].length() == 0))
            errors.reject(null,"Line number : "+lineNo+" is missing one of the field values \n" +
                    "'PEPTIDE SEQUENCE','IS CHILD','PROTEIN CATEGORY','PEPTIDE GROUP','ID','SEQUENCE LENGTH',\n" +
                    "'AASTART','AAEND','IN A LIST' and 'HLA RESTRICTION'.");
        else
        {
            if(!fields[0].trim().toUpperCase().matches("[A-Z]+") || fields[0].trim().toUpperCase().equals("NA") )
                errors.reject(null,"Line number : "+lineNo+" contains an invalid 'PEPTIDE SEQUENCE' : " + fields[0] +
                        "The Peptide Sequence must only contain letters(A-Z) and no spaces.Peptide Sequence 'NA' is invalid.\n");
            else
            {
                if(validateInteger(fields[5].trim()) != null && fields[0].trim().length() != validateInteger(fields[5].trim()))
                errors.reject(null,"Line number : "+lineNo+" has a 'PEPTIDE SEQUENCE' : " + fields[0] +
                        "whose length is not equal to 'SEQUENCE LENGTH' .Check the file.\n");
            }
            if(!fields[1].trim().toUpperCase().equals("TRUE") && !fields[1].trim().toUpperCase().equals("FALSE") )
                errors.reject(null,"Line number : "+lineNo+" contains an invalid 'IS CHILD' : " + fields[1] +
                        "The Is Child must only contain either 'TRUE' or 'FALSE'.\n");
            if(!proteinCategoryMap.containsKey(fields[2].trim().toUpperCase()))
                errors.reject(null,"Line number : "+lineNo+" contains the 'PROTEIN CATEGORY' " + fields[2] +
                        " that does not exists in the database. Contact SCHARP to add new Protein Category");
            if(!peptideGroupMap.containsKey(fields[3].trim().toUpperCase()))
                errors.reject(null,"Line number : "+lineNo+" contains a 'PEPTIDE GROUP' " + fields[3] +
                        " which is not in the database. You can add peptides to existing groups only or need to add a new Group.");
            if (fields[4].trim() == null)
                errors.reject(null,"Line number : "+lineNo+" contains an invalid value for 'ID' "+ fields[4]+
                        "'ID' values must be float.");
            if (validateInteger(fields[5].trim()) == null || validateInteger(fields[6].trim()) == null || validateInteger(fields[7].trim()) == null)
                errors.reject(null,"Line number : "+lineNo+" contains an invalid values for one of " +
                        "'SEQUENCE LENGTH'("+fields[5] +"),"+ "'AASTART'("+fields[6]+") or 'AAEND'("+fields[7]+").\n"+
                        "All of these values must be Integers.\n");
            if(proteinCategoryMap.containsKey(fields[2].trim().toUpperCase()) && validateInteger(fields[6].trim()) != null
                    && validateInteger(fields[7].trim()) != null && !fields[2].trim().toUpperCase().contains("-")
                    && validateInteger(fields[7].trim()) < validateInteger(fields[6].trim()))
                errors.reject(null,"Line number : "+lineNo+" contains an invalid values for one of " +
                        "'AASTART'("+fields[6]+") or 'AAEND'("+fields[7]+").'AASTART' must be less than 'AAEND' for non-hyphanated Protein Category.");

            if(fields[1].trim().toUpperCase().equals("TRUE"))
            {
                if(!fields[3].trim().toUpperCase().equals("OPTIMAL EPITOPES"))
                    errors.reject(null,"Line number : "+lineNo+" If the peptide is a child then the Peptide Group: "+fields[2].trim().toUpperCase()+
                            " must be 'OPTIMAL EPITOPES'.");
                if(fields[8] == null || fields[8].length() == 0 || !optimalElitopeListMap.containsKey(fields[8].trim().toUpperCase()))
                    errors.reject(null,"Line number : "+lineNo+" If the peptide is a child then the value for 'IN A LIST' is required and must pre exist in database.Contact SCHARP to add new Optimal Epitope List");
            }
            else
            {
                if((fields[8] != null && fields[8].length() != 0) || (fields[9] != null && fields[9].length() != 0))
                    errors.reject(null,"Line number : "+lineNo+" If the peptide is not a child then the values for 'IN A LIST' and 'HLA RESTRICTION' are not required.");
            }
        }
        if(errors.getErrorCount() >0)
            return false;
        return true;
    }

    private Peptides createPeptide(String line,String fileName) throws SQLException
    {
        String [] fields = new String[10];
        for(int i =0;i<line.split("\t",10).length;i++)
        {
            fields[i]=line.split("\t",10)[i];
        }
        Peptides peptide = new Peptides();
        peptide.setPeptide_sequence(fields[0].trim().toUpperCase());
        peptide.setChild(Boolean.parseBoolean(fields[1].trim().toLowerCase()));
        peptide.setProtein_cat_id(proteinCategoryMap.get(fields[2].trim().toUpperCase()).getProtein_cat_id());
        Source src = new Source();
        src.setPeptide_group_id(peptideGroupMap.get(fields[3].trim().toUpperCase()).getPeptide_group_id());
        src.setPeptide_id_in_group(fields[4].trim());
        peptide.setSrc(src);
        peptide.setSequence_length(validateInteger(fields[5].trim()));
        peptide.setAmino_acid_start_pos(validateInteger(fields[6].trim()));
        peptide.setAmino_acid_end_pos(validateInteger(fields[7].trim()));
        if(fields[8]!=null && fields[8].trim().length() !=0)
            peptide.setOptimal_epitope_list_id(optimalElitopeListMap.get(fields[8].trim().toUpperCase()).getOptimal_epitope_list_id());
        if(fields[9] !=null && fields[9].trim().length() !=0)
        {
            String  hla = fields[9].trim();
            if(hla.charAt(0) == '"')
            hla = hla.substring(1,hla.length());
            if(hla.charAt(hla.length()-1) == '"')
            hla = hla.substring(0,hla.length()-2);
            peptide.setHla_restriction(hla);
        }
        peptide.setParent(false);
        peptide.setSrc_file_name(fileName);
        return peptide;
    }

    public static Integer validateInteger(String value)
    {
        try
        {
            return Integer.valueOf(value);
        }
        catch(NumberFormatException e){return null;}
    }
    private boolean validateFirstLine(String line, Errors errors)
    {
        String [] fields = line.split("\t");
        if(fields.length  != 10)
            errors.reject(null,"Line number : 1 in the Peptides file has to be tab delimited and should have 10 fields.\n"+
                    "'PEPTIDE SEQUENCE','IS CHILD','PROTEIN CATEGORY','PEPTIDE GROUP','ID','SEQUENCE LENGTH',\n" +
                    "'AASTART','AAEND','IN A LIST' and 'HLA RESTRICTION'.\n"+
                    "Peptides file is not in the right format.Please check the file and try to upload again.");
        else{
            for(int i = 0;i <10;i++)
            {
                if(fields[i] ==null || fields[i].length() == 0)
                    errors.reject(null,"Line number : 1 in the Peptides file has to be tab delimited and should have 10 fields.\n"+
                            "'PEPTIDE SEQUENCE','IS CHILD','PROTEIN CATEGORY','PEPTIDE GROUP','ID','SEQUENCE LENGTH',\n" +
                            "'AASTART','AAEND','IN A LIST' and 'HLA RESTRICTION'.\n"+
                            "Peptides file is not in the right format.Please check the file and try to upload again.");
            }
        }
        if(fields[0] != null && !fields[0].trim().equalsIgnoreCase("PEPTIDE SEQUENCE"))
            errors.reject(null,"File Import Failed.\nThe first field in the first line (Header) must be 'PEPTIDE SEQUENCE'");
        if(fields[1] != null && !fields[1].trim().equalsIgnoreCase("IS CHILD"))
            errors.reject(null,"File Import Failed.\nThe Second field in the first line (Header) must be 'IS CHILD'");
        if(fields[2] != null && !fields[2].trim().equalsIgnoreCase("PROTEIN CATEGORY"))
            errors.reject(null,"File Import Failed.\nThe Third field in the first line (Header) must be 'PROTEIN CATEGORY'");
        if(fields[3] != null && !fields[3].trim().equalsIgnoreCase("PEPTIDE GROUP"))
            errors.reject(null,"File Import Failed.\nThe Fourth field in the first line (Header) must be 'PEPTIDE GROUP'");
        if(fields[4] != null && !fields[4].trim().equalsIgnoreCase("ID"))
            errors.reject(null,"File Import Failed.\nThe Fifth field in the first line (Header) must be 'ID'");
        if(fields[5] != null && !fields[5].trim().equalsIgnoreCase("SEQUENCE LENGTH"))
            errors.reject(null,"File Import Failed.\nThe Sixth field in the first line (Header) must be 'SEQUENCE LENGTH'");
        if(fields[6] != null && !fields[6].trim().equalsIgnoreCase("AASTART"))
            errors.reject(null,"File Import Failed.\nThe Seventh field in the first line (Header) must be 'AASTART'");
        if(fields[7] != null && !fields[7].trim().equalsIgnoreCase("AAEND"))
            errors.reject(null,"File Import Failed.\nThe Eighth field in the first line (Header) must be 'AAEND'");
        if(fields[8] != null && !fields[8].trim().equalsIgnoreCase("IN A LIST"))
            errors.reject(null,"File Import Failed.\nThe Ninth field in the first line (Header) must be 'IN A LIST'");
        if(fields[9] != null && !fields[9].trim().equalsIgnoreCase("HLA RESTRICTION"))
            errors.reject(null,"File Import Failed.\nThe Tenth field in the first line (Header) must be 'HLA RESTRICTION'");
        if(errors.getErrorCount() > 0)
            return false;
        return true;
    }
}
