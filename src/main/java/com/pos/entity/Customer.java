package com.pos.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "customers", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"owner_id", "phone"})
})
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    private String name;

    @Column
    private String phone;
}
