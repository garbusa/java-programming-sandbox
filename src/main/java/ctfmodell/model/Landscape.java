package ctfmodell.model;

import ctfmodell.model.enums.FieldEnum;
import ctfmodell.model.exception.LandscapeException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Landscape {

    private PoliceOfficer policeOfficer;
    private int height;
    private int width;
    private int baseX;
    private int baseY;
    private FieldEnum[][] landscape;
    private List<Flag> flags;

    public Landscape() {
        this.height = 10;
        this.width = 10;
        this.baseX = 9;
        this.baseY = 9;

        this.landscape = this.generateFields();
        this.flags = new ArrayList<Flag>();
    }

    public Landscape(int height, int width, int baseX, int baseY) {
        this.height = height;
        this.width = width;

        this.baseX = baseX;
        this.baseY = baseY;

        this.landscape = this.generateFields();
        this.flags = new ArrayList<Flag>();
    }

    private FieldEnum[][] generateFields() {
        FieldEnum[][] fields = new FieldEnum[this.height][this.width];
        for (int y = 0; y < fields.length; y++) {
            for (int x = 0; x < fields[y].length; x++) {
                if (x == this.baseX && y == this.baseY) {
                    fields[y][x] = FieldEnum.BASE;
                } else {
                    fields[y][x] = FieldEnum.EMPTY;
                }
            }
        }

        return fields;
    }

    /* **************************************** ADD & DELETE ******************************************* */

    public void addFlag(Flag flag) {
        int x = flag.getxPos();
        int y = flag.getyPos();
        switch (this.landscape[y][x]) {
            case EMPTY:
                this.landscape[y][x] = FieldEnum.FLAG;
                this.flags.add(flag);
                break;
            case POLICE_OFFICER:
                this.landscape[y][x] = FieldEnum.OFFICER_AND_FLAG;
                this.flags.add(flag);
                break;
            default:
                throw new LandscapeException(String.format("Auf den Koordinaten (%d,%d) kann keine Flagge platziert werden!", y, x));
        }
    }

    public void deleteFlag(Flag flag) {
        int x = flag.getxPos();
        int y = flag.getyPos();
        FieldEnum field = this.landscape[flag.getyPos()][flag.getxPos()];

        if(!this.isNotEndOfField(y, x)) {
            throw new LandscapeException("Es wurde versuch eine Flagge aus einem nicht existierenden Feld zu löschen!");
        }

        switch (field) {
            case FLAG:
                this.landscape[y][x] = FieldEnum.EMPTY;
                this.flags.remove(flag);
                break;
            case OFFICER_AND_FLAG:
                this.landscape[y][x] = FieldEnum.POLICE_OFFICER;
                this.flags.remove(flag);
                break;
            default:
                throw new LandscapeException(String.format("Auf den Koordinaten (%d,%d) kann keine Flagge entfernt werden!", y, x));
        }

    }

    public void addUnarmedTerrorist(int y, int x) {
        if (this.landscape[y][x] == FieldEnum.EMPTY || this.landscape[y][x] == FieldEnum.ARMED_TERRORIST) {
            this.landscape[y][x] = FieldEnum.UNARMED_TERRORIST;
        } else {
            throw new LandscapeException(String.format("Auf den Koordinaten (%d,%d) kann kein Terrorist platziert werden!", y, x));
        }
    }

    public void deleteUnarmedTerrorist(int y, int x) {
        if(!this.isNotEndOfField(y, x)) {
            throw new LandscapeException("Es wurde versuch ein Terrorist aus einem nicht existierenden Feld zu löschen!");
        } else if(this.landscape[y][x] == FieldEnum.UNARMED_TERRORIST) {
            this.landscape[y][x] = FieldEnum.EMPTY;
        } else {
            throw new LandscapeException("Auf diesem Feld befindet sich kein Terrorist!");
        }
    }

    public void addArmedTerrorist(int y, int x) {
        if (this.landscape[y][x] == FieldEnum.EMPTY || this.landscape[y][x] == FieldEnum.UNARMED_TERRORIST) {
            this.landscape[y][x] = FieldEnum.ARMED_TERRORIST;
        } else {
            throw new LandscapeException(String.format("Auf den Koordinaten (%d,%d) kann kein Terrorist platziert werden!", y, x));
        }
    }

    public void deleteArmedTerrorist(int y, int x) {
        if(!this.isNotEndOfField(y, x)) {
            throw new LandscapeException("Es wurde versuch ein Terrorist aus einem nicht existierenden Feld zu löschen!");
        } else if(this.landscape[y][x] == FieldEnum.ARMED_TERRORIST) {
            this.landscape[y][x] = FieldEnum.EMPTY;
        } else {
            throw new LandscapeException("Auf diesem Feld befindet sich kein Terrorist!");
        }
    }

    /* **************************************** HELPER ******************************************* */

    protected boolean isNotEndOfField(int y, int x) {
        return (y > -1 && y < this.landscape.length)
                && (x > -1 && x < this.landscape[y].length);
    }

    private boolean hasOfficer() {
        return this.policeOfficer != null;
    }

    /* **************************************** GETTER & SETTER ******************************************* */

    public PoliceOfficer getPoliceOfficer() {
        return policeOfficer;
    }

    public void setPoliceOfficer(PoliceOfficer policeOfficer) {
        this.policeOfficer = policeOfficer;
        int x = policeOfficer.getyPos();
        int y = policeOfficer.getxPos();
        switch (this.landscape[y][x]) {
            case EMPTY:
                this.landscape[y][x] = FieldEnum.POLICE_OFFICER;
                break;
            case FLAG:
                this.landscape[y][x] = FieldEnum.OFFICER_AND_FLAG;
                break;
            case BASE:
                this.landscape[y][x] = FieldEnum.OFFICER_AND_BASE;
                break;
            default:
                throw new LandscapeException(String.format("Auf den Koordinaten (%d,%d) kann kein Officer platziert werden!", y, x));
        }
    }

    public void unsetPoliceOfficer() {
        int x = policeOfficer.getyPos();
        int y = policeOfficer.getxPos();
        this.policeOfficer = null;
        switch (this.landscape[y][x]) {
            case POLICE_OFFICER:
                this.landscape[y][x] = FieldEnum.EMPTY;
                break;
            case OFFICER_AND_FLAG:
                this.landscape[y][x] = FieldEnum.FLAG;
                break;
            case OFFICER_AND_BASE:
                this.landscape[y][x] = FieldEnum.BASE;
                break;
            default:
                throw new LandscapeException(String.format("Auf den Koordinaten (%d,%d) kann kein Officer platziert werden!", y, x));
        }
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        //Kopiere alle möglichen Felder von der alten zur neuen

        //Prüfe ob Akteur oder Base nicht mit kopiert worden sind

        //Falls nicht dabei, setze Akteur auf (min,min) und Base (max, max)

        //Falls feld 1x1 ist = fehlermeldung und initial restoren (min 2x2 erlaubt)

        //Falls der Fall eintritt, das die Base auf ein Feld mit einer Flagge gesetzt wird, dann entwerne Flagge

    }

    public FieldEnum[][] getLandscape() {
        return landscape;
    }

    public List<Flag> getFlags() {
        return flags;
    }

    public void setFlags(List<Flag> flags) {
        this.flags = flags;
    }
}
