package ru.whitesharky.mediamanager.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.NaturalId;

import java.util.Objects;

@Entity
@Table(name = "role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_id_generator")
    @SequenceGenerator(name = "role_id_generator", sequenceName = "sq_role_id", allocationSize = 1)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NaturalId
    private RoleName name;

    public Role() {
    }

    public Role(RoleName name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoleName getName() {
        return name;
    }

    public void setName(RoleName name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Role role = (Role) o;
        return Objects.equals(id, role.id) && name == role.name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}