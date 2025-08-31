package org.example.movices.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.example.movices.model.entity.enums.RoleType;

@Entity
@Table(name = "roles")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false, unique = true)
    private RoleType name;

}
