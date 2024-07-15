/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import org.json.JSONObject;
import org.json.XML;

/**
 *
 * @author USUARIO
 */
public class Suggestion 
{
    int id_person;
    int valoration;
    String suggestion_type;
    String observation;

    public Suggestion() {
    }

    public Suggestion(int id_person, int valoration, String suggestion_type, String observation) {
        this.id_person = id_person;
        this.valoration = valoration;
        this.suggestion_type = suggestion_type;
        this.observation = observation;
    }

    public int getId_person() {
        return id_person;
    }

    public void setId_person(int id_person) {
        this.id_person = id_person;
    }

    public int getValoration() {
        return valoration;
    }

    public void setValoration(int valoration) {
        this.valoration = valoration;
    }

    public String getSuggestion_type() {
        return suggestion_type;
    }

    public void setSuggestion_type(String suggestion_type) {
        this.suggestion_type = suggestion_type;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

  
    
    public String returnJson() {
        JSONObject jsonU = new JSONObject(this);
        return jsonU.toString();
    }
    
    
}
