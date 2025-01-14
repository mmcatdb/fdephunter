/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package de.uni.passau.core.dataset;

/**
 *
 * @author pavel.koupil
 */
public interface DatasetMetadata {

    public String getFilepath();

    public String getFilename();

    public long getSize();

    public boolean hasHeader();

    public int getNumberOfColumns();

    public int getNumberOfRows();
}
