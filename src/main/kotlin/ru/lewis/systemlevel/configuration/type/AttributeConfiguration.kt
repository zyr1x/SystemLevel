package ru.lewis.systemlevel.configuration.type

import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class AttributeConfiguration(
    val attribute: Attribute,
    val modifier: AttributeModifier
)
