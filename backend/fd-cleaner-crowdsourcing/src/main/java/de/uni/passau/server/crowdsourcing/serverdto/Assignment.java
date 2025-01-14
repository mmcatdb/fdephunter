package de.uni.passau.server.crowdsourcing.serverdto;

import de.uni.passau.core.nex.NegativeExample;

public class Assignment {

    public final ExpertUser expert;
    public final NegativeExample negativeExample;

    public Assignment(ExpertUser expert, NegativeExample negativeExample) {
        this.expert = expert;
        this.negativeExample = negativeExample;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Assignment{");
        sb.append("expertUser=").append(expert);
        sb.append(", negativeExample=").append(negativeExample);
        sb.append('}');
        return sb.toString();
    }

}
