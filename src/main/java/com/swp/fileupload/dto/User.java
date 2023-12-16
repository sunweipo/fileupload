package com.swp.fileupload.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author user
 * @version $Revision: 1.0 $, $Date: 2021年7月26日 下午5:05:26 $
 */
@Data
public class User implements Serializable {
    private static final long serialVersionUID = -1989557293813410870L;
    private String name;
    private String sex;
    private String hobby;
    private int age;

    /**
     * @param name
     * @param sex
     * @param hobby
     * @param age
     */
    public User(String name, String sex, String hobby, int age) {
        this.name = name;
        this.sex = sex;
        this.hobby = hobby;
        this.age = age;
    }

    /**
     *
     */
    public User() {
    }

}
