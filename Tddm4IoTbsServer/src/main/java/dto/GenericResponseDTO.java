/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author DUVAL CARVAJAL
 */
@Getter
@Setter
@Data
public class GenericResponseDTO {
    private String status;
    private String message;
    private String data;
}
