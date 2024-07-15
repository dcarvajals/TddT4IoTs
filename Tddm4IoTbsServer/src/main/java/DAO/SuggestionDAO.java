/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import javax.swing.table.DefaultTableModel;
import models.Person;
import models.Suggestion;
import util.Conection;

/**
 *
 * @author USUARIO
 */
public class SuggestionDAO 
{
    private Conection conex;
    
     public String[] insertSuggestion(Suggestion s) {
        String query = String.format("select * from suggestion_insert('%s')", s.returnJson());
        
        conex = new Conection();
        DefaultTableModel tab = conex.returnRecord(query);
        if (tab.getRowCount() > 0) {
            return new String[]{
                tab.getValueAt(0, 0).toString(),
            };
        } else {
            return new String[]{"4", "[]"};
        }
    }
    
}
