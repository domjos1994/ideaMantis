package de.domjos.ideaMantis.soap;

import org.ksoap2.serialization.SoapObject;

class ObjectRef extends SoapObject {
    private int id;
    private String name;

    ObjectRef(String s, String s1) {
        super(s, s1);
        this.id = 0;
        this.name = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
