package com.realestate.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// Составной первичный ключ для таблицы favorites
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FavoriteId implements Serializable {
    private Long user;
    private Long listing;
}
