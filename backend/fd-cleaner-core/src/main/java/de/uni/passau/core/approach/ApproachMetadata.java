/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package de.uni.passau.core.approach;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;

/**
 *
 * @author pavel.koupil
 */
public interface ApproachMetadata {

    public abstract ApproachName getName();

    public abstract String getLabel();

    public abstract String getAuthor();

}
