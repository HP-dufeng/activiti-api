package org.activiti.app.security;


import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.activiti.engine.identity.User;

public class WUCCUser implements User {

    private String sub;
    private String preferred_username;
    private Collection<String> role;

    @JsonProperty("FullName")
    private String fullName;

    @JsonProperty("SalesDepartment")
    private String salesDepartment;

    @JsonProperty("SuperAdmin")
    private String superAdmin;

    private String name;

    public String getSub(){return this.sub;}
    public void setSub(String sub){this.sub=sub;}

    public String getPreferred_username(){return this.preferred_username;}
    public void setPreferred_username(String preferred_username){this.preferred_username=preferred_username;}

    public Collection<String> getRole() {
        return role;
    }
    public void setRole(Collection<String> role){this.role = role;}

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSalesDepartment() {
        return salesDepartment;
    }

    public void setSalesDepartment(String salesDepartment) {
        this.salesDepartment = salesDepartment;
    }

    public String getSuperAdmin() {
        return superAdmin;
    }

    public void setSuperAdmin(String superAdmin) {
        this.superAdmin = superAdmin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    private String id;
    @Override
    public String getId() {
        return this.name;
    }

    @Override
    public void setId(String s) {
        this.id = this.name;
    }

    private String firstName;
    @Override
    public String getFirstName() {
        return this.firstName;
    }

    @Override
    public void setFirstName(String s) {
        this.firstName = s;
    }

    private String lastName;
    @Override
    public void setLastName(String s) {
        this.lastName = s;
    }

    @Override
    public String getLastName() {
        return this.lastName;
    }

    private String email;
    @Override
    public void setEmail(String s) {
        this.email = s;
    }

    @Override
    public String getEmail() {
        return this.email;
    }


    private String password;
    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public void setPassword(String s) {
        this.password = s;
    }

    @Override
    public boolean isPictureSet() {
        return false;
    }
}

