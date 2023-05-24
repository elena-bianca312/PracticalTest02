package ro.pub.cs.systems.eim.practical2test;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class PokemonInformation {

    private final String[] abilities;

    private final String type;

    private final String URL;

    public PokemonInformation(String[] abilities, String type, String URL) {
        this.abilities = abilities;
        this.type = type;
        this.URL = URL;
    }

    public String getAbilities() {
        return Arrays.toString(abilities);
    }

    public String getType() {
        return type;
    }

    public String getURL() {
        return URL;
    }

    @NonNull
    @Override
    public String toString() {
        return "PokemonInformation{" + "abilities='" + abilities + '\'' + ", type='" + type + '\'' + ", URL='" + URL + '\'' + '}';
    }
}
