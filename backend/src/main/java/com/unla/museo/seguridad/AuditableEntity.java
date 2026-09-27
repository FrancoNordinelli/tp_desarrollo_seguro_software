package com.unla.museo.seguridad;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@MappedSuperclass
public abstract class AuditableEntity {

    @Column(name = "CREATION")
    private LocalDateTime creation =  LocalDateTime.now();

    @Column(name = "CREATED_BY")
    private String createdBy;

}
