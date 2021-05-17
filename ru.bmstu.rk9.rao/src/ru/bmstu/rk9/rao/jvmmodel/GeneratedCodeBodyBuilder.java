package ru.bmstu.rk9.rao.jvmmodel;

import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtend2.lib.StringConcatenationClient;

class GeneratedCodeBodyBuilder extends StringConcatenationClient {
    final StringConcatenation collectedLines;
    
    public GeneratedCodeBodyBuilder() {
        collectedLines = new StringConcatenation();
    }

    public GeneratedCodeBodyBuilder append(StringConcatenationClient newLines) {
        collectedLines.append(newLines);
        collectedLines.newLineIfNotEmpty();
        return this;
    }

    public GeneratedCodeBodyBuilder append(String newLienes) {
        collectedLines.append(newLienes);
        collectedLines.newLineIfNotEmpty();
        return this;
    }
    
    

    @Override
    protected void appendTo(TargetStringConcatenation target) {
    	target.append(collectedLines);
    }
}
