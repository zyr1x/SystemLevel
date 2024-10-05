package ru.lewis.systemlevel.model.hibernate.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "systemlevel_players")
open class PlayerHibernateEntity() {

    @Id
    @Column(name = "player_uniqueid", nullable = false)
    open var uuid: UUID? = null

    @Column(name = "level", nullable = false)
    open var level: Int? = null

    @Column(name = "exp", nullable = false)
    open var exp: Double? = null

    constructor(
        uuid: UUID,
        level: Int,
        exp: Double
    ) : this() {
        this.uuid = uuid
        this.level = level
        this.exp = exp
    }

}