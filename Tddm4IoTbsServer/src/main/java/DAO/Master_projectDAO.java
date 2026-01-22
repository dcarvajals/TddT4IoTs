/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import javax.swing.table.DefaultTableModel;
import models.Master_project;
import util.Conection;

/**
 * Class for managing project data
 *
 * @author tonyp
 */
public class Master_projectDAO {

    /**
     * Global database connection variable
     */
    private Conection conex;

    /**
     * Class constructor
     */
    public Master_projectDAO() {
        conex = new Conection();
    }

    /**
     * Get all the project.
     *
     * @param idtype indicates the option to use in the function
     * @param idquery it is only used in the case of wanting to obtain the
     * detailed data of a component
     * @return a json with the data about the components.
     */
    public String[] selectProjects(String idtype, String idquery) {
        String query = String.format("select * from master_project_select(%s, %s)", idtype, idquery);
        
        DefaultTableModel tab = conex.returnRecord(query);
        if (tab.getRowCount() > 0) {
            return new String[]{
                tab.getValueAt(0, 0).toString(),
                tab.getValueAt(0, 1).toString()};
        } else {
            return new String[]{"4", "[]"};
        }
    }
    
    public String selectProjectById(String id_project){
        String query = String.format("Select * from master_project_select('%s')", id_project);
        DefaultTableModel table = conex.returnRecord(query);
        
        if(table.getRowCount() > 0){
            return table.getValueAt(0, 0).toString();
        }
        else{
            return "";
        }
    }
    
    public String getGanttObjects(String id_project) {
        String query = String.format("Select * from get_gantt_objects('%s')", id_project);
        System.out.println(query);
        DefaultTableModel table = conex.returnRecord(query);
        
        if(table.getRowCount() > 0){
            return table.getValueAt(0, 0).toString();
        }
        else{
            return "";
        }
    }

    /**
     * Method used to create a project.
     *
     * @param proj MasterProject object with the data to process.
     * @param id_user The project administrator user.
     * @return Returns a vector with the results of the process.
     */
    public String[] insertProject(Master_project proj, String id_user) {
        String query = String.format("select * from master_project_insert('%s', %s)", proj.returnXml(), id_user);
        
        DefaultTableModel tab = conex.returnRecord(query);
        if (tab.getRowCount() > 0) {
            return new String[]{
                tab.getValueAt(0, 0).toString(),
                tab.getValueAt(0, 1).toString()};
        } else {
            return new String[]{"4", ""};
        }
    }

    /**
     * Method used to update a project.
     *
     * @param proj MasterProject object with the data to process.
     * @return Returns a vector with the results of the process.
     */
    public String[] updateProject(Master_project proj, String id_user) {
        String query = String.format("select * from master_project_update('%s', %s)", proj.returnXml(), id_user);
        DefaultTableModel tab = conex.returnRecord(query);
        if (tab.getRowCount() > 0) {
            return new String[]{
                tab.getValueAt(0, 0).toString(),
                tab.getValueAt(0, 1).toString()};
        } else {
            return new String[]{"4", ""};
        }
    }

    /**
     * Method used to update a project.
     *
     *
     * @return Returns a vector with the results of the process.
     */
    public String[] updateModule(String idproj, String module, String state, String iduser) {
        String query = String.format("select * from master_project_select_path(%s, %s, %s,'%s')",
                idproj, iduser, module, state);
        
        DefaultTableModel tab = conex.returnRecord(query);
        if (tab.getRowCount() > 0) {
            return new String[]{
                tab.getValueAt(0, 0).toString(),
                tab.getValueAt(0, 1).toString(),
                tab.getValueAt(0, 2).toString()};
        } else {
            return new String[]{"4", "", ""};
        }
    }

    /**
     * Method used to update a project.
     *
     *
     * @param idquery
     * @return Returns a vector with the results of the process.
     */
    public String[] getHome(String idquery) {
        String query = String.format("select * from query_home(%s)", idquery);
        
        DefaultTableModel tab = conex.returnRecord(query);
        if (tab.getRowCount() > 0) {
            return new String[]{
                tab.getValueAt(0, 0).toString(),
                tab.getValueAt(0, 1).toString()};
        } else {
            return new String[]{"4", "[]"};
        }
    }

    public String[] valitPermitEditJson(String idproj, String id_person) {
        String query = String.format("select * from permit_master_valid(%s, %s)", idproj, id_person);
        
        DefaultTableModel tab = conex.returnRecord(query);
        if (tab.getRowCount() > 0) {
            return new String[]{
                tab.getValueAt(0, 0).toString(),
                tab.getValueAt(0, 1).toString()};
        } else {
            return new String[]{"4", ""};
        }
    }
    
    public String getNameProject (String idproj){
        String query = "SELECT name_mp FROM master_project WHERE id_masterproject = " + idproj;
        return conex.returnRecord(query).getValueAt(0, 0).toString().replaceAll(" ", "");
    } 
    
