package com.attus.prazos.domain;

/** Papel do usuario no sistema. Usado como authority do Spring Security ("ROLE_" + nome). */
public enum Role {
    USER,
    ADMIN
}
