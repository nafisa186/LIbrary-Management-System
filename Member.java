
package com.library.model;

public class Member extends Person {
    private String phone;

    public Member(int id, String name, String email, String phone) {
        super(id, name, email);
        this.phone = phone;
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String getRole() {
        return "MEMBER";
    }
}
