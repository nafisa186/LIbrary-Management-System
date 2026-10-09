
package com.library.model;

public class Librarian extends Person {
    public Librarian(int id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public String getRole() {
        return "LIBRARIAN";
    }
}
