/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.uni.passau.server.approach;

import de.uni.passau.core.approach.ApproachMetadata;
import de.uni.passau.core.approach.AbstractApproach.ApproachName;

/**
 *
 * @author pavel.koupil
 */
public class HyFDMetadata implements ApproachMetadata {

    @Override
    public final ApproachName getName() {
        return ApproachName.HyFD;
    }

    @Override
    public final String getLabel() {
        return "HyFD";
    }

    @Override
    public String getAuthor() {
        return "Metanome";
    }

}
