package org.jacpfx.entity;

import java.io.Serializable;

/**
 * Created by Andy Moncsek on 19.11.15.
 */
public class MyTestObject implements Serializable{
    private final String name;
    private final String lastName;

    public MyTestObject(String name, String lastName) {
        this.name = name;
        this.lastName = lastName;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MyTestObject)) return false;

        MyTestObject that = (MyTestObject) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return !(lastName != null ? !lastName.equals(that.lastName) : that.lastName != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        return result;
    }
}