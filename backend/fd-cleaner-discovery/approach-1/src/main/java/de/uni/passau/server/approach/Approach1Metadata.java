/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package de.uni.passau.server.approach;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;
import de.uni.passau.core.approach.ApproachMetadata;

/**
 *
 * @author pavel.koupil
 */
public class Approach1Metadata implements ApproachMetadata {

    @Override
    public final ApproachName getName() {
        return ApproachName.Approach1;
    }

    @Override
    public final String getLabel() {
        return "Approach 1";
    }

    @Override
    public String getAuthor() {
        return "Stefan Klessinger et al.";
    }

}