    public boolean updateStatusDownload (String idproj, String param_download) {
        String query = "update master_project set "+param_download+" = 'T' where id_masterproject = "+idproj+";";
        return conex.modifyBD(query);
    }

    public String[] selectHomeProject(String id_type) {
        String query = String.format("select * from master_project_select_home(%s)", id_type);
        DefaultTableModel tab = conex.returnRecord(query);
        if (tab.getRowCount() > 0) {
            return new String[]{
                tab.getValueAt(0, 0).toString(),
                tab.getValueAt(0, 1).toString()};
        } else {
            return new String[]{"4", ""};
        }
    }

    public String[] deleteProject(String id_type, String id_project, String id_person) {
        String query = String.format("select * from master_project_delete(%s, %s, %s)", id_type, id_project, id_person);
        
        DefaultTableModel tab = conex.returnRecord(query);
        if (tab.getRowCount() > 0) {
            return new String[]{
                tab.getValueAt(0, 0).toString(),
                tab.getValueAt(0, 1).toString()};
        } else {
            return new String[]{"4", ""};
        }
    }

    public String[] deleteModule(String id_type, String id_subtype, String id_project, String id_person) {
        String query = String.format("select * from master_project_delete_module(%s, %s, %s, %s)", id_type, id_subtype, id_project, id_person);
        
        DefaultTableModel tab = conex.returnRecord(query);
        if (tab.getRowCount() > 0) {
            return new String[]{
                tab.getValueAt(0, 0).toString(),
                tab.getValueAt(0, 1).toString()};
        } else {
            return new String[]{"4", ""};
        }
    }

    public String[] shareProjectforEmail(String idUser, String emailShare, String idproj, String permitShare, String stateShare) {
        String query = String.format("select * from permit_master_insert_participant('%s', %s, '%s', '%s')", emailShare, idproj, permitShare, stateShare);
        
        DefaultTableModel tab = conex.returnRecord(query);
        if (tab.getRowCount() > 0) {
            return new String[]{
                tab.getValueAt(0, 0).toString()
            };
        } else {
            return new String[]{"4", ""};
        }
    }

    public String[] listShareProject(String idType, String idUser, String idMProject) {
        String query = String.format("select * from permit_master_select(%s, %s, %s)", idType, idUser, idMProject);
        
        DefaultTableModel tab = conex.returnRecord(query);
        if (tab.getRowCount() > 0) {
            return new String[]{
                tab.getValueAt(0, 0).toString(),
                tab.getValueAt(0, 1).toString()
            };
        } else {
            return new String[]{"4", ""};
        }
    }
    
    public boolean aceptInvitation (String idProject, String permit, String joinActive) {
        String sentecy = "update permit_master set permit_pm = '"+permit+"', joinactive_pm = '"+joinActive+"' where id_permitmaster = " + idProject;
        return conex.modifyBD(sentecy);
    }
    
    public String getprojectProperty(String idperson, String idmasterproject) {
        String query = String.format("select * from project_user_property('%s', '%s')", idperson, idmasterproject);
        
        DefaultTableModel tab = conex.returnRecord(query);
        if (tab.getRowCount() > 0) 
            return tab.getValueAt(0, 0).toString();
        else 
            return "[]";
    }
    
    public String[] insertMemberInProject(String permit_json) {
        System.out.println(String.format("select * from permit_master_insert('%s'::jsonb)", permit_json));
        String query = String.format("select * from permit_master_insert('%s'::jsonb)", permit_json);
        DefaultTableModel tab = conex.returnRecord(query);
        if (tab.getRowCount() > 0) {
            return new String[]{
                tab.getValueAt(0, 0).toString(),
            };
        } else {
            return new String[]{"4"};
        }
    }
    
    public String[] selectMembersInProject(String idMasterProject) {
        System.out.println(String.format("Select * from select_members_project('%s')", idMasterProject));
        String query = String.format("Select * from select_members_project('%s')", idMasterProject);
        DefaultTableModel table = conex.returnRecord(query);
        
        if(table.getRowCount() > 0){
            return new String[]{
                table.getValueAt(0, 0).toString()
            };
        }
        
        return new String []{
            "[]"
        };
    }
    
    public String parseSharePermit(String permit){
        String permitShare = permit;
        
        if (permit.equals("SHARE_ADMIN")) {
            permitShare = "A";
        } else if (permit.equals("SHARE_WRITER")) {
            permitShare = "W";
        } else {
            permitShare = "R";
        }
        
        return permitShare;
    }
    
    public String parseShareState(String state){
        String stateShare = state;
        switch (state) {
            case "WAITING":
                stateShare = "S";
                break;
            case "ACTIVE":
                stateShare = "A";
                break;
            default:
                stateShare = "I";
                break;
        }
        
        return stateShare;
    }
    
    public boolean activateOpenAi (String id_master_project, String status_openai) {
        String sentency = "update master_project set status_openai = '"+status_openai+"' where id_masterproject = " + id_master_project;
        return conex.modifyBD(sentency);
    }
}
