package ru.krlvm.invader;

import ru.krlvm.powertunnel.data.DataStore;

public class ScriptStore extends DataStore {
    
    public ScriptStore(String fileName) {
        super(fileName);
    }

    @Override
    public String getFileFormat() {
        return "js";
    }
}
