/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import DAO.SuggestionDAO;
import com.google.gson.JsonArray;
import models.Suggestion;
import util.Methods;

/**
 *
 * @author USUARIO
 */
public class SuggestionCtrl 
{
    
    SuggestionDAO sDAO;
    
    public SuggestionCtrl()
    {
        sDAO=new SuggestionDAO();
    }
    
    
     public String [] SaveSuggestion(Suggestion s){
        String status = "0";
        String message = "Error when saving data";
        String data = "[]";               
        
        String [] response = sDAO.insertSuggestion(s);
        
        if((response[0].equals("1"))){
            status="1";
            message = "Data saved correctly";
        }
        
        return new String[] {status, message, data};
    }
    
}
