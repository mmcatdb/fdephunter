/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package de.uni.passau.core.dataset;

/**
 *
 * @author pavel.koupil
 */
public interface Dataset extends DatasetData {

    public void load();

    public void free();

    public DatasetMetadata getMetadata();

    public boolean isLoaded();

}
