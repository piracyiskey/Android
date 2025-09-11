package org.convenient.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "user")
@Data
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    private String id;
    private String email;
    private String phone;
    private String profile_pic;
    private String full_name;
    private String password;
    private String role;
    private Date create_at;
}
