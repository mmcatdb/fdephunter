/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package de.uni.passau.core.nex;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pavel.koupil
 */
public class NegativeExampleUpdater {

    public NegativeExample updateNegativeExample(NegativeExample negativeExample, Decision decision) {
        List<String> view = new ArrayList<>(negativeExample.view);
        for (String column : decision.getRejectedColumns())
            view.remove(column);

        return NegativeExampleBuilder.createUpdated(negativeExample, view);
    }

}
